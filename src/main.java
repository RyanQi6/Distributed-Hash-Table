import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    //java main master address node_port client_port
    //java main slave id address node_port client_port master_port
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args[0].equals("master") && args.length == 4) {
            //code for client and master node

            String address = args[1];
            int node_port = Integer.parseInt(args[2]);
            int client_port = Integer.parseInt(args[3]);

            //create client
            Client client = new Client(new Unicast(address, client_port, Config.parseConfig("configFile")));
            System.out.println("Client starts running!");

            //create master node
            NodeEntry node_entry = new NodeEntry(0, address, node_port);
            NodeEntry client_info = new NodeEntry(-1, address, client_port);
            Unicast u = new Unicast(address, node_port, Config.parseConfig("configFile"));
            MasterNode masterNode = new MasterNode.NodeBuilder(node_entry, client_info, u).build();
            client.alterFingerTable(0, node_entry);
            System.out.println("Master node starts running!");

            masterNode.addTestData();

            //wait for and deal with commands
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String s = br.readLine();
                String[] strings = s.split(" ");
                if(s.equals("join")) {
                    client.join(9);
                    client.join(6);
                    client.join(5);
                    client.join(8);
                    client.join(7);
                    client.join(4);
                    client.join(2);
                    client.join(1);
                    client.join(3);
                } else if(s.equals("ft")) {
                    client.printFigureTable();
                } else if(strings[0].equals("show") && strings.length == 2) {
                    if(strings[1].equals("all")) {
                        client.showAll();
                    }
                    else
                        client.show(Integer.parseInt(strings[1]));
                } else if(s.equals("test")) {
//                    client.u.unicast_send("127.0.0.1", 3000, "TESTINFO");
                }
                else
                    System.out.println("Invalid command.");
            }

        } else if(args[0].equals("slave") && args.length == 6) {
            //code for slave nodes.

            int id = Integer.parseInt(args[1]);
            String address = args[2];
            int node_port = Integer.parseInt(args[3]);
            int client_port = Integer.parseInt(args[4]);
            int master_port =  Integer.parseInt(args[5]);

            //create slave node
            NodeEntry client_info = new NodeEntry(-1, address, client_port);
            NodeEntry master_info = new NodeEntry(0, address, master_port);
            Unicast u = new Unicast(address, node_port, Config.parseConfig("configFile"));
            SlaveNode n = new SlaveNode.NodeBuilder(new NodeEntry(id, address, node_port), client_info, master_info, u).build();

            n.addTestData();

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