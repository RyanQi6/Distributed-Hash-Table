import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    //java main master/slave id address node_port client_port
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args[0].equals("master") && args.length == 5) {
            //code for client and master node

            int id = Integer.parseInt(args[1]);
            String address = args[2];
            int node_port = Integer.parseInt(args[3]);
            int client_port = Integer.parseInt(args[4]);

            //create client
            Client client = new Client(new Unicast(address, client_port, Config.parseConfig("configFile")));

            //create master node
            NodeEntry node_entry = new NodeEntry(id, address, node_port);
            NodeEntry client_info = new NodeEntry(-1, address, client_port);
            Unicast u = new Unicast(address, node_port, Config.parseConfig("configFile"));
            MasterNode masterNode = new MasterNode.NodeBuilder(node_entry, client_info, u).build();

            //wait for and deal with commands
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String s = br.readLine();
                String[] strings = s.split(" ");
                if(s.equals("join")) {
                    client.join(1);
                    client.join(2);
                    client.join(3);
                } else if(s.equals("ft")) {
                    System.out.println(client.getFigure_table());
                } else if(strings[0].equals("show") && strings.length == 2) {
                    if(strings[1].equals("all")) {
                    }
                    else
                        client.show(Integer.parseInt(strings[1]));
                }
                else
                    System.out.println("Invalid command.");
            }

        } else if(args[0].equals("slave") && args.length == 5) {
            //code for slave nodes.

            int id = Integer.parseInt(args[1]);
            String address = args[2];
            int node_port = Integer.parseInt(args[3]);
            int client_port = Integer.parseInt(args[4]);

            //create slave node
            NodeEntry client_info = new NodeEntry(-1, address, client_port);
            Unicast u = new Unicast(address, node_port, Config.parseConfig("configFile"));
            SlaveNode n = new SlaveNode.NodeBuilder(new NodeEntry(id, address, node_port), client_info, u).build();

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