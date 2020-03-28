package assessment1;

import java.util.Arrays;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

class WorkerMain {

    public static void main(String[] args) throws Exception {

        String ipAddr = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket(ipAddr, port);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String line;
        while ((line = input.readLine()) != null) {
            System.out.println("receive:" + line);
            new Worker(line, out).start();
        }
    }
}

class Worker extends Thread {
    String content;
    DataOutputStream out;
    static Object lock = new Object();

    public Worker(String str, DataOutputStream out) {
        this.content = str;
        this.out = out;
        // System.out.println("thread create id:" + id);
    }

    // @Override
    public void run() {
        // System.out.println("thread begin id:" + id);
        int index = this.content.indexOf("*");
        String str = this.content.substring(0, index);
        Integer id = Integer.parseInt(this.content.substring(index + 1));
        int[] counts = freC(str);
        String result = id.toString() + "*" + Arrays.toString(counts);
        try {
            synchronized (lock) {
                this.out.writeBytes(result + "\n");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(result);
        System.out.println("receive:" + this.content + ":finish");
    }

    private int[] freC(String str) {

        int[] counts = new int[26];
        for (char c : str.toCharArray()) {
            int i = c - 97;
            counts[i]++;
        }
        return counts;
    }
}