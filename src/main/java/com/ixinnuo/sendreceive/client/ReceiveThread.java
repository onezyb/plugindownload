package com.ixinnuo.sendreceive.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReceiveThread implements Runnable {
    //public class ReceiveThread extends Thread {
//    private Socket socket;
//    private InputStream is;
private BufferedReader br;
    public ReceiveThread(BufferedReader is) {
        this.br = is;
    }

    @Override
    public void run() {
//        boolean brekFlg = false;
//        while (true) {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            InputStream is = null;
//            try {
//                is = s.getInputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            String infos = null;
//            try {
//                while ((infos = br.readLine()) != null) {
//                    System.out.println("我是客户端，服务端说：" + infos);
////                        s.close();
////                        brekFlg = true;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        while (true) {
//        InputStreamReader isr = new InputStreamReader(is);
//        BufferedReader br = new BufferedReader(isr);
        String infos = null;
        try {
            while ((infos = br.readLine()) != null) {
                System.out.println("我是客户端，服务器说：" + infos);
            }
//            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }
}