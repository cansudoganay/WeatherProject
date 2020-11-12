package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class MessageProtocol {

	public static class Header {
		protected byte phase;
		protected byte type;
		protected int psize;
		public boolean wasTimedout = false;
		
		public Header(byte phase, byte type, int psize) {
			this.phase = phase;
			this.type = type;
			this.psize = psize;
		}

		public byte getPhase() {
			return phase;
		}

		public byte getType() {
			return type;
		}
		
		public int getPSize() {
			return psize;
		}
	}
	
	public static class Message {
		protected Header header;
		protected String payload;
		
		public Message(Header header, String payload) {
			this.header = header;
			this.payload = payload;
		}

		public Header getHeader() {
			return header;
		}

		public String getPayload() {
			return payload;
		}
	}
	
	public static void sendMessage(DataOutputStream outStream, byte phase, byte type, String payload) {
		int psize = payload.getBytes().length;
		try {
			outStream.write(phase);
			outStream.write(type);
			outStream.writeInt(psize);
			outStream.writeBytes(payload);
		}
		catch(IOException e) {
			System.err.println("Couldn't send the message");
			return;
		}
		System.out.println("Message sent");
	}
	
	public static Message readMessage(DataInputStream inStream) {
		Header header = readHeader(inStream);
		String payload = readPayload(inStream, header);
		return new Message(header, payload);
	}
	
	public static Header readHeader(DataInputStream inStream) {
		byte phase;
		byte type;
		int psize;
		try {
			phase = inStream.readByte();
			type = inStream.readByte();
			psize = inStream.readInt();
		} 
		catch(SocketTimeoutException e){
			Header h = new Header((byte)0, (byte)0, 0);
			h.wasTimedout = true;
			return h;
		}
		catch(IOException e){
			System.err.println("Couldn't read header");
			return null;
		}
		return new Header(phase, type, psize);
	}
	
	public static String readPayload(DataInputStream inStream, Header header) {
		byte[] payload = new byte[header.getPSize()];
		try {
			inStream.readFully(payload);
		} 
		catch(IOException e) {
			e.printStackTrace();
		}
		return new String(payload);
	}

}
