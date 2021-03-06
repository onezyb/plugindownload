import com.ixinnuo.sendreceive.client.ReceiveThread;
import com.ixinnuo.sendreceive.client.SendThread;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class SocketClient {

//    @Test
//    public void clients() throws IOException, InterruptedException {
public static void main(String[] args) throws IOException, InterruptedException {
        try {
            //创建一个客户端socket
            Socket s = new Socket("47.104.175.44", 5566);
//            Socket s = new Socket("localhost", 5566);
            s.setKeepAlive(true);
            //规定了先接收
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            ReceiveThread rt = new ReceiveThread(br);
            //测试客户端下载超时
//            Thread.sleep(10 * 60 * 30);
            new Thread(rt).start();
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            String infos = null;
//            try {
//                while ((infos = br.readLine()) != null) {
//                    System.out.println("我是客户端，服务器说：" + infos);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //关闭读
//            br.close();
//            isr.close();
            //模拟客户端发一次消息
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            OutputStream oos = null;
            PrintWriter pw = null;
            try {
                oos = s.getOutputStream();
                pw = new PrintWriter(oos);
                String sd = "00";
                System.out.println("发送给服务器" + sd);
                pw.write(sd);
                pw.flush();
                s.shutdownOutput();
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            } finally {//?关闭写？
//                oos.close();
//                pw.close();
//                s.close();
            }

//            SendThread st = new SendThread(s);
//            new Thread(st).start();

//            new Thread(st).start();
//            new Thread(new ReceiveThread(s)).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class mythread extends Thread {
        private PrintWriter pw;
        private OutputStream ots;

        public mythread(OutputStream otss, PrintWriter pww) {
            this.pw = pww;
            this.ots = otss;
        }

        @Override
        public void run() {
            int k = 10;
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pw.write("00");
                pw.flush();
                if (k == -1) {
                    pw.close();
                    try {
                        ots.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
    }


}

