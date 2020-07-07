package com.github.alenfive.rocketapi.function;

/**
 * 函数接口，实现此接口可自动注册到脚本执行上下文中
 */
public interface IFunction {
    String getVarName();
}
