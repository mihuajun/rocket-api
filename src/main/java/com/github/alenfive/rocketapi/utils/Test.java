package com.github.alenfive.rocketapi.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/11/20 9:28
 * @UpdateDate: 2020/11/20 9:28
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
public class Test {
    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        engine.eval("def xx = \"name\"");
    }
}
