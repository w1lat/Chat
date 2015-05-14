package javaEE.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Таня on 11.05.2015.
 */
public class AsynchronClientSocket {

    public static void main(String[] args) throws IOException {
        final Socket socket = new Socket("localhost", 8888);
        //client read message
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    Scanner console = new Scanner(System.in);
                    while(true){
                        String message = console.nextLine();
                        printWriter.println(message);
                        printWriter.flush();
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
                    while(true){
                        String message = bufferedReader.readLine();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
