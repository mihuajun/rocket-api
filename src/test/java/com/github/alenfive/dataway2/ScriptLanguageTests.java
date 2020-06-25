package com.github.alenfive.dataway2;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @Description: 脚本引擎测试
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/22 19:25
 * @UpdateDate: 2020/6/22 19:25
 * @UpdateRemark: init
 * @Version: 1.0
 */
public class ScriptLanguageTests {
    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("js");
        engine.eval("var fruits = [\"Banana\", \"Orange\", \"Apple\", \"Mango\"]; var d = fruits.toString();");
        System.out.println(engine.get("fruits") instanceof Object[]);
        System.out.println(engine.get("d").getClass());
        System.out.println(engine.get("d"));
    }
}
