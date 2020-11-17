package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 一、使用NIO完成网络通信的三个核心：
 * 1.通道（Channel）：负责连接
 *
 *  java.nio.channels.Channel 接口：
 *      |--SelectableChannel
 *          |--SocketChannel
 *          |--ServerSocketChannel
 *          |--DatagramChannel
 *
 *          |--Pipe.SinkChannel
 *          |--Pipe.SourceChannel
 *
 * 2.缓冲区（Buffer）：负责数据的存取
 *
 * 3.选择器（Selector）：是SelectableChannel的多路复用器。用于监控SelectableChannel的IO状况
 */
public class BlockingChannel {
    public static void main(String[] args)throws Exception {
        new Thread(){
            @Override
            public void run() {
                try {
                    server();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


        new Thread(){
            @Override
            public void run() {
                try {
                    System.out.println("睡眠3S");
                    Thread.sleep(3000);
                    System.out.println("醒来");
                    client();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    //客户端
    private static void client() throws IOException {
        SocketChannel sChannel = null;
        FileChannel fInChannel = null;
        try {
            //1.获取通道
            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

            //获取本地Channel用来读取本地的文件到缓冲区
            fInChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);

            //2.分配指定大小的缓冲区
            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            //3.读取本地文件，发送到服务端
            //fInChannel将读取到的数据写入bBuf
            while (fInChannel.read(bBuf) !=-1){
                //因为bBuf是写模式所以要转换模式
                bBuf.flip();
                //sChannel从bBuf里读取数据
                sChannel.write(bBuf);
                //每一次读都会将bBuf装满，所以在每次循环的结束要clear以便下次读
                bBuf.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            sChannel.close();
            fInChannel.close();
        }
    }

    //服务端
    private static void server() throws IOException {
        ServerSocketChannel ssChannel = null;
        SocketChannel sChannel = null;
        FileChannel fChannel = null;

        try {
            //1.获取通道
            ssChannel = ServerSocketChannel.open();
            //获取一个本地文件的Channel，用来写入磁盘文件，此Channel的属性有：目标点是2.jpg、写模式、创建模式（没有就创建有就覆盖）
            fChannel = FileChannel.open(Paths.get("2.jpg"),StandardOpenOption.WRITE,StandardOpenOption.CREATE);

            //2.绑定连接
            ssChannel.bind(new InetSocketAddress(9898));

            //3.获取客户端连接的通道
            sChannel = ssChannel.accept();

            //4.分配缓冲区用来存储
            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            //5.接受客户端的数据，并保存到本地
            //拿到客户端的Channel之后读取内部内容并写入bBuf
            while (sChannel.read(bBuf) != -1) {
                bBuf.flip();
                //fChannel将缓冲区内的数据写入到目标点（2.jpg）
                fChannel.write(bBuf);
                bBuf.clear();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            ssChannel.close();
            sChannel.close();
            fChannel.close();
        }

    }
}
