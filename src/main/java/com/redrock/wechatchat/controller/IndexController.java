package com.redrock.wechatchat.controller;

import com.google.gson.Gson;
import com.redrock.wechatchat.dao.UserDao;
import com.redrock.wechatchat.netty.NettyContent;
import com.redrock.wechatchat.netty.been.Message;
import com.redrock.wechatchat.netty.been.User;
import com.redrock.wechatchat.netty.been.WechatMessage;
import com.redrock.wechatchat.netty.wechatInfo.WechatInfoGetter;
import com.redrock.wechatchat.netty.wechatInfo.WechatUserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
public class IndexController {
    private static final String TOKEN = "zxczxc";

    @Autowired
    NettyContent content;

    @Autowired
    UserDao userDao;

    private WechatInfoGetter infoGetter;

    Gson gson = new Gson();

    public IndexController() {
        infoGetter = new WechatInfoGetter("wxd5fb5e11338cc615", "ced687fad3cd8d0fdc79582ceae4b386");
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

    @PostMapping(value = "/access", produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public WechatMessage reply(@RequestBody WechatMessage message) {
        System.out.println("??");
        WechatMessage responseMessage = new WechatMessage();
        responseMessage.setFromUserName(message.getToUserName());
        responseMessage.setToUserName(message.getFromUserName());
        responseMessage.setContent("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd5fb5e11338cc615&redirect_uri=https%3a%2f%2fapi.mashiroc.cn%2f&response_type=code&scope=snsapi_userinfo&state=test#wechat_redirect");
        responseMessage.setMsgType("text");
        responseMessage.setCreateTime(String.valueOf(new Date().getTime()));
        System.out.println(responseMessage.getFromUserName());
        System.out.println(responseMessage.getToUserName());
        System.out.println("emm");
        return responseMessage;
    }

    @GetMapping("/getInfo")
    public String getInfo(String code) {
        System.out.println("code: " + code);
        if ("test".equals(code)) {
            User user = userDao.getUserByNickname("MashiroC");
            System.out.println(user.getNickname());
            return gson.toJson(user);
        } else if ("emm".equals(code)) {
            User user = userDao.getUserByNickname("黑白森子夕");
            return gson.toJson(user);
        } else {
            WechatUserInfo userInfo = infoGetter.getUserInfo(code);
            User user = new User();
            user.setNickname(userInfo.getNickname());
            user.setOpenid(userInfo.getOpenid());
            user.setHeadUrl(userInfo.getHeadimgurl());
            System.out.println(user.getNickname());
            System.out.println(user.getOpenid());
            System.out.println(user.getHeadUrl());
            if (isUserExist(user)) {
                userDao.updateUser(user);
            } else {
                userDao.addUser(user);
            }
            return gson.toJson(user);
        }
    }

    @PostMapping("/addFriend")
    public void addFriend(String fromUser, String toUser) {//openid       nickname or openid
        Message message = new Message();
        User from = userDao.getUserByOpenid(fromUser);
        User to = userDao.getUserByOpenidOrNickname(toUser);
        if (to == null) {
            message.setFromUser("server");
            message.setToUser(from.getNickname());
            message.setType("chat");
            message.setText("未找到用户！");
            content.send(message);
            return;
        }

        List<User> friendList = userDao.getFriend(from.getOpenid());
        for (User user : friendList) {
            if (user.getNickname().equals(toUser) || user.getOpenid().equals(to.getOpenid())) {
                message.setFromUser("server");
                message.setToUser(from.getNickname());
                message.setType("chat");
                message.setText("你和对方已是好友");
                content.send(message);
                return;
            }
        }

        List<User> userList = userDao.getUnAddFriend(from.getOpenid());
        for (User user : userList) {
            if (user.getNickname().equals(to.getOpenid())) {
                message.setFromUser("server");
                message.setToUser(from.getNickname());
                message.setType("chat");
                message.setText("你已经发送了申请");
                content.send(message);
                return;
            }
        }

        userDao.addFriend(from.getOpenid(),to.getOpenid(),0);

        message.setFromUser(from.getNickname());
        message.setToUser(to.getNickname());
        message.setType("add");
        message.setText(from.getHeadUrl());
        content.send(message);

    }

    @PostMapping("/addFriendSuccess")
    public String addFriendSuccess(String fromUser, String toUser) {
        User from = userDao.getUserByNickname(fromUser);
        userDao.uploadFriendSuccess(from.getOpenid(), toUser);
        userDao.addFriend(toUser, from.getOpenid(),1);
        content.addFriendCallBack(fromUser);
        return null;
    }

    @GetMapping("/getFriendList")
    public String getFriendList(String openid) {
        List<User> userList = userDao.getFriend(openid);
        System.out.println(userList);
        return gson.toJson(userList);
    }

    @GetMapping("/getUnFriendList")
    public String getUnfriendList(String openid) {
        List<User> userList = userDao.getUnAddFriend(openid);
        System.out.println("un " + userList);
        return gson.toJson(userList);
    }

    @GetMapping("/getAllMessage")
    public String getAllMessage(String openid){
        System.out.println("open "+openid);
        List<Message> list = userDao.getAllMessage(openid);
        return gson.toJson(list);
    }

    private boolean isUserExist(User user) {
        return userDao.getUserByOpenid(user.getOpenid()) != null;
//        return true;
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
