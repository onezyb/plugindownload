import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class BfTest {
//    private static int i = 0;

    public static void test(int i) {
        try {
            //创建一个客户端socket
            Socket socket = new Socket("localhost", 5566);
            //向服务器端传递信息
            OutputStream ots = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(ots);
            pw.write("00");
            pw.flush();
            //关闭输出流
            socket.shutdownOutput();
            //获取服务器端传递的数据
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String info = null;
            while ((info = br.readLine()) != null) {
                System.out.println("No" + i + "我是客户端，服务器说：" + info);
            }
            //关闭资源
            br.close();
            isr.close();
            is.close();
            pw.close();
            ots.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MyThread extends Thread {
        private int count;

        public MyThread(int i) {
            this.count = i;
        }

        @Override
        public void run() {
            test(count);
        }
    }

    public static void main(String[] args) {

        for (int i=0; i < 2; i++) {
            MyThread mtd = new MyThread(i);
            mtd.start();
        }
    }

}
