package assessment1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(8888);
        while (true) {
            // 2.接收客户端
            Socket socket = server.accept();
            new MyReader(socket).start();
        }

    }
}

class MyReader extends Thread {

    private Socket socket;
    private String key;
    static Object lock = new Object();
    static HashMap<String, Character> result = new HashMap<String, Character>();

    public MyReader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // PrintWriter pw = new PrintWriter(socket.getOutputStream());
            // pw.println("Welcome to CharFreqServer\n" + "Type any of the following
            // options: \n"
            // + "NewRequest <INPUTSTRING>\n" + "StatusRequest <passcode>\n" + "Exit\n");
            // pw.flush();
            // 接收到请求后使用 socket 进行通信，创建 BufferedReader 用于读取数据
            // 3.服务端要从Socket管道中得到一个字节输入流。
            // 4.把字节输入流转换成字符输入流，再转换成高级缓冲字符输入流。
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes("Welcome to CharFreqServer\n" + "Type any of the following options: \n"
                    + "NewRequest  <INPUTSTRING>\n" + "StatusRequest  <passcode>\n" + "Exit\n");
            out.flush();
            // 5.读取数据
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("服务端接收：" + line);
                if (line.startsWith("NewRequest ") == true) {
                    // key
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    String input = line.substring(11);
                    this.key = this.GetKey(input);

                    out.writeBytes(this.key);

                    Thread worker = new Thread(new Worker(input, this.key, lock, result));
                    worker.start();

                } else if (line.startsWith("StatusRequest ") == true) {
                    String input = line.substring(14);
                    // this.result[this.key] = f;
                    if (input.equals(this.key)) {
                        Character reply = '*';
                        synchronized (this) {
                            reply = result.getOrDefault(this.key, '*');
                        }
                        if (reply != '*') {
                            // handle
                            out.writeChars(reply.toString());
                        } else {
                            out.writeBytes("waiting");
                        }

                    } else {
                        System.out.println(input);
                        System.out.println(this.key);
                        out.writeBytes("error key!");

                    }
                    // checkkey
                } else if (line.startsWith("Exit ") == true) {
                    break;
                } else {
                    System.out.println("error！");
                    break;
                }
            }
            this.socket.close();
        } catch (Exception e) {
            System.out.println("有人离线！");
        }
    }

    private String GetKey(String line) {
        try {
            String input = line.substring(11);

            byte[] secretBytes = MessageDigest.getInstance("md5").digest(input.getBytes());

            // BigInteger bigint = new BigInteger(1, secretBytes);
            // String md5code = bigint.toString();
            String md5code = "";
            for (int i = 0; i < secretBytes.length; i++) {
                md5code += Integer.toHexString((0x000000ff & secretBytes[i]) | 0xffffff00).substring(6);
            }
            return md5code;
        } catch (Exception e) {

            return "";
        }
    }
}
