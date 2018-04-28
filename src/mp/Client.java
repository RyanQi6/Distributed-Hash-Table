package mp;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Client {
    Map<Integer, NodeEntry> finger_table;
    Unicast u;

    volatile boolean show_all_lock;

    public Client(Unicast u) {
        finger_table = new HashMap<>();
        this.u = u;
        show_all_lock = false;
        startListen();
    }

    public void alterFingerTable(int id, NodeEntry info) {
        finger_table.put(id, info);
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
                System.out.println("    Msg received: " + message);
                String sender_ip = message.substring(0, Utility.nthIndexOf(message, "||", 1));
                Integer sender_port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 1) + 2, Utility.nthIndexOf(message, "||", 2)));

                String command = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 3));

                if(command.equals("ResponseMyself")) {
                    String output = "";
                    int node_id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    int numKeys = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 4) + 2, Utility.nthIndexOf(message, "||", 5)));

                    output += node_id + "\n" + "FingerTable: ";
                    for(int i = 0; i < 8; ++i) {
                        String temp = message.substring(Utility.nthIndexOf(message, "||", 5+i) + 2, Utility.nthIndexOf(message, "||", 5+i+1));
                        output += temp;
                        if(i != 7)
                            output += ", ";
                    }

                    output += "\n" + "Keys: ";
                    for(int i = 0; i < numKeys; ++i) {
                        String key = message.substring(Utility.nthIndexOf(message, "||", 13+i) + 2, Utility.nthIndexOf(message, "||", 13+i+1));
                        output += key + " ";
                    }

                    System.out.println("=============================================================");
                    System.out.println("*************************************************************");
                    System.out.println(output);
                    System.out.println("*************************************************************");
                    System.out.println("=============================================================");
                    show_all_lock = false;
                } else if(command.equals("2")){
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);
                    int fourthSplit = Utility.nthIndexOf(message,"||", 4);
                    Integer failed_node = Integer.parseInt(message.substring(thirdSplit + 2, fourthSplit));
                    if(this.finger_table.remove(failed_node) != null){
                        System.out.println("Node " + failed_node + " is down!");
                    }
                } else if(command.equals("5")){
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);
                    int fourthSplit = Utility.nthIndexOf(message,"||", 4);
                    int k = Integer.parseInt(message.substring(thirdSplit + 2, fourthSplit));
                    System.out.println("Key " + k + " not found.");
                } else if(command.equals("4")){
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);
                    int fourthSplit = Utility.nthIndexOf(message,"||", 4);
                    int node_id = Integer.parseInt(message.substring(thirdSplit + 2, fourthSplit));
                    int k = Integer.parseInt(message.substring(fourthSplit + 2, message.length()-2));
                    System.out.println("Key " + k + " is in node " + node_id);
                }
            }
            Thread.sleep(1);
        }
    }

    // key board command

    //communicate with ndoes

    // join
    public void join(int p) throws IOException {
        String currentPath = System.getProperty("user.dir");
        ProcessBuilder pb = new ProcessBuilder( currentPath + "/main", Integer.toString(p));
        Process pr = pb.start();
        finger_table.put(p, new NodeEntry(p, "127.0.0.1", 3000 + p));
        System.out.println("Created node " + p);
    }

    public void jtest(int p) throws IOException {
        finger_table.put(p, new NodeEntry(p, "127.0.0.1", 3000 + p));
        System.out.println("Created node " + p);
    }

    // find p k
    public void find(int p, int k) {
        if(finger_table.containsKey(p))
            u.unicast_send(this.finger_table.get(p).address, this.finger_table.get(p).port, "3||" + k);
        else
            System.out.println("Node " + p + " does not exist.");
    }

    // crash p
    public void crash(int p) {
        u.unicast_send(this.finger_table.get(p).address, this.finger_table.get(p).port, "7||" + p);
    }

    // show p
    public void show(int p) throws IOException, InterruptedException {
        NodeEntry p_info = finger_table.get(p);
        if(p_info == null) {
            System.out.println(p + " does not exist!");
            show_all_lock = false;
        }
        else {
            u.unicast_send(p_info.address, p_info.port, "ShowYourself");
        }
    }

    //show all
    public void showAll() throws IOException, InterruptedException {
        for(Integer i: getIdList()) {
            while (show_all_lock) { }
            show(i);
            show_all_lock = true;
        }
    }

    public void printFingerTable() {
        System.out.println(finger_table);
    }

    protected Integer[] getIdList() {
        Set<Integer> idSet = finger_table.keySet();
        Integer[] idArray = idSet.toArray(new Integer[0]);
        Arrays.sort(idArray);
        return idArray;
    }
}