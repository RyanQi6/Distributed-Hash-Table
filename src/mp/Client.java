package mp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    Map<Integer, NodeEntry> figure_table;
    Unicast u;

    volatile boolean show_all_lock;

    public Client(Unicast u) {
        figure_table = new HashMap<>();
        this.u = u;
        show_all_lock = false;
        startListen();
    }

    private void startListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(listener).start();
    }

    private void listen() throws InterruptedException, IOException {
        String message;
        while(true) {
            if ((message = u.unicast_receive()) != null) {
                String sender_ip = message.substring(0, Utility.nthIndexOf(message, "||", 1));
                Integer sender_port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 1) + 2, Utility.nthIndexOf(message, "||", 2)));

                String command = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 3));

                if(command.equals("ResponseFigureTable")) {
                    String output = "";
                    int node_id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    output += node_id + "\n" + "FingerTable: ";
                    for(int i = 0; i < 8; ++i) {
                        String temp = message.substring(Utility.nthIndexOf(message, "||", 4+i) + 2, Utility.nthIndexOf(message, "||", 4+i+1));
                        output += temp;
                        if(i != 7)
                            output += ", ";
                    }
                    output += "\n" + "Keys: ";
                    System.out.println(output);
                    show_all_lock = false;
                }
            }
            Thread.sleep(1000);
        }
    }

    // key board command

    //communicate with ndoes

    // join
    public void join(int p) throws IOException {
        String currentPath = System.getProperty("user.dir");
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", currentPath, "main", "slave", Integer.toString(p), "127.0.0.1", Integer.toString(3000+p), Integer.toString(u.port));
        Process pr = pb.start();
        figure_table.put(p, new NodeEntry(p, "127.0.0.1", 3000 + p));
        System.out.println("Created node " + p);
    }

    // find p k
    public void find(int p, int k) {}

    // crash p
    public void crash(int p) {}

    // show p
    public void show(int p) throws IOException, InterruptedException {
        NodeEntry p_info = figure_table.get(p);
        if(p_info == null) {
            System.out.println(p + " does not exist!");
            show_all_lock = false;
        }
        else {
            u.unicast_send(p_info.address, p_info.port, "ShowFigureTable");
        }
    }

    //show all
    public void showAll() throws IOException, InterruptedException {
        for(int i = 1; i < 4; ++i) {
            while (show_all_lock) { }
            show(i);
            show_all_lock = true;
        }
    }

    public Map<Integer, NodeEntry> getFigure_table() {
        return figure_table;
    }
}