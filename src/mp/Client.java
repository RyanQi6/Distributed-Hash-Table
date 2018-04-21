package mp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    Map<Integer, NodeEntry> figure_table;
    Unicast u;

    public Client(Unicast u) {
        figure_table = new HashMap<>();
        this.u = u;
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
                System.out.println(message);
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
        if(p_info == null)
            System.out.println(p + " does not exist!");
        else {
            u.unicast_send(p_info.address, p_info.port, "show your table");
        }
    }

    //show all
    public void showAll() {}

    public Map<Integer, NodeEntry> getFigure_table() {
        return figure_table;
    }
}