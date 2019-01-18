package com.ixinnuo.config;

import com.ixinnuo.sendreceive.server.ReceiveThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.*;

public class SocketServerTest extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SocketServerTest.class);

//        private static final String filePath = "E:\\PROJECTSS\\file\\PluginSetup.exe";
    private static final String filePath = "/usr/local/socketst/PluginSetup.exe";
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
    public SocketServerTest(int port, int max_conn, int max_cache,
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
            Socket socket = null;
            try {
                try {
                    socket = server.accept();
                    logger.info("the socket [" + socket.getInetAddress() + ":"
                            + socket.getPort() + "] has been connection");
                    socket.setKeepAlive(true);
                    if (timeout != 0) {
                        socket.setSoTimeout(timeout);
                    }
                    try {
                        // 将此处的socket监听放置到进程池//socket适配器（需要实现Runnable）
                        exService.execute(new SocketAdapt(socket, byteBuffer, size));
                        //建立了一个通信连接
                        logger.info("the socket [" + socket.getInetAddress()
                                + ":" + socket.getPort()
                                + "] has been put in ExecutorService");
//                        ReceiveThread rhd = new ReceiveThread(socket);
//                        new Thread(rhd).start();
                    } catch (RejectedExecutionException e) {
                        e.fillInStackTrace();
                        throw new Exception(
                                "becaue the ERROR "
                                        + e.getMessage()
                                        + " the thread is full and the thread cache is full");
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    if (socket != null)
                        socket.close();
                    throw new Exception("because the ERROR " + e.getMessage()
                            + " so close this socket");
                } catch (IOException e) {
                    e.printStackTrace();
                    if (socket != null)
                        socket.close();
                    throw new Exception("because the ERROR " + e.getMessage()
                            + " so close this socket");
                }
            } catch (Exception e) {
                e.fillInStackTrace();
                logger.info("fillInStackTrace***********" + e.getMessage());
// 一旦发生异常,还继续进行
                continue label;
            }
        }

    }

    /***
     *
     * @param args
     */
    public static void main(String[] args) {
//        连接后我就会推消息给客户端，两分钟没不read我的消息（认为客户端故障？），强制关闭socket
//          //同时保持（max_conn + max_cache）个连接//线程池
//        new SocketServerTest(5566, 3000 * 2, 100, 2 * 60 * 1000);

        int port = Integer.parseInt(System.getProperty("port"));
        int max_conn = Integer.parseInt(System.getProperty("max_conn"));
        int max_cache = Integer.parseInt(System.getProperty("max_cache"));
        int timeout = Integer.parseInt(System.getProperty("timeout"));
        System.out.println("port: " + port + ",max_conn: " + max_conn + ",max_cache: " + max_cache + ",timeout: " + timeout);
        new SocketServerTest(port, max_conn, max_cache, timeout);

//        new SocketServerTest(5566, 1000 * 2, 100, 2 * 60 * 1000);
    }
}
