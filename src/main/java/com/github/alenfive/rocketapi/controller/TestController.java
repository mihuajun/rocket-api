package com.github.alenfive.rocketapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 测试类
 */
@RestController
@RequestMapping
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = {"/hello/{hello}99"})
    public Object test(String id,
                       String name,
                       @PathVariable String hello,
                       ApiInfo apiInfo){
        return jdbcTemplate.queryForList("select * from user");
    }

    @GetMapping(value = {"/hello/test1"})
    public Object test11(){
        return "hello";
    }

    @RequestMapping("/file/upload2")
    public void fileUpload(MultipartFile[] files,HttpServletRequest request) throws IOException {
        if (request.getContentType().indexOf("application/json") >=0){
            Map<String,Object> body = objectMapper.readValue(request.getInputStream(),Map.class);
        }
    }
    @GetMapping(value = {"/hello/test2"})
    public Object test2(HttpServletRequest request){

        Set<MediaType> mediaTypeSet = new HashSet<>();
        mediaTypeSet.add(MediaType.ALL);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypeSet);
        return null;
    }

    @PostMapping(value = {"/hello/test3"})
    public Object test3(@RequestBody ApiInfo apiInfo){
        return null;
    }
}
