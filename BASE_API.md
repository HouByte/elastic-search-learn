
> [官方安装教程](https://www.elastic.co/guide/en/elastic-stack-get-started/current/get-started-elastic-stack.html)
# 一、RESTful API
IP：ElasticSearch 地址默认 localhost:9200
|  method | url地址 | 描述 |
|--|--|--|
PUT | /索引名称/类型名称/文档id|创建文档（指定文档id )
POST|/索引名称/类型名称|创建文档(随机文档id )
POST|/索引名称/类型名称/文档id/_update|修改文档
DELETE|/索引名称/类型名称/文档id|删除文档
GET|/索引名称/类型名称/文档id|查询文档通过文档id
POST|/索引名称/类型名称/_search|查询所有数据

## 1.1 基本操作
创建文档（指定文档id )
```json
# PUT  /索引名称/~类型名称~/文档id
# { 请求体}
PUT /demo/test/1
{
  "name":"Vincent Vic",
  "tip":"Flowboot"
}
```
请求响应
```json
#! Deprecation: [types removal] Specifying types in document index requests is deprecated, use the typeless endpoints instead (/{index}/_doc/{id}, /{index}/_doc, or /{index}/_create/{id}).
{
  "_index" : "demo",
  "_type" : "test",
  "_id" : "1",
  "_version" : 1,
  "result" : "created",
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "_seq_no" : 0,
  "_primary_term" : 1
}

```

创建索引

```json
# PUT  /索引名称
PUT /demo2
{
  "mappings":{
    "properties": {
      "name":{
        "type": "text"
      },
      "age":{
        "type": "long"
      }
    }
  }
}
```

查询创建的索引

```json
GET demo2
```
得到的数据

```json
{
  "demo2" : {
    "aliases" : { },
    "mappings" : {
      "properties" : {
        "age" : {
          "type" : "long"
        },
        "name" : {
          "type" : "text"
        }
      }
    },
    "settings" : {
      "index" : {
        "creation_date" : "1649492013747",
        "number_of_shards" : "1",
        "number_of_replicas" : "1",
        "uuid" : "NuUNYLzcQbCel-EPKgFOCg",
        "version" : {
          "created" : "7060299"
        },
        "provided_name" : "demo2"
      }
    }
  }
}
```

添加数据

```json
PUT /demo2/_doc/1
{
  "name":"VVVV",
  "age":22
}
```
查看索引
```json
GET /demo2/_doc/1
```
修改索引数据
```json
POST /demo2/_doc/1/_update
{
  "doc":{
    "name":"ccc"
  }
}
```
删除文档/索引
```java
DELETE /demo2
DELETE /demo2/_doc/1
```


## 1.2 复杂查询
准备工作
![在这里插入图片描述](https://img-blog.csdnimg.cn/96f3209d95c445389710c20c66ecc6ac.png)
创建文档添加数据，本文如图所示

###  1.2.1 匹配查询

```json
# 匹配查询
GET test4/user/_search
{
  "query":{
    "match":{
      "name":"张三"
    }
  }
}
```

### 1.2.2 匹配查询 - 过滤字段
```json
# 匹配查询 - 过滤字段
GET test4/user/_search
{
  "query":{
    "match":{
      "name":"张三"
    }
  }
  , "_source": ["name","desc"]
}

```

### 1.2.3  排序，分页

```java
# 匹配查询 - 排序，分页 : from 第几个数据考试 ，size ： 查询数量
GET test4/user/_search
{
  "query":{
    "match":{
      "name":"张三"
    }
  }
  ,"sort": [
    {
      "age": {
        "order": "desc"
      }
    }
  ],
  "from": 0,
  "size": 1
}
```
### 1.2.4 多条件匹配
```json
# 多条件匹配 
GET test4/user/_search
{
  "query":{
    "bool":{
      "must": [
        {
          "match": {
            "name":"张三"
          }
        },
        {
          "match": {
            "age":23
          }
        }
      ]
    }
  }
 
}

```
### 1.2.5 取反匹配匹配 
```json
# 取反匹配匹配 
GET test4/user/_search
{
  "query":{
    "bool":{
      "must_not": [
        {
          "match": {
            "age":23
          }
        }
      ]
    }
  }
 
}
```

### 1.2.6 过滤匹配
> gt:大于  gte :大于等于 lt:小于 lte:小于等于

```json
# 过滤匹配 
GET test4/user/_search
{
  "query":{
    "bool":{
      "filter": {
        "range":{
          "age":{
            "gte":10,
            "lte":23
          }
        }
      }
    }
  }
 
}
```
### 1.2.7 多条件和过滤匹配组合
```json
GET test4/user/_search
{
  "query":{
    "bool":{
      "must": [
        {
          "match": {
            "age":23
          }
        }
      ],
      "filter": {
        "range":{
          "age":{
            "gte":10,
            "lte":23
          }
        }
      }
    }
  }
 
}
```
### 1.2.8 多个条件空格隔开
```json
# 多个条件空格隔开
GET test4/user/_search
{
  "query":{
    "match":{
      "tags":"躺平 技术"
    }
  }
 
}

```
### 1.2.9 或匹配
```json
# 或匹配
GET test4/user/_search
{
  "query":{
    "bool":{
      "should":[
        {
          "match": {
            "name": "Bugio"
          }
        },
        {
          "match": {
            "name": "Bigger"
          }
        }
      ]
    }
  }
 
}

```
### 1.2.10 高亮查询
```json
# 高亮查询
GET test4/user/_search
{
  "query":{
   "match": {
      "name": "Bugio"
    }
  },
  "highlight": {
    "fields": {
      "name":{}
    }
  }
}
```
### 1.2.10 高亮查询 - 自定义标签
```java
# 高亮查询- 自定义标签
GET test4/user/_search
{
  "query":{
   "match": {
      "name": "Bugio"
    }
  },
  "highlight": {
    "pre_tags": "<p class='key' style='color:red>", 
    "post_tags": "</p>", 
    "fields": {
      "name":{}
    }
  }
 
}


```


# Spring Boot 集成 ElasticSearch 
maven 项目ElasticSearch 核心依赖,版本和ElasticSearch 使用的一致以免因为版本差异导致不必要的bug
```xml
 <properties>
        <elasticsearch.version>7.6.2</elasticsearch.version>
 </properties>
<dependencies>
	<dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
	</dependency>
</dependencies>
```

# JAVA API
[官方文档](https://www.elastic.co/guide/index.html)
[ Java High Level REST Client 7.17版本文档直达](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-initialization.html)

```java
package cn.flowboot.es.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * <h1>配置bean</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/04/09
 */
@Slf4j
@Configuration
public class ElasticSearchClientConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        return client;
    }
}

```
使用的文档对象
```java
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private String name;
    private Integer age;
}
```

Spring Boot Test 测试中使用API案例，使用了fastjson
```java
import cn.flowboot.es.doc.User;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/04/09
 */
@SpringBootTest
public class ElasticSearchApplicationTest {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Test
    public void contextLoads(){
        System.out.println(client);
    }

    /**
     * 测试索引创建索引 Request PUT boot_index
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        //1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("boot_index");
        //2.客户端执行请求 IndicesClient,请求后获取响应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        // 打印
        System.out.println(createIndexResponse.toString());
    }

    /**
     * 测试判断索引是否存在
     * @throws IOException
     */
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("boot_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试删除索引
     * @throws IOException
     */
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("boot_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.toString());
    }

    /**
     * 测试创建文档
     * @throws IOException
     */
    @Test
    public void testAddDocument() throws IOException {
        User user = new User("test",23);

        IndexRequest request = new IndexRequest("boot_index");
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
//        request.timeout("1s");
        request.source(JSON.toJSONString(user), XContentType.JSON);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    /**
     * 测试判断文档是否存在
     * @throws IOException
     */
    @Test
    public void testIsExist() throws IOException {
        GetRequest request = new GetRequest("boot_index","1");
        //不获取返回的_source的上下文了
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试获得文档
     * @throws IOException
     */
    @Test
    public void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("boot_index","1");
        GetResponse getResponse = client.get(request, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSource());
        System.out.println(getResponse);
    }

    /**
     * 测试更新文档
     * @throws IOException
     */
    @Test
    public void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("boot_index","1");
        request.timeout("1s");
        User user = new User("abc", 22);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse);
    }

    /**
     * 测试更新文档
     * @throws IOException
     */
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("boot_index","1");
        request.timeout("1s");
        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse);
    }

    /**
     * 测试批量加入文档
     * @throws IOException
     */
    @Test
    public void testBulkDocument() throws IOException {
        User user = new User("test",23);

        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(1));

        for (int i = 0; i < 5; i++) {
            request.add(new IndexRequest("boot_index").id(""+(i+1))
                    .source(JSONObject.toJSONString(new User("test"+(i+1),18+i)),XContentType.JSON));
        }

        BulkResponse indexResponse = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    /**
     * 测试查询文档
     * @throws IOException
     */
    @Test
    public void testSearchDocument() throws IOException {


        SearchRequest request = new SearchRequest("boot_index");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询条件，我们可以使用QueryBuilders工具来实现
        // QueryBuilders.termQuery精确
        // QueryBuilders.matchAllQuery()匹配所有

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "test1");
        // MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(6, TimeUnit.SECONDS));
        //分页
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        sourceBuilder.highlighter(highlightBuilder);
        request.source(sourceBuilder);
        SearchResponse indexResponse = client.search(request, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        for (SearchHit hit : indexResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }

}

```
