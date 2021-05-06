package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.vo.DocsInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认API信息接口同步，
 */
@Slf4j
@Component
public class DefaultApiDocSync implements IApiDocSync {

    @Override
    public String sync(DocsInfo docsInfo) {
        return "Successful push";
    }
}
