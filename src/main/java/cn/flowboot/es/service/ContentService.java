package cn.flowboot.es.service;

import java.util.List;
import java.util.Map;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
public interface ContentService {

    void captureJdDataAsync(String keyword);

    List<Map<String,Object>> searchByPage(String keyword,int pageNum,int pageSize);

    List<Map<String,Object>> searchByHighlightAndPage(String keyword,int pageNum,int pageSize);
}
