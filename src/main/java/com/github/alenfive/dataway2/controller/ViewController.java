package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.Dataway2Properties;
import com.github.alenfive.dataway2.datasource.DataSourceManager;
import com.github.alenfive.dataway2.utils.LoginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/22 9:37
 * @UpdateDate: 2020/5/22 9:37
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 页面显示
 */
@Controller
@RequestMapping("${spring.dataway2.base-path:/api-ui}")
public class ViewController {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private Dataway2Properties properties;

    @Value("${spring.application.name}")
    private String service;

    @GetMapping
    public String index(Model model,HttpServletRequest request){
        model.addAttribute("dataSourceList",dataSourceManager.getDialectMap().keySet());
        model.addAttribute("user", LoginUtils.getUser(request));
        model.addAttribute("service", service);
        model.addAttribute("basePath",buildBasePath(request));
        return "dataway2/api-index";
    }

    @GetMapping("/{id}/{page}")
    public String index(Model model, @PathVariable String id, @PathVariable String page, HttpServletRequest request){
        model.addAttribute("dataSourceList",dataSourceManager.getDialectMap().keySet());
        model.addAttribute("currApi",id);
        model.addAttribute("currPage",page);
        model.addAttribute("user", LoginUtils.getUser(request));
        model.addAttribute("service", service);
        model.addAttribute("basePath",buildBasePath(request));
        return "dataway2/api-index";
    }

    private String buildBasePath(HttpServletRequest request){
        String basePath = request.getContextPath()+properties.getBasePath();
        basePath = basePath.replace("//","/");
        if (basePath.endsWith("/")){
            basePath = basePath.substring(0,basePath.length()-1);
        }
        return basePath;
    }
}
