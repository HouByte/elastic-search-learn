package cn.flowboot.es.controller;

import cn.flowboot.es.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/25
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class ContentController {

    private final ContentService contentService;


    @GetMapping("/search/{keyword}/{pageNum}/{pageSize}")
    List<Map<String,Object>> searchByPage(@PathVariable("keyword")String keyword, @PathVariable("pageNum") Integer pageNum,@PathVariable("pageSize") Integer pageSize){
        log.info("keyword {} pageNum {} pageSize {}",keyword,pageNum,pageSize);
        return contentService.searchByPage(keyword, pageNum, pageSize);
    }
    @GetMapping("/search/highlight/{keyword}/{pageNum}/{pageSize}")
    List<Map<String,Object>> searchByHighlightAndPage(@PathVariable("keyword")String keyword, @PathVariable("pageNum") Integer pageNum,@PathVariable("pageSize") Integer pageSize){
        log.info("keyword {} pageNum {} pageSize {}",keyword,pageNum,pageSize);
        return contentService.searchByHighlightAndPage(keyword, pageNum, pageSize);
    }

}
