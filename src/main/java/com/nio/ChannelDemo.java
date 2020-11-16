package com.nio;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 * 一、通道（Channel）：用于源节点与目标节点的连接。在Java NIO中负责缓冲区中数据的传输。Channel本身不存储数据，因此需要配合缓冲区进行传输。
 * <p>
 * 二、通道的主要实现类： java.nio.Channel 接口
 * --FileChannel ---本地文件
 * --SocketChannel ----tcp
 * --ServerSocketChannel ----tcp
 * --DatagramChannel ----udp
 * <p>
 * 三、获取通道
 * 1.Java 针对支持通道的类提供了 getChannel()方法
 * 本地IO：
 * FileInputStream/FileOutputStream
 * RandomAccessFile
 * <p>
 * 网络IO：
 * Socket
 * ServerSocket
 * DatagramSocket
 * 2.在JDK 1.7中的NIO.2针对各个通道提供了静态方法open()
 * 3.在JDK 1.7中的NIO.2的Files工具类的newByteChannel()
 *
 * 四、通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * 五、分散（Scatter）与聚集（Gather）
 * 分散读取（Scatter Reads）:将通道中的数据分散到多个缓冲区中，依次顺序分散到缓冲区
 * 聚集写入（Gathering Writes）:将多个缓冲区中的数据聚集到通道中
 *
 *
 * 六、字符集：Charset
 * 编码：字符串 -> 字节数组
 * 解码：字节数组 -> 字符串
 *
 *
 */
public class ChannelDemo {
    public static void main(String[] args) throws Exception {
        //test01();
        //test02();
        //test03();
        //test04();
        //test05();
        test06();





    }


    /**
     * 利用通道完成文件的复制(非直接缓冲区)
     */
    private static void test01() {
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
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    /**
     * 使用直接缓冲区完成文件的复制（内存映射文件安）
     */
    private static void test02() throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            //使用open创建一个通道，此通道的信息有路径（目标点）是根路径下的1.jpg、只支持读模式
            inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
            //CREATE_NEW没有这个文件（2.jpg）就创建，有就报错
            /*
            用open创建一个通道，此通道的信息有路径（目标点）是根路径下的2.jpg、支持读和写模式（因为下面创建的MappedByteBuffer是READ_WRITE，要与之对应）、
            支持如果没有目标源的文件则创建，如果有则报错
             */
            outChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE_NEW);

            //内存映射文件道理和AllocateDirect一样，文件在物理内存中
            //开辟一块内存，用于读取数据，大小是inChannel.size()
            MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            //开辟一块内存，用于读写数据，大小是inChannel.size()
            MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            //直接对缓冲区进行数据的读写操作
            byte[] dst = new byte[outMappedBuf.limit()];
            //将数据读取到dst
            inMappedBuf.get(dst);
            //从dst中取数据写入到目标点
            outMappedBuf.put(dst);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inChannel.close();
            outChannel.close();
        }


    }

    /**
     * 通道之间的数据传输(用的也是直接缓冲区的方式)
     */
    private static void test03() throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
            outChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

            /*
            to 和 from一样，用哪个都行
             */
            //inChannel 传输到 outChannel 从inChannel的第0个位置总共传输inChannel.size()个字节
            inChannel.transferTo(0,inChannel.size(),outChannel);
            //outChannel 收到来自 inChannel的数据 源头是inChannel 从inChannel的第0个位置总共传输inChannel.size()个字节
            outChannel.transferFrom(inChannel,0,inChannel.size());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inChannel.close();
            outChannel.close();
        }
    }

    /**
     * 分散读取、聚集写入
     * @throws Exception
     */
    private static void test04()throws Exception{
        FileChannel channel = null;
        FileChannel channel1 = null;
        try {
            RandomAccessFile raf = new RandomAccessFile("a.txt","rw");
            //1.获取通道
            channel = raf.getChannel();
            //2.分配指定大小的缓冲区
            ByteBuffer allocate1 = ByteBuffer.allocate(1024);
            ByteBuffer allocate2 = ByteBuffer.allocate(2048);

            //3.分散读取
            ByteBuffer[] byteBuffers = {allocate1,allocate2};
            channel.read(byteBuffers);

            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.flip();
            }
            System.out.println(new String(byteBuffers[0].array(), 0, byteBuffers[0].limit()));
            System.out.println("********************************************");
            System.out.println(new String(byteBuffers[1].array(), 0, byteBuffers[1].limit()));

            //4.聚集写入
            RandomAccessFile raf2 = new RandomAccessFile("b.txt","rw");
            channel1 = raf2.getChannel();
            channel1.write(byteBuffers);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            channel.close();
            channel1.close();
        }

    }

    /**
     * 字符集---显示一共支持多少种字符集
     */
    private static void test05(){
        Map<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entries = map.entrySet();
        for (Map.Entry<String, Charset> entry : entries) {
            System.out.println(entry.getKey() + "\t"+entry.getValue());
        }
    }

    /**
     * 字符集
     */
    private static void test06()throws Exception{
        Charset gbk = Charset.forName("GBK");


        //获取编码器
        CharsetEncoder encoder = gbk.newEncoder();
        //获取解码器
        CharsetDecoder decoder = gbk.newDecoder();

        CharBuffer allocate = CharBuffer.allocate(1024);
        allocate.put("测试字符集");
        System.out.println("allocate"+"\t"+allocate.position()+"\t"+allocate.limit()+"\t"+allocate.capacity());
        allocate.flip();
        System.out.println("allocate->flip()"+"\t"+allocate.position()+"\t"+allocate.limit()+"\t"+allocate.capacity());
        //编码  将allocate中的字符读取出来，通过GBK编码的格式写入bBuf,此时的bBuf处于写模式
        ByteBuffer bBuf = encoder.encode(allocate);
        System.out.println("allocate->flip()->编码"+"\t"+allocate.position()+"\t"+allocate.limit()+"\t"+allocate.capacity());
        System.out.println("bBuf"+"\t"+bBuf.position()+"\t"+bBuf.limit()+"\t"+bBuf.capacity());


        //解码 将bBuf中的字符读取出来，通过GBK解码的格式写入cBuf,此时的cBuf处于写模式
        CharBuffer cBuf = decoder.decode(bBuf);
        System.out.println("使用GBK解码"+"\t"+cBuf.toString());
        System.out.println("使用GBK解码cBuf"+"\t"+cBuf.position()+"\t"+cBuf.limit()+"\t"+cBuf.capacity());
        System.out.println("使用GBK解码bBuf"+"\t"+bBuf.position()+"\t"+bBuf.limit()+"\t"+bBuf.capacity());

        //使用utf-8解码
        Charset utf8 = Charset.forName("UTF-8");
        //因为bBuf处于写模式，要将其转换成读模式才能将其取出通过utf-8编码转换成utf8Buf
        bBuf.flip();
        System.out.println("使用UTF8->flip()"+"\t"+bBuf.position()+"\t"+bBuf.limit()+"\t"+bBuf.capacity());
        CharBuffer utf8Buf = utf8.decode(bBuf);
        System.out.println("使用UTF8解码"+"\t"+utf8Buf.toString());
        System.out.println("使用UTF8解码"+"\t"+bBuf.position()+"\t"+bBuf.limit()+"\t"+bBuf.capacity());


    }


}
