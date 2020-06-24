package com.github.alenfive.dataway2;

import javax.script.*;
import java.util.Collection;

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
    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("js");
        engine.eval("var fruits = [\"Banana\", \"Orange\", \"Apple\", \"Mango\"]; var d = fruits.toString();");
        System.out.println(engine.get("fruits") instanceof Object[]);
        System.out.println(engine.get("d").getClass());
        System.out.println(engine.get("d"));
    }
}
