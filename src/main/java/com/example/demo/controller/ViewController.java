package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
 
	@RequestMapping("/upload")
	public String index() {
		return "upload";
	}
	
	@RequestMapping("/demo")
	public String demo() {
		return "demo";
	}
	
	/***************************************************
	 * Admin Page Start
	 ***************************************************/

	
}
