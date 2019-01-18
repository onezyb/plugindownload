package com.ixinnuo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;


public class SocketAdapt implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketAdapt.class);
    private Socket socket;
    private ByteBuffer byteBuffer;
    private Long size;

    public SocketAdapt(Socket skt, ByteBuffer file, Long size) {
        this.socket = skt;
        this.byteBuffer = file;
        this.size = size;
    }


    @Override
    public void run() {
        logger.info("建立了一次连接，服务端开始输出下载流");
        ////stream对象
        OutputStream os = null;
        try {
            try {

                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                if (socket != null) {
                    os.close();
                    socket.close();
                }

                logger.error("Because the ERROR " + e.getMessage()
                        + " so close this socket");
            }

            DataOutputStream dos = new DataOutputStream(os);

            senderThd shd = new senderThd(os, dos);
            new Thread(shd).start();
            //关闭
            receiverThd rhd = new receiverThd(os, dos);
            new Thread(rhd).start();
        } catch (Exception e) {
            e.fillInStackTrace();
            logger.error("一个通信实例的主线程，fillInStackTrace,系统错误或未知错误***********，{}", e.getMessage());
        }
    }

    class senderThd implements Runnable {
        private OutputStream os;
        private DataOutputStream dos;

        public senderThd(OutputStream os, DataOutputStream dos) {
            this.dos = dos;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                try {
                    dos.writeInt(size.intValue());
                    os.write(byteBuffer.array(), 0, size.intValue());
                    dos.flush();
                    os.flush();
                    logger.info("下载给Ip，{}", socket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (socket != null) {
                        dos.close();
                        os.close();
                        socket.close();
                    }

                    logger.error("Because the ERROR " + e.getMessage()
                            + " so close this socket");
                }
            } catch (Exception e) {
                e.fillInStackTrace();
                logger.error("发消息的线程，fillInStackTrace,系统错误或未知错误***********,{}", e.getMessage());
            }
        }
    }

    class receiverThd implements Runnable {
        private OutputStream os;
        private DataOutputStream dos;

        public receiverThd(OutputStream os, DataOutputStream dos) {
            this.dos = dos;
            this.os = os;

        }

        @Override
        public void run() {
            InputStream is = null;
            long startTime = System.currentTimeMillis();
            try {
                while (true) {//监听消息
                    try {
                        is = socket.getInputStream();
                        DataInputStream dis = new DataInputStream(is);
                        String msg = dis.readUTF();
                        if (msg.equals("00")) {//正常关闭
                            logger.info("客户端消息00，关闭连接 ..");
                            break;
                        }
                        if ((System.currentTimeMillis() - startTime) >= 120000) {//超时异常关闭//2*60*1000 ms
                            logger.info("2分钟没收到00，关闭连接 ..");
                            break;
                        }

                    } catch (IOException e) {
                        //写关闭
                        dos.close();
                        os.close();
                        //读关闭
                        is.close();
                        socket.close();
                        logger.error("communication exception is: " + e.getMessage());
                        //各种异常的判断
//                        //如果超时两分钟就关闭socket,从而释放线程池，让其他客户端能够进来
//                        if (e.getMessage().equals("Read timed out")) {
//                            //如果是这种超时异常就能立即关闭socket，并不再监听安装成功消息
//                        } else if (e.getMessage().equals("Connection reset")) {//客户端异常关闭或连接异常中断等
//                        } else {//异步的??
//                            if (e.getMessage().equals("Software caused connection abort: recv failed")) {
//                            } else {
//                                logger.error(e.getMessage());
//                                e.printStackTrace();
//                            }
//                        }
                    }
                }
                //写关闭
                dos.close();
                os.close();
                //读关闭
                is.close();
                socket.close();
            } catch (Exception e) {
                e.fillInStackTrace();
                logger.error("系统异常(包括通信异常) " + e.getMessage());
            }

        }
    }
}
