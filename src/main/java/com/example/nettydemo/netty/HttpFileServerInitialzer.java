package com.example.nettydemo.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServerInitialzer extends ChannelInitializer<SocketChannel> {
    private ImageEventHandler imageEventHandler;
    public void registerImageHandler(ImageEventHandler handler) {
        imageEventHandler = handler;
    }
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // 有http编解码器
        pipeline.addLast(new HttpRequestDecoder());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
        // 几乎在netty中的编程，都会使用到此hanler
        pipeline.addLast(new HttpObjectAggregator(10240*10240));
        pipeline.addLast(new HttpResponseEncoder());
        HttpServerHandler httpServerHandler = new HttpServerHandler();
        httpServerHandler.registerImageHandler(imageEventHandler);
        pipeline.addLast(httpServerHandler);
    }
}
