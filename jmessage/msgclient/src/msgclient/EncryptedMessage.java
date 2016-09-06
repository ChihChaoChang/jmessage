package msgclient;

public class EncryptedMessage {
	String 	mSenderID;
	String 	mReceiverID;
	String 	mMessageText;
	long	mSentTime;
	
	public EncryptedMessage(String senderID, String receiverID, String messageText, long sentTime) {
		this.mSenderID = senderID;
		this.mReceiverID = receiverID;
		this.mMessageText = messageText;
		this.mSentTime = sentTime;
	}
	
	public String getSenderID() {
		return mSenderID;
	}
	
	public String getReceiverID() {
		return mReceiverID;
	}
	
	public String getMessageText() {
		return mMessageText;
	}
	
	public long getSentTime() {
		return mSentTime;
	}
	
}