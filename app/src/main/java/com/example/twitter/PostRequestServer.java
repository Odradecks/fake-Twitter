package com.example.twitter;
import java.io.*;
import java.net.*;

public class PostRequestServer {
    public static void main(String[] args) {
        try {
            // 绑定到服务器的 IP 地址 150.158.15.33 和端口 8080
            InetAddress serverAddress = InetAddress.getByName("150.158.15.33");
            ServerSocket serverSocket = new ServerSocket(8080, 50, serverAddress);

            System.out.println("Server started on 150.158.15.33:8080");

            while (true) {
                // 接收来自客户端的连接
                Socket socket = serverSocket.accept();

                // 创建输入和输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                // 读取 POST 请求数据
                String line;
                StringBuilder requestData = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    requestData.append(line).append("\n");
                }

                // 输出请求数据
                System.out.println("Received POST data: \n" + requestData);

                // 响应客户端
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: text/plain");
                writer.println();
                writer.println("Data received successfully.");

                // 关闭连接
                writer.close();
                reader.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
