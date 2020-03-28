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
import java.util.concurrent.ArrayBlockingQueue;

public class Server {
    public static void main(String[] args) throws Exception {

        Integer port = 8888;
        Integer workerPort = 9999;
        try {
            port = Integer.parseInt(args[0]);
            workerPort = Integer.parseInt(args[1]);

        } catch (Exception e) {
            System.out.println("error params!");
            System.out.println(e.getMessage());
        }
        // Integer workernum = Integer.parseInt(args[2]);
        // Integer port = 8888;
        // Integer workerPort = 9999;

        WorkerServer ws = new WorkerServer(workerPort);
        ws.start();

        System.out.println("listening port :" + port.toString());
        ServerSocket server = new ServerSocket(port);
        while (true) {
            // 2.接收客户端
            Socket socket = server.accept();
            System.out.println("accept one client ");
            new MyReader(socket, ws).start();
        }
    }
}

class WorkerServer extends Thread {
    private Integer port;
    public WorkerReader wr;

    public WorkerServer(int port) {
        this.port = port;
    }

    public void run() {

        System.out.println("listening port :" + this.port.toString());
        ServerSocket server;
        try {
            server = new ServerSocket(this.port);

            while (true) {
                // 2.接收客户端
                Socket socket = server.accept();
                System.out.println("accept one worker ");
                wr = new WorkerReader(socket, 5);
                wr.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class WorkerReader extends Thread {
    private Socket socket;
    private Object lock;
    private MyReader myreader;
    private DataOutputStream output;
    private ArrayBlockingQueue<Integer> workerQ;

    private HashMap<Integer, MyReader> resultHandle;

    public WorkerReader(Socket socket, int count) {
        this.socket = socket;
        try {
            output = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        lock = new Object();
        this.workerQ = new ArrayBlockingQueue<Integer>(count);
        for (int i = 0; i < count; i++) {
            workerQ.add(i);
        }
        this.resultHandle = new HashMap<Integer, MyReader>();
    }

    public synchronized void Sendwork(MyReader mr, String str) {
        try {
            while (workerQ.isEmpty()) {
                Thread.sleep(10);
            }

            Integer workerID = workerQ.poll();
            this.resultHandle.put(workerID, mr);
            output.writeBytes(str + "*" + workerID.toString() + "\n");
            System.out.println(String.format("worker ID: %d start...", workerID));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        System.out.println("worker reader running...");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                int i = line.indexOf("*");
                int workid = Integer.parseInt(line.substring(0, i));
                String result = line.substring(i + 1);

                String counts = result.replace(']', ',').replace('[', ' ');
                int[] d = new int[26];
                int count = 0;
                while (true) {
                    int idx = counts.indexOf(',');
                    d[count++] = Integer.parseInt(counts.substring(0, idx).trim());
                    if (counts.length() == 2)
                        break;
                    counts = counts.substring(idx + 2);
                }

                MyReader mr = this.resultHandle.get(workid);
                for (int j = 0; j < 26; j++) {
                    mr.result[j] += d[j];
                }
                this.workerQ.add(workid + 5);

                System.out.println(String.format("worker ID: %d finish...", workid));
                // lock.notifyAll();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

class MyReader extends Thread {

    private Socket socket;
    private String key;
    private WorkerServer wr;
    // static Object lock = new Object();
    public int[] result;

    public MyReader(Socket socket, WorkerServer wr) {
        this.socket = socket;
        this.wr = wr;
        this.result = new int[26];
    }

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
                    String input = line.substring(11);
                    input = input.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    for (int i = 0; i < 26; i++) {
                        result[i] = 0;
                    }
                    if (input.length() == 0) {
                        out.writeBytes("input does not contain one character!!!!");
                    } else if (input.length() < 50) {
                        this.key = this.GetKey(input);

                        out.writeBytes("key is (" + this.key + ")\n");

                        this.result = this.freC(input);

                    } else if (input.length() < 100) {
                        this.key = this.GetKey(input);

                        out.writeBytes("key is (" + this.key + ")\n");

                        this.wr.wr.Sendwork(this, input);

                    } else {
                        this.key = this.GetKey(input);

                        out.writeBytes("key is (" + this.key + ")\n");

                        int length = 50;
                        int numDivided = input.length() / length;
                        numDivided = (input.length() % length != 0) ? numDivided + 1 : numDivided;
                        for (int i = 0; i < numDivided; i++) {
                            int begin = i * length;
                            int end = Math.min((i + 1) * length, input.length());
                            String slice = input.substring(begin, end);

                            this.wr.wr.Sendwork(this, slice);
                        }
                    }

                } else if (line.startsWith("StatusRequest ") == true) {
                    String input = line.substring(14);
                    // this.result[this.key] = f;
                    if (input.equals(this.key)) {
                        Character reply = '*';
                        int max = 0;
                        int maxIdx = 0;
                        for (int i = 1; i < 26; i++) {
                            if (result[i] > max) {
                                max = result[i];
                                maxIdx = i;
                            }
                        }
                        reply = (char) (maxIdx + 97);
                        if (reply != '*') {
                            // handle
                            String strout = String.format("high frequent character is (%c)(%d)", reply, result[maxIdx]);
                            out.writeChars(strout + "\n");
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
            System.out.println(e.getMessage());
            System.out.println("有人离线！");
        }
    }

    private int[] freC(String str) {

        int[] counts = new int[26];
        for (char c : str.toCharArray()) {
            int i = c - 97;
            counts[i]++;
        }
        return counts;
    }

    private String GetKey(String line) {
        try {
            String input = line;

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
