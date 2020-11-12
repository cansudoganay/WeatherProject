package main;

import java.net.*;
import java.util.ArrayList;

public class MultithreadedSocketServer {
	
    public static void main(String[] args) throws Exception {
    	
        try {
        	ArrayList<ServerClientThread> connections = new ArrayList<ServerClientThread>();
            ServerSocket ss = new ServerSocket(8888);
            int counter = 0;
            System.out.println("Server Started ....");
            while (true) {
                counter++;
                Socket s = ss.accept();  //server accept the client connection request
                ServerClientThread sct = new ServerClientThread(s, counter); //send  the request to a separate thread
                sct.start();
                connections.add(sct);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}