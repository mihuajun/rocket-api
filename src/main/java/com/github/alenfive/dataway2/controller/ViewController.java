package com.github.alenfive.dataway2.controller;

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
@RequestMapping("/api-ui")
public class ViewController {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Value("${spring.application.name}")
    private String service;

    @GetMapping
    public String index(Model model,HttpServletRequest request){
        model.addAttribute("dataSourceList",dataSourceManager.getDialectMap().keySet());
        model.addAttribute("user", LoginUtils.getUser(request));
        model.addAttribute("service", service);
        return "api_index";
    }

    @GetMapping("/{id}/{page}")
    public String index(Model model, @PathVariable String id, @PathVariable String page, HttpServletRequest request){
        model.addAttribute("dataSourceList",dataSourceManager.getDialectMap().keySet());
        model.addAttribute("currApi",id);
        model.addAttribute("currPage",page);
        model.addAttribute("user", LoginUtils.getUser(request));
        model.addAttribute("service", service);
        return "api_index";
    }
}
