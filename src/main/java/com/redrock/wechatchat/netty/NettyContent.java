package com.redrock.wechatchat.netty;

import com.google.gson.Gson;
import com.redrock.wechatchat.dao.UserDao;
import com.redrock.wechatchat.netty.been.Message;
import com.redrock.wechatchat.netty.been.User;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NettyContent {

    @Autowired
    private UserDao userDao;

    private Gson gson = null;
    private Map<Channel, String> channelMap = null;
    private Map<String, Channel> openidMap = null;
    private ChannelGroup onlionGroup = null;

    public NettyContent() {
        openidMap = new HashMap<>();//uri openid
        channelMap = new HashMap<>();
        onlionGroup = new DefaultChannelGroup("onlionGroup", GlobalEventExecutor.INSTANCE);
        gson = new Gson();

    }

    public void userAdd(Map<String, String> param, Channel incoming) {
        String openid = param.get("openid");
        channelMap.put(incoming, openid);
        openidMap.put(openid, incoming);
        onlionGroup.add(incoming);
        System.out.println(openid + "上线");
    }

    public void userLeave(Channel leave) {
        String openid = channelMap.get(leave);
        channelMap.remove(leave);
        openidMap.remove(openid);
        onlionGroup.remove(leave);
        System.out.println(openid + "离开");
    }

    public void send(Message message) {
        String toUserNickname = message.getToUser();
        System.out.println("from:" + message.getFromUser());
        System.out.println("to: " + toUserNickname);
        System.out.println("text:" + message.getText());
        String openid = null;
        User user = userDao.getUserByNickname(toUserNickname);
        openid = user.getOpenid();

        Channel toUser = openidMap.get(openid);
        if ("chat".equals(message.getType()) && !"server".equals(message.getFromUser()))
            userDao.saveMessage(message);

        if (toUser != null) {
            toUser.writeAndFlush(new TextWebSocketFrame(gson.toJson(message)));
        } else {
            Message response = new Message();
            response.setFromUser("server");
            response.setToUser(message.getFromUser());
            response.setType("chat");
            response.setText(message.getToUser() + "不在线");
            send(response);
        }

    }

    public boolean isFriend(String fromUser, String toUser) {//nickname
        User from = userDao.getUserByNickname(fromUser);
        List<User> userList = userDao.getFriend(from.getOpenid());
        for (User user : userList) {
            String nickname = user.getNickname();
            if (nickname.equals(toUser)) {
                return true;
            }
        }
        return false;
    }

    public void addFriendCallBack(String reciverUser) {
        System.out.println("callback");
        Message message = new Message();
        message.setFromUser("server");
        message.setToUser(reciverUser);
        message.setType("newFriend");
        message.setText("emm");
        send(message);
    }


}
