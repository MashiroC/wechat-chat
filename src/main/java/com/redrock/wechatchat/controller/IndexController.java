package com.redrock.wechatchat.controller;

import com.google.gson.Gson;
import com.redrock.wechatchat.dao.UserDao;
import com.redrock.wechatchat.been.Message;
import com.redrock.wechatchat.been.User;
import com.redrock.wechatchat.been.WechatMessage;
import com.redrock.wechatchat.netty.NettyRepository;
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
    NettyRepository content;

    @Autowired
    UserDao userDao;

    private WechatInfoGetter infoGetter;

    Gson gson = new Gson();

    public IndexController() {
        infoGetter = new WechatInfoGetter("wxd5fb5e11338cc615", "ced687fad3cd8d0fdc79582ceae4b386");
    }

    @GetMapping("/getInfo")
    public String getInfo(String code) {
        if ("test".equals(code)) {
            User user = userDao.getUserByNickname("MashiroC");
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
        return gson.toJson(userList);
    }

    @GetMapping("/getUnFriendList")
    public String getUnfriendList(String openid) {
        List<User> userList = userDao.getUnAddFriend(openid);
        return gson.toJson(userList);
    }

    @GetMapping("/getAllMessage")
    public String getAllMessage(String openid){
        List<Message> list = userDao.getAllMessage(openid);
        return gson.toJson(list);
    }

    private boolean isUserExist(User user) {
        return userDao.getUserByOpenid(user.getOpenid()) != null;
//        return true;
    }
}
