package com.github.alenfive.dataway2.function;

import com.github.alenfive.dataway2.extend.ApiInfoContent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description:日志输出
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Component
@Slf4j
public class LogFunction implements IFunction{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Override
    public String getVarName() {
        return "log";
    }

    public void info(String format,Object ... args){
        log.info(format,args);
        apiInfoContent.putLog("info:"+ MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void debug(String format,Object ... args){
        log.debug(format,args);
        apiInfoContent.putLog("debug: "+ MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void error(String format,Object ... args){
        log.error(format,args);
        apiInfoContent.putLog("error: "+ MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void warn(String format,Object ... args){
        log.error(format,args);
        apiInfoContent.putLog("warn: "+ MessageFormatter.arrayFormat(format, args).getMessage());
    }

    public void trace(String format,Object ... args){
        log.error(format,args);
        apiInfoContent.putLog("trace: "+ MessageFormatter.arrayFormat(format, args).getMessage());
    }
}
