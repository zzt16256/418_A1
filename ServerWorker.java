package assessment1;

import java.util.HashMap;
import java.util.LinkedList;

class Worker implements Runnable {

    private int id;
    private String content;
    private String key;
    private Object lock;
    private LinkedList<HashMap<Character, Integer>> result = new LinkedList<HashMap<Character, Integer>>();

    public Worker(int id, String input, String key, Object lock, LinkedList<HashMap<Character, Integer>> result) {
        this.id = id;
        this.content = input;
        this.lock = lock;
        this.result = result;
        this.key = key;

        System.out.println("thread create id:" + id);
    }

    // @Override
    public void run() {
        System.out.println("thread begin id:" + id);
        String s = this.content.replaceAll("[^a-zA-Z]", "").toLowerCase();
        HashMap<Character, Integer> result = frec(s);
        synchronized (this.lock) {
            try {
                this.result.add(result);
                lock.notifyAll();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        System.out.println("thread finish id:" + id);
    }

    private HashMap<Character, Integer> frec(String str) {
        // int max = 0; calculate number of occurrences per character
        HashMap<Character, Integer> map = new HashMap<Character, Integer>();
        for (char c : str.toCharArray()) {
            Integer i = map.get(c);
            int value = (i == null) ? 0 : i;
            map.put(c, ++value);
            // max = value > max ? value : max;
        }
        return map;
        // for (Character key : map.keySet()) {
        // if (map.get(key) == max) {
        // System.out.println("most frequent Character => " + key + ", Count => " +
        // max);
        // return key;
        // }
        // }
        // return '*';
    }

}