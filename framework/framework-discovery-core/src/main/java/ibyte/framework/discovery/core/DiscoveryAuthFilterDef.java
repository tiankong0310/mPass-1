package ibyte.framework.discovery.core;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.dto.Response;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.web.BaseWebFilter;
import com.ibyte.common.web.IWebFilterDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 注册中心鉴权
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Component
@Slf4j
@Order(IWebFilterDef.FILTER_ORDER_DEFAULT - 100)
class DiscoveryAuthFilterDef implements IWebFilterDef {
    /**
     * 加密bean
     */
    @Resource
    private TextEncryptor textEncryptor;

    /**
     * 启用认证拦截
     */
    @Value("${encrypt.key:}")
    private String securityKey;

    @Override
    public BaseWebFilter getFilterInstance() {
        return new AuthFilter();
    }

    private class AuthFilter extends BaseWebFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            if (!StringUtils.isEmpty(securityKey)) {
                String security = request.getHeader(NamingConstant.HEADER_KEY_SERVICE);
                String serviceName = null;
                if (!StringUtils.isEmpty(security)) {
                    serviceName = textEncryptor.decrypt(security);
                      //TODO 存在的服务鉴权doFilter

                }
                if (!StringUtils.isEmpty(serviceName)) {
                    log.warn("应用'" + serviceName + "'不在允许连接的应用清单中.");
                } else {
                    log.warn("请求地址：" + request.getRequestURI() + ",头部无认证信息.");
                }
                String errorCode = "framework-discovery:discovery.auth.error";
                response.setStatus(401);
                response.setCharacterEncoding(Charset.defaultCharset().displayName());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON.toString());
                response.getWriter().println(JSONObject.toJSONString(Response.err(errorCode, ResourceUtil.getString(errorCode))));
            } else {
                chain.doFilter(request, response);
            }
        }
    }
}
