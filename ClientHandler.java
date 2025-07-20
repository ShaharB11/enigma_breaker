package com.example.enigma_breaker;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
	private boolean is = true;
	private Socket client;
	private  BufferedReader in;
	private  PrintWriter message_out;
	private String userName;
	private boolean user;
	
	public ClientHandler(Socket client) throws IOException {
		this.client = client;
		user=false;
		in = new BufferedReader (new InputStreamReader(client.getInputStream()));
		message_out = new PrintWriter(this.client.getOutputStream(), true);
		
	}
	public void HandShake() {
		try {
			if (!password()) {
		        System.err.println("Handshake failed");
				is = false;
			}
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		}
	}
	public boolean password() throws IOException {
		String un="",pass = "";
		int i=0;
		if (Server.time.containsKey(client)) {
			if (System.currentTimeMillis() - Server.time.get(client) < 60000) {
				message_out.println("Try again later");			
				return false; //If the computer didn't wait long enough after his last failed attempt to connect.
			}
		}
		boolean user_exist = false;
		for (;i<3 && !user_exist;i++) {
			message_out.println("UserName?");
			un=in.readLine();
			if (un != null && un.equals("user")) {
				user = true;
				message_out.println("Logged");
				return true; //If user (application).
			}
			try {
				pass = Server.Agents.get(un);
				if (pass.length() > 8)
					return false; //Already used user name.
				if (Server.Agents.containsKey(un))
					user_exist = true;
			}
			catch (Exception e) {
				System.out.println("Fail " + i + " UserName: " +e);
				message_out.println("UserName?");
			}
		}
		if (i==3 && !user_exist) {
			message_out.println("Failed!");			
			Server.wait(client);
			return false;
		}
		userName = un; //For the end part.
		try{
			//Now the password Part.
			String key = randomEncryption();
			message_out.println(key);
			int sum = 0; int add = 0;
			for (int x=0 ; x<8;x++) { //To encrypt the password verification.
				add = Integer.parseInt(key.split("#")[x]);
				sum += (int)pass.charAt(x) + add;}
			sum = sum/Integer.parseInt("" + key.split("#")[key.split("#").length-1]);
			message_out.println("Password?");
			for (int n=0;n<3;n++) {
				String help = in.readLine();
				
				if (help != null && Integer.parseInt(help + "") == sum) { //If what the agent send is equal to what the server calculated.
					message_out.println("Logged");
					Server.Agents.put(un, Server.Agents.get(un) + "E"); //In order for the server to see that the agent with this user name is currently connected.
					return true;
				}	
				message_out.println("Password?");
			}
			message_out.println("Failed");
			Server.wait(client);
			return false;
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}
	public String randomEncryption() {
		String c="";
		int num;
		for (int n=0;n<8;n++) {
			num =(int)(Math.random()*21) -10;
			while (num==0)
				num =(int)(Math.random()*21) -10;
			c +=  num + "#";
		}
		num = (int)(Math.random()*25);
		while (num==0)
			num = (int)(Math.random()*25);
		c += num;
		return c;
		//Generates a random encryption formula.
	}
	public  void SendMessage() {
		if (!Server.encryptedtTextsQ.isEmpty()) {
			String text = Server.encryptedtTextsQ.head();
			String range = Server.sendRange();
			if (range == null /*|| range.length() > Enigma.key_length*/) {
				message_out.print("nope");
				return; //If the range is null, it means that there are no more keys to send.
			}
			System.out.printf("%s << Handle range %s\n" , this.userName, range);

			String msgToSend =  Server.startTime + ":" +  range + ":" + text;
			message_out.print(msgToSend);
		}
		else
			message_out.print("nope");
		//If there's a text, send it and the key range. If there isn't any, send "nope". 
	}
	public void ReceiveMessage() throws IOException {
		String result = in.readLine();
		message_out.print("got that");
		if (Server.finished) {
			System.out.println("Server finished, ignoring messages from " + this.userName);
			return; //If the server is already finished, don't add the result.
		}
		System.out.printf("%s >> %s\n", this.userName, result);
		result = validateResultAndRemoveValidation(result);
		if (result.isEmpty()) {
			return; //If the result is invalid, don't add it.
		}
		System.out.printf("%s (accepted) >> %s\n", this.userName, result);
		Server.insertResult(result);
		if (Server.send_units <= Server.numItems()) {
			Server.finished = true;
			System.out.printf("Done after %d ms\n", System.currentTimeMillis() - Server.firstAgnetConnectTime);
			result = Server.finalResult();
			String[] possibleKeys = result.split(" ");
			if (possibleKeys.length > 10) {
				System.err.printf("Too many possible keys (%d) - failed to decrypt. Please try again\n" , possibleKeys.length);
				System.exit(3);
			}
			System.out.printf("Displaying %d possible keys and the resulting decrypted texts\n", possibleKeys.length);
			String encryptedText = Server.encryptedtTextsQ.head();
			for (String key : possibleKeys) {
				System.out.printf("%s: %s\n",key, Enigma.decryptWithKey(encryptedText, key));
			}

			Server.encryptedtTextsQ.remove(); //Remove the text that was decrypted.
			System.exit(0);
		}
	//Receives the result from the agent.
	}

	private String validateResultAndRemoveValidation(String result) {
		String[] items = result.split(":");
		if (items.length != 3) {
			System.err.printf("Invalid result received from %s: %s\n", this.userName, result);
			return "";
		}
		if (!items[0].equals(Server.startTime)) {
			System.err.printf("un relevant result received from  %s and will be ignored: %s\n", this.userName, result);
			return "";
		}
		return result.replace(Server.startTime  + ":" ,  "");

	}

	@Override
	public void run() {
		try {
			run1();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				client.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		public void run1() {
		// TODO Auto-generated method stub
		HandShake();
		if (is) {
			if (!user) {
					try {
						//If this is an agent that passed the verification.
						if (Server.firstAgnetConnectTime == 0) {//If this is the first agent to connect.
							Server.firstAgnetConnectTime = System.currentTimeMillis();
							System.out.println("First valid agent connected at: " + Server.firstAgnetConnectTime);
						}
						String what = in.readLine();
						if (what != null && what.equals("result"))
							ReceiveMessage();
						else if(what != null && what.equals("message")) SendMessage();
						else {
							System.out.println("Invalid command received from agent: " + what);
							message_out.print("invalid!");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				try {
				Server.Agents.put(userName, Server.Agents.get(userName).substring(0,Server.Agents.get(userName).length()-1));//To remove the E and allow the same user name to reconnect.
				message_out.close();
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();}
				}
			else {
				String str="";
				try {
					//If this is a client.
					str = in.readLine();
					System.out.println("client command: " + str);
					if (str.equals("encrypt")) {
						str = in.readLine();
						String [] s = str.split(":");
						String send = Enigma.encrypt(s[0], s[1]);
						message_out.print(send);
						message_out.close();
					}
					else if (str.equals("decrypt")) {
						str = in.readLine();
						String [] s = str.split(":");
						String send = Enigma.decryptWithKey(s[0], s[1]);
						message_out.println(send);
						message_out.close();
					}
					else if (str.equals("Enigma")) {
						str = in.readLine();
						Server.encryptedtTextsQ.insert(str);
						message_out.close();

					}
					else if (str.equals("check")) {
						System.out.println("Checking...");
						String returned = Server.finalResult();
						System.out.println("Returning: " + returned);
						message_out.println(returned);
						if (!returned.equals("")) {
							while (!Server.results.isEmpty())
								Server.results.remove();
							Server.currentMax = "0".repeat(Enigma.key_length); 
						}
						message_out.close();
					}
					else
						message_out.close();//If not a valid request- close connection.
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
			}	 	
		else {
			message_out.close();
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
