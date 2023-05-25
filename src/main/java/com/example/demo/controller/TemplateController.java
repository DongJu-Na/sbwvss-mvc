package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "pages")
public class TemplateController {

    @GetMapping(value = "template")
    public ModelMap template() {
        return new ModelMap();
    }
    
    @GetMapping(value = "templateList")
    public ModelMap templateList() {
        return new ModelMap();
    }
    
    @GetMapping(value = "templateEdit/{tCode}")
    public String templateEdit(@PathVariable("tCode") String tCode) {
        return "pages/templateEdit" + tCode;
    }

}
