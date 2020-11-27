package com.github.alenfive.rocketapi.controller;

import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.utils.PackageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Api ui 页面显示
 */
@Controller
@RequestMapping("${spring.rocket-api.base-register-path:/interface-ui}")
public class ViewController {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private RocketApiProperties properties;

    @Value("${spring.application.name}")
    private String service;

    @GetMapping
    public String index(Model model){
        model.addAttribute("dataSourceList",dataSourceManager.getDialectMap().keySet());
        model.addAttribute("service", service);
        model.addAttribute("configEnabled",properties.isConfigEnabled());
        model.addAttribute("version", PackageUtils.getVersion());
        return "rocketapi/api-index";
    }


}
