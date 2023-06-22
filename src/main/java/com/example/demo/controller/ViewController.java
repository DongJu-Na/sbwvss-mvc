package com.example.demo.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import java.net.InetAddress;

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
	
	@RequestMapping("/myInfo")
	public String myInfo() {
		try {
		  	System.out.println("PC 정보");
		  	System.out.println();

		  	InetAddress ip = InetAddress.getLocalHost();

		  	System.out.println("Name + IP: " + ip.toString());
		  	System.out.println("Name:" + ip.getHostName());
		  	System.out.println("IP address: " + ip.getHostAddress());
		  	System.out.println("Full name: " + ip.getCanonicalHostName());
		}

		catch(Exception ex) {
			ex.printStackTrace();
		}
		return "myInfo";
	}
	
	
}
