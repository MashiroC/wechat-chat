package com.redrock.wechatchat.controller;

import com.google.gson.Gson;
import com.redrock.wechatchat.been.TuringResponseEntity;
import com.redrock.wechatchat.been.WechatMessage;
import com.redrock.wechatchat.util.TuringUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

@RestController
public class AccessController {

    private static final String TOKEN = "zxczxc";

    @PostMapping(value = "/access", produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public WechatMessage reply(@RequestBody WechatMessage message) {
        TuringResponseEntity entity = TuringUtil.chatWithTuringRobot(message.getFromUserName(), message.getContent());
        String content = entity.getText();

        WechatMessage responseMessage = new WechatMessage();
        responseMessage.setFromUserName(message.getToUserName());
        responseMessage.setToUserName(message.getFromUserName());
        responseMessage.setContent(content);
        responseMessage.setMsgType("text");
        responseMessage.setCreateTime(String.valueOf(new Date().getTime()));
        return responseMessage;
    }

    @GetMapping("/access")
    public String access(@Param("signature") String signature, @Param("timestamp") String timestamp,
                         @Param("nonce") String nonce, @Param("echostr") String echostr) {
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (signature != null && checkSignature(signature, timestamp, nonce)) {
            return echostr;
        }
        return null;
    }

    private static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = new String[]{TOKEN, timestamp, nonce};
        // 将token、timestamp、nonce三个参数进行字典序排序
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
        }
        MessageDigest md = null;
        String tmpStr = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将三个参数字符串拼接成一个字符串进行sha1加密
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        content = null;
        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
        return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
    }


    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        String s = new String(tempArr);
        return s;
    }
}
