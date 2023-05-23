package com.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassName NettyServer
 * @Description:
 * @Author 刘苏义
 * @Date 2023/5/23 21:18
 * @Version 1.0
 */
@Component

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    // 服务端NIO线程组
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    public ChannelFuture start(String host, int port) {
        ChannelFuture channelFuture = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(bossGroup, workGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            // 自定义服务处理
//                            socketChannel.pipeline().addLast(new ServerHandler());
//                        }
//                    });
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // LineBasedFrameDecoder能够将接收到的数据在行尾进行拆分。
                            // 设置解码帧的最大长度，如果帧的长度超过此值抛出异常
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            // 服务处理
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });
            // 绑定端口并同步等待
            channelFuture = bootstrap.bind(host, port).sync();
            logger.info("======Start Up Success!=========");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelFuture;
    }

    public void close() {
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        logger.info("======Shutdown Netty Server Success!=========");
    }
}