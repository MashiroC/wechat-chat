package com.redrock.wechatchat.netty.handler;

import com.google.gson.Gson;
import com.redrock.wechatchat.been.Message;
import com.redrock.wechatchat.netty.NettyRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Gson gson = new Gson();

    private NettyRepository content;

    public ChatHandler(NettyRepository content) {
        this.content = content;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame socketFrame) throws Exception {
        String json = socketFrame.text();
        Message message = gson.fromJson(json, Message.class);
        String fromUser = message.getFromUser();
        String toUser = message.getToUser();
        if (content.isFriend(fromUser, toUser)) {
            content.send(message);
        }else{
            Message response = new Message();
            response.setFromUser("server");
            response.setToUser(fromUser);
            response.setType("chat");
            response.setText("你未与"+toUser+"添加好友");
            content.send(response);
        }
    }

}
