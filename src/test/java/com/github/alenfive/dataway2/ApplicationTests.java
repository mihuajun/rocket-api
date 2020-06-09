package com.github.alenfive.dataway2;

import com.github.alenfive.dataway2.entity.ApiParams;
import com.github.alenfive.dataway2.extend.ApiPagerInterface;
import com.github.alenfive.dataway2.service.ScriptParseService;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/28 16:01
 * @UpdateDate: 2020/5/28 16:01
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu test
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={Dataway2Application .class})// 指定启动类
@Slf4j
public class ApplicationTests {

    @Autowired
    private ScriptParseService parseService;

    @Test
    public void testVar() {
        StringBuilder script  = new StringBuilder("where id in = #{id} and name=#{name}");
        ApiParams apiParams = new ApiParams();
        apiParams.putParam("id","123");
        apiParams.putParam("name","456");
        parseService.buildParams(script,apiParams);
        log.info("testVar:{}",script);
        assert script.toString().equals("where id in = '123' and name='456'");
    }

    @Test
    public void testArrayVar(){
        StringBuilder script  = new StringBuilder("where id in (#{idList})");
        ApiParams apiParams = new ApiParams();
        apiParams.putParam("idList", Arrays.asList("11",22));
        parseService.buildParams(script,apiParams);
        log.info("testFor:{}",script);
        assert script.toString().equals("where id in ('11',22)");
    }

    @Test
    public void testIf(){
        StringBuilder script  = new StringBuilder("where #?{id,and id=#{id}} and 1=1");
        ApiParams apiParams = new ApiParams();
        apiParams.putParam("id","123");
        parseService.buildIf(script,apiParams);
        log.info("testIf:{}",script.toString());
        assert script.toString().equals("where and id=#{id} and 1=1");

        script  = new StringBuilder("where #?{id,and id=#{id}} and 1=1");
        apiParams = new ApiParams();
        parseService.buildIf(script,apiParams);
        log.info("testIf:{}",script.toString());
        assert script.toString().equals("where  and 1=1");
    }

    @Test
    public void testFor(){
        List<Map<String,Object>> ss = new ArrayList<>();

    }

    @Test
    public void testExtract(){
        String script  =
                "//注释\n" +
                "start11;\n" +
                "//单行注释\n" +
                "end11;";
        List<StringBuilder> scriptList = parseService.extractExecutableScript(script);
        System.out.println(scriptList);
    }

    @Autowired
    private ApiPagerInterface apiPager;

    @Test
    public void testPager(){
        StringBuilder script = new StringBuilder("select * from user limit #{index},#{pageSize}");

        ApiParams apiParams = new ApiParams();
        apiParams.putParam("pageNo",2);

        Integer pageNo = null;
        Object value = parseService.buildParamItem(apiParams,apiPager.getPageNoVarName());
        if (StringUtils.isEmpty(value)){
            apiParams.putParam(apiPager.getPageNoVarName(),apiPager.getPageNoDefaultValue());
            pageNo = apiPager.getPageNoDefaultValue();
        }else {
            pageNo = Integer.valueOf(value.toString());
        }

        Integer pageSize = null;

        value = parseService.buildParamItem(apiParams,apiPager.getPageSizeVarName());
        if (StringUtils.isEmpty(value)){
            apiParams.putParam(apiPager.getPageSizeVarName(),apiPager.getPageSizeDefaultValue());
            pageSize = apiPager.getPageSizeDefaultValue();
        }else{
            pageSize =Integer.valueOf(value.toString());

        }

        apiParams.putParam(apiPager.getIndexVarName(),apiPager.getIndexVarValue(pageSize,pageNo));
        parseService.buildParams(script,apiParams);
        log.info("testPager:{}",script.toString());
        assert script.toString().equals("select * from user limit 15,15");

    }
}