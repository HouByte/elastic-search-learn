package cn.flowboot.es;


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
     * 测试批量加入文档
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
