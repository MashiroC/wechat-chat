package com.redrock.wechatchat.netty.handler;

import com.redrock.wechatchat.netty.NettyContent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Autowired
    private NettyContent content;

    public HttpRequestHandler(NettyContent content) {
        this.content=content;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println("add!");
        Channel incoming = ctx.channel();
        String uri = request.uri();
        System.out.println(uri);
        if (uri.startsWith("/ws")) {
            Map<String,String> map = new HashMap<>();
            String s = uri.replaceFirst("/ws\\?", "");
            String[] param = s.split("&");
            for (String str : param) {
                String[] keyValue = str.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                map.put(key,value);
            }
            content.userAdd(map,incoming);
            request.setUri("/ws");

            ctx.fireChannelRead(request.retain());
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx){
        Channel leave = ctx.channel();
        content.userLeave(leave);
    }
}
