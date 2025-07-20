# EnigmaBreaker

A Java project that employs a distributed network of agents on personal desktops to collaboratively perform parallel decryption attempts on text encrypted with an unknown key, echoing the historical efforts to break the German Enigma code while drawing inspiration from the SETI@home project’s approach to distributed computing.

The project was originally written with an Android application as a front end as my high school final project (2019).
<br>It was modified to remove the dependency with the Android application for an easier activation from desktops and laptops.



## Compilation

1. Open a terminal in the project root directory (where the source are)
2. Compile the source code:<br>
javac -d out *.java<br>

3. Create dedicated jar for each program:<br>
jar cfe enigma_breaker.jar com.example.enigma_breaker.Server -C out . <br>
jar cfe enigma_breaker_agent.jar com.example.enigma_breaker.Agent -C out .<br>
jar cfe encrypt.jar com.example.enigma_breaker.Enigma -C out .

# Running

## Break an encoded message

 java -jar enigma_breaker.jar  < encrypted message file name >

 The program would spin a server that would use available agents to decrypt the message in the file
 and print its content and the encryption key that was found.
 Once the message is decrypted the program will terminate.

 **Example:**<br>
 java -jar enigma_breaker.jar encrypted-msg.txt 

 

 Upon startup the program would print the list of users and passwords that can be used by the assiting agents.

## Spin up agents to assist the breaking command
 java -jar enigma_breaker_agent.jar <user name> <password> <breaker host> <port>

 Spin up an agent that would will connect to the breaking command and would help it to break the encoding.
Several agents can be spinned up to on the local machine and additional machines to utilize more CPU power 
to break the encrypted message.
The agent will remain running to assist any invocation of the breaker command.
 
 **Example:**<br>
  java -jar enigma_breaker_agent.jar MyPC1 Oy71zpD# localhost 10 <br>
  java -jar enigma_breaker_agent.jar UserMe -tvWEa46 10.0.0.9 10



## Utility to encrypt/decrypt messages
 java -jar encrypt.jar e|d <6 digits encryption key> <filename>

**Example:**<br>
java -jar encrypt.jar e 123456 clear-text-msg.txt 
java -jar encrypt.jar d 123456 encrypted-msg.txt

## Sample messages
Clear text message and its encrypted version with key 823473

clear-text-msg.txt      
encrypted-msg.txt 






