package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;

/**
 * 一、使用NIO完成网络通信的三个核心：
 * 1.通道（Channel）：负责连接
 * <p>
 * java.nio.channels.Channel 接口：
 * |--SelectableChannel
 * |--SocketChannel
 * |--ServerSocketChannel
 * |--DatagramChannel
 * <p>
 * |--Pipe.SinkChannel
 * |--Pipe.SourceChannel
 * <p>
 * 2.缓冲区（Buffer）：负责数据的存取
 * <p>
 * 3.选择器（Selector）：是SelectableChannel的多路复用器。用于监控SelectableChannel的IO状况
 */
public class NonBlockingChannel {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                server();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                client();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private static void client() throws Exception {
        SocketChannel sChannel = null;
        try {
            //1.获取通道
            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

            //2.切换成非阻塞模式
            sChannel.configureBlocking(false);

            //3.分配一个指定大小的缓冲区
            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            //4.发送数据给服务端
            bBuf.put((new Date().toString() + "\n" + "你好").getBytes());

            bBuf.flip();

            sChannel.write(bBuf);
            bBuf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sChannel.close();
        }

    }

    private static void server() throws Exception {
        //1.获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        //2.切换成非阻塞模式
        ssChannel.configureBlocking(false);
        //3.绑定连接
        ssChannel.bind(new InetSocketAddress(9898));
        //4.获取选择器
        Selector selector = Selector.open();
        //5.将通道（ssChannel）注册到选择器（selector）上,并指定此选择器监听此通道的接收事件
        //此步可以理解为只要ssChannel被连接才将此通道注册到选择器
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6.轮询式的获取选择其上已经“准备就绪”的事件
        //如果>0则选择器上最少有1个通道已经就绪
        //注意此处监听的是接收事件，在没有客户端连接到server时selector.select()的结果是0
        while (selector.select() > 0) {
            //7.获取当前选择其中所有注册的“选择键（已就绪的监听事件）”
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                //8.获取准备“就绪”的事件
                SelectionKey sk = iterator.next();
                //9.判断具体是什么事件
                if (sk.isAcceptable()) {
                    //10.若“接收就绪”，获取客户端连接
                    SocketChannel sChannel = ssChannel.accept();
                    //11.切换成非阻塞
                    sChannel.configureBlocking(false);
                    //12.将接受的客户端通道注册到选择器,接受客户端通道的目的就是为了读，所以监听“读就绪”
                    sChannel.register(selector, SelectionKey.OP_READ);

                } else if (sk.isReadable()) {
                    System.out.println("isReadable");
                    //13.获取当前选择器上“读就绪状态的”通道
                    SocketChannel sChannel = (SocketChannel) sk.channel();
                    //14.读取数据
                    ByteBuffer bBuf = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = sChannel.read(bBuf)) > 0) {
                        System.out.println(new String(bBuf.array(), 0, len));
                        bBuf.clear();
                    }

                    //这个通道和上面的接收就绪的通道是一个，关闭一次就够了
                    sChannel.close();

                }
                //15.取消选择键
                iterator.remove();

            }
        }

    }

}
