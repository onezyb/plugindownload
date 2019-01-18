package com.ixinnuo.sendreceive.server;

import java.io.*;
import java.net.Socket;

public class ReceiveThread implements Runnable {private OutputStream os;
    private DataOutputStream dos;
    private Socket socket;

    public ReceiveThread(Socket socket) {
        this.socket =socket;

    }

    @Override
    public void run() {
        boolean brekFlg = false;
        InputStreamReader isr = null;
        BufferedReader br = null;
        while (true) {
            InputStream is = null;
            try {
                is = socket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                String infos = null;
                while ((infos = br.readLine()) != null) {
                    if (infos.equals("00")) {//客户端下载完毕,流读完毕，关闭stream和socket
                        System.out.println("客户端安装成功");
                        brekFlg = true;
                        break;
                    }
                }
            } catch (IOException e) {
                //如果超时两分钟就关闭socket,从而释放线程池，让其他客户端能够进来
                if (e.getMessage().equals("Read timed out")) {
                    //如果是这种超时异常就能立即关闭socket，并不再监听安装成功消息
                    brekFlg = true;
                } else if (e.getMessage().equals("Connection reset")) {//客户端异常关闭或连接异常中断等
                    brekFlg = true;
                } else {//异步的??
                    e.printStackTrace();
                }
            }
            if (brekFlg) {
                try {
                    //写关闭
//                    dos.close();
//                    os.close();
                    //读关闭
                    br.close();
                    isr.close();
                    is.close();
                    socket.close();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}