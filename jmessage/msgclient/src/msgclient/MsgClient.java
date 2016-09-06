// MsgClient.java
//
// Copyright (c) 2016 Matthew Green
// Part of the JMessage messaging client, used for Practical Cryptographic
// Systems at JHU. 
// 
// Note that the J could stand for "Janky". This is demonstration code
// that contains deliberate vulnerabilities included for students to find.
// You're free to use it, but it is not safe to use it for anything
// you care about. 
// 
// TL;DR: if you deploy it in production I will laugh at you. 
// 
// Distributed under the MIT License. See https://opensource.org/licenses/MIT.

package msgclient;

import java.util.Scanner;
import org.apache.commons.cli.*;

public class MsgClient {
	
	static final int DEFAULT_PORT = 8000;
	static final String DEFAULT_PASSWORD = "";
	
	String	serverName;
	int		serverPort;
	String	serverUsername;
	String	serverPassword;
	MessageEncryptor mEncryptor;
	ServerConnection mServerConnection;
	
	Scanner scanner;
	
	public void parseArguments(String[] args) throws Exception {
    	// Parse command line arguments
    	Options options = new Options();

        Option server = new Option("s", "server", true, "server name");
        server.setRequired(true);
        options.addOption(server);

        Option port = new Option("p", "port", true, "server port (default 8000)");
        port.setRequired(false);
        options.addOption(port);
        
        Option uname = new Option("u", "username", true, "username");
        uname.setRequired(true);
        options.addOption(uname);
        
        Option password = new Option("w", "password", false, "password (default is none)");
        password.setRequired(false);
        options.addOption(password);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("msgclient", options);

            System.exit(1);
            return;
        }
        
        // Optional arguments
        if (cmd.hasOption("p") == true) {
        	serverPort = Integer.parseInt(cmd.getOptionValue("p"));
        } else {
        	serverPort = DEFAULT_PORT;
        }
        
        if (cmd.hasOption("p") == true) {
        	serverPassword = cmd.getOptionValue("p");
        } else {
        	serverPassword = "";
        }
        
        // Required arguments
        serverName = cmd.getOptionValue("s");
        serverUsername = cmd.getOptionValue("u");
	}
	
	public void printHelp() {
		System.out.println("Available commands:");
		System.out.println("   get (or empty line)  - check for new messages");
		System.out.println("   c(ompose) <user>     - compose a message to <user>");
		System.out.println("   f(ingerprint) <user> - return the key fingerprint of <user>");
		System.out.println("   genkeys              - generates and registers a new keypair");
		System.out.println("   h(elp)               - prints this listing");
		System.out.println("   q(uit)               - exits");
	}
	
	// Get messages from the server
	public void getMessages() throws Exception {
		System.out.println("Getting messages from server...");
		// TODO
	}
	
	// Compose a new message
	public void composeMessage(String recipient) throws Exception {
		
		// First look up the recipient's public key on the server
		MsgKeyPair recipientKey = mServerConnection.lookupKey(recipient);
		if (recipientKey == null) {
			System.out.println("Could not find a key for user " + recipient);
			return;
		}
		
		// Read in a message
		System.out.println("Enter your message and hit return (empty line cancels message):");
		String message = scanner.nextLine().trim();
		
		if (message.isEmpty() == true) {
			System.out.println("Message canceled.");
		} else {
			// Print out the message just as a test
			System.out.println("Sending the message: " + message);
			
			// Encrypt the message to the recipient
			String encryptedMessage = mEncryptor.encryptMessage(message, recipientKey);
			if (encryptedMessage == null) {
				System.out.println("Error encrypting message.");
			} else {
				// Send the encrypted message
				mServerConnection.sendEncryptedMessage(recipient, encryptedMessage);
				
				System.out.println("Message sent.");
			}
		} 
	}
	
	// Print the key fingerprint of a user
	public void printFingerprint(String recipient) throws Exception {
		MsgKeyPair recipientKey = null;
		
		if (recipient.isEmpty() == false) {
			// First look up the recipient's public key on the server
			recipientKey = mServerConnection.lookupKey(recipient);
			if (recipientKey == null) {
				System.out.println("Could not find a key for user " + recipient);
				return;
			}
		}
		
		// Print our fingerprint and the user's fingerprint
		System.out.println("Your key fingerprint: ");
		System.out.println(MessageEncryptor.computeFingerprint(mEncryptor.getEncodedPublicKeys()));
		
		if (recipientKey != null) {
			System.out.println("Fingerprint for user " + recipient + ":");
			System.out.println(MessageEncryptor.computeFingerprint(recipientKey.getEncodedPubKey()));
		}
	}
	
	// Generates a MsgKeyPair, registers it with server
	public void registerKeys() throws Exception {
		//System.out.println("Generating new MsgKeyPair...");
		
		// TODO
	}
	
	public void mainLoop() throws Exception {
		boolean running = true;
		scanner = new Scanner(System.in);
		
		while (running == true) {
			// Print a command prompt and wait for user input
			System.out.print("enter command> ");
			String command = scanner.nextLine().trim();
		
			// Parse input into tokens
			String[] parsedString = command.split("[ ]+");
			
			// Check input
			if (command.isEmpty() || parsedString[0].equals("get")) {
				getMessages();
			}
			else if (parsedString[0].startsWith("c")) {
				if (parsedString.length > 1) {
					composeMessage(parsedString[1]);
				} else {
					System.out.println("Usage: compose <username>");
				}
			} else if (parsedString[0].startsWith("f")) {
				if (parsedString.length > 1) {
					printFingerprint(parsedString[1]);
				} else {
					printFingerprint("");
				}	
			}
			else if (parsedString[0].startsWith("h") || parsedString[0].startsWith("?")) {
				printHelp();
			} else if (parsedString[0].startsWith("q")) {
				running = false;
			}
			
			//System.out.println(command);
		}
		
		scanner.close();
	}
	
	public void runClient() throws Exception {
		
		// Create an encryption class and a server connection class
		mEncryptor = new MessageEncryptor(serverUsername);
		mServerConnection = new ServerConnection(serverName, serverPort, serverUsername, serverPassword);
		mServerConnection.connectToServer();
		
		// Run an encryption test
		if (TestEncryption.testEncryption("Test message") == false) {
			System.out.println("Encryption self-test failed.");
		}
		
		// Register our public keys
		registerKeys();
		
		// All tests and registration complete
		System.out.println("Server connection successful. Ready to begin.");
		
		// With a server connection, start the main loop
		mainLoop();
		
		// Shut down the connection to the server
		mServerConnection.shutDown();
	}
	
    public static void main(String[] args) throws Exception {
    	
    	// Create a client and parse command line arguments
    	MsgClient msgClient = new MsgClient();
    	msgClient.parseArguments(args);
    	
    	// Run the client
    	msgClient.runClient();
    	
        // The end
        System.out.println("Shutting down...");
    }
    
}