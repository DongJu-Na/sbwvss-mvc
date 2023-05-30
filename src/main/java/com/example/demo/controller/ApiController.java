package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${video.uploadPath}")
	private String uploadPath;
	
  @GetMapping("/{fileName}")
  public ResponseEntity<InputStreamResource> streamResourceFromDisk(@PathVariable String fileName) throws Exception {
  	HttpHeaders headers = new HttpHeaders();
    	String FILE_DIRECTORY = uploadPath + fileName;
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
  
  @GetMapping("/getVideoFile")
  public ResponseEntity<List<String>> getVideoFile() throws Exception {
      List<String> result = null;
      
      String directoryPath = uploadPath; 
      
      File directory = new File(directoryPath);
      File[] files = directory.listFiles();
      
      List<String> fileNames = new ArrayList<>();
      if (files != null) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".mp4")) {
                fileNames.add(file.getName());
            }
        }
    }
      
      return ResponseEntity.ok(fileNames);
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
  
  
  @PostMapping("/save")
  public ResponseEntity<Map<String, Object>> createVideo(@RequestParam Map<String, MultipartFile> files,
                                                        @RequestParam(value = "templateCode", required = true) String tc,
                                                        @RequestParam(value = "qrcodeUrl", required = false) String qrcodeUrl,
                                                        @RequestParam(value = "qrCodeLocation", required = false) String qrCodeLocation,
                                                        @RequestParam(value = "qrcodeBgColor", required = false) String qrcodeBgColor,
                                                        @RequestParam(value = "qrcodeColor", required = false) String qrcodeColor,
                                                        @RequestParam(value = "bgTransparent", required = false) String bgTransparent
                                                        // 파라미터가... 추후 Map으로 관리
                                                        ) throws Exception {
      String resultFileName = "";
      if (!files.isEmpty()) {
          try {
              resultFileName = service.save(files, tc, qrcodeUrl, qrCodeLocation ,qrcodeBgColor ,qrcodeColor , bgTransparent);
          } catch (Exception e) {
              e.printStackTrace();
              return ResponseEntity.ok().body(Collections.singletonMap("status", "fail"));
          }
      }
      
      Map<String, Object> response = new HashMap<>();
      response.put("status", "success");
      response.put("resultFileName", resultFileName);
      
      return ResponseEntity.ok().body(response);
  }
  
}

