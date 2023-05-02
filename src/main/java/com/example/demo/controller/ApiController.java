package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.ApiService;

@RestController
@RequestMapping("/api/v1/stream")
public class ApiController {
	
	@Autowired
	private ApiService service;
	
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
  
  @PostMapping("/upload")
  public ResponseEntity<String> streamResourceUploadDisk(@RequestParam("video") MultipartFile vd,
  																											 @RequestParam("checkSize") String checkSize
  		) throws Exception {
  	  if(vd != null) {
  	  	System.out.println("비디오 저장 start");
  	  	try {
  	  		service.uploadFile(vd,checkSize);
				} catch (Exception e) {
					e.printStackTrace();
					return new ResponseEntity<>("fail", HttpStatus.NOT_IMPLEMENTED);
				}
  	  }
      return new ResponseEntity<>("success", HttpStatus.OK);
  }
	
}