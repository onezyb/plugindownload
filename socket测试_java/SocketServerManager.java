package com.ixinnuo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.concurrent.*;

/**
 * 创建socketconnect的对象,该处是socket连接池及缓冲池
 *
 * @author sunny.sun
 * @version 1.0
 */
public class SocketServerManager extends Thread {
    //    private static final Logger logger = LoggerFactory
//            .getLogger(SocketServerManager.class);
    private static final String filePath = "E:\\PROJECTSS\\file\\PluginSetup.exe";
    //linux
//    private static final String filePath = "/usr/local/socketst/PluginSetup.exe";
    /**
     * 端口号
     */
    //配置0100
    private int port;
    /**
     * 最大连接数
     */
    private int max_conn;
    /**
     * 缓冲区大小
     */
    private int max_cache;
    /**
     * socket 超时时间
     */
    private int timeout;
    /**
     * server对象
     */
    private ServerSocket server;
    /**
     *
     */
    /**
     * 线程池对象
     */
    private ExecutorService exService;


    /**
     * 该构造方法提供设置最大连接数,最大缓存数,超时时间,此处把socket服务端作为一个单独进程;
     * 而把socket连接进程放置到线程池和缓存池,以提高性能
     *
     * @param port      端口号
     * @param max_conn  最大连接池
     * @param max_cache 最大缓存池
     * @param timeout   超时时间
     */
    public SocketServerManager(int port, int max_conn, int max_cache,
                               int timeout) {
        this.port = port;
        this.max_conn = max_conn;
        this.timeout = timeout <= 0 ? 0 : timeout;
        this.max_cache = max_cache;
// 将socket连接放到线程池中,如果设置最大连接数为0时,使用自动分配线程
        if (max_conn <= 0) {
            exService = Executors.newCachedThreadPool();
        } else {
// 如果没有设置缓存值,则随系统资源调配
            if (max_cache <= 0) {
                exService = new ThreadPoolExecutor(0, max_conn, 60000L,
                        TimeUnit.MILLISECONDS, new SynchronousQueue());// 线程池
                return;
            }
// 队列池,将无法进入线程池的内容存放socketadapte队列池中
            else {
                ArrayBlockingQueue socketadaptes = new ArrayBlockingQueue(
                        max_cache);
                exService = new ThreadPoolExecutor(0, max_conn, 60000L,
                        TimeUnit.MILLISECONDS, socketadaptes);// 线程池
            }
        }
// 启动
        this.start();
    }

    @Override
    public void run() {

        File file = new File(filePath);
        FileChannel fc = null;
        FileInputStream fis = null;
        Long size = 0L;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        fc = fis.getChannel();
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = ByteBuffer.allocate((int) fc.size());
            while ((fc.read(byteBuffer)) > 0) {//读
            }
            size = fc.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        label:
        while (true) {
            try {
                Socket socket = null;
                try {
                    socket = server.accept();
                    System.out.println("the socket [" + socket.getInetAddress() + ":"
                            + socket.getPort() + "] has been connection");
                    socket.setKeepAlive(true);
                    if (timeout != 0) {
                        socket.setSoTimeout(timeout);
                    }
                    try {
                        // 将此处的socket监听放置到进程池//socket适配器（需要实现Runnable）
//                        exService.execute(new SocketAdapt(socket));
                        System.out.println("the socket [" + socket.getInetAddress()
                                + ":" + socket.getPort()
                                + "] has been put in ExecutorService");

                        ////stream对象
                        OutputStream os = socket.getOutputStream();
                        DataOutputStream dos = null;
                        dos = new DataOutputStream(os);
                        dos.writeInt(size.intValue());
                        //发文件
                        os.write(byteBuffer.array(), 0, size.intValue());
                        //线程监听消息00//OutputStream os
//                        mythread mtd = new mythread(socket, os, dos);

                    } catch (RejectedExecutionException e) {
// TODO: handle exception
                        e.fillInStackTrace();
                        throw new Exception(
                                "becaue the"
                                        + e.getMessage()
                                        + "the thread is full and the thread cache is full");
                    }
                } catch (SocketException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                    if (socket != null)
                        socket.close();
                    throw new Exception("because the error " + e.getMessage()
                            + "so close this socket");
                } catch (IOException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                    if (socket != null)
                        socket.close();
                    throw new Exception("because the error " + e.getMessage()
                            + "so close this socket");
                }
            } catch (Exception e) {
                e.fillInStackTrace();
// TODO: handle exception
                System.out.println(e.getMessage());
// 一旦发生异常,还继续进行
                continue label;
            }
        }

    }

    //jian听客户端消息00
    class mythread extends Thread {
        private Socket socket;
        private OutputStream os;
        private DataOutputStream dos;

        public mythread(Socket sktt, OutputStream oss, DataOutputStream doss) {
            this.socket = sktt;
            this.dos = doss;
            this.os = oss;
        }

        @Override
        public void run() {
            //客户端消息-----
//                        int k = 0;
            boolean brekFlg = false;
            while (true) {
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String infos = null;
                try {
                    while ((infos = br.readLine()) != null) {
                        if (infos.equals("00")) {//客户端下载完毕,流读完毕，关闭stream和socket
                            System.out.println("客户端消息：" + infos);
                            System.out.println("the socket [" + socket.getInetAddress() + ":"
                                    + socket.getPort() + "] has installed");
                            dos.close();
                            os.close();
                            socket.close();
                            brekFlg = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (brekFlg) break;
            }

        }


    }

    /***
     *
     * @param args
     */
    public static void main(String[] args) {
        //3000最大连接，*2表示一次建立连接和一次通信；；
//        连接后我就会推消息给客户端，两分钟没不read我的消息（认为客户端故障？），强制关闭socket
//          //同时保持（max_conn + max_cache）个连接
//        new SocketServerManager(5566, 3000 * 2, 100, 2 * 60 * 1000);
        new SocketServerManager(5566, 2000 * 2, 100, 2 * 1000);
    }
}
