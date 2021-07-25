package com.github.alenfive.rocketapi;

import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.regex.Pattern;

/**
 * 脚本引擎测试
 */
@Slf4j
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


    @Test
    public void testIndexOf(){
        int start = 0;
        String str = "12345612";
        int index = str.indexOf("1",start);
        log.info("index:{}",index);
        StringBuilder sb = new StringBuilder(str);

        log.info("sub str:{}",sb.replace(0,2,"---"));
    }

    @Test
    public void testParamGet(){
        Pattern r = Pattern.compile("(#|\\$)\\{(.*?)\\}");
        StringBuilder script = new StringBuilder("where id = #{if(a>1){return \"1}else{return 2}} and name = #{\"name}");
        //Matcher matcher = r.matcher(str);

        String flag = "#{";
        //匹配参数#{}
        do {
            int startIf = script.indexOf(flag);
            if (startIf == -1) {
                break;
            }

            int ifCloseIndex = -1;
            int quotationMark = 0;
            int bigBracket = 1;

            for(int i=startIf+flag.length();i<script.length();i++){
                char c = script.charAt(i);

                if (quotationMark > 0){
                    if (c == '\\') {
                        i++;
                        continue;
                    }
                    if (c == '"'){
                        quotationMark --;
                    }
                    continue;
                }

                if (c == '"'){
                    quotationMark ++;
                    continue;
                }


                if (c == '{'){
                    bigBracket ++ ;
                }

                if (c == '}'){
                    bigBracket -- ;
                }

                if (c == '}' && bigBracket == 0){
                    ifCloseIndex = i;
                    break;
                }
            }

            if (ifCloseIndex == -1){
                throw new IllegalArgumentException("missed if close '}'");
            }

            script = script.replace(startIf,ifCloseIndex+1,"");
            log.info("script:{}",script.substring(startIf+flag.length(),ifCloseIndex));
        }while (true);


    }

}
