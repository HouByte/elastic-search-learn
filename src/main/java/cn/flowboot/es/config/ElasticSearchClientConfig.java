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

    @PreDestroy
    public void closeElasticSearchClient(){
        try {
            log.info("Stopping RestHighLevelClient");
            restHighLevelClient().close();
            log.info("Has stopped RestHighLevelClient");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
