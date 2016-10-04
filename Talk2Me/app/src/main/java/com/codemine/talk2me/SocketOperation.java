package com.codemine.talk2me;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketOperation implements Runnable{

    private String msg;
    private StringBuilder dealResult;

    public SocketOperation(String msg, StringBuilder dealResult) {
        this.msg = msg;
        this.dealResult = dealResult;
    }

    public String sendMsg() throws IOException {
        Socket socket = new Socket("192.168.31.132", 2333);
        String ipAddress = socket.getInetAddress().toString();
        BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bfw.write(msg + "\n");
        bfw.flush();
        dealResult.append(bfr.readLine());
        return dealResult.toString();
    }

    @Override
    public void run() {
        try {
            sendMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
