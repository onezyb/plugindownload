package com.ixinnuo.taskpkg;

import java.io.*;
import java.net.Socket;

public class CloseTimeout implements Runnable {
    private OutputStream os;
    private DataOutputStream dos;
    private Socket skt;

    public CloseTimeout(Socket s, OutputStream os, DataOutputStream dos) {
        this.dos = dos;
        this.os = os;
        this.skt = s;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10 * 1000);//两分钟关闭
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean brekFlg = false;
        InputStream is = null;
        try {
            is = skt.getInputStream();
        } catch (IOException e) {
//            if (e.getMessage().equals("Read timed out")) {
//                //如果是这种超时异常就能立即关闭socket，并不再监听安装成功消息
//                brekFlg = true;
//            } else
            if (e.getMessage().equals("Connection reset")) {//客户端异常关闭或连接异常中断等
                brekFlg = true;
            } else if (e.getMessage().equals("Software caused connection abort: recv failed")) {
                brekFlg = true;
            } else {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        if (brekFlg) {
            try {
                dos.close();
                os.close();
                skt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
