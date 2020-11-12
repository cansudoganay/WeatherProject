package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import main.MessageProtocol.Header;

public class ServerClientThread extends Thread {
	Socket serverClient;
	MultithreadedSocketServer server;
	int clientUserName;
	DataInputStream inStream;
	DataOutputStream outStream;
	private static ArrayList<String[]> inputs = new ArrayList<>();
	byte Auth_Request = (byte) 0;
	byte Auth_Challenge = (byte) 1;
	byte Auth_Fail = (byte) 2;
	byte Auth_Success = (byte) 3;
	byte Auth_Phase = (byte) 0;
	byte Query_Phase = (byte) 1;
	int TIMEOUT_DURATION = 10*1000; //10 seconds


	public ServerClientThread(Socket inSocket, int clientUserName) {
		super("ServerClientThread");
		this.serverClient = inSocket;
		this.clientUserName = clientUserName;
	}

	public void run() {

		getUsers();

		try {
			serverClient.setSoTimeout(TIMEOUT_DURATION);
			inStream = new DataInputStream(serverClient.getInputStream());
			outStream = new DataOutputStream(serverClient.getOutputStream());

			String clientMessage = "", serverMessage = "";
			boolean existingUser = false;
			int dialogueIndex = 0;
			int userIndex = -1;
			boolean allquestionsAnswered = false;
			boolean timedOut = false;

			while (!clientMessage.equals("stop")) {
				clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
				System.out.println("client says: " + clientMessage);

				for (int i = 0; i < inputs.size(); i++) {
					String currentUserName = inputs.get(i)[0];
					if (currentUserName.equals(clientMessage)) {
						existingUser = true;
						userIndex = i;
					}
				}
				if (existingUser) {
					serverMessage = "Connected to server";
					MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Request, serverMessage);
					serverClient.setSoTimeout(TIMEOUT_DURATION);
					outStream.flush();
					while (!clientMessage.equals("stop")) {
						if (allquestionsAnswered) {
							// authenticate is done

						
							// 
							String token = tokenCreate(InetAddress.getLocalHost(),inputs.get(userIndex)[0]);

							InetAddress add=InetAddress.getLocalHost();
							String clientIP=add.getHostAddress();
							ArrayList<String> privateInfo = new ArrayList<>();
							privateInfo.add(0,clientIP+8888); //since 8888 is the port 
							privateInfo.add(1,inputs.get(userIndex)[0]);
							privateInfo.add(2,token);

							serverMessage = 
									"\n1. Current weather forecast\n" + 
											//"City id or city name\n" + 
											"2. Weather triggers\n" + //BU KALDIRILACAK
											"3. Basic weather maps\n" + 
											//"zoom, x, y \n" + 
											"4. Minute forecast for  1 hour \n" + 
											//"City id or city name\n" + 
											"5. Historical weather for 5 days";
							//"City id or city name";

							MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, token + "-" + serverMessage);
							outStream.flush();
							clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
							System.out.println("client says: " + clientMessage);

							//While loopun olacak sanirsam
							if(clientMessage.equals("1")||clientMessage.equals("2")||clientMessage.equals("4")||clientMessage.equals("5")) {
								ServerSocket dataSocket = new ServerSocket(9999);

								Socket helperSocket = null;
								InputStream in = null;
								OutputStream out = null;

								serverMessage = "Enter city id or city name";
								if(clientMessage.equals("1")) {
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, serverMessage);
									outStream.flush();
									clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
									JSONObject latLon = (JSONObject) WeatherRequest.readJson(clientMessage);
									String response = WeatherRequest.getCurrentWeather((double)latLon.get("lat"), (double)latLon.get("lon"), token);

									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "TRUE&&" + "current_" + token + ".json && \n" + response);
									dataSocketTransfer(helperSocket, dataSocket, in, out, "current_" + token + ".json");
									outStream.flush();
								}
								else if(clientMessage.equals("4")) {
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, serverMessage);
									outStream.flush();
									clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
									JSONObject latLon = (JSONObject) WeatherRequest.readJson(clientMessage);
									String response = WeatherRequest.getMinuteForecast((double)latLon.get("lat"), (double)latLon.get("lon"), token);
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "TRUE&&" + "minute_" + token + ".json && \n" + response);
									dataSocketTransfer(helperSocket, dataSocket, in, out, "minute_" + token + ".json");
									outStream.flush();
								}
								else if(clientMessage.equals("2")) {
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, serverMessage);
									outStream.flush();
									clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
									JSONObject latLon = (JSONObject) WeatherRequest.readJson(clientMessage);
									String response = WeatherRequest.getSevenDays((double)latLon.get("lat"), (double)latLon.get("lon"), token);
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "TRUE&&" + "DailySeven_" + token + ".json && \n" + response);
									dataSocketTransfer(helperSocket, dataSocket, in, out, "DailySeven_" + token + ".json");
									
									outStream.flush();
								}
								else if(clientMessage.equals("5")){
									//5 ICIN
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, serverMessage);
									outStream.flush();
									clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
									JSONObject latLon = (JSONObject) WeatherRequest.readJson(clientMessage);
									String response = WeatherRequest.get5Days((double)latLon.get("lat"), (double)latLon.get("lon"), token);
									MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "TRUE&&" + "historical_" + token + ".json && \n" + response);
									dataSocketTransfer(helperSocket, dataSocket, in, out, "historical_" + token + ".json");
									outStream.flush();
								}
								dataSocket.close();
							}else if (clientMessage.equals("3")){
								ServerSocket dataSocket = new ServerSocket(9999);

								Socket helperSocket = null;
								InputStream in = null;
								OutputStream out = null;

								MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "Enter layer name(temp_new, clouds_new etc.), zoom, x tile coordinate, y tile coordinate. (respectively)");
								outStream.flush();
								clientMessage = MessageProtocol.readPayload(inStream, MessageProtocol.readHeader(inStream));
								String[] tokens = clientMessage.split(Pattern.quote(","));
								String response = WeatherRequest.getMap(tokens[0], Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), token);
								MessageProtocol.sendMessage(outStream, Query_Phase, Auth_Success, "TRUE&&" + "basic_" + token + ".png &&" + response);
								dataSocketTransfer(helperSocket, dataSocket, in, out, "basic_" + token + ".png");
								outStream.flush();
								dataSocket.close();
							} 
							
							break; // Not break, but go to query phase

						} else {
							// System.out.println(inputs.get(userIndex)[dialogueIndex+1]);
							String str = inputs.get(userIndex)[dialogueIndex + 1];

							MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Challenge, str);
							outStream.flush();
							Header header = MessageProtocol.readHeader(inStream); 
							//enters the loop
							serverClient.setSoTimeout(TIMEOUT_DURATION);
							if(header != null && header.wasTimedout) {
								System.out.println(header);
								MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Fail, "Connection timedout");
								outStream.flush();
								outStream.close();
								serverClient.close();
								timedOut = true;
								break;
							}
							clientMessage = MessageProtocol.readPayload(inStream, header);
							System.out.println("client says: " + clientMessage);
							if (clientMessage.equals(inputs.get(userIndex)[dialogueIndex + 2])) {
								dialogueIndex += 2;
								if (dialogueIndex == 6) {// since we are looking for 4 since we have 2 questions and 2 answers.
									allquestionsAnswered = true;
									serverClient.setSoTimeout(0);
								}
							} else {
								serverMessage = "Wrong answer. Authentication failed.";
								MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Fail, serverMessage);
								outStream.flush();
								outStream.close();
								serverClient.close();
								break;
							}
						}
					}

				} else {
					serverMessage = "Invalid user. Authentication failed.";
					MessageProtocol.sendMessage(outStream, Auth_Phase, Auth_Fail, serverMessage);
					outStream.flush();
					outStream.close();
					serverClient.close();
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		finally {
			System.out.println("Client disconnected");
			try {
				outStream.flush();
				outStream.close();
				serverClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String tokenCreate(InetAddress address, String strings) {
		//   	Random randomHIP = new Random();
		Random randomHN = new Random();
		Random randomUN = new Random();

		//    	String hostIP = address.getHostAddress();
		String hostName = address.getHostName();
		String userName = strings;

		//    	int HIPindex=randomHIP.nextInt(hostIP.length());
		int HNindex=randomHN.nextInt(hostName.length());
		int UNindex=randomUN.nextInt(userName.length());

		String token = hostName.substring(0, HNindex) + userName.substring(0, UNindex);
		return token;
	}

	public static void getUsers() {
		try {
			File inputFile = new File("users.txt");
			Scanner fileScanner = new Scanner(inputFile);
			while (fileScanner.hasNextLine()) {
				String data = fileScanner.nextLine();
				inputs.add(data.split(","));
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void dataSocketTransfer(Socket helperSocket, ServerSocket dataSocket, InputStream in, OutputStream out, String filename) throws IOException {
		try {
			helperSocket = dataSocket.accept();
		} catch (IOException ex) {
			System.out.println("Can't accept client connection. ");
		}
		try {
			in = helperSocket.getInputStream();
		} catch (IOException ex) {
			System.out.println("Can't get socket input stream. ");
		}

		try {
			out = new FileOutputStream(filename);
		} catch (FileNotFoundException ex) {
			System.out.println("File not found. ");
		}

		byte[] bytes = new byte[16*1024];

		int count;
		while ((count = in.read(bytes)) > 0) {
			out.write(bytes, 0, count);
		}

		out.close();
		in.close();
		helperSocket.close();
		dataSocket.close();
	}


}
