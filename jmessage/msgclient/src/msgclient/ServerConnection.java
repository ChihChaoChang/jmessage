// ServerConnection.java
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

import org.apache.http.impl.client.*;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.commons.io.IOUtils;
import java.net.URI;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;

public class ServerConnection {

	static final String PROTOCOL_TYPE = "http";
	static final String KEYLOOKUP_PATH = "/lookupKey";
	static final String MESSAGELOOKUP_PATH = "/getMessages";
	static final String KEYREGISTER_PATH = "/registerKey";
	static final String RESPONSE_KEYDATA = "keyData";

	String mServerName;
	String mUsername;
	String mPassword;
	int mPort;
	
	public ServerConnection(String serverName, int port, String username, String password) {
		mServerName = serverName;
		mUsername = username;
		mPassword = password;
		mPort = port;
	}
	
	public void connectToServer() {
		// TODO
	}
	
	public JSONObject makeGetToServer(String path) {
		CloseableHttpResponse response;
		JSONObject jsonObject = null;
		
		// Send a GET request to the server
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			// Create a URI	         
			URI uri = new URIBuilder()
			        .setScheme(PROTOCOL_TYPE)
			        .setHost("jsonplaceholder.typicode.com")
			        .setPath("/posts/1")
			        //.setHost(mServerName)
			        //.setPath(path)
			        .build();
	        HttpGet httpget = new HttpGet(uri);
	        httpget.addHeader("accept", "application/json");
	        //System.out.println(httpget.getURI());

	        response = httpClient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == 200) {           
            	String jsonData = IOUtils.toString(response.getEntity().getContent());
            	JSONParser parser = new JSONParser();
            	Object obj = parser.parse(jsonData);
            	jsonObject = (JSONObject) obj;
            } else {
            	System.out.println("Received status code " + response.getStatusLine().getStatusCode() + "from server");
            }
            
	        response.close();
	        httpClient.close();
		} catch (Exception e) {
			System.out.println(e);
			return null;
		} 
		
		return jsonObject;
	}
	
	public MsgKeyPair lookupKey(String recipient) {
		MsgKeyPair result = null;
		
		JSONObject jsonObject = makeGetToServer(KEYLOOKUP_PATH);
        String keyData = (String) jsonObject.get("title");
        keyData = keyData.trim();
        System.out.println(keyData);
            	
        // Attempt to parse the key blob back into a MsgKeyPair
        try {
        	if (keyData != null && keyData.isEmpty() == false) {
        		result = new MsgKeyPair(keyData);
        	} else {
        		System.out.println("Did not receive a key from the server");
        	}
        } catch (Exception e) {
        	// Unable to parse the key data
        	System.out.println("Encountered a malformed key");
        	result = null;
        }
		
		// Finally, return the key if it was found
		return result;
	}
	
	public ArrayList<EncryptedMessage> lookupMessages(String myID) {
		ArrayList<EncryptedMessage> result = new ArrayList<EncryptedMessage>();
		
		// TODO, need to specify requestor ID here!
		JSONObject jsonObject = makeGetToServer(MESSAGELOOKUP_PATH);
        long numMessages = (long) jsonObject.get("numMessages");
        if (numMessages <= 0) {
        	return null;
        }

        JSONArray msg = (JSONArray) jsonObject.get("messages");
		Iterator<JSONObject> iterator = msg.iterator();
		while (iterator.hasNext()) {
			JSONObject nextMessage = iterator.next();
			long sentTime = (long) nextMessage.get("sentTime");
			String encryptedMessage = (String) nextMessage.get("message");
			String fromID = (String) nextMessage.get("senderID");
			
			if (encryptedMessage != null) {
				if (encryptedMessage.trim().isEmpty() == false) {		
					EncryptedMessage eMsg = new EncryptedMessage(fromID.trim(), myID.trim(), 
							encryptedMessage.trim(), sentTime);
					result.add(eMsg);
				}
			}
		}
        
		// Return the resulting list
		return result;
	}
	
	public boolean registerKey(String senderID, MsgKeyPair senderKey) {
		String encodedKey = senderKey.getEncodedPubKey();
		
		// TODO: Actually send the key to the server somehow right here!
		
		JSONObject jsonObject = makeGetToServer(KEYREGISTER_PATH);
        if (jsonObject == null) {
        	System.out.println("Key registration failed.");
        }
        
        // TODO: placeholder until this routine is fixed
        return false;
	}
	
	public void sendEncryptedMessage(String recipient, String encryptedMessage) {
		// TODO
	}
	
	public void shutDown() {
	}
}