# ElasticSearch 使用 canal 同步数据
[Canal 下载](https://github.com/alibaba/canal/releases)
![1.1.5](https://img-blog.csdnimg.cn/52309c4b0eed4b9aa5b1d1292e8c20be.png)
> 需要下载
> canal.adapter-1.1.5.tar.gz
 canal.admin-1.1.5.tar.gz
 canal.deployer-1.1.5.tar


# Canal的各个组件的用途：

- canal-server（canal-deploy）：可以直接监听MySQL的binlog，把自己伪装成MySQL的从库，只负责接收数据，并不做处理。
- canal-adapter：相当于canal的客户端，会从canal-server中获取数据，然后对数据进行同步，可以同步到MySQL、Elasticsearch和HBase等存储中去。
- canal-admin：为canal提供整体配置管理、节点运维等面向运维的功能，提供相对友好的WebUI操作界面，方便更多用户快速和安全的操作。


# 环境

应用 |	端口	|版本
|--|--|--|
MySQL |	3306 |	5.7
Elasticsearch |	9200 |	7.6.2
Kibanba |	5601 |	7.6.2
canal-server |	11111 |	1.1.15
canal-adapter |	8081 |	1.1.15
canal-admin |	8089 |	1.1.15

# MySQL配置

> 由于canal是通过订阅MySQL的binlog来实现数据同步的，所以我们需要开启MySQL的binlog写入功能，并设置binlog-format为ROW模式
my.ini 主要修改部分
```yaml
[mysqld]
## 设置server_id，同一局域网中需要唯一
server_id=101 
## 指定不需要同步的数据库名称
binlog-ignore-db=mysql  
## 开启二进制日志功能
log-bin=mall-mysql-bin  
## 设置二进制日志使用内存大小（事务）
binlog_cache_size=1M  
## 设置使用的二进制日志格式（mixed,statement,row）
binlog_format=row  
## 二进制日志过期清理时间。默认值为0，表示不自动清理。
expire_logs_days=7  
## 跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断。
## 如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致
slave_skip_errors=1062  

```

配置完成后需要重新启动MySQL，命令查看binlog是否启用和binlog模式；

```shell

mysql> show variables like '%log_bin%';
+---------------------------------+----------------------------------------------------------------------+
| Variable_name                   | Value                                                                |
+---------------------------------+----------------------------------------------------------------------+
| log_bin                         | ON                                                                   |
| log_bin_basename                | D:\Programs\phpstudy_pro\Extensions\MySQL5.7.26\data\mysql-bin       |
| log_bin_index                   | D:\Programs\phpstudy_pro\Extensions\MySQL5.7.26\data\mysql-bin.index |
| log_bin_trust_function_creators | OFF                                                                  |
| log_bin_use_v1_row_events       | OFF                                                                  |
| sql_log_bin                     | ON                                                                   |
+---------------------------------+----------------------------------------------------------------------+
6 rows in set (0.03 sec)

mysql> show variables like 'binlog_format%';  
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
1 row in set (0.01 sec)
```
创建一个拥有从库权限的账号，用于订阅binlog，创建的账号为canal:canal
```sql
CREATE USER canal IDENTIFIED BY 'canal';  
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;
```

创建好测试用的数据库canal-test，新建数据表作为demo

```sql
CREATE TABLE `product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sub_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `pic` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

```
# 配置Canal 服务器
修改配置文件conf/example/instance.properties，按如下配置即可，主要是修改数据库相关配置；如果账号（上述创建）和端口一致可以在bin目录中启动，linux 用户启动sh，Windows用户启动bat
```java
# 需要同步数据的MySQL地址
canal.instance.master.address=127.0.0.1:3306
canal.instance.master.journal.name=
canal.instance.master.position=
canal.instance.master.timestamp=
canal.instance.master.gtid=
# 用于同步数据的数据库账号
canal.instance.dbUsername=canal
# 用于同步数据的数据库密码
canal.instance.dbPassword=canal
# 数据库连接编码
canal.instance.connectionCharset = UTF-8
# 需要订阅binlog的表过滤正则表达式
canal.instance.filter.regex=.*\\..*

```

# 配置Canal 客户端
**canal-adapter配置**
> 记得启动ElasticSearch服务

修改配置文件conf/application.yml，按如下配置即可，主要是修改canal-server配置、数据源配置和客户端适配器配置；建议直接备份原配置，可以直接替换内容
```yaml
server:
  port: 8081
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null

canal.conf:
  mode: tcp # 客户端的模式，可选tcp kafka rocketMQ
  flatMessage: true # 扁平message开关, 是否以json字符串形式投递数据, 仅在kafka/rocketMQ模式下有效
  zookeeperHosts: # 对应集群模式下的zk地址
  syncBatchSize: 1000 # 每次同步的批数量
  retries: 0 # 重试次数, -1为无限重试
  timeout: # 同步超时时间, 单位毫秒
  accessKey:
  secretKey:
  consumerProperties:
    # canal tcp consumer
    canal.tcp.server.host: 127.0.0.1:11111 #设置canal-server的地址
    canal.tcp.zookeeper.hosts:
    canal.tcp.batch.size: 500
    canal.tcp.username:
    canal.tcp.password:

  srcDataSources: # 源数据库配置
    defaultDS:
      url: jdbc:mysql://127.0.0.1:3306/canal_test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
      username: canal
      password: canal
  canalAdapters: # 适配器列表
    - instance: example # canal实例名或者MQ topic名
      groups: # 分组列表
        - groupId: g1 # 分组id, 如果是MQ模式将用到该值
          outerAdapters:
            - name: logger # 日志打印适配器
            - name: es7 # ES同步适配器
              hosts: http://127.0.0.1:9200 # ES连接地址
              properties:
                mode: rest # 模式可选transport(9300) 或者 rest(9200)
                # security.auth: test:123456 #  only used for rest mode
                cluster.name: elasticsearch # ES集群名称

```
添加配置文件../conf/es7/product.yml，用于配置MySQL中的表与Elasticsearch中索引的映射关系；

```yaml
dataSourceKey: defaultDS # 源数据源的key, 对应上面配置的srcDataSources中的值
destination: example  # canal的instance或者MQ的topic
groupId: g1 # 对应MQ模式下的groupId, 只会同步对应groupId的数据
esMapping:
  _index: canal_product # es 的索引名称
  _id: _id  # es 的_id, 如果不配置该项必须配置下面的pk项_id则会由es自动分配
  sql: "SELECT
        	p.id AS _id,
        	p.title,
        	p.sub_title,
        	p.price,
        	p.pic
        FROM
        	product p"        # sql映射
  etlCondition: "where a.c_time>={}"   #etl的条件参数
  commitBatch: 3000   # 提交批大小

```
linux 用户启动sh，Windows用户启动bat

> 启动如果提醒druid报错：class com.alibaba.druid.pool.DruidDataSource  cannot be cast to
>  项目中多模块中的druid jar包冲突导致。
## 解决方案
下载1.1.5 源码，修改项目中子项目client-adapter下escore的pom中druid包的scope为provided模式。
```xml
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid</artifactId>
      <scope>provided</scope> #新增 让es的xxxx-with-dependency.jar不包含druid相关包
  </dependency> 
```
canal 源码根目录打包
> mvn clean install -Dmaven.test.skip=true
将client-adapter.es7x-1.1.5-jar-with-dependencies.jar 替换 plugin文件夹中的jar包
> canal 源码目录下 ../client-adapter/es7x 下target中
>  打包好的jar [client-adapter.es7x-1.1.5-jar-with-dependencies.jar](https://download.csdn.net/download/Vincent_Vic_/85110090)
>  偷懒可以直接用，不收费

# 数据同步测试
在Elasticsearch中创建索引，和MySQL中的product表相对应，直接在Kibana的Dev Tools中使用如下命令创建即可；
```json
PUT canal_product
{
  "mappings": {
    "properties": {
      "title": {
        "type": "text"
      },
      "sub_title": {
        "type": "text"
      },
      "pic": {
        "type": "text"
      },
      "price": {
        "type": "double"
      }
    }
  }
}

```
![在这里插入图片描述](https://img-blog.csdnimg.cn/51632558128f43278e5b23f50d67f4fe.png)
创建完成后可以查看下索引的结构；
```json
GET canal_product/_mapping
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/d54f85b880df469ea002b155283fff42.png)
创建一条记录；
```sql
INSERT INTO product (title, sub_title, price, pic ) VALUES (  '小米8', ' 全面屏游戏智能手机 6GB+64GB', 1999.00, NULL );
```
Elasticsearch搜索查看是否同步
```java
GET canal_product/_search
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/020a50e490db41f8851b26596343779b.png)
对数据进行修改
```sql
UPDATE product SET title='小米10' WHERE id=5
```
再次查看发现也同步了
![在这里插入图片描述](https://img-blog.csdnimg.cn/6a366cbe3101455fb04c6da7846af177.png)

# Canal 管理页面搭建
创建canal-admin需要使用的数据库canal_manager，创建SQL脚本为canal-admin目录下的 ..conf/canal_manager.sql
![在这里插入图片描述](https://img-blog.csdnimg.cn/13d32bfd3b8f4172877acf450f5908b3.png)
修改配置文件conf/application.yml，按如下配置即可，主要是修改数据源配置和canal-admin的管理账号配置，注意需要用一个有读写权限的数据库账号，比如**root:root**；
> 此部分为默认配置文件信息，一致则无需修改
```java
server:
  port: 8089
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

spring.datasource:
  address: 127.0.0.1:3306
  database: canal_manager
  username: root
  password: root
  driver-class-name: com.mysql.jdbc.Driver
  url: jdbc:mysql://${spring.datasource.address}/${spring.datasource.database}?useUnicode=true&characterEncoding=UTF-8&useSSL=false
  hikari:
    maximum-pool-size: 30
    minimum-idle: 1

canal:
  adminUser: admin
  adminPasswd: admin

```
修改canal-server的conf/canal_local.properties文件进行配置，主要是修改canal-admin的配置，修改完成后使用sh bin/startup.sh local 或者 .\bin\startup.bat local重启canal-server
```java
# register ip
canal.register.ip =

# canal admin config
canal.admin.manager = 127.0.0.1:8089
canal.admin.port = 11110
canal.admin.user = admin
canal.admin.passwd = 4ACFE3202A5FF5CF467898FC58AAB1D615029441
# admin auto register
canal.admin.register.auto = true
canal.admin.register.cluster = 

```

启动canal-admin服务,输入账号密码admin:123456登录
[http://localhost:8089/](http://localhost:8089/)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f8ee9387ae2f43a295913094eb00fdac.png)
更多查看[官方文档](https://github.com/alibaba/canal/wiki)