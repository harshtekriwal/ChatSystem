package com.company;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static String x;
    private static String y;
    private static  Socket socket=null;
    private static DataInputStream input=null;
    private static DataOutputStream output=null;
    private static DataInputStream in=null;
    private  JFrame jFrame;
    private JButton button;
    private JTextField pm;
    private JTextField text;
    private JTextArea jt;

    public static void Register(String address,int port){
       JFrame jf=new JFrame();
       jf.getContentPane().setLayout(new FlowLayout());
       JLabel l=new JLabel("USERNAME");
       JLabel l2=new JLabel("PASSWORD");
        JTextField jt= new JTextField("",10);
        JTextField jt2 =new JTextField("",10);
        JButton but=new JButton("create account");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(l);
        jf.getContentPane().add(jt);
        jf.getContentPane().add(l2);
        jf.getContentPane().add(jt2);
        jf.getContentPane().add(but);
        jf.setSize(400, 300);
        jf.setLocation(100, 200);
        jf.setVisible(true);
        final String[] username = {""};
        final String[] password = {""};
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource()==but){
                    username[0] =jt.getText();
                    password[0] =jt2.getText();
                    try{
                        socket =new Socket(address,port);
                        output=new DataOutputStream(socket.getOutputStream());
                        input=new DataInputStream(socket.getInputStream());
                        output.writeUTF("Register");
                        output.writeUTF(username[0]);
                        output.writeUTF(password[0]);
                        String result=input.readUTF();
                        System.out.println(result);


                    }
                    catch(Exception E){
                        E.printStackTrace();
                    }
                }
            }
        });





    }
    public Client(String address,int port)
    {
     String showMessage="";
        try {

            socket = new Socket(address, port);
            in=new DataInputStream(socket.getInputStream());
            input = new DataInputStream(System.in);
            output = new DataOutputStream(socket.getOutputStream());
            Scanner a=new Scanner(System.in);
            System.out.println("ENTER YOUR USERNAME:");
            String username=a.nextLine();
            System.out.println("Enter YOUR PASSWORD:");
            String password=a.nextLine();
            output.writeUTF("LOGIN");
            output.writeUTF(username);
            output.writeUTF(password);
             showMessage=in.readUTF();
            System.out.println(showMessage);
            if(showMessage.equals("No such account exists.")){
                socket.close();
            }
            else {
                jFrame = new JFrame();
                jFrame.getContentPane().setLayout(new FlowLayout());
                button = new JButton("CLICK ME");
                text = new JTextField("", 20);
                jt = new JTextArea(10, 30);
                pm = new JTextField("", 5);

                jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jFrame.getContentPane().add(pm);
                jFrame.getContentPane().add(text);
                jFrame.getContentPane().add(button);
                jFrame.getContentPane().add(jt);
                jFrame.setSize(400, 300);
                jFrame.setLocation(100, 200);
                jFrame.setVisible(true);
            }
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        if(showMessage.equals("CONNECTED...")) {
            Thread t1 = new takeInput(socket, input, output, button, text, jt, pm);
            t1.start();
            Thread t2 = new receiverMessage(socket, in, jt);
            t2.start();
        }

            return;

    }

    public static void main(String []args)
    {
        Scanner a= new Scanner(System.in);
        int choice ;
        do {
            System.out.println("ENTER WHAT YOU WANNA DO:-");
            System.out.println("1.Register:-");
            System.out.println("2.Login:-");
            System.out.println("3.Exit");
            System.out.println("Enter your choice:-");
            choice=a.nextInt();
            switch(choice){
                case 1: {
                    Register("127.0.0.1",5000);
                    break;
                }
                case 2:{

                    Client client=new Client("127.0.0.1", 5000);
                    break;
                }
                case 3:{
                    break;
                }

            }

        }while(choice!=3);

        return ;

    }
}
 class takeInput extends Thread {
     String receiver;
    String line;
    JButton button;
    JTextField text;
     JTextArea jt;
     JTextField pm;      DataInputStream input=null;
     Socket s;
     DataOutputStream output=null;

     public takeInput(Socket s, DataInputStream input,DataOutputStream output,JButton button,JTextField text,JTextArea jt,JTextField pm){
         this.s=s;
         this.input=input;
         line="";
         this.output=output;
         this.button=button;
         this.text=text;
         this.jt=jt;
         this.pm=pm;

     }
    public void run(){

        button.addActionListener(new ActionListener() {

            @Override

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == button) {
                    receiver=pm.getText();
                    line = text.getText();
                    text.setText("");


                    try{
                        if(receiver.equals("")) {
                            output.writeUTF(line);
                            jt.append("YOU : " + line);
                            jt.append("\n");
                        }
                        else{
                            output.writeUTF("**"+receiver+line);
                            jt.append("To "+receiver +":"+line);
                            jt.append("\n");
                        }
                    }
                    catch(IOException ignore){

                    }
                }
            }
        });
     }
}
class receiverMessage extends Thread{
    String line;
    private DataInputStream input=null;
    Socket s;
    JTextArea jt;
    public receiverMessage(Socket s,DataInputStream input,JTextArea jt){
        this.s=s;
        this.input=input;
        line="";
        this.jt=jt;
    }
    public void run()
    {
        while (true) {
            try {
                line = input.readUTF();
                jt.append(line);
                jt.append("\n");
            } catch (IOException i) {
                break;
            }

        }
    }
}