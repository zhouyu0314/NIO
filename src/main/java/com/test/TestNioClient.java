package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestNioClient {
    public static void main(String[] args)throws Exception {
        SocketChannel sChannel = null;
        FileChannel fChannel = null;
        try {
            //获取Socket通道
            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
            //改为非阻塞
            sChannel.configureBlocking(false);
            //获取本地io通道
            fChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);

            //创建缓冲区
            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            //通过本地IO Channel往缓冲区写入数据
            while (fChannel.read(bBuf) != -1) {
                bBuf.flip();
                //从缓冲区中读取数据给通道
                sChannel.write(bBuf);
                bBuf.clear();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sChannel.close();
            fChannel.close();
        }
    }

}
