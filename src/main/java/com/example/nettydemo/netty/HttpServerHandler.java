package com.example.nettydemo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private ImageEventHandler imageEventHandler;
    public void registerImageHandler(ImageEventHandler handler) {
        imageEventHandler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest req) throws Exception {
        System.out.println(Thread.currentThread().getId());
        try {
            // 处理文件上传
            if (HttpMethod.POST.name().equalsIgnoreCase(req.method().name()) && "/pic/upload".equals(req.uri())) {
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(true), req);
                List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();
                for (InterfaceHttpData bodyHttpData : bodyHttpDatas) {
                    InterfaceHttpData.HttpDataType httpDataType = bodyHttpData.getHttpDataType();
                    if (InterfaceHttpData.HttpDataType.Attribute.equals(httpDataType)) {
                        Attribute attribute = (Attribute) bodyHttpData;
                        String tokenName = attribute.getName();
                        if (tokenName.equals("token")) {
                            String token = attribute.getValue();
                            System.out.println("token="+token);
                        }
                    } else if (InterfaceHttpData.HttpDataType.FileUpload.equals(httpDataType)) {
                        final FileUpload fileUpload = (FileUpload) bodyHttpData;
                        File tempFile = fileUpload.getFile();
                        String filePath = "/media/abc/" + fileUpload.getFilename();
                        File file = new File(filePath);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        try (FileChannel inputChannel = new FileInputStream(tempFile).getChannel(); FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
                            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                decoder.destroy();
            }

        } finally {
            // 定义发送的数据消息
            ByteBuf content = Unpooled.copiedBuffer("ok", CharsetUtil.UTF_8);
            // 构建一个http response
            FullHttpResponse response =
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            content);
            // 为响应增加数据类型和长度
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            // 把响应刷到客户端
            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("channelRegistered");
        imageEventHandler.handle("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("channelUnregistered");
        imageEventHandler.handle("channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("channelActive");
        imageEventHandler.handle("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("channelInactive");
        imageEventHandler.handle("channelInactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
