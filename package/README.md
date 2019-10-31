### 核心JAR架构依赖视图
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/000659_b0861629_1468963.png "JAR.png")


- [x] package
     - [x] mvn-min     #  单核依赖
     - [x] mvn-parent  #  基础父级依赖
     - [x] starter-discovery-center     # 注册中心 & 配置中心
     - [x] starter-mpass-server         # 程序启动  / 部署上可拆可合 -> api & core || api & client