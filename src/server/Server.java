package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comm.Message;

public class Server implements Runnable {
    final static int MASTER = 0;
    final static int LISTENER = 1;
    int purpose;
    static int port;
    
    static Map<Socket, ObjectInputStream> clientInputs = 
            new HashMap<Socket, ObjectInputStream>();
    static Map<Socket, ObjectOutputStream> clientOutputs = 
            new HashMap<Socket, ObjectOutputStream>();
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
            System.out.println("Started server on port " + port);

            while(!exiting) {
                try {
                    Socket clientSocket = ss.accept();
                    ObjectOutputStream clientOut = 
                            new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream clientIn =
                            new ObjectInputStream(clientSocket.getInputStream());
                    clientOutputs.put(clientSocket, clientOut);
                    clientInputs.put(clientSocket, clientIn);

                    System.out.println("Got connection from " 
                            + clientSocket.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    System.out.println("IOException while getting client:");
                    e.printStackTrace();
                }
            }


        } catch(IOException e) {
            System.out.println("Couldn't start server:");
            e.printStackTrace();
            return;
        }
    }
    
    void update(Socket client) {
        ObjectInputStream in = clientInputs.get(client);
        ObjectOutputStream out = clientOutputs.get(client);
        try {
            //read the last message received by the client
            Message last = (Message)in.readObject();
            //get the equivalent message from our timestamp map
            //this message may have a next message, which is the linked list of all
            //more recent messages we have saved.
            Message updates = timeToMessage.get(last.getTime());
            //send it back
            out.writeObject(updates);
        } catch (Exception e) {
            System.out.println("Couldn't read message from client:");
            e.printStackTrace();
        }
    }
}
