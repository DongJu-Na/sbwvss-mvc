package com.example.demo.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonUtils {
	
  public static Map<String, Object> jsonToMap(String json) {
  	Map<String, Object> result = new HashMap<String,Object>();
    Gson gson = new Gson();
    Type type = new TypeToken<Map<String, Object>>(){}.getType();
    
    try {
    	result = gson.fromJson(json, type);
		} catch (Exception e) {
			result = null;
		}
    return result; 
  }
	
  
}
