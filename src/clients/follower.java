package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class follower {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);
            BufferedReader in
                    = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            String line = null;
            System.out.print("Entrez un ou plusieurs identifiants séparés par des virgules, exemple : jean,kevin,ange");

            String user = scanner.nextLine();
            String[] splitted = user.split(",");
            for(String user_name : splitted){
                out.println("RCV_IDS author:@" + user_name + "\\r\\n\\r\\n");
                out.flush();
                String response = in.readLine();
                List<String> message_ids = Arrays.asList(response.split(","));
                for(int message_id : message_ids.stream().map(Integer::parseInt).collect(Collectors.toList())){
                    out.println("RCV_MSG msg_id:" + message_id + "\\r\\n\\r\\n");
                    out.flush();
                    System.out.println(in.readLine());
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


