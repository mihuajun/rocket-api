package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.Dataway2Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
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
    private Dataway2Properties properties;

    @GetMapping
    public String index(){
        return "index";
    }

    @GetMapping("/v2")
    public String index2(Model model){
        model.addAttribute("apiPrefix",properties.getApiPrefix());
        return "index_bak";
    }
}
