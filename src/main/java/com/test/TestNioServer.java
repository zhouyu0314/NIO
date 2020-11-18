package com.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class TestNioServer {
    public static void main(String[] args) throws Exception {
        //获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        //创建缓冲区
        ByteBuffer bBuf = ByteBuffer.allocate(1024);
        //改为非阻塞
        ssChannel.configureBlocking(false);
        //绑定
        ssChannel.bind(new InetSocketAddress(9898));


        //获取选择器
        Selector selector = Selector.open();
        //将通道注册到选择器
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);
        //只要选择器上注册的通道数量>0
        while (selector.select() > 0) {
            //获取所有的注册的选择键
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //对注册的选择键进行循环
            while (iterator.hasNext()) {
                SelectionKey sk = iterator.next();
                //判断选择键的类型
                if (sk.isAcceptable()) {
                    //如果是接收就绪，则接收Channel并注册
                    SocketChannel sChannel = ssChannel.accept();
                    sChannel.configureBlocking(false);
                    //客户端有数据我们才能读取，所以监听的是读模式
                    sChannel.register(selector,SelectionKey.OP_READ);
                    //当注册完之后，因为在删除了本选择键之后selector.select()还是>0，所有还是会进入循环
                }else if(sk.isReadable()){
                    System.out.println("isReadable");
                    //判断是否是读就是模式
                    FileChannel fChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                    //获取就读就绪的通道
                    SocketChannel sChannel = (SocketChannel)sk.channel();
                    sChannel.configureBlocking(false);
                    //循环读取里面的数据
                    while (sChannel.read(bBuf) != -1) {
                        System.out.println("read");
                        bBuf.flip();
                        fChannel.write(bBuf);
                        bBuf.clear();
                    }

                    //此if只会进来一次，然后通道建立成功之后就会持续读取数据直到客户端数据发送完然后需要将通道关闭
                    //要不然此通道会一直在select上注册
                    sChannel.close();
                    fChannel.close();
                }

                iterator.remove();
            }


        }

    }
}
