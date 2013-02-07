package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {
    final static int MASTER = 0;
    final static int LISTENER = 1;
    int purpose, port;
    static Map<Socket, InputStream> clientInputs;
    static Map<Socket, OutputStream> clientOutputs;
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
        clientInputs = new HashMap<Socket, InputStream>();
        clientOutputs = new HashMap<Socket, OutputStream>();
    }
    
    public void run() {
        switch(purpose) {
        case MASTER:
            System.out.println("Starting server on port " + port);

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

            while(!exiting) {
                try {
                    Socket clientSocket = ss.accept();
                    OutputStream clientOut = 
                            new ObjectOutputStream(clientSocket.getOutputStream());
                    InputStream clientIn =
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
}
