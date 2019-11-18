package com.ibyte.framework.discovery.core;

import com.ibyte.framework.discovery.client.interceptor.DiscoveryHeaderClientFilter;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaIdentityHeaderFilter;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClient;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClientImpl;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.EurekaServerIdentity;
import com.netflix.eureka.cluster.DynamicGZIPContentEncodingFilter;
import com.netflix.eureka.cluster.HttpReplicationClient;
import com.netflix.eureka.cluster.PeerEurekaNode;
import com.netflix.eureka.cluster.PeerEurekaNodes;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.resources.ServerCodecs;
import com.netflix.eureka.transport.JerseyReplicationClient;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.*;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

/**
 * 服务注册中心服务器端配置
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Configuration
@EnableEurekaServer
@EnableConfigServer
public class DiscoveryServerConfig {

    @Resource
    private ApplicationInfoManager applicationInfoManager;

    @Resource
    private EurekaServerConfig eurekaServerConfig;

    @Resource
    private EurekaClientConfig eurekaClientConfig;

    /**
     * 使用本地配置仓库，springcloud config如果有其他的本地配置仓库，
     * native不能用，所以此处特地声明使用,
     * 设置本地文件配置的优先级最低，这样jdbc的设置可以覆盖native的配置
     *
     * @param factory
     * @param environmentProperties
     * @return
     */
    @Bean
    @Profile("native")
    public NativeEnvironmentRepository nativeEnvironmentRepository(@SuppressWarnings("SpringJavaAutowiringInspection") NativeEnvironmentRepositoryFactory factory,
                                                                   @SuppressWarnings("SpringJavaAutowiringInspection") NativeEnvironmentProperties environmentProperties) {
        NativeEnvironmentRepository nativeRepository = factory.build(environmentProperties);
        nativeRepository.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        return nativeRepository;
    }

    /**
     * 使用混合配置仓库
     *
     * @param environmentRepositories
     * @return
     * @throws Exception
     */
    @Primary
    @Bean
    public SearchPathCompositeEnvironmentRepository searchPathCompositeEnvironmentRepository(
            List<EnvironmentRepository> environmentRepositories) throws Exception {
        return new SearchPathCompositeEnvironmentRepository(environmentRepositories);
    }

    /**
     * 自定义配置中心节点，主要目的是注册中心间配置拷贝时的认证，可使用自定义的
     *
     * @param registry
     * @param serverCodecs
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public PeerEurekaNodes peerEurekaNodes(@SuppressWarnings("SpringJavaAutowiringInspection") PeerAwareInstanceRegistry registry,
                                           @SuppressWarnings("SpringJavaAutowiringInspection") ServerCodecs serverCodecs) {
        return new CustomRefreshablePeerEurekaNodes(registry, this.eurekaServerConfig,
                this.eurekaClientConfig, serverCodecs, this.applicationInfoManager);
    }

    /**
     * 同EurekaServerAutoConfiguration.RefreshablePeerEurekaNodes用于自定义PeerEurekaNodes
     * 区别在于createReplicationClient增加了自定义认证头信息
     */
    static class CustomRefreshablePeerEurekaNodes extends PeerEurekaNodes
            implements ApplicationListener<EnvironmentChangeEvent> {
        private final String sslPrefix = "https://";

        public CustomRefreshablePeerEurekaNodes(
                final PeerAwareInstanceRegistry registry,
                final EurekaServerConfig serverConfig,
                final EurekaClientConfig clientConfig,
                final ServerCodecs serverCodecs,
                final ApplicationInfoManager applicationInfoManager) {
            super(registry, serverConfig, clientConfig, serverCodecs, applicationInfoManager);
        }

        @Override
        protected PeerEurekaNode createPeerEurekaNode(String peerEurekaNodeUrl) {
            HttpReplicationClient replicationClient = createReplicationClient(serverConfig, serverCodecs, peerEurekaNodeUrl);
            String targetHost = hostFromUrl(peerEurekaNodeUrl);
            if (targetHost == null) {
                targetHost = "host";
            }
            return new PeerEurekaNode(registry, targetHost, peerEurekaNodeUrl, replicationClient, serverConfig);
        }

        /**
         * 创建自定义配置中心复制client对象
         *
         * @param config
         * @param serverCodecs
         * @param serviceUrl
         * @return
         */
        private JerseyReplicationClient createReplicationClient(EurekaServerConfig config, ServerCodecs serverCodecs, String serviceUrl) {
            String name = JerseyReplicationClient.class.getSimpleName() + ": " + serviceUrl + "apps/: ";
            EurekaJerseyClient jerseyClient;
            try {
                String hostname;
                try {
                    hostname = new URL(serviceUrl).getHost();
                } catch (MalformedURLException e) {
                    hostname = serviceUrl;
                }
                String jerseyClientName = "Discovery-PeerNodeClient-" + hostname;
                EurekaJerseyClientImpl.EurekaJerseyClientBuilder clientBuilder = new EurekaJerseyClientImpl.EurekaJerseyClientBuilder()
                        .withClientName(jerseyClientName)
                        .withUserAgent("Java-EurekaClient-Replication")
                        .withEncoderWrapper(serverCodecs.getFullJsonCodec())
                        .withDecoderWrapper(serverCodecs.getFullJsonCodec())
                        .withConnectionTimeout(config.getPeerNodeConnectTimeoutMs())
                        .withReadTimeout(config.getPeerNodeReadTimeoutMs())
                        .withMaxConnectionsPerHost(config.getPeerNodeTotalConnectionsPerHost())
                        .withMaxTotalConnections(config.getPeerNodeTotalConnections())
                        .withConnectionIdleTimeout(config.getPeerNodeConnectionIdleTimeoutSeconds());

                if (serviceUrl.startsWith(sslPrefix) &&
                        "true".equals(System.getProperty("com.netflix.eureka.shouldSSLConnectionsUseSystemSocketFactory"))) {
                    clientBuilder.withSystemSSLConfiguration();
                }
                jerseyClient = clientBuilder.build();
            } catch (Throwable e) {
                throw new RuntimeException("Cannot Create new Replica Node :" + name, e);
            }

            String ip = null;
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
            }

            ApacheHttpClient4 jerseyApacheClient = jerseyClient.getClient();
            jerseyApacheClient.addFilter(new DynamicGZIPContentEncodingFilter(config));

            EurekaServerIdentity identity = new EurekaServerIdentity(ip);
            jerseyApacheClient.addFilter(new EurekaIdentityHeaderFilter(identity));
            //增加自定义头认证信息
            jerseyApacheClient.addFilter(new DiscoveryHeaderClientFilter());

            return new JerseyReplicationClient(jerseyClient, serviceUrl);
        }

        @Override
        public void onApplicationEvent(final EnvironmentChangeEvent event) {
            if (shouldUpdate(event.getKeys())) {
                updatePeerEurekaNodes(resolvePeerUrls());
            }
        }

        /**
         * Check whether specific properties have changed.
         *
         * @param changedKeys
         * @return
         */
        protected boolean shouldUpdate(final Set<String> changedKeys) {
            assert changedKeys != null;

            // if eureka.client.use-dns-for-fetching-service-urls is true, then
            // service-url will not be fetched from environment.
            if (clientConfig.shouldUseDnsForFetchingServiceUrls()) {
                return false;
            }

            if (changedKeys.contains("eureka.client.region")) {
                return true;
            }

            for (final String key : changedKeys) {
                // property keys are not expected to be null.
                if (key.startsWith("eureka.client.service-url.") ||
                        key.startsWith("eureka.client.availability-zones.")) {
                    return true;
                }
            }

            return false;
        }
    }


}
