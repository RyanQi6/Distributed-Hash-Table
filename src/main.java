import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args[0].equals("master") && args.length == 5) {
            //code for client and master node

            int id = Integer.parseInt(args[1]);
            String address = args[2];
            int client_port = Integer.parseInt(args[3]);
            int node_port = Integer.parseInt(args[4]);

            //create master node
            NodeEntry node_entry = new NodeEntry(id, address, node_port);
            Unicast u = new Unicast(address, node_port, Config.parseConfig("configFile"));
            MasterNode masterNode = new MasterNode.NodeBuilder(node_entry, u).build();

            //create client
            Client client = new Client(new Unicast(address, client_port, Config.parseConfig("configFile")));

            //wait for and deal with commands
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

        } else if(args[0].equals("slave") && args.length == 4) {
            //code for slave nodes.

            int id = Integer.parseInt(args[1]);
            String address = args[2];
            int port = Integer.parseInt(args[3]);
            Unicast u = new Unicast(address, port, Config.parseConfig("configFile"));

            //create slave node
            SlaveNode n = new SlaveNode.NodeBuilder(new NodeEntry(id, address, port), u).build();

            //keep the program running
            while (true) {
                Thread.sleep(1000);
            }

        } else {
            System.out.println("Invalid command.");
            System.exit(0);
        }

    }
}