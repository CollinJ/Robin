package com.Robin.proto;

import java.nio.ByteBuffer;
import java.util.Date;

public class UserMessage {
	/**
	 * 
	 * Author - Public Key of the sender of the message [384 bytes of data]
	 * 
	 * Time Stamp - yy mm dd hh mm ss [8 bytes of data]
	 * 
	 * Unique ID - Hash of the Author and Time Stamp
	 * 
	 * Content - Message to be sent [Variable length
	 * 
	 */
	final static int authorSize = 384;
	final static int timeStampSize = 8;
	byte[] author = new byte[UserMessage.authorSize];
	long timeStamp;
	byte[] contents;
	byte[] finalMessage = null;

	// we created the message
	public UserMessage(String message) {
		// create a ByteBuffer for the networked version of the message
		this.contents = message.getBytes();
		timeStamp = (new Date()).getTime();
		// this.author = TODO: Get public key from Crypto
		makeByteArray(); // this call finally allocates+instantiates the
							// ByteBuffer
	}

	// we are forwarding a message
	public UserMessage(String message, long timeStamp) {
		this.contents = message.getBytes();
		this.timeStamp = timeStamp;
		makeByteArray();

		// this.author = TODO: Get public key from Crypto
	}

	// create a message object from bytes from a connection
	public UserMessage(byte[] newMessage) {

		author = UserMessage.memCpy(newMessage, 0, UserMessage.authorSize);
		timeStamp = (ByteBuffer.wrap(newMessage, UserMessage.authorSize,
				UserMessage.timeStampSize)).getLong();
		contents = UserMessage.memCpy(newMessage, UserMessage.authorSize
				+ UserMessage.timeStampSize, newMessage.length
				- UserMessage.authorSize - UserMessage.timeStampSize);
		finalMessage = newMessage;
	}

	public static byte[] memCpy(byte[] src, int offset, int length) {
		byte[] dest = new byte[length];
		for (int i = 0; i < length; i++) {
			dest[i] = src[offset + i];
		}
		return dest;
	}

	public void makeByteArray() {
		int size = UserMessage.authorSize + UserMessage.timeStampSize
				+ contents.length;
		ByteBuffer finalMessageBuffer = ByteBuffer.allocate(size + 4);
		finalMessageBuffer.putInt(size);
		finalMessageBuffer.put(author); // author comes first
		finalMessageBuffer.putLong(timeStamp); // timeStamp is next
		finalMessageBuffer.put(contents); // finally message contents
		finalMessage = finalMessageBuffer.array();
		finalMessageBuffer.clear();
	}

	public byte[] getByteArray() {
		return finalMessage;
	}

	@Override
	public String toString() {
		String s = new String(contents);
		Date d = new Date(timeStamp);
		return d.toString() + ": " + s;

	}

}
