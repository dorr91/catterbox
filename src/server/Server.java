package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import comm.Message;
import server.Client;

public class Server implements Runnable {
    final static int MASTER = 0;
    final static int LISTENER = 1;
    int purpose;
    static int port;
    
    static Map<Date, Message> timeToMessage = 
            new HashMap<Date, Message>();
    boolean exiting = false;
    
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: ./server -p <port>");
        }
        Server s = new Server(MASTER, 0);
        s.run();
    }
    
    public Server(int purpose, int port) {
        this.port = port;
        this.purpose = purpose;
    }
    
    public void run() {
        switch(purpose) {
        case MASTER:
            Runnable listener = new Server(LISTENER, port);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
            
            break;
            
        case LISTENER:
            listen();
            break;
        }
    }

    void listen() {
        try {
            ServerSocket ss = new ServerSocket(port);
            port = ss.getLocalPort();
            System.out.println("Listening for connections on port " + port);

            while(!exiting) {
                try {
                    Socket clientSocket = ss.accept();
                    System.out.println("Got connection from " 
                            + clientSocket.getInetAddress().getHostAddress());

                    handle(new Client(clientSocket));
                } catch (IOException e) {
                    System.out.println("IOException while getting client:");
                    e.printStackTrace();
                }
            }


        } catch(IOException e) {
            System.out.println("Couldn't start server:");
            e.printStackTrace();
        }
    }
    
    void handle(Client c) {
        try {
            //read the request sent by the client
            String request = (String) c.read();
            if(request.equals("pull")) {
                update(c);
            } else if(request.equals("push")) {
                //do something else
            }
        } catch (IOException e) {
            System.out.println("Problem reading from client at " + c.getAddress());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Got an unknown object from client at " + c.getAddress());
            e.printStackTrace();
        }
    }
    
    void update(Client c) {
        try {
            //read the last message received by the client
            Message last = (Message)c.read();
            //get the equivalent message from our timestamp map
            //this message may have a next message, which is the linked list of all
            //more recent messages we have saved.
            Message updates = timeToMessage.get(last.getTime());
            //send it back
            c.send(updates);
        } catch (Exception e) {
            System.out.println("Couldn't read message from client:");
            e.printStackTrace();
        }
    }
    
    void readMessage(Client c) {
        try {
            String content = (String) c.read();
            Message m = new Message(content);
            
        } catch (IOException e) {
            System.out.println("Problem reading from client at " + c.getAddress());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Got an unknown object from client at " + c.getAddress());
            e.printStackTrace();
        }
        
    }
}
