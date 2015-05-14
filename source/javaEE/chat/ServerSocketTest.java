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

        ServerSocket ss = new ServerSocket(8888);
        Writer writer = new FileWriter(history);
        String lineSeparator = System.getProperty("line.separator");
        while(true){
            try {
                Socket client = ss.accept();

                String keyForMap = "" + client.getInetAddress() + client.getPort();
                clients.put(keyForMap, client);

                Thread clientReadTread = new Thread(new InputClientMessageThread(client.getInetAddress(),
                        client.getPort(),client.getInputStream(), clients, history, userNames));
                clientReadTread.start();

//                Thread clientWriteThread = new Thread(new OutputClientMessageThread(client.getInetAddress().toString(),
//                        client.getPort(),client.getOutputStream()));
//                clientWriteThread.start();

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


//class OutputClientMessageThread implements Runnable {
//
//    private PrintWriter pw;
//    private String ip;
//    private int port;
//
//    public OutputClientMessageThread(String ip, int port,OutputStream outputStream) {
//        this.ip = ip;
//        this.port = port;
//        pw = new PrintWriter(outputStream);
//    }
//
//    @Override
//    public void run() {
//        /*while(true){
//                pw.println("Hello from server time is " + new Date().toString());
//                pw.flush();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }*/
//    }
//}

class InputClientMessageThread implements Runnable {

    private BufferedReader bf;
    private InetAddress ip;
    private int port;
    private Map<String, Socket> clients;
    private PrintWriter pw;
    private File history;
    private String keyForMap;
    private Map<String, String> userNames;
    private String userName = "";

    public InputClientMessageThread(InetAddress ip, int port,InputStream inputStream, Map<String, Socket> clients, File history, Map<String, String> userNames) {
        this.ip = ip;
        this.port = port;
        bf = new BufferedReader(new InputStreamReader(inputStream));
        this.clients = clients;
        this.history = history;
        keyForMap = "" + ip + port;
        this.userNames = userNames;
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
                String s = bf.readLine();
                if(s != null){
                    Iterator<Map.Entry<String, Socket>> iterator = clients.entrySet().iterator();


                    while (iterator.hasNext()){
                        Map.Entry<String, Socket> pair = iterator.next();
                        if(!keyForMap.equals(pair.getKey())) {
                            String out = String.format(userNames.get(keyForMap) + " -> " + s);
                            if(pair.getValue().isConnected()) {
                                pw = new PrintWriter(pair.getValue().getOutputStream());
                                pw.println(out);
                                pw.flush();
                            }
                            synchronized (this) {
                                fileWriter.write(out + lineSeparator + "\r");
                                fileWriter.flush();
                                notifyAll();
                            }
                        }
                    }
                }
            } catch (IOException e) {
//                e.printStackTrace();
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
}