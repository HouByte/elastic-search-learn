package cn.flowboot.es.controller;

import cn.flowboot.es.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2021/08/24
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class CaptureController {

    private final ContentService contentService;

    @GetMapping("capJd")
    public String captureJDGood(String keyword){
        contentService.captureJdDataAsync(keyword);
        return "请求成功";
    }
}
