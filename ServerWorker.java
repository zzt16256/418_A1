package assessment1;

import java.util.HashMap;

class Worker implements Runnable {

    private String content;
    private String key;
    private Object lock;
    private HashMap<String, Character> result;

    public Worker(String input, String key, Object lock, HashMap<String, Character> result) {
        this.content = input;
        this.lock = lock;
        this.result = result;
        this.key = key;
    }

    // @Override
    public void run() {
        String s = this.content.replaceAll("[^a-zA-Z]", "").toLowerCase();
        Character f = frec(s);
        synchronized (this.lock) {
            try {
                this.result.put(this.key, f);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private Character frec(String str) {
        int max = 0;
        HashMap<Character, Integer> map = new HashMap<Character, Integer>(str.length());
        for (char c : str.toCharArray()) {
            Integer i = map.get(c);
            int value = (i == null) ? 0 : i;
            map.put(c, ++value);
            max = value > max ? value : max;
        }
        for (Character key : map.keySet()) {
            if (map.get(key) == max) {
                System.out.println("most frequent Character => " + key + ", Count => " + max);
                return key;
            }
        }
        return '*';
    }

}