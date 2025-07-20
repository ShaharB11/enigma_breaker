package com.example.enigma_breaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.sleep;


public class Agent {
    static Set<String> keyWords = new HashSet<>();

    public static boolean log(BufferedReader input, PrintWriter message_out, String uName, String p) throws IOException {
        try {
            String react = "";
            react = input.readLine();
            if (react == null) {
                return false;
            }
            while (react.equals("UserName?")) {
                message_out.println(uName); //Send user name.
                react = input.readLine();
                if (react == null) {
                    return false;
                }
            }
            if (react.equals("Failed!"))
                return false;
            int sum = 0;
            int add = 0;
            if (!react.equals("Password?")) {
                for (int x = 0; x < 8; x++) { //The encryption of the password.
                    add = Integer.parseInt(react.split("#")[x]);
                    sum += (int) p.charAt(x) + Integer.parseInt(react.split("#")[x]);
                }
                sum = sum / Integer.parseInt("" + react.split("#")[react.split("#").length - 1]);
            }
//            System.out.println("sum is :" + sum);
            react = input.readLine();
            while (react.equals("Password?")) {
                message_out.println(sum);
                react = input.readLine();
            }
            if (react.startsWith("Logged")) return true;
            return false;
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return false;
    }

    public static String scan(String message, String max) {
        HashMap<Integer, String> results = new HashMap<Integer, String>();
        int max_range = Integer.parseInt(max);
        int min = max_range - Server.add; //To get the bottom key of the range.
        int words = 0;
        int num_max = 1;
        String ret = "";
        String text = "";
        for (int i = min; i < max_range; i++) {
            text = Enigma.decryptWithKey(message, "" + i).toLowerCase(); //All to lower case letters.
            words = findInText(text); //How many known words were found in the decrypted text.
            if (words >= num_max) {
                if (words > num_max) { //In order to save only the keys that generates the biggest number of known words.
                    num_max = words;
                    results.clear(); //Clear the HashMap from words that generates less known words than the one found.
                }
                if (results.containsKey(words))
                    results.put(words, results.get(words) + i + " "); //If another key with the same amount of known words found.
                else
                    results.put(words, i + " ");
            }
        }
        String keys = results.get(num_max);
        results.clear();
        if (keys == null)
            return "0: ";
        return num_max + ": " + keys;
    }

    public static int findInText(String text) {
        //How many known words are found.
        String[] wordsInText = text.split("[\\s,.;:!?()\\[\\]{}\"'<>/\\\\|@#$%^&*_+=-]+");
        int count = 0;
        for (String word : wordsInText) {
            if (keyWords.contains(word)) {
                count++;
            }
        }
        return count;
    }

    public static void insertWords() {
        keyWords.add("hello");
        keyWords.add("good");
        keyWords.add("morning");
        keyWords.add("evening");
        keyWords.add("weather");
        keyWords.add("base");
        keyWords.add("and");
        keyWords.add("are");
        keyWords.add("you");
        keyWords.add("new");
        keyWords.add("happy");
        keyWords.add("world");
        keyWords.add("that");


    }

    public static void main(String args[]) throws UnknownHostException, IOException, InterruptedException {
        insertWords();

        String uName = "Computer-123456"; //User name example.
        String p = "aT5&&p0q";
        String serverName = "127.0.0.1";  // Indicating the place to put Server's IP
        int port = 10;
        String ret = "";
        String message = "message";
        boolean failed = false;
        boolean job = true;
        if (args.length >= 2) {
            uName = args[0];
            p = args[1];
        }
        if (args.length >= 4) {
            serverName = args[2];
            port = Integer.parseInt(args[3]);
        }
        System.out.printf("Agent started with user name: '%s' and will assist the breaking command at %s:%d'\n" ,
                uName, serverName, port);

        while (true) {
            Socket s = null;
            try {
                if (failed) { //If the computer failed to connect to the server, wait one minute before trying again.
                    sleep(2000);
                    failed = false;
                }
                if (!job) {
                    System.out.printf("\r %s Server has no work, trying again in 2 seconds...", new Date());
                    job = true;
                    sleep(2000);
                }
                s = new Socket(serverName, port);
                //s.setSoTimeout(1000);
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter message_out = new PrintWriter(s.getOutputStream(), true);
                if (log(input, message_out, uName, p)) {
                    System.out.println("Logged in successfully");
                    message_out.println(message); //Send what
//                    System.out.println("Message received is : " + message);
                    if (message.equals("message")) {
                        String received = input.readLine();
                        if (received != null) {
                            if (!received.equals("nope")) {
                                String serverStartTime = received.split(":")[0];
                                String range = received.split(":")[1];
                                String text = received.split(":")[2];
                                if (!range.equals("0")) {
                                    message = "result"; //For the next cycle, so the agent returns the result.
                                    System.out.println("Processing request to check range " + range);
                                    ret = serverStartTime  + ":" + scan(text, range);
                                }
                            } else {
                                job = false;
                            }
                        }
                    } else if (message.equals("result")) {

                        message_out.println(ret);
                        String confirmation = input.readLine();
                        if (confirmation != null && confirmation.equals("got that")) {
                            System.out.println("Result sent successfully: " + ret);
                        } else {
                            System.out.println("Failed to send result.");
                        }
                        message = "message"; //For the next cycle, so the agent request another job.
                    }
                } else {
                    System.out.println("Failed to login with user " + uName); //If the logging in procedure has failed.
                    failed = true;
//                    System.exit(1);
                }

            } catch (ConnectException ce) {
                System.out.printf("\r %s Server is not available, trying again in 1 seconds...", new Date());
                sleep(1000);

            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                sleep(1000);

            } finally {
                if (s != null && !s.isClosed()) {
                    s.close();
                }
            }
        }


    }

}
