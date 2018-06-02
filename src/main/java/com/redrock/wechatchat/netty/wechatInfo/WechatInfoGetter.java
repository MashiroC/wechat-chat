package com.redrock.wechatchat.netty.wechatInfo;

import com.google.gson.Gson;
import com.redrock.wechatchat.util.SendUtil;
import org.springframework.stereotype.Component;

public class WechatInfoGetter {

    private static Gson gson = new Gson();
    private String appid;
    private String secret;

    public WechatInfoGetter(String appid, String secret) {
        this.appid = appid;
        this.secret = secret;
    }

    public WechatUserInfo getUserInfo(String code) {
        WechatUserInfo userInfo = null;
        String url1 = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid + "&secret=" + secret + "&code=" +
                code + "&grant_type=authorization_code";
        String result1 = SendUtil.sendGet(url1);
        WechatCodeResponse wechatCodeResponse = gson.fromJson(result1, WechatCodeResponse.class);
        if (wechatCodeResponse.getErrcode() == null) {
            String url2 = "https://api.weixin.qq.com/sns/userinfo?access_token=" + wechatCodeResponse.getAccessToken() + "&openid=" + wechatCodeResponse.getOpenid() + "&lang=zh_CN";
            String result2 = SendUtil.sendGet(url2);
            userInfo = gson.fromJson(result2, WechatUserInfo.class);
            if (userInfo.getErrcode() != null) {
                throw new RuntimeException("获取信息发生错误！\n错误码为"+userInfo.getErrcode()+"\n错误信息为："+userInfo.getErrmsg());
            }
        } else {
            throw new RuntimeException("获取信息发生错误！\n错误码为:" + wechatCodeResponse.getErrcode() + "\n错误描述为：" + wechatCodeResponse.getErrmsg());
        }
        return userInfo;
    }
}
