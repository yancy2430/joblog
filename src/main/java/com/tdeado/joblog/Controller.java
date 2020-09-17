package com.tdeado.joblog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器类
 */
@RestController
@RequestMapping("/")
public class Controller {
    @Autowired
    RedisService redisService;
    @RequestMapping("add")
    public boolean add(String type,String content){
        String key = type + "_" + System.currentTimeMillis();
        redisService.set(key,content);
        System.err.println(redisService.get(key));
        return true;
    }
}
