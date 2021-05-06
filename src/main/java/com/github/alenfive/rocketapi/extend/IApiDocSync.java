package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.vo.DocsInfo;

/**
 * API信息接口同步，
 */
public interface IApiDocSync {
    public String sync(DocsInfo docsInfo);
}
