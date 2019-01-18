import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleSocket {

    public static void main(String[] args) {
        ServerSocket ss = null;
        OutputStream os = null;
        try {
            ss = new ServerSocket(5566);
            while (true) {
                Socket s = ss.accept();
                String ip = s.getInetAddress().getHostName();
                os = s.getOutputStream();
                os.write("ServerSocket回复请求".getBytes());
                os.flush();
                s.close();
                System.out.println(ip + "---" + new String("^_^成功完成一次请求"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
