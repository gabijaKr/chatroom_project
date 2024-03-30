//Gabija Kriksciunaite Informatika 1 grupe
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
public class Server extends JFrame implements Runnable
{
    private  ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    static JTextArea area = new JTextArea();
    JScrollPane sp;

    public Server(String title)
    {
        super(title);

        connections = new ArrayList<>();
        done = false;

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(Color.black);
        //scrollbar
        sp = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setBounds(10,20,775,470);
        add(sp);

    }
    @Override
    public void run()
    {
        try
        {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();

            while(!done)
            {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e)
        {
            shutdown();
        }
    }

    public void broadcast(String message)
    {
        for(ConnectionHandler ch : connections)
        {
            if(ch != null)
            {
                ch.sendMessage(message);
            }
        }
    }
    public void shutdown() // shutdowns the server
    {
        try
        {
            done  = true;
            pool.shutdown();
            if (!server.isClosed())
            {
                server.close();
            }
            for(ConnectionHandler ch : connections)
            {
                ch.shutdown();
            }
        }
        catch(IOException e)
        {
            //ignore
        }
    }
    class ConnectionHandler implements Runnable
    {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        public String file;


        public ConnectionHandler(Socket client)
        {
            this.client  = client;
        }
        public void run()
        {
            try
            {
                File myFile = new File("Room.txt");
                if(myFile.createNewFile())
                {
                    System.out.println("File created: " + myFile.getName());
                }
                else
                {
                    //System.out.println("File already exists.");
                }
            }catch (IOException e)
            {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            try
            {
                File myFile = new File("Room.txt");
                Scanner myReader = new Scanner(myFile);
                while (myReader.hasNextLine())
                {
                    String output = myReader.nextLine();
                    System.out.println(output);
                    area.append(output + "\n");
                }
                myReader.close();

                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Please enter your nickname: ");

                nickname  = in.readLine();

                System.out.println(nickname + " connected!");
                area.append(nickname + " connected!" + "\n");

                broadcast(nickname + " joined the chatroom!");
                file = file + nickname + " joined the chatroom!" + System.lineSeparator();

                String message;
                while((message = in.readLine()) != null)
                {
                    if (message.startsWith("/nick"))
                    {
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2)
                        {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            file = file + (nickname + " renamed themselves to " + messageSplit[1]) + System.lineSeparator();
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            area.append(nickname + " renamed themselves to " + messageSplit[1] + "\n");
                            nickname = messageSplit[1];
                        }
                        else
                        {
                            out.println("No nickname provided");
                        }
                    }
                    else if (message.startsWith("quit"))
                    {
                        file = file + (nickname + " left the chat") + System.lineSeparator();

                        broadcast(nickname + " left the chat");
                        System.out.println((nickname + " left the chat"));
                        area.append(nickname + " left the chat" + "\n");

                        shutdown();
                    }
                    else
                    {
                        //myWriter.write(nickname + ": " + message);
                        file = file + (nickname + ": " + message + "            / " + java.time.LocalDate.now()) + System.lineSeparator();
                        broadcast(nickname + ": " + message);

                        area.append(nickname + ": " + message + "            / " + java.time.LocalDate.now() + "\n");
                        System.out.println(nickname + ": " + message + "            / " + java.time.LocalDate.now());
                    }
                }
            }catch (Exception e)
            {
                shutdown();
            }

        }
        public void sendMessage(String message)
        {
            out.println(message);
        }
        public void shutdown()
        {
            try
            {
                FileWriter myWriter = new FileWriter ("Room.txt");
                myWriter.write(file);
                myWriter.close();

                in.close();
                out.close();
                if(!client.isClosed())
                {
                    client.close();
                }
            }
            catch (IOException e)
            {
                //ignore
            }
        }
    }
    public static void main(String[] args)
    {
        Server server = new Server("CHATROOM");
        server.setSize(800,600);
        server.setLocation(50,50);
        server.run();

    }
}

