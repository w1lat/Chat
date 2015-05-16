package javaEE.chat;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by serhii on 11.05.15.
 */
public class ServerSocketTest {



    public static void main(String[] args) throws IOException {
        Map<String, Socket> clients = new HashMap<>();
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        File history = new File("../history " + df.format(today)  + ".txt");
        Map<String, String> userNames = new HashMap<>();

        ServerSocket ss = new ServerSocket(33333);
        Writer writer = new FileWriter(history);
        String lineSeparator = System.getProperty("line.separator");
        int i = 0;
        while(true){
            try {
                Socket client = ss.accept();

                String keyForMap = "" + client.getInetAddress() + client.getPort();
                clients.put(keyForMap, client);

                Thread clientReadTread = new Thread(new InputClientMessageThread(client.getInetAddress(),
                        client.getPort(),client.getInputStream(), clients, history, userNames));
                clientReadTread.start();

//                    Thread ReceiveClientMessageThread = new Thread(new OutputClientMessageThread(client.getInetAddress().toString(),
//                            client.getPort(), client.getOutputStream()));
//                    clientWriteThread.start();
//                    i++;

                while(userNames.get(keyForMap) == null){
                    Thread.sleep(500);
                }
                    String message = String.format("ip %s, port %s user Name %s connected to server localhost\n",
                            client.getInetAddress(),
                            client.getPort(),
                            userNames.get(keyForMap));


                    System.out.println(message);
                    writer.write(message + lineSeparator + "\r");
                    writer.flush();

            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}


//class ReceiveClientMessageThread implements Runnable {
//
//    private Map<String, Socket> clients;
//    private Map<String, String> userNames;
//
//    public ReceiveClientMessageThread(Map<String, Socket> clients, Map<String, String> userNames ) {
//        this.clients = clients;
//        this.userNames = userNames;
//    }
//
//    @Override
//    public void run() {
//        while(true){
//            Iterator<Map.Entry<String, Socket>> iterator = clients.entrySet().iterator();
//
//
//            while (iterator.hasNext()){
//                Map.Entry<String, Socket> pair = iterator.next();
//                Message receivedMessage = receiveMessage();
//                receivedMessage.setMessage(userNames.get(pair.getKey()) + " -> " + receivedMessage.getMessage());
//        }
//    }
//}


class InputClientMessageThread implements Runnable {

    private BufferedReader bf;
    private InputStream is;
    private InetAddress ip;
    private int port;
    private Map<String, Socket> clients;
    private PrintWriter pw;
    private File history;
    private String keyForMap;
    private Map<String, String> userNames;
    private String userName = "";
    private String fileName;

    public InputClientMessageThread(InetAddress ip, int port,InputStream inputStream, Map<String, Socket> clients, File history, Map<String, String> userNames) {
        this.ip = ip;
        this.port = port;
        bf = new BufferedReader(new InputStreamReader(inputStream));
        this.clients = clients;
        this.history = history;
        keyForMap = "" + ip + port;
        this.userNames = userNames;
        this.is = inputStream;
        fileName = "../" + ip.toString().substring(1) + port;
    }

    @Override
    public void run() {
        while(userName.equals("")) {
            userName = JOptionPane.showInputDialog("Input your user name plz");
        }


        userNames.put(keyForMap, userName);
        Writer fileWriter = null;
        String lineSeparator = System.getProperty("line.separator");

        try {
            fileWriter = new FileWriter(history, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            try {
                Message receivedMessage = receiveMessage();

                if(receivedMessage != null){
                    Iterator<Map.Entry<String, Socket>> iterator = clients.entrySet().iterator();


                    while (iterator.hasNext()){
                        Map.Entry<String, Socket> pair = iterator.next();
                        receivedMessage.setMessage(userNames.get(keyForMap) + " -> " + receivedMessage.getMessage());
                        if(!keyForMap.equals(pair.getKey()) && pair.getValue().isConnected()) {

                            sendMessage(receivedMessage, pair.getValue());

                            synchronized (this) {
                                fileWriter.write("[" + receivedMessage.getTime() + "] " + receivedMessage.getMessage() + lineSeparator + "\r");
                                fileWriter.flush();
                                notifyAll();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                String message = String.format("ip %s, port %s disconnected from server localhost\n",
                        clients.get(keyForMap).getInetAddress(),
                        clients.get(keyForMap).getPort());
                try {
                    fileWriter.write(message + lineSeparator);
                    fileWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println(message);
                clients.remove(keyForMap);
                userNames.remove(keyForMap);
                break;
            }
        }
    }


    private Message receiveMessage() throws IOException {
        BufferedInputStream bis;
        BufferedOutputStream bos;

        bis = new BufferedInputStream(is);
        bos = new BufferedOutputStream(new FileOutputStream(new File (fileName + "ReceivedMessage")));
        byte[] byteArray = new byte[1024];
        int out;
        while ((out = bis.read(byteArray)) != -1){
            bos.write(byteArray,0,out);
        }
        bis.close();
        bos.close();

        FileInputStream fis = new FileInputStream(fileName + "ReceivedMessage");
        ObjectInputStream oin = new ObjectInputStream(fis);
        Message receivedMessage = null;
        try {
            receivedMessage = (Message)oin.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return  receivedMessage;
    }

    private void sendMessage(Message message, Socket client) throws IOException {
        BufferedInputStream bis;
        BufferedOutputStream bos;

        FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName + "SendMessage"));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        bis = new BufferedInputStream(new FileInputStream(fileName + "SendMessage"));
        bos = new BufferedOutputStream(client.getOutputStream());
        byte[] byteArray = new byte[1024];
        int in;
        while ((in = bis.read(byteArray)) != -1){
            bos.write(byteArray,0,in);
        }
        bis.close();
        bos.close();
    }

}