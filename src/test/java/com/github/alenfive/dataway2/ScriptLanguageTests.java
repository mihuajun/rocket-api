package com.github.alenfive.dataway2;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/6/22 19:25
 * @UpdateDate: 2020/6/22 19:25
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 脚本引擎测试
 */
public class ScriptLanguageTests {
    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval("let list1 = java.util.Objects.equals('a','a');");
        System.out.println(engine.get("list1"));
    }
}
