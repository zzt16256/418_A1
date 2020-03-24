package assessment1;

import java.util.HashMap;

class Producer implements Runnable {

    private String content;
    private String key;
    private Object lock;
    private HashMap<String, Character> result;

    public Producer(String input, String key, Object lock, HashMap<String, Character> result) {
        this.content = input;
        this.lock = lock;
        this.result = result;
        this.key = key;
    }

    // @Override
    public void run() {

        int length = 20;
        int numDivided = content.length() / length;
        numDivided = (content.length() % length != 0) ? numDivided + 1 : numDivided;
        for (int i = 0; i < numDivided; i++) {
            int begin = i * length;
            int end = Math.min((i + 1) * length, content.length());
            String slice = content.substring(begin, end);

            // Thread worker = new Thread(new Worker(input, this.key, lock, result));
            // worker.start();
        }
    }

}