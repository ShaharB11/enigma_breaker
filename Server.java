package com.example.enigma_breaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static long firstAgnetConnectTime;
    public static String startTime = Long.toString(System.currentTimeMillis());
    static HashMap<String, String> Agents = new HashMap<String, String>(); //Details of Agents.
    static HashMap<Socket, Long> time = new HashMap<Socket, Long>();
    static Queue<String> results = new Queue<String>();
    static AtomicInteger numItemsInResults = new AtomicInteger(0); //To keep track of the number of items in results.
    static final int send_units = 10;
    static boolean finished = false;
    static String currentMax = "0".repeat(Enigma.key_length);
    static int add = Integer.parseInt("1" + currentMax.substring(1) + "0") / send_units;
    static Queue<String> encryptedtTextsQ = new Queue<String>();

    public static void addAgents() {
        Agents.put("Computer-123456", "aT5&&p0q");
        Agents.put("MyPC1", "Oy71zpD#");
        Agents.put("NameName", "8+f(6aLx");
        Agents.put("UserMe", "-tvWEa46");
        Agents.put("UserYou", "-tvWEa46");
        System.out.println("List of agent users and passwords:");
        Agents.forEach((key, value) -> {
            System.out.println("User: " + key + ", Password: " + value);
        });
//		encryptedtTextsQ.insert(Enigma.encrypt("hello world, hello world", "123456"));
//        encryptedtTextsQ.insert(Enigma.encrypt("hello and good morning World. The weather will be good today", "765432"));
    }

    public static void wait(Socket s) {
        time.put(s, System.currentTimeMillis());
    }

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.err.printf("Usage: java -jar enigma_breaker_server.jar  <encrypted filename>\n");
            System.exit(1);
        }

        if (encryptedtTextsQ.isEmpty()) {
            String filename = args[0];
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename)));
            if (content.length() == 0) {
                System.err.println("File is empty, please provide a valid encrypted file.");
                System.exit(1);
            }
            System.out.printf("Loaded %d characters from encrypted file: %s\n", content.length(), filename);

            encryptedtTextsQ.insert(content);
        }
        addAgents();

        ServerSocket listener = new ServerSocket(10);
        System.out.println("Listening on port 10 and waiting for agents...");
        while (true) {
            Socket client = listener.accept();
            //System.out.println("Server connected to client");
            ClientHandler clientThread = new ClientHandler(client);
            new Thread(clientThread).start();
        }
    }

    public static int numItems() {
//        Queue<String> help = new Queue<String>();
//        int count = 0;
//        while (!results.isEmpty()) {
//            help.insert(results.remove());
//            count++;
//        }
//        while (!help.isEmpty()) {
//            results.insert(help.remove());
//        }
//        return count;
        return numItemsInResults.get();
    }

    public static String finalResult() {
        Queue<String> help = new Queue<String>();
        int num_max = 0;
        String best_keys = "";
        int num_words = 0;
        if (finished) {
            while (!results.isEmpty()) {
                try {
                    num_words = Integer.parseInt(results.head().split(": ")[0] + "");
                } catch (NumberFormatException e) {
                    System.err.printf("Ignoring faulty result %s\n", results.remove());
                    continue;
                }
                if (num_words > num_max) {
                    num_max = num_words;
                    best_keys = results.head().split(": ")[1];
                } else if (num_words == num_max) {
                    best_keys += " " + results.head().split(": ")[1];
                }
                help.insert(results.remove());
            }
            while (!help.isEmpty()) {
                results.insert(help.remove());
            }
            return best_keys;
        }
        return "";

    }

    public static String sendRange() {
        // TODO Auto-generated method stub
        synchronized (Server.class) {
            currentMax = "" + (Integer.parseInt(currentMax) + add);
            if (Server.send_units == Server.numItems()) {
                return null;
            }
            return currentMax;
        }
        //Add the number needed to get to the new max key.
    }

    public static void insertResult(String result) {
        results.insert(result);
        numItemsInResults.incrementAndGet();
    }
}
