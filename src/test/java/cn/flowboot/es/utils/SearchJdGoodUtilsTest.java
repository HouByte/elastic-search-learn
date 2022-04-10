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
