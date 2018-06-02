package com.redrock.wechatchat.netty.wechatInfo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class WechatCodeResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private String expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    private String openid;

    private String scope;


    private String errcode;

    private String errmsg;
}
