package com.redrock.wechatchat.netty.handler;

import com.google.gson.Gson;
import com.redrock.wechatchat.netty.been.Message;
import com.redrock.wechatchat.netty.NettyContent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Gson gson = new Gson();

    private NettyContent content;

    public ChatHandler(NettyContent content) {
        this.content = content;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame socketFrame) throws Exception {
        String json = socketFrame.text();
        System.out.println(json);
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

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("add");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }
}
