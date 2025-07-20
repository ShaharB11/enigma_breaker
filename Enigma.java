package com.example.enigma_breaker;

import java.io.IOException;
import java.util.Random;

public class Enigma {
	static int key_length=6;
	public static String encrypt(String text, String key) {
		int len = key.length();
		for (int n=0;n<key_length-len;n++) {
			key = "0" + key; //To make sure the key has the right length.
		}
		String encText="";
		int help=0;
		int track_text=0;
		Random r = new Random();
		for (int i=0;track_text<text.length();i++) {
			if (i%key_length%2 == 1) { //Change the letter
				encText += (char)((int)text.charAt(track_text) + Integer.parseInt("" + key.charAt(i%key_length)));
				track_text++;
			}
			else { //Add a random letter.
				help = 0;
				for (;help<Integer.parseInt("" + key.charAt(i%key_length));help++)
					encText += (char)(r.nextInt(26) + 'a');	
			}
		}
		return encText;
	}
	
	public static String decryptWithKey(String message, String key) {
		int len = key.length();
		for (int n=0;n<key_length-len;n++) {
			key = "0" + key; //To make sure there is a 6 digit key.
		}
    	int place=0;
    	int count=-1;
    	int track_message = 0;
    	String decText = "";
    	for (int i=0;i<message.length();i++) {
			if (place%key_length%2 == 1) { //Return the character back to the original and un-encrypted character.
				//here.
				decText += (char)((int)message.charAt(track_message) - Integer.parseInt("" + key.charAt(place%key_length))); 
				place++;
				track_message++;
			}
			else { //Remove the "fake" characters.
				if (count == -1)
					count = Integer.parseInt("" + key.charAt(place%key_length));
				if (count <= 1) {
					count = -1;
					track_message += Integer.parseInt("" + key.charAt(place%key_length));
					place++;
				}
				else
					count--;	
			}
    	}
    	return decText;
    }
	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.err.printf("Usage: java -jar encrypt.jar e|d <%d digits encryption key> <clear text filename>\n", key_length);
			System.exit(1);
		}
		String cmd = args[0];
		String key = args[1];
		if (key.length() != key_length || !key.matches("[1-9]+")) {
			System.err.printf("Encryption key should be made of digits 1-9 and should be %d digits long\n", key_length);
			System.exit(1);
		}
		String filename = args[2];
		String origContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename)));
		String output;
		if (cmd.equals("e")) {
			output = Enigma.encrypt(origContent, key);
		} else {
			output = Enigma.decryptWithKey(origContent, key);
		}
		System.out.println(output);
	}
}
