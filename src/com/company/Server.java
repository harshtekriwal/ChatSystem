package com.company;
import javax.xml.crypto.Data;
import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
public class Server
{
    public static boolean CheckCredentialsRegister(String username, String password){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/db?useSSL=false", "root", "rambo");
            Statement stmt = con.createStatement();
            ResultSet rs=stmt.executeQuery("select name from details");
            while(rs.next()){
                if(rs.getString(1).equals(username)){

                    con.close();
                    return false;
                }
            }
            stmt.executeUpdate("insert into details value('"+username+"','"+password+"')");
            con.close();
            return true;
        }

        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean CheckCredentialslogin(String name,String password,HashMap<String,Socket> users){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/db?useSSL=false", "root", "rambo");
            Statement stmt = con.createStatement();
            ResultSet rs=stmt.executeQuery("select name,password from details");
            while(rs.next()){
                if(rs.getString(1).equals(name)&&rs.getString(2).equals(password)){
                    if(users.keySet().contains(name)){
                        con.close();
                        System.out.println("USER ALREADY LOGGED IN.");
                        return false;
                    }
                    else {
                        con.close();
                        return true;
                    }
                }
            }
           con.close();
            return false;
        }
        catch(Exception e){
        e.printStackTrace();
        return false;
        }
    }
    public static void main(String args[]) throws IOException
    {

         HashMap<String,Socket> users= new HashMap<String, Socket>();
         ServerSocket server=new ServerSocket(5000);


        System.out.println("Server Connected....");
        while(true)
        {
            Socket s=null;
            try{
                s=server.accept();
                DataInputStream input=new DataInputStream(s.getInputStream());
                DataOutputStream output=new DataOutputStream(s.getOutputStream());
                String z=input.readUTF();
                if(z.equals("Register")){
                    System.out.println("LOL");
                    String username=input.readUTF();

                    String password=input.readUTF();
                    boolean status =CheckCredentialsRegister(username,password);
                    if(status==true){
                        output.writeUTF("REGISTRATION SUCCESSFULL");
                    }
                    else{
                        output.writeUTF("USERNAME WITH THAT ACCOUNT EXISTS");
                    }
                }
                else {
                    String username=input.readUTF();
                    String password=input.readUTF();
                    boolean available = CheckCredentialslogin(username,password,users);
                    if (available == true) {
                        System.out.println(username + " is online");
                        output.writeUTF("CONNECTED...");

                        users.put(username, s);
                        Thread t = new ClientHandler(s, input, users, username);
                        t.start();
                    } else {
                        output.writeUTF("No such account exists.");
                        s.close();
                    }
                }
            }
            catch(Exception e)
            {

                s.close();
            }
        }
    }
}

class ClientHandler extends Thread
{
    DataOutputStream output;
    HashMap<String,Socket> users;
    Socket s;
    DataInputStream input;
    String line="";
    String x="";

    ClientHandler(Socket s,DataInputStream input,HashMap<String,Socket> users,String uname)
    {
        this.users=users;
        this.s=s;
        this.input=input;
        this.x=uname;
    }

    public void run()
    {

        try{

            for(Socket socket:users.values()) {
                if (!socket.equals(s)) {
                    output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(x+" is Online");
                }
            }
        }
        catch(IOException i){
            System.out.println(i);
        }

        while(!line.equals("over")){
            int flag=0;
            try {
                line = input.readUTF();
                if (!line.equals("over")) {
                    System.out.println(x + " : " + line);
                }
                if (line.startsWith("**")) {
                    for (String a : users.keySet()) {
                        if (line.startsWith("**"+a)) {
                            output = new DataOutputStream(users.get(a).getOutputStream());
                            output.writeUTF("Private Text from "+x+":"+ line.substring(2+a.length()));
                            flag=1;
                            break;

                        } else {
                            continue;
                        }
                    }
                    if(flag==0){
                        output=new DataOutputStream(s.getOutputStream());
                        output.writeUTF("User is Offline or Wrong Username Entered");

                    }

                } else {
                    for (Socket socket : users.values()) {
                        if (!socket.equals(s)) {
                            output = new DataOutputStream(socket.getOutputStream());
                            if (!line.equals("over")) {
                                output.writeUTF(x + " : " + line);
                            }
                        }
                    }
                }
            }
            catch(IOException e)
            {
                break;
            }
        }


        try
        {
            System.out.println(x+" is offline now.");
            for(Socket socket:users.values()) {
                if (!socket.equals(s)) {
                    output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(x+" is offline now.");
                }
            }
            users.remove(x);
            s.close();
            input.close();
        }
        catch(IOException e){
           System.out.println(e);

        }
    }

}
