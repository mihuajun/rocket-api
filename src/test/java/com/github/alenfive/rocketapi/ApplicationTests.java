package com.github.alenfive.rocketapi;

import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

/**
 * 数据为操作解析器测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={RocketAPIApplication.class})// 指定启动类
@Slf4j
public class ApplicationTests {

    @Autowired
    private ScriptParseService parseService;

    @Test
    public void testVar() {
        StringBuilder script  = new StringBuilder("from ${table} where id in = #{id} and name=#{name123456789}#{name123456789}");
        Map<String,Object> params = new HashMap<>();
        params.put("id","123");
        params.put("table","t_user");
        params.put("name123456789"," and #{phone}");
        parseService.buildParams(script,null,params);
        log.info("testVar:{}",script);
        assert script.toString().equals("from t_user where id in = '123' and name=' and #{phone}'' and #{phone}'");
    }

    @Test
    public void testArrayVar(){
        StringBuilder script  = new StringBuilder("where id in (#{idList})");
        Map<String,Object> params = new HashMap<>();
        params.put("idList", Arrays.asList("11",22));
        parseService.buildParams(script,null,params);
        log.info("testFor:{}",script);
        assert script.toString().equals("where id in ('11',22)");
    }

    @Test
    public void testRandomArrayVar(){
        StringBuilder script  = new StringBuilder("where id = #{idList[1].name}");
        Map<String,Object> params = new HashMap<>();
        Map<String,Object> child = new HashMap<>();
        child.put("name","王");

        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add(child);
        params.put("idList", list);
        parseService.buildParams(script,null,params);
        log.info("testRandomArrayVar:{}",script);
        assert script.toString().equals("where id = '王'");
    }

    @Test
    public void testIf(){
        StringBuilder script  = new StringBuilder("where ?{id,and id=#{id}} and 1=1");
        Map<String,Object> params = new HashMap<>();
        params.put("id","123");
        parseService.buildIf(script,params);
        log.info("testIf:{}",script.toString());
        assert script.toString().equals("where and id=#{id} and 1=1");

        script  = new StringBuilder("where ?{id,and id=#{id}} and 1=1");
        params = new HashMap<>();
        parseService.buildIf(script,params);
        log.info("testIf:{}",script.toString());
        assert script.toString().equals("where  and 1=1");
    }

    @Test
    public void testFor(){
        List<Map<String,Object>> ss = new ArrayList<>();

    }


    @Autowired
    private IApiPager apiPager;

    @Test
    public void testPager(){
        StringBuilder script = new StringBuilder("select * from user limit #{index},#{pageSize}");

        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",2);

        Integer pageNo = null;
        Object value = parseService.buildContentScopeParamItem(params,apiPager.getPageNoVarName());
        if (StringUtils.isEmpty(value)){
            params.put(apiPager.getPageNoVarName(),apiPager.getPageNoDefaultValue());
            pageNo = apiPager.getPageNoDefaultValue();
        }else {
            pageNo = Integer.valueOf(value.toString());
        }

        Integer pageSize = null;

        value = parseService.buildContentScopeParamItem(params,apiPager.getPageSizeVarName());
        if (StringUtils.isEmpty(value)){
            params.put(apiPager.getPageSizeVarName(),apiPager.getPageSizeDefaultValue());
            pageSize = apiPager.getPageSizeDefaultValue();
        }else{
            pageSize =Integer.valueOf(value.toString());

        }

        params.put(apiPager.getIndexVarName(),apiPager.getIndexVarValue(pageSize,pageNo));
        parseService.buildParams(script,null,params);
        log.info("testPager:{}",script.toString());
        assert script.toString().equals("select * from user limit 15,15");

    }

    @Test
    public void jsScript() throws ScriptException {
        String str = "4+5";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        System.out.println(engine.eval(str));
    }
}