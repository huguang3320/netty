package cn.itcast.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 学习配置全队列容量，一个tcp的客户端和服务端建立连接，就会先将数据存放到全队列中
 * 全队列的大小可配置，本案例就配置了最大容量为2，当有第三个客户端连接时，就会拒绝连接。
 *
 * ctrl + alt + B 可以查看某个类的实现类
 *
 * windows 默认分配200空间
 * linux 默认分配128空间
 */


public class 测试backlog服务端 {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 2)    //配置全队列的最大容量
                .childHandler(new ChannelInitializer<NioSocketChannel>(){

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                    }
                }).bind(9999);
    }
}
