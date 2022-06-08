> ElasticSearch  7.6.2
# 搭建项目
maven 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>cn.flowboot.es</groupId>
    <artifactId>elastic-search-learn</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <elasticsearch.version>7.6.2</elasticsearch.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.74</version>
        </dependency>
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.10.2</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```
配置

```yaml
spring:
  application:
    name: jd-es
  thymeleaf:
    cache: false

server:
  port: 9090

```

# 爬取京东数据
爬取京东核心工具类
```java
package cn.flowboot.es.utils;

import cn.flowboot.es.doc.Good;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * <h1>爬取京东数据</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@Slf4j
@Component
public class SearchJdGoodUtils {

    private static Integer getTotalNums() throws IOException {
        String url = "https://search.jd.com/Search?keyword=java";
        //解析网页
        Document document = Jsoup.parse(new URL(url), 30000);
        String text = document.getElementById("J_topPage").getElementsByTag("i").eq(0).text();
        return Integer.parseInt(text);
    }

    /**
     * 获取书籍
     * @param keyword 查询关键字
     * @return
     * @throws Exception
     */
    public Set<Good> getGoodAll(String keyword) throws Exception {
        log.info("开始捕获关于：{} 数据",keyword);
        Set<Good> goods = new HashSet<>();
        //爬取频繁会触发反爬，可以修改小数量，休眠提高时间，或者添加代理
        int total = getTotalNums()
        for (int i = 1; i < total; i++) {
            Random random = new Random();
            Thread.sleep(random.nextInt(5000)+36000);
            Set<Good> list = this.getGoodByPage(keyword, i,0);
            log.info("{} 捕获 {} 条",keyword,list.size());
            goods.addAll(list);

        }
        return goods;
    }

    private Set<Good> getGoodByPage(String keyword, int page, int sort) throws IOException, InterruptedException {
        String url = "https://search.jd.com/Search?keyword="+keyword+"&page="+page;//+"&psort="+sort;
        //解析网页
        Element element = null;
        int i = 0;
        do{
            Document document = Jsoup.parse(new URL(url), 30000);
            element = document.getElementById("J_goodsList");
            if (i >= 4){

                break;
            }else if (i > 0){
                log.error("{} 重新尝试获取",keyword);
                Thread.sleep(new Random().nextInt(5000)+22000);
            }
            i++;
        } while (element == null);

        if (element == null){
            log.error("获取失败");
            Thread.sleep(5000);
            return new HashSet<>();
        }
        //获取li标签
        Elements elements = element.getElementsByTag("li");
        Set<Good> goods = new HashSet<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            Elements elementsByClass = el.getElementsByClass("p-price");
            String price = null;
            if (elementsByClass.size() != 0) {
                price = elementsByClass.get(0).getElementsByTag("i").eq(0).text();
            }
            if (price == null){
                continue;
            }
            String title = el.getElementsByClass("p-name").eq(0).text();

            Good good = Good.builder()
                    .title(title)
                    .imgUrl(img)
                    .price(Double.parseDouble(price.trim()))
                    .build();
            goods.add(good);
        }
        return goods;
    }

}
```
 代理隧道可以替代SearchJdGoodUtils 中的Jsoup.parse(new URL(url), 30000)
```java
package cn.flowboot.es.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

/**
 * <h1>代理隧道</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/25
 */
public class JsoupUtils {

    // 代理隧道验证信息
    final static String ProxyUser = "ProxyUser";
    final static String ProxyPass = "ProxyPass";

    // 代理服务器
    final static String ProxyHost = "http-dyn.abuyun.com";
    final static Integer ProxyPort = 9020;
    public static Document getDocument(String url)
    {
        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(ProxyUser, ProxyPass.toCharArray());
            }
        });

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyHost, ProxyPort));

        try {
            // 此处自己处理异常、其他参数等
            Document doc = Jsoup.connect(url)
                    .followRedirects(false)
                    .timeout(3000)
                    .proxy(proxy)
                    // 忽略证书校验
                    .validateTLSCertificates(false)
                    .get();

           return doc;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

```

ElasticSearch 服务类，实现商品搜索，批量添加
```java
package cn.flowboot.es.service.impl;

import cn.flowboot.es.service.ElasticSearchService;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <h1> ElasticSearch 服务</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {

    private final RestHighLevelClient restHighLevelClient;

    /**
     * 批量添加数据
     *
     * @param index
     * @param list
     */
    @Override
    public boolean bulkAddData(String index, Set<T> list) {

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");

        //批处理请求
        for (T t : list) {
            bulkRequest.add(
                    new IndexRequest(index).source(JSON.toJSONString(t), XContentType.JSON)
            );
        }
        BulkResponse bulk = null;
        try {
            bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("添加es数据 {} 条",list.size());
            return !bulk.hasFailures();
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * 查询
     *
     * @param index    索引
     * @param keyword  关键字
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return
     */
    @Override
    public List<Map<String, Object>> searchByPage(String queryField,String index, String keyword, int pageNum, int pageSize,boolean isHighlight) {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);

        sourceBuilder.query(QueryBuilders.matchQuery(queryField,keyword));
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //是否高亮
        if (isHighlight){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(queryField);
            highlightBuilder.requireFieldMatch(false);
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        request.source(sourceBuilder);
        try {
            //执行搜索
            SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            List<Map<String, Object>> list = new ArrayList<>();
            for (SearchHit hit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if (isHighlight){
                    HighlightField field = hit.getHighlightFields().get(queryField);
                    if (field != null){
                        Text[] fragments = field.fragments();
                        StringBuilder text = new StringBuilder();
                        for (Text fragment : fragments) {
                            text.append(fragment);
                        }
                        sourceAsMap.put(queryField,text.toString());
                    }
                }

                list.add(sourceAsMap);
            }
            return list;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}

```
爬取数据后直接添加进ElasticSearch 
```java
package cn.flowboot.es.utils;

import cn.flowboot.es.doc.Good;
import cn.flowboot.es.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/04/10
 */
@Slf4j
@SpringBootTest
public class SearchJdGoodUtilsTest {

    @Autowired
    private SearchJdGoodUtils searchJdGoodUtils;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Test
    public void ImportDataToES(){
        try {
            //"spring", "html","java", "c"
            List<String> keys = Arrays.asList("python", "bigdata", "hadoop", "mac", "iphone", "mi", "it", "css", "html", "vue", "excel","huawei","one","360","oppo","vivo");
            for (String key : keys) {
                log.info("查询 {}",key);
                Set<Good> goodAll = searchJdGoodUtils.getGoodAll(key);
                boolean good = elasticSearchService.bulkAddData("good", goodAll);
                log.info("是否添加成功 {}",good);
                Thread.sleep(new Random().nextInt(5000)+8000);//休息
            }

        } catch (Exception e) {
            log.info("抓取数据异常：{}",e);
        }
    }
}

```

# 页面使用
使用到vue,jquery，axios,其中img,css,js等文件访问: [Gitee](https://gitee.com/Vincent-Vic/elastic-search-learn/tree/master/src/main/resources/static)
> 网页来自互联网
index.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="utf-8"/>
    <title>ES仿京东实战</title>
    <link rel="stylesheet" th:href="@{/css/style.css}"/>
</head>

<body class="pg">
<div class="page" id="app">
    <div id="mallPage" class=" mallist tmall- page-not-market ">

        <!-- 头部搜索 -->
        <div id="header" class=" header-list-app">
            <div class="headerLayout">
                <div class="headerCon ">
                    <!-- Logo-->
                    <h1 id="mallLogo">
                        <img th:src="@{/images/jdlogo.png}" alt="">
                    </h1>

                    <div class="header-extra">

                        <!--搜索-->
                        <div id="mallSearch" class="mall-search">
                            <form name="searchTop" class="mallSearch-form clearfix">
                                <fieldset>
                                    <legend>天猫搜索</legend>
                                    <div class="mallSearch-input clearfix">
                                        <div class="s-combobox" id="s-combobox-685">
                                            <div class="s-combobox-input-wrap">
                                                <input type="text" autocomplete="off" value="dd" id="mq"
                                                       class="s-combobox-input" aria-haspopup="true" v-model="keyword">
                                            </div>
                                        </div>
                                        <button type="submit" @click.prevent="handleSearch" id="searchbtn">搜索</button>
                                    </div>
                                </fieldset>
                            </form>
                            <ul class="relKeyTop">
                                <li><a>Java</a></li>
                                <li><a>前端</a></li>
                                <li><a>Linux</a></li>
                                <li><a>大数据</a></li>
                                <li><a>理财</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品详情页面 -->
        <div id="content">
            <div class="main">
                <!-- 品牌分类 -->
                <form class="navAttrsForm">
                    <div class="attrs j_NavAttrs" style="display:block">
                        <div class="brandAttr j_nav_brand">
                            <div class="j_Brand attr">
                                <div class="attrKey">
                                    品牌
                                </div>
                                <div class="attrValues">
                                    <ul class="av-collapse row-2">
                                        <li><a href="#"> ES</a></li>
                                        <li><a href="#"> Java </a></li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
                <!-- 排序规则 -->
                <div class="filter clearfix">
                    <a class="fSort fSort-cur">综合<i class="f-ico-arrow-d"></i></a>
                    <a class="fSort">人气<i class="f-ico-arrow-d"></i></a>
                    <a class="fSort">新品<i class="f-ico-arrow-d"></i></a>
                    <a class="fSort">销量<i class="f-ico-arrow-d"></i></a>
                    <a class="fSort">价格<i class="f-ico-triangle-mt"></i><i class="f-ico-triangle-mb"></i></a>
                </div>

                <!-- 商品详情 -->
                <div class="view grid-nosku">

                    <div class="product" v-for="item in results">
                        <div class="product-iWrap">
                            <!--商品封面-->
                            <div class="productImg-wrap">
                                <a class="productImg">
                                    <img :src="item.imgUrl">
                                </a>
                            </div>
                            <!--价格-->
                            <p class="productPrice">
                                <em><b>¥</b>{{item.price}}</em>
                            </p>
                            <!--标题-->
                            <p class="productTitle">
                                <a v-html="item.title"></a>
                            </p>
                            <!-- 店铺名 -->
                            <div class="productShop">
                                <span>店铺： Java </span>
                            </div>
                            <!-- 成交信息 -->
                            <p class="productStatus">
                                <span>月成交<em>999笔</em></span>
                                <span>评价 <a>3</a></span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 使用vue -->
<script th:src="@{/js/axios.min.js}"></script>
<script th:src="@{/js/vue.min.js}"></script>
<script>


    new Vue({
        el:'#app',
        data:{
            keyword:'',
            results:[]
        },
        methods:{
            handleSearch(){
                var keyword = this.keyword;
                console.log(keyword);
                axios.get('search/highlight/'+keyword+'/1/10').then(res=>{
                    this.results = res.data;
                })
            }
        }
    })

</script>
</body>
</html>

```
页面控制器
```java
@Controller
public class IndexController {

    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }
}
```

完整代码： [代码仓库](https://gitee.com/Vincent-Vic/elastic-search-learn/)
