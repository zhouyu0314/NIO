package com.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 一、通道（Channel）：用于源节点与目标节点的连接。在Java NIO中负责缓冲区中数据的传输。Channel本身不存储数据，因此需要配合缓冲区进行传输。
 *
 * 二、通道的主要实现类： java.nio.Channel 接口
 *      --FileChannel ---本地文件
 *      --SocketChannel ----tcp
 *      --ServerSocketChannel ----tcp
 *      --DatagramChannel ----udp
 *
 * 三、获取通道
 * 1.Java 针对支持通道的类提供了 getChannel()方法
 *      本地IO：
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile
 *
 *      网络IO：
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 * 2.在JDK 1.7中的NIO.2针对各个通道提供了静态方法open()
 * 3.在JDK 1.7中的NIO.2的Files工具类的newByteChannel()
 */
public class ChannelDemo {
    public static void main(String[] args) {
        test01();
    }

    /**
     * 利用通道完成文件的复制(非直接缓冲区)
     */
    private static void test01()  {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fis = new FileInputStream("1.jpg");
            fos = new FileOutputStream("2.jpg");

            //1.获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //2.分配缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //3.将通道中的数据存入缓冲区
            while (inChannel.read(buffer) != -1) {
                buffer.flip();//边读边写
                outChannel.write(buffer);
                buffer.clear();//清空缓冲区并切换到读模式
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (outChannel!=null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inChannel!=null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }
}
