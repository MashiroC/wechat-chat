package com.redrock.wechatchat.util;

import com.redrock.wechatchat.been.TuringResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class TuringUtil {
    public static TuringResponseEntity chatWithTuringRobot(String openid,String text){
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String,String> param = new LinkedMultiValueMap<>();
        param.add("key","2a96c43cf08342d1bb10ea14769cc07b");
        param.add("info",text);
        param.add("userid",openid);
        return restTemplate.postForObject("http://www.tuling123.com/openapi/api",param, TuringResponseEntity.class);
    }
}
