package main;

import java.net.*;
import java.util.concurrent.TimeUnit;

import main.MessageProtocol.Message;

import java.io.*;

public class Client {

	public static void main(String[] args) throws Exception {

		try {
			byte Auth_Request = (byte) 0;
			byte Auth_Challenge = (byte) 1;
			byte Auth_Fail = (byte) 2;
			byte Auth_Success = (byte) 3;
			byte Auth_Phase = (byte) 0;
			byte Query_Phase = (byte) 1;
			Socket socket = new Socket("127.0.0.1", 8888);

			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String clientMessage = "", serverMessage = "", token = "";
			boolean authenticated = false;

			System.out.println("Enter your user name please:");
			clientMessage = br.readLine();
			MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Request, clientMessage); 
			outStream.flush();
			serverMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
			System.out.println(serverMessage);
			serverMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
			System.out.println(serverMessage);

			while(true) {
				clientMessage = br.readLine();
				if(clientMessage.equals("quit")) break;
				if(inStream.available() > 0) {
                    Message msg = MessageProtocol.readMessage(inStream);
                    serverMessage = msg.getPayload();
                    System.out.println(serverMessage);
                    if(msg.getHeader().getType() == Auth_Fail) break;
                }
				MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Request, clientMessage);
				outStream.flush();
				Message msg = MessageProtocol.readMessage(inStream);
				serverMessage = msg.getPayload();
				System.out.println(serverMessage);
				if(msg.getHeader().getType() == Auth_Fail) break;
				if(msg.getHeader().getPhase() == Query_Phase && !authenticated) {
					token = serverMessage.split("-")[0];
					authenticated = true;
				}
				if(msg.getHeader().getType() == Auth_Success) {
					boolean recieveData = false;
					
					
					if(serverMessage.split("&&")[0].equals("TRUE")) {
						recieveData = true;
						
					}
				
					
					
					if(recieveData) {
					
						Socket helperSocket = null;
						helperSocket = new Socket("127.0.0.1", 9999);
						String filename = serverMessage.split("&&")[1];
						filename = filename.trim();
						File file = new File(filename);
						
						// Get the size of the file
						long length = file.length();
						byte[] bytes = new byte[64 * 1024 ];
						
					        
						InputStream in = new FileInputStream(file);
						OutputStream out = helperSocket.getOutputStream();
						System.out.println("File transfer started !");
						int count;
						while ((count = in.read(bytes)) > 0) {
							out.write(bytes, 0, count);
						}
						System.out.println("File transferred successfully");
						out.close();
						in.close();
						helperSocket.close();
					}
				}
			}
			outStream.flush();
			outStream.close();
			socket.close();

		} catch (Exception e) {
			System.out.println(e);
		} 
	}
	 private static void getAllFiles(File curDir) {

	        File[] filesList = curDir.listFiles();
	        for(File f : filesList){
	            if(f.isDirectory())
	                getAllFiles(f);
	            if(f.isFile()){
	                System.out.println(f.getName());
	            }
	        }

	 }
	private void saveFile(Socket clientSock, String fileName) throws IOException {
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream(fileName);
		byte[] buffer = new byte[4096];

		int filesize = 15123; // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while ((read=dis.read(buffer)) > 0) {
			fos.write(buffer,0,read);
		}

		fos.close();
		dis.close();
	}
}