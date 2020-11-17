package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Date;
import java.util.Iterator;

/**
 * UDP
 */
public class NonBlockingChannel2 {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static void send() throws Exception {
        DatagramChannel dChannel = null;
        try {
            dChannel = DatagramChannel.open();

            dChannel.configureBlocking(false);

            ByteBuffer bBuf = ByteBuffer.allocate(1024);


            bBuf.put((new Date().toString() + "\n" + "你好").getBytes());
            bBuf.flip();
            dChannel.send(bBuf, new InetSocketAddress("127.0.0.1", 9898));
            bBuf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dChannel.close();
        }

    }


    private static void receive() throws IOException {

        DatagramChannel dChannel = DatagramChannel.open();

        dChannel.configureBlocking(false);

        dChannel.bind(new InetSocketAddress(9898));

        Selector selector = Selector.open();

        dChannel.register(selector, SelectionKey.OP_READ);

        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey sk = iterator.next();
                if (sk.isReadable()) {
                    ByteBuffer bBuf = ByteBuffer.allocate(1024);
                    dChannel.receive(bBuf);

                    bBuf.flip();

                    System.out.println(new String(bBuf.array(), 0, bBuf.limit()));

                    bBuf.clear();

                }
                iterator.remove();
            }
        }

    }
}
