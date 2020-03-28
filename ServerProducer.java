package assessment1;

import java.util.HashMap;
import java.util.LinkedList;

class Producer implements Runnable {

    private String content;
    private String key;
    private Object lock;
    private Object workerLock;
    private HashMap<String, Character> result;
    private LinkedList<HashMap<Character, Integer>> wrokerResult = new LinkedList<HashMap<Character, Integer>>();

    public Producer(String input, String key, Object lock, HashMap<String, Character> result) {
        this.content = input;
        this.lock = lock;
        this.result = result;
        this.key = key;
        this.workerLock = new Object();
    }

    // @Override
    public void run() {

        int length = 10;
        String input = this.content;
        int numDivided = input.length() / length;
        numDivided = (input.length() % length != 0) ? numDivided + 1 : numDivided;
        for (int i = 0; i < numDivided; i++) {
            int begin = i * length;
            int end = Math.min((i + 1) * length, input.length());
            String slice = input.substring(begin, end);

            // Thread worker = new Thread(new Worker(i, slice, this.key, lock,
            // wrokerResult));
            // worker.start();
        }
        synchronized (workerLock) {
            while (wrokerResult.size() != numDivided) {
                try {
                    wrokerResult.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        // for (HashMap<Character, Integer> hashMap : wrokerResult) {
        // for (HashMap.Entry<Character, Integer> entry : hashMap.entrySet()) {
        // Character c = entry.getKey();
        // Integer i = result.get(c);
        // int value = (i == null) ? 0 : i;
        // value += entry.getValue();
        // map.put(c, value);
        // }
        // }

        HashMap<Character, Integer> result = new HashMap<Character, Integer>();
        wrokerResult.forEach((map) -> {
            map.forEach((c, v) -> {
                Integer i = result.get(c);
                int value = (i == null) ? 0 : i;
                value += v;
                result.put(c, value);
            });
        });

        // 函数式写法
        Character frequentC = result.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

        System.out.println(this.key + " : " + frequentC);
        synchronized (this.lock) {
            this.result.put(this.key, frequentC);
        }
    }
}