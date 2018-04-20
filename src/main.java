import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client(new Unicast("127.0.0.1", 2999, Config.parseConfig("configFile")));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String s = br.readLine();
            if(s.equals("join")) {
                client.join(1);
                client.join(2);
                client.join(3);
            }
            else
                System.out.println("Invalid command.");
        }
    }
}