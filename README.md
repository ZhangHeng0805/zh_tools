# 工程简介



# 配置文件
* ## application.yml
```yaml
spring:
  application:
    # 应用名称
    name: zh_tools
  datasource:
    # 数据库驱动：
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 数据源名称
    name: defaultDataSource
  main:
    log-startup-info: false
  resources:
    static-locations: classpath:/static/
  thymeleaf:
    # 开启 MVC Thymeleaf 视图解析（默认值： true ）
    enabled: true
    # 检查模板是否存在，然后再呈现
    check-template: true
    # 检查模板位置是否正确（默认值 :true ）
    check-template-location: true
    servlet:
      #Content-Type 的值（默认值： text/html ）
      content-type: text/html
    # 开启模板缓存（默认值： true ）
    cache: false
    # 模板编码
    encoding: UTF-8
  mvc:
    static-path-pattern: /static/**
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: update
  jackson:
    date-format: yyyy-MM-dd'T'HH:mm:ssXXX
    time-zone: GMT+8
logging:
  pattern:
    #设置日志等级
    #    level: INFO
    #设置输出日期格式
    dateformat: yyyy-MM-dd HH:mm:ss.SSS
    rolling-file-name: log/%d{yyyy-MM-dd}日志_%i.log
  file:
    path: log
    max-size: 10MB
    max-history: 50
    total-size-cap: 10GB
server:
  servlet:
    encoding:
      charset: UTF-8
  error:
    include-stacktrace: always
  tomcat:
    accept-count: 500
    threads:
      max: 500
    max-connections: 10000

```
* ## application.properties
```properties
# 应用服务 WEB 访问端口
server.port=8081
server.servlet.context-path=/

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# 数据库连接地址
spring.datasource.url=jdbc:mysql://${setting.data-host}/zh_tools?characterEncoding=utf8
#spring.datasource.url=jdbc:mysql://127.0.0.1:3306/zh_tools?characterEncoding=utf8&serverTimezone=UTC&rewriteBatchedStatements=true
# 数据库用户名&密码：
spring.datasource.username=root
spring.datasource.password=***
#session超时时间
server.servlet.session.timeout=1m

#是否定时发送安卓图片邮件
timing.email.if-send-img=true
#是否定时发送普通访问报表邮件
timing.email.if-send-visitor-form=true
#是否定时发送安卓访问报表邮件
timing.email.if-send-android-log-form=true

android.upload.img.rules=300,1000,2000,4000

#请求Session注册
my-filter0.exclude-path=/test/**,/favicon.ico,/error,/config/**,/android_listener/**,/static/**,/666,/proxy/**,/getVerify/**,/download/split/**
#请求频率
my-filter1.exclude-path=/test/**,/favicon.ico,/error,/config/**,/android_listener/**,/static/**,/666,/proxy/**,/getVerify/**,/download/split/**
#请求次数
my-filter2.exclude-path=/test/**,/favicon.ico,/error,/config/**,/android_listener/**,/static/**,/666,/proxy/**,/getVerify/**,/download/split/**

```
* ## setting.properties
```properties
#数据库地址
setting.data-host=127.0.0.1:3306
#数据库地址访问密码
#setting.data-pwd=***
#管理员通知邮箱
setting.admin-email=zhangheng_0805@163.com

# 应用名称（文件夹名）
setting.application_name=ZH Tools
# 文件根目录
setting.baseDir=files/
#每个ip每日最大请求次数
setting.max-request-counts=200
#相邻请求的间隔时间[ms](若请求间隔时间小于setting.request-interval，则拦截)
setting.request-interval=1000
#允许打印的响应信息的状态码
setting.access-print-code=200,400,404,500
#是否打印所有请求信息(若设置为true，则setting.access-print-code失效)
setting.is-print-all-request-info=false
#是否直接使用外链下载地址
setting.isUseDownLoadUrl=true
#外部APP下载链接(当更新文件夹内无更新文件时，使用此链接作为下载链接)
setting.app-down-load-url=https://gitee.com/ZhangHeng0805/MyOkHttp/releases/download/V23.04.10/ZH%20Tools_V23.04.10.apk
#网站访问地址
setting.mainUrl=http://zh-tools.zhangheng0805.asia/
#app介绍
setting.app_introduce=一款可以免费搜索音乐、影视、实时疫情、翻译、天气、字典、画板...的自制安卓工具APP，欢迎下载体验！
#微信公众号外链
setting.weixin_url=https://mp.weixin.qq.com/s?__biz=MzIwMDQ2OTg4NA==&mid=2247484118&idx=1&sn=30dd3f7f2a4d93a6fdce4fb808e7c506&chksm=96fdfec5a18a77d35645d8e8f55477353aeb9a949fc73a2c302ca336155f8becae635e26f022#rd
#配置文件-首页图片
setting.index_img_config_file=index-img-url.txt
#首页界面
#index1 普通原始
#index2 背景图片
#index3 动态图片
setting.index_html=index3
#应用版本，更新版本即可更新更新应用
setting.version=V23.04.13
#IP变换验证
setting.IP_change_verify=false
#获取客户端IP的请求头名称
setting.ip-headers-name=X-Forwarded-For,Proxy-Client-IP,WL-Proxy-Client-IP,HTTP_X_FORWARDED_FOR,HTTP_X_FORWARDED,HTTP_X_CLUSTER_CLIENT_IP,HTTP_CLIENT_IP,HTTP_FORWARDED_FOR,HTTP_FORWARDED,HTTP_VIA,REMOTE_ADDR,X-Real-IP,X-Ngrok-IP

```

