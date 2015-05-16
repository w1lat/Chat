package javaEE.chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Таня on 11.05.2015.
 */
public class AsynchronClientSocket2 {

    public static void main(String[] args) throws IOException {
        final Socket socket = new Socket("localhost", 33333);
        //client read message
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Scanner console = new Scanner(System.in);
                    String fileName = "../" + socket.getInetAddress().toString().substring(10) + socket.getPort();
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName + "SendMessage"));

                    while(true){
                        String string = console.nextLine();
                        Message message = new Message();
                        message.setMessage(string);

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(message);
                        objectOutputStream.flush();
                        objectOutputStream.close();

                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + "SendMessage"));
                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                        byte[] byteArray = new byte[1024];
                        int in;
                        while ((in = bis.read(byteArray)) != -1){
                            bos.write(byteArray,0,in);
                        }
                        bis.close();
                        bos.close();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Message receivedMessage = null;
                    while(true){
                        String fileName = "../" + socket.getInetAddress().toString().substring(10) + socket.getPort();
                        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File (fileName + "ReceivedMessage")));
                        byte[] byteArray = new byte[1024];
                        int out;
                        while ((out = bis.read(byteArray)) != -1){
                            bos.write(byteArray,0,out);
                        }
                        bis.close();
                        bos.close();

                        FileInputStream fis = new FileInputStream(fileName + "ReceivedMessage");
                        ObjectInputStream oin = new ObjectInputStream(fis);
                        try {
                            receivedMessage = (Message)oin.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        System.out.println("[" + receivedMessage.getTime() + "]" + " " + receivedMessage.getMessage());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
