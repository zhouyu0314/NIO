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
 *
 */
public class BlockingChannel2 {
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
                    Thread.sleep(3000);
                    client();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private static void client() throws Exception{
        SocketChannel sChannel = null;
        FileChannel fChannel = null;
        try {
            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));

            fChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);

            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            while (fChannel.read(bBuf)!=-1) {
                bBuf.flip();
                sChannel.write(bBuf);
                bBuf.clear();
            }
            System.out.println("发送完毕，等待接受客户端消息！");
            sChannel.shutdownOutput();
            //接受服务端发送来的消息
            int len = 0;
            while ((len = sChannel.read(bBuf) )!= -1) {
                System.out.println(new String(bBuf.array(), 0, len));
                bBuf.clear();
            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sChannel.close();
            fChannel.close();
        }
    }


    private static void server()throws Exception{
        ServerSocketChannel ssChannel = null;
        SocketChannel sChannel = null;
        FileChannel fChannel = null;
        try {
            ssChannel = ServerSocketChannel.open();
            ssChannel.bind(new InetSocketAddress(9898));
            sChannel = ssChannel.accept();
            fChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer bBuf = ByteBuffer.allocate(1024);

            //这里会一直读，因为只要客户端Channel不关闭，就永远不会是-1
            //所以要shutdown
            while (sChannel.read(bBuf) != -1) {
                bBuf.flip();
                fChannel.write(bBuf);
                bBuf.clear();
            }
            System.out.println("通知客户端");
            //通知客户端
            bBuf.put("客户端接受成功！".getBytes());
            bBuf.flip();
            sChannel.write(bBuf);
            sChannel.shutdownOutput();



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ssChannel.close();
            sChannel.close();
            fChannel.close();
        }
    }
}
