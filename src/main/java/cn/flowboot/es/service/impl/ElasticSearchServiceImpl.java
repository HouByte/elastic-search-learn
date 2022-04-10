package cn.flowboot.es.service.impl;

import cn.flowboot.es.service.ElasticSearchService;
import com.alibaba.fastjson.JSON;
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
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@Slf4j
@Service
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {

    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public ElasticSearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

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
