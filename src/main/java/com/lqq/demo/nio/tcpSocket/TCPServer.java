package com.lqq.demo.nio.tcpSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        //通过ServerSocketChannel 的open()方法创建一个ServerSocketChannel对象
        ServerSocketChannel ssc= ServerSocketChannel.open();

        //1. 通过ServerSocketChannel 绑定ip地址和端口号
        ssc.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(),8888));

        //2. 通过ServerSocketChannel的accept()方法创建一个SocketChannel对象用户从客户端读/写数据
        SocketChannel sc=ssc.accept(); // TODO 这里会挂起等待客户端哦

        //3. 创建读数据/写数据缓冲区对象来读取客户端数据或向客户端发送数据
        //读取客户端发送的数据
        ByteBuffer buffer= ByteBuffer.allocate(1024);
        //从通道中读取数据到缓冲区
        sc.read(buffer);
        StringBuffer sb=new StringBuffer();
        buffer.flip();
        while(buffer.hasRemaining()){
            sb.append((char)buffer.get());
        }
        System.out.println(sb.toString());

        ByteBuffer buffer2=ByteBuffer.allocate(1024);
        //向客户端发送数据
        buffer2.put("data has been received.".getBytes());
        buffer2.flip();
        sc.write(buffer2);

        //4. 关闭SocketChannel和ServerSocketChannel
        sc.close();
        ssc.close();
    }
}