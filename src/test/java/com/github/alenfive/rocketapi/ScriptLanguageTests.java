package com.github.alenfive.rocketapi;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 脚本引擎测试
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


    @Test
    public void grammarCheck() {
        try {
            String expression = "if(a==1) return 1;";
            new GroovyShell().parse(expression);
        } catch(MultipleCompilationErrorsException cfe) {
            ErrorCollector errorCollector = cfe.getErrorCollector();
            System.out.println("Errors: "+errorCollector.getErrorCount());
        }
    }

}
