package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "pages")
public class LoginContoller {

    @GetMapping(value = "login")
    public ModelMap mmTableElements() {
        return new ModelMap();
    }

}
