package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stream")
public class Controller {
	
  @GetMapping("/{fileName}")
  public ResponseEntity<InputStreamResource> streamResourceFromDisk(@PathVariable String fileName) throws Exception {
  	HttpHeaders headers = new HttpHeaders();
    	String FILE_DIRECTORY = "uploads/" + fileName;
    	headers.set("Accept-Ranges", "bytes");
      headers.set("Content-Type", "video/mp4");
      
      File file = new File(FILE_DIRECTORY);
      
      if(!file.exists()) {
      	return new ResponseEntity<>(new InputStreamResource(null), headers, HttpStatus.NOT_FOUND);
      }
      
      InputStream inputStream = new FileInputStream(FILE_DIRECTORY);
      headers.set("Content-Length", String.valueOf(file.length()));
      
      return new ResponseEntity<>(new InputStreamResource(inputStream), headers, HttpStatus.OK);
  }
	
}
