package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import comm.Message;

public class Client {
    static String masterLocation;
    static int masterPort;
    static Message oldest, newest;
    static boolean exiting;
    
    public static void main(String [] args) {
        if(args.length != 2) {
            System.out.println("Usage: ./catterbox-client <hostname> <port>");
            return;
        }
        
        try {
            String hostname = args[0];
            int port = Integer.parseInt(args[1]);
            Client c = new Client(hostname, port);
            
            c.run();
        } catch (Exception e) {
            System.out.println("Initialization exception:");
            e.printStackTrace();
        }
    }
    
    public Client(String hostname, int port) 
            throws UnknownHostException, IOException {
        masterLocation = hostname;
        masterPort = port;
        //make sure we can connect to the server
        new Socket(masterLocation, masterPort).close();
    }
    
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while(!exiting) {
            
            try {
                if(in.ready()) {
                    String line = in.readLine();
                    Message newMsg = new Message(line);
                    send(newMsg);
                }

                Message news = update();
                if(news != null) {

                    //print the new messages
                    print(news);

                    //update our list with the new messages
                    newest.append(news);
                    newest = newest.last();
                }
            } catch (IOException e) {
                System.out.println("Error:");
                e.printStackTrace();
            }
        }
    }

    public static Message update() {
        try {

            //open a socket to the master
            Socket server = new Socket(masterLocation, masterPort);
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());

            //tell the server we're requesting new info
            out.writeObject("pull");
            //send our latest message to tell the server what we know
            out.writeObject(newest);
            //get anything that's happened since the last message we got
            Message news = (Message) in.readObject();
            
            
            in.close();
            out.close();
            server.close();
            
            return news;
            
        } catch (UnknownHostException e) {
            System.out.println("Master could not be found at " + masterLocation);
        } catch (ClassNotFoundException e) {
            System.out.println("Unknown object received from master:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error writing to master:");
            e.printStackTrace();
        }
        
        //exception
        return null;
    }

    static void send(Message msg) {
        try {
            
            //open a socket to the master
            Socket server = new Socket(masterLocation, masterPort);
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());

            //tell the server we have a new message
            out.writeObject("push");
            //send the new message (as a string)
            out.writeObject(msg.getContent());
            
            //read back a message object containing all the messages we've missed
            //our message should be the last in the list (?)
            Message result = (Message) in.readObject();
            
            print(result);

            in.close();
            out.close();
            server.close();

        } catch (UnknownHostException e) {
            System.out.println("Master could not be found at " + masterLocation);
        } catch (IOException e) {
            System.out.println("Error writing to master:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Unknown object received from master:");
            e.printStackTrace();
        }
    }
    
    static void print(Message m) {
        //recursively print a chain of messages
        if(m == null) return;
        System.out.println(m.getTime() + ": " + m.getContent());
        print(m.next);
    }
}
