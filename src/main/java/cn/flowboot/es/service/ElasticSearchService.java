package cn.flowboot.es.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
public interface ElasticSearchService <T>{

    /**
     * 批量添加数据
     */
    boolean bulkAddData(String index, Set<T> list);

    /**
     * 查询
     * @param index 索引
     * @param keyword 关键字
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return
     */
    List<Map<String,Object>> searchByPage(String queryField,String index,String keyword, int pageNum, int pageSize,boolean isHighlight);

}
