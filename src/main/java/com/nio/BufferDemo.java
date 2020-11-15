package com.nio;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 一、缓冲区（Buffer）:在Java NIO中负责数据的村区。缓冲区就是数组。用于存储不同数据类型的数据
 *
 * 根据数据类型的不同（boolean除外），提供了相应类型的缓冲区：
 * ByteBuffer
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 * 上述缓冲区的管理方式几乎一致，通过allocate()获取缓冲区
 *
 *
 * 二、缓冲区存储数据的两个核心方法：
 * put()：存入数据到缓冲区中
 * get()：获取缓冲区中的数据
 *
 *
 * 三、缓冲区中的四个核心属性：
 * capacity：容量，表示缓冲区中最大存储数据的容量。一旦声明不能改变。
 * limit：表示缓冲区中的可以操作数据的大小。（limit后面的数据不能进行读写）
 * position：位置，表示缓冲区中正在操作数据的位置。
 * mark：标记，表示记录当前position的位置。可以通过reset()恢复到mark的位置
 * 0<= mark <= position <= limit <= capacity
 * hasRemaining()判断缓冲区是否还有剩余的数据
 * remaining()返回缓冲区剩余的可操作的数据个数
 *
 *
 * 四、直接缓冲区与非直接缓冲区
 * 非直接缓冲区：通过allocate()分配的缓冲区，将缓冲区建立在JVM的内存中
 * 直接缓冲区：通过allocateDirect()分配直接缓冲区，将缓冲区建立在操作系统的物理内存中可以提高效率
 *
 *
 *
 */
public class BufferDemo {
    public static void main(String[] args) throws Exception{
        //test01();
        //test02();
        test03();
    }

    private static void test01() throws UnsupportedEncodingException {
        String str= "你好";
        //分配一个指定大小的缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        System.out.println("*****************allocate()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());

        //利用put()存入数据到缓冲区
        byteBuffer.put(str.getBytes("utf-8"));
        System.out.println("*****************put()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());

        //切换到读取数据的模式

        byteBuffer.flip();
        System.out.println("*****************flip()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());

        //利用get()读取缓冲区中的数据
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        System.out.println(new String(bytes, 0, bytes.length));
        System.out.println("*****************get()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());

        //rewind()：可重复读数据
        byteBuffer.rewind();
        System.out.println("*****************rewind()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());

        //clear()清空缓冲区，并恢复到写模式，但是缓冲区中的数据依然存在，数据处于“被遗忘状态”
        byteBuffer.clear();
        System.out.println("*****************clear()*******************");
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.limit());
        System.out.println(byteBuffer.capacity());
        System.out.println("*****************被遗忘状态*******************");
        byte[] bytes1 = new byte[6];
        byteBuffer.get(bytes1);
        System.out.println(new String(bytes1,"utf-8"));

    }

    private static void test02(){
        String str = "abcd";
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(str.getBytes());
        byteBuffer.flip();
        byte[] b1 = new byte[byteBuffer.limit()];
        byteBuffer.get(b1,0,2);
        System.out.println(new String(b1,0,2));
        System.out.println(byteBuffer.position());
        //mark 标记
        System.out.println("**********mark************");
        byteBuffer.mark();

        byteBuffer.get(b1,2,2);
        System.out.println(new String(b1,2,2));
        System.out.println(byteBuffer.position());
        //reset()：恢复到mark位置
        byteBuffer.reset();
        System.out.println(byteBuffer.position());

        if (byteBuffer.hasRemaining()) {
            System.out.println("**********查看缓冲区还有多个字节数据************");
            System.out.println(byteBuffer.remaining());
        }
    }

    private static void test03(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println("查看当前缓冲区是否是直接缓冲区");
        System.out.println(byteBuffer.isDirect());
    }



}
