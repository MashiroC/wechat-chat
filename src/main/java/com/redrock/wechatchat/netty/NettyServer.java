package com.redrock.wechatchat.netty;

import com.redrock.wechatchat.dao.UserDao;
import com.redrock.wechatchat.netty.handler.ChatHandler;
import com.redrock.wechatchat.netty.handler.HttpRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;

@Component
public class NettyServer {
    private final int PORT = 8081;
    NioEventLoopGroup group = null;

    @Autowired
    NettyRepository content;
    @Autowired
    UserDao userDao;

    @PostConstruct
    public void run() throws InterruptedException, IOException {
        group = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(PORT))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(64 * 1024))
                                .addLast(new HttpRequestHandler(content))
                                .addLast(new WebSocketServerProtocolHandler("/ws"))
                                .addLast(new ChatHandler(content));
                    }
                });
        System.out.println("开启");
        Channel f = b.bind().sync().channel();

    }

    @PreDestroy
    public void close() throws InterruptedException {
        System.out.println("close");
        group.shutdownGracefully().sync();
    }

}