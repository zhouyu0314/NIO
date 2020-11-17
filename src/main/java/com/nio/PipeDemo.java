package com.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeDemo {
    public static void main(String[] args) throws Exception {
        test01();
    }

    private static void test01() throws IOException {
        Pipe.SinkChannel sinkChannel = null;
        Pipe.SourceChannel sourceChannel = null;
        try {
            //1.获取管道
            Pipe pipe = Pipe.open();
            //2.将缓冲区中的数据写入管道
            ByteBuffer bBuf = ByteBuffer.allocate(1024);
            sinkChannel = pipe.sink();
            bBuf.put("通过单向管道发送数据".getBytes());
            bBuf.flip();
            sinkChannel.write(bBuf);


            //3.读取缓冲区数据
            sourceChannel = pipe.source();
            bBuf.flip();
            int len = sourceChannel.read(bBuf);
            System.out.println(new String(bBuf.array(), 0, len));


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sinkChannel.close();
            sourceChannel.close();

        }

    }
}
