package assessment1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        // 1.建立Socket管道连接
        Socket socket = new Socket("localhost", 8888);
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner scan = new Scanner(System.in);
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        try {
            // 使用Socket 创建PrintWriter和BufferedReader进行读写数据
            // PrintWriter pw = new PrintWriter(socket.getOutputStream());
            // pw.flush();
            // 接收数据
            String line = null;
            while (is.ready()) {
                line = is.readLine();
                System.out.println(line);
            }
            // 关闭资源
            // pw.close();
            // is.close();
            // socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 3.得到一个打印流与Socket的输出管道接通。
        // PrintStream ps = new PrintStream(socket.getOutputStream());
        // 2.给服务端反复的发送消息。
        // 可以一直的接收键盘的输入
        while (true) {
            String line = scan.nextLine();
            out.write(line.getBytes());
            out.flush();

            line = is.readLine();
            System.out.println(line);
            if (line == "good") {
                break;
            }
        }
        out.close();
        // ps.close();
        scan.close();
        socket.close();
    }
}
