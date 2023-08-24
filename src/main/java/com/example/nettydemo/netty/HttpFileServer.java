package com.example.nettydemo.netty;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.util.ResourceLeakDetector;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Component
public class HttpFileServer {
    private static class SingletionWSServer {
        static final HttpFileServer instance = new HttpFileServer();
    }

    public static HttpFileServer getInstance() {
        return SingletionWSServer.instance;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    public HttpFileServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 255 * 255)
                .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .childHandler(new HttpFileServerInitialzer());
        // Netty的资源泄露探测器
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
    }

    public void start() throws InterruptedException {
        this.future = server.bind(8088).sync();
        this.future.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> channelFuture.channel().close());
        System.out.println("netty websocket server 启动完毕, port:8088");
    }
}
