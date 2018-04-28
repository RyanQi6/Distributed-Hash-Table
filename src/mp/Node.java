package mp;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

public abstract class Node {
    public final int send_heartbeat_interval = 10000;  // 10s
    public final int receive_waiting_limit = 30000;    // 30s

    NodeEntry self_info;
    NodeEntry client_info;
    Map<Integer, NodeEntry> finger_table;
    NodeEntry predecessor_pointer;
    List<Integer> key_container;
    Unicast u;

    public boolean successor_alive = true;
    Timer send_timer;
    Timer receive_timer;

    // Failure Detection
    // Timer setter
    public Timer sendHeartbeatTimer(int delay) {
        this.send_timer = new Timer();
        this.send_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                destroySendTimer();
                send_timer = sendHeartbeatTimer(delay);

                try {
//                    System.out.println("predecessor id is: " + predecessor_pointer.id);
                    u.c.startClient(predecessor_pointer.address, predecessor_pointer.port);
                } catch (ConnectException e) {
                    return ;
                } catch(IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    u.c.closeClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                u.unicast_send(predecessor_pointer.address, predecessor_pointer.port, "1||successor is alive");
            }
        }, delay);
        return send_timer;
    }

    public Timer receiveHeartbeatTimer(int delay) {
        this.receive_timer = new Timer();
        this.receive_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                destroyReceiveTimer();
                receive_timer = receiveHeartbeatTimer(delay);

                successor_alive = false;
                u.unicast_send(client_info.address, client_info.port, "2||" + finger_table.get(0).id + "||node is down");

                failureRecovery(finger_table.get(0).id, self_info.id);
            }
        }, delay);
        return receive_timer;
    }

    // Timer destroyer
    public void destroySendTimer(){
        this.send_timer.cancel();
    }
    public void destroyReceiveTimer(){
        this.receive_timer.cancel();
    }

    // When the heartbeat from successor is received, clear the timer, and time again
    public void receivedHeartbeat(){
        successor_alive = true;
        destroyReceiveTimer();
        this.receive_timer = receiveHeartbeatTimer(receive_waiting_limit);
    }

    // Failure recovery: To be implemented
    public void failureRecovery(int failed_node, int predecessor_of_failed_node) {
        if(this.finger_table.get(0).id != failed_node && this.self_info.id == predecessor_of_failed_node){
            return;
        }
//        if(failed_node <= predecessor_pointer.id + 128 && failed_node >= predecessor_pointer.id){
            u.unicast_send(predecessor_pointer.address, predecessor_pointer.port, "6||"+ failed_node + "||" + predecessor_of_failed_node +  "||node is down");
            if(failed_node == predecessor_pointer.id){
//                System.out.println("Modifying the predecessor");
                predecessor_pointer.id = predecessor_of_failed_node;
                predecessor_pointer.port = 3000 + predecessor_of_failed_node;
//                System.out.println("New predecessor id is: " + predecessor_pointer.id);
            }
//        }
        modify_finger_table(failed_node);
    }

    // Figure table modification after the node fails
    // case 1: failed node is not in the figure table, do nothing
    // case 2: failed node is on the last position of the figure table
    // case 3: failed node is not on the last position
    public void modify_finger_table(int failed_node){
        // case 1
        if(failed_node > this.self_info.id + 128 || failed_node < this.self_info.id){
            return;
        }
        // case 2: find the successor, and replace the failed node entries with the successor of the failed node
        if(this.finger_table.get(this.finger_table.size()-1).id == failed_node){
            NodeEntry successor = find_successor(failed_node);
            for(int i=0; i<this.finger_table.size(); ++i){
                if(this.finger_table.get(i).id == failed_node){
                    this.finger_table.put(i, successor);
                }
            }
        }
        // case 3: replace the failed node with the successor of the failed node
        else {
            //find the index of failed node's successor
            int index = -1;
            boolean lock = false;
            for(int i=this.finger_table.size()-1; i>=0; --i){
                if(this.finger_table.get(i).id == failed_node ){
                    if(!lock){
                        index = i+1;
                        lock = true;
                    }
                    this.finger_table.put(i, this.finger_table.get(index));
                }
            }
        }
    }

    public void print_finger_table() {
        System.out.println(finger_table);
    }

    public void alter_finger_table(int index, NodeEntry node_info) {
        finger_table.put(index, node_info);
    };

    public void set_predecessor(NodeEntry predecessor_pointer) {
        this.predecessor_pointer = predecessor_pointer;
    };

    public NodeEntry get_finger_table(int index) {
        return finger_table.get(0);
    }

    public void add_key(int key) {
        key_container.add(key);
    }

    protected void startListen() {
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

    public void listen() throws InterruptedException, IOException {
        String message;
        while(true) {
            if ((message = u.unicast_receive()) != null) {
                System.out.println("    Msg received: " + message);
                String sender_ip = message.substring(0, Utility.nthIndexOf(message, "||", 1));
                Integer sender_port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 1) + 2, Utility.nthIndexOf(message, "||", 2)));
                String command = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 3));
                if(command.equals("ShowYourself")) {
                    String response = "ResponseMyself";
                    response += "||" + self_info.id + "||" + key_container.size();
                    for(int i = 0; i < 8; ++i)
                        response += "||" + finger_table.get(i).id;
                    Integer[] keys = key_container.toArray(new Integer[0]);
                    Arrays.sort(keys);
                    for(int i : keys)
                        response += "||" + i;
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("AskFindSuccessor")) {
                    int id =Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    //start a new thread to deal with the request to avoid blocking the listen function
                    Runnable listener = new Runnable() {
                        @Override
                        public void run() {
                            deal_with_be_AskedFindSuccessor(sender_ip, sender_port, id);
                        }
                    };

                    new Thread(listener).start();
                }
                else if(command.equals("ResponseAskFindSuccessor")) {
                    ask_find_successor_msg = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 6));
                    ask_find_successor_lock = false;
                }
                else if(command.equals("AskReturnPredecessor")) {
                    String response = "ResponseAskReturnPredecessor||" + predecessor_pointer.id + "||" + predecessor_pointer.address + "||" + predecessor_pointer.port;
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("ResponseAskReturnPredecessor")) {
                    ask_return_predecessor_msg = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 6));
                    ask_return_predecessor_lock = false;
                }
                else if(command.equals("AskSetPredecessor")) {
                    int predecessor_id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    String predecessor_address = message.substring(Utility.nthIndexOf(message, "||", 4) + 2, Utility.nthIndexOf(message, "||", 5));
                    int predecessor_port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 5) + 2, Utility.nthIndexOf(message, "||", 6)));
                    NodeEntry predecessor = new NodeEntry(predecessor_id, predecessor_address, predecessor_port);
                    set_predecessor(predecessor);
                    u.unicast_send(sender_ip, sender_port, "ResponseAskSetPredecessor");
                }
                else if(command.equals("ResponseAskSetPredecessor")) {
                    ask_set_predecessor_lock = false;
                }
                else if(command.equals("AskUpdateFingerTable")) {
                    int i = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    int id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 4) + 2, Utility.nthIndexOf(message, "||", 5)));
                    String address = message.substring(Utility.nthIndexOf(message, "||", 5) + 2, Utility.nthIndexOf(message, "||", 6));
                    int port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 6) + 2, Utility.nthIndexOf(message, "||", 7)));;
                    NodeEntry node_info = new NodeEntry(id, address, port);
                    //start a new thread to deal with the request to avoid blocking the listen function
                    Runnable listener = new Runnable() {
                        @Override
                        public void run() {
                            deal_with_be_AskedUpdateFingerTable(sender_ip, sender_port, node_info, i);
                        }
                    };

                    new Thread(listener).start();
                }
                else if(command.equals("ResponseAskUpdateFingerTable")) {
                    ask_update_finger_table_lock = false;
                }
                else if(command.equals("AskReadFingerTable")) {
                    int i = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    NodeEntry node_info = finger_table.get(i);
                    String response = "ResponseAskReadFingerTable||" + node_info.id + "||" + node_info.address + "||" + node_info.port;
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("ResponseAskReadFingerTable")) {
                    ask_read_finger_table_msg = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 6));
                    ask_read_finger_table_lock = false;
                }
                else if(command.equals("AskClosestPrecedingFinger")) {
                    int id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    NodeEntry node_info = closest_preceding_finger(id);
                    String response = "ResponseAskClosestPrecedingFinger||" + node_info.id + "||" + node_info.address + "||" + node_info.port;
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("ResponseAskClosestPrecedingFinger")) {
                    ask_closest_preceding_finger_msg = message.substring(Utility.nthIndexOf(message, "||", 2) + 2, Utility.nthIndexOf(message, "||", 6));
                    ask_closest_preceding_finger_lock = false;
                }
                else if(command.equals("AskAlterFingerTable")) {
                    int i = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    int id = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 4) + 2, Utility.nthIndexOf(message, "||", 5)));
                    String address = message.substring(Utility.nthIndexOf(message, "||", 5) + 2, Utility.nthIndexOf(message, "||", 6));
                    int port = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 6) + 2, Utility.nthIndexOf(message, "||", 7)));;
                    NodeEntry node_info = new NodeEntry(id, address, port);
                    finger_table.put(i, node_info);
                    String response = "ResponseAskAlterFingerTable";
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("ResponseAskAlterFingerTable")) {
                    ask_alter_finger_table_lock = false;
                }
                else if(command.equals("AskTransferKey")) {
                    int minimal = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 3) + 2, Utility.nthIndexOf(message, "||", 4)));
                    int maximum = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 4) + 2, Utility.nthIndexOf(message, "||", 5)));
                    String response = "ResponseAskTransferKey||";
                    String key_list = "";
                    int num_of_keys = 0;
                    for(int i = 0; i < key_container.size(); ++i) {
                        if(minimal < key_container.get(i) && key_container.get(i) <= maximum) {
                            num_of_keys++;
                            key_list += "||" + key_container.get(i);
                            key_container.remove(i);
                            i--;
                        }
                    }
                    response += num_of_keys + key_list;
                    u.unicast_send(sender_ip, sender_port, response);
                }
                else if(command.equals("ResponseAskTransferKey")) {
                    ask_transfer_keys_msg = message.substring(Utility.nthIndexOf(message, "||", 2) + 2);
                    ask_transfer_keys_lock = false;
                }
                else {
                    int firstSplit = Utility.nthIndexOf(message, "||", 1);
                    int secondSplit = Utility.nthIndexOf(message, "||", 2);
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);

                    Integer command_mode = Integer.parseInt(message.substring(secondSplit + 2, thirdSplit));

                    // command mode switch
                    if(command_mode == 1){
                        receivedHeartbeat();
                    } else if(command_mode == 3){
                        int k = Integer.parseInt(message.substring(thirdSplit + 2, message.length()-2));
                        //start a new thread to avoid blocking listen() function
                        Runnable listener = new Runnable() {
                            @Override
                            public void run() {
                                find(k);
                            }
                        };
                        new Thread(listener).start();

                    } else if(command_mode == 6) {
                        int fourthSplit = Utility.nthIndexOf(message, "||", 4);
                        int fifthSplit = Utility.nthIndexOf(message, "||", 5);
                        Integer failed_node = Integer.parseInt(message.substring(thirdSplit + 2, fourthSplit));
                        Integer predecessor_failed_node = Integer.parseInt(message.substring(fourthSplit + 2, fifthSplit));
                        failureRecovery(failed_node, predecessor_failed_node);
                    } else if(command_mode == 7) {
                        crash();
                    }
                }
            }
            Thread.sleep(10);
        }
    }

    // find p k
    public void find(int k){
        if(k < 0 || k > 255){
            u.unicast_send(client_info.address, client_info.port, "5||" + k + "||key k is not found");
            return;
        }

        NodeEntry successor = find_successor(k);
        if(self_info.id == successor.id){
            if(this.key_container.contains(k))
                u.unicast_send(client_info.address, client_info.port, "4||" + this.self_info.id + "||" + k);
            else
                u.unicast_send(client_info.address, client_info.port, "5||" + k + "||key k is not found");
        } else {
            u.unicast_send(successor.address, successor.port, "3||" + k);
        }
    }

    public void crash() {
        System.exit(1);
    }

    public void addTestData() {
        alter_finger_table(0,new NodeEntry(self_info.id + 1,"222.222.111.111", 9999));
        alter_finger_table(1,new NodeEntry(self_info.id + 2,"222.222.111.121", 9989));
        alter_finger_table(2,new NodeEntry(self_info.id + 3,"222.222.111.111", 9999));
        alter_finger_table(3,new NodeEntry(self_info.id + 4,"222.222.111.121", 9989));
        alter_finger_table(4,new NodeEntry(self_info.id + 5,"222.222.111.111", 9999));
        alter_finger_table(5,new NodeEntry(self_info.id + 6,"222.222.111.121", 9989));
        alter_finger_table(6,new NodeEntry(self_info.id + 7,"222.222.111.111", 9999));
        alter_finger_table(7,new NodeEntry(self_info.id + 8,"222.222.111.121", 9989));

        add_key(self_info.id + 1);
        add_key(self_info.id + 2);
        add_key(self_info.id + 3);
        add_key(self_info.id + 4);
        add_key(self_info.id + 5);
        add_key(self_info.id + 6);
    }

    //ask another node to find successor for id
    volatile String ask_find_successor_msg;
    volatile boolean ask_find_successor_lock;
    public NodeEntry ask_find_successor(NodeEntry performer, int id) {
        ask_find_successor_lock = true;
        //msg = "AskFindSuccessor||id"
        u.unicast_send(performer.address, performer.port, "AskFindSuccessor||" + id);
        while(ask_find_successor_lock) {}
        //msg = "ResponseAskFindSuccessor||successor's id||successor's address||successor's port"
        int successor_id = Integer.parseInt(ask_find_successor_msg.substring(Utility.nthIndexOf(ask_find_successor_msg, "||", 1) + 2, Utility.nthIndexOf(ask_find_successor_msg, "||", 2)));
        String successor_address = ask_find_successor_msg.substring(Utility.nthIndexOf(ask_find_successor_msg, "||", 2) + 2, Utility.nthIndexOf(ask_find_successor_msg, "||", 3));
        int successor_port = Integer.parseInt(ask_find_successor_msg.substring(Utility.nthIndexOf(ask_find_successor_msg, "||", 3) + 2));
        NodeEntry successor = new NodeEntry(successor_id, successor_address, successor_port);
        return successor;
    }

    public void deal_with_be_AskedFindSuccessor(String sender_ip, int sender_port, int id) {
        NodeEntry successor = find_successor(id);
        String response = "ResponseAskFindSuccessor||" + successor.id + "||" + successor.address + "||" + successor.port;
        u.unicast_send(sender_ip, sender_port, response);
    }

    //find successor of id
    public NodeEntry find_successor(int id) {
        NodeEntry predecessor = find_predecessor(id);
        return ask_read_finger_table(predecessor, 0);
    }

    //find predecessor of id
    public NodeEntry find_predecessor(int id) {
        NodeEntry n_prime = self_info;
//        System.out.println("find_predecessor:"+n_prime.id + " " + unwrap_id_for_other_node(id, n_prime.id) + " " + unwrap_id_for_other_node(ask_read_finger_table(n_prime, 0).id, n_prime.id));
        while(!(n_prime.id < unwrap_id_for_other_node(id, n_prime.id) && unwrap_id_for_other_node(id, n_prime.id) <= unwrap_id_for_other_node(ask_read_finger_table(n_prime, 0).id, n_prime.id))) {
            n_prime = ask_closest_preceding_finger(n_prime, id);
        }
        return n_prime;
    }

    //ask a node to return it's closest preceding finger for id
    volatile String ask_closest_preceding_finger_msg;
    volatile boolean ask_closest_preceding_finger_lock;
    public NodeEntry ask_closest_preceding_finger(NodeEntry performer, int id) {
        ask_closest_preceding_finger_lock = true;
        //msg = "AskClosestPrecedingFinger||id"
        u.unicast_send(performer.address, performer.port, "AskClosestPrecedingFinger||" + id);
        while(ask_closest_preceding_finger_lock) {}
        //msg = "ResponseAskClosestPrecedingFinger||id||address||port"
        int node_id = Integer.parseInt(ask_closest_preceding_finger_msg.substring(Utility.nthIndexOf(ask_closest_preceding_finger_msg, "||", 1) + 2, Utility.nthIndexOf(ask_closest_preceding_finger_msg, "||", 2)));
        String node_address = ask_closest_preceding_finger_msg.substring(Utility.nthIndexOf(ask_closest_preceding_finger_msg, "||", 2) + 2, Utility.nthIndexOf(ask_closest_preceding_finger_msg, "||", 3));
        int node_port = Integer.parseInt(ask_closest_preceding_finger_msg.substring(Utility.nthIndexOf(ask_closest_preceding_finger_msg, "||", 3) + 2));
        NodeEntry node_info = new NodeEntry(node_id, node_address, node_port);
        return node_info;
    }

    public NodeEntry closest_preceding_finger(int id) {
        for(int i = 7; i >=0; --i) {
            if(self_info.id < unwrap_id(finger_table.get(i).id) && unwrap_id(finger_table.get(i).id) < unwrap_id(id))
                return finger_table.get(i);
        }
        return self_info;
    }

    //ask node to return its predecessor
    volatile String ask_return_predecessor_msg;
    volatile boolean ask_return_predecessor_lock;
    public NodeEntry ask_return_predecessor(NodeEntry performer) {
        ask_return_predecessor_lock = true;
        //msg = "AskReturnPredecessor"
        u.unicast_send(performer.address, performer.port, "AskReturnPredecessor");
        while(ask_return_predecessor_lock) {}
        //msg = "ResponseAskReturnPredecessor||predecessor's id||predecessor's address||predecessor's port"
        int predecessor_id = Integer.parseInt(ask_return_predecessor_msg.substring(Utility.nthIndexOf(ask_return_predecessor_msg, "||", 1) + 2, Utility.nthIndexOf(ask_return_predecessor_msg, "||", 2)));
        String predecessor_address = ask_return_predecessor_msg.substring(Utility.nthIndexOf(ask_return_predecessor_msg, "||", 2) + 2, Utility.nthIndexOf(ask_return_predecessor_msg, "||", 3));
        int predecessor_port = Integer.parseInt(ask_return_predecessor_msg.substring(Utility.nthIndexOf(ask_return_predecessor_msg, "||", 3) + 2));
        NodeEntry predecessor = new NodeEntry(predecessor_id, predecessor_address, predecessor_port);
        return predecessor;
    }

    //ask node to set its predecessor
    volatile boolean ask_set_predecessor_lock;
    public void ask_set_predecessor(NodeEntry performer, NodeEntry predecessor) {
        ask_set_predecessor_lock = true;
        //msg = "AskSetPredecessor||predecessor's id||predecessor's address||predecessor's port"
        u.unicast_send(performer.address, performer.port, "AskSetPredecessor||" + predecessor.id + "||" + predecessor.address + "||" + predecessor.port);
        while (ask_set_predecessor_lock) {}
    }

    //ask node to update its finger table (for join process)
    volatile boolean ask_update_finger_table_lock;
    public void ask_update_finger_table(NodeEntry performer, NodeEntry node_info, int i) {
        ask_update_finger_table_lock = true;
        //msg = "AskUpdateFingerTable||index||id||address||port"
        u.unicast_send(performer.address, performer.port, "AskUpdateFingerTable||" + i + "||" + node_info.id + "||" + node_info.address + "||" + node_info.port);
        while(ask_update_finger_table_lock) {}
    }


    //ask node to return an entry of it's finger table
    volatile String ask_read_finger_table_msg;
    volatile boolean ask_read_finger_table_lock;
    public NodeEntry ask_read_finger_table(NodeEntry performer, int i) {
        ask_read_finger_table_lock = true;
        //msg = "AskReadFingerTable||index"
        u.unicast_send(performer.address, performer.port, "AskReadFingerTable||" + i);
        while (ask_read_finger_table_lock) {
        }
        //msg = "ResponseAskReadFingerTable||id||address||port"
        int id = Integer.parseInt(ask_read_finger_table_msg.substring(Utility.nthIndexOf(ask_read_finger_table_msg, "||", 1) + 2, Utility.nthIndexOf(ask_read_finger_table_msg, "||", 2)));
        String address = ask_read_finger_table_msg.substring(Utility.nthIndexOf(ask_read_finger_table_msg, "||", 2) + 2, Utility.nthIndexOf(ask_read_finger_table_msg, "||", 3));
        int port = Integer.parseInt(ask_read_finger_table_msg.substring(Utility.nthIndexOf(ask_read_finger_table_msg, "||", 3) + 2));
        NodeEntry node_info = new NodeEntry(id, address, port);
        return node_info;
    }

    //ask node to return an entry of it's finger table
    volatile boolean ask_alter_finger_table_lock;
    public void ask_alter_finger_table(NodeEntry performer, int i, NodeEntry node_info) {
        ask_alter_finger_table_lock = true;
        //msg = "AskAlterFingerTable||i||id||address||port"
        u.unicast_send(performer.address, performer.port, "AskAlterFingerTable||" + i + "||" + node_info.id + "||" + node_info.address + "||" + node_info.port);
        while(ask_alter_finger_table_lock) {}
    }

    public void update_finger_table(NodeEntry node_info, int i) {
        if(self_info.id <= unwrap_id(node_info.id) && unwrap_id(node_info.id) < unwrap_id(finger_table.get(i).id)) {
            finger_table.put(i, node_info);
            ask_update_finger_table(predecessor_pointer, node_info, i);
        }
    }

    public void deal_with_be_AskedUpdateFingerTable(String sender_ip, int sender_port, NodeEntry node_info, int i) {
        update_finger_table(node_info, i);
        String response = "ResponseAskUpdateFingerTable";
        u.unicast_send(sender_ip, sender_port, response);
    }

    //ask node to transfer some of its keys to caller
    volatile String ask_transfer_keys_msg;
    volatile boolean ask_transfer_keys_lock;
    public void ask_transfer_keys(NodeEntry performer, int minimal, int maximal) {
        ask_transfer_keys_lock = true;
        //msg = "AskTransferKey||minimal key number (exclusive)||maximum key number (inclusive)"
        u.unicast_send(performer.address, performer.port, "AskTransferKey||" + minimal + "||" + maximal);
        while (ask_transfer_keys_lock) {
        }
        //msg = "ResponseAskTransferKey||number of keys||first key||second key||...||last key||"
        //notice that this msg is slightly different from others that there is "||" in the end
        int num_of_keys =  Integer.parseInt(ask_transfer_keys_msg.substring(Utility.nthIndexOf(ask_transfer_keys_msg, "||", 1) + 2, Utility.nthIndexOf(ask_transfer_keys_msg, "||", 2)));
        for(int i = 0; i < num_of_keys; ++i) {
            int key = Integer.parseInt(ask_transfer_keys_msg.substring(Utility.nthIndexOf(ask_transfer_keys_msg, "||", 2 + i) + 2, Utility.nthIndexOf(ask_transfer_keys_msg, "||", 2 + i + 1)));
            key_container.add(key);
        }
    }

    public int get_start(int i) {
        return (self_info.id + (int)Math.pow(2, i))%256;
    }

    public int unwrap_id(int id_to_unwrap) {
        return id_to_unwrap>self_info.id?id_to_unwrap:id_to_unwrap+256;
    }

    public int unwrap_id_for_other_node(int id_to_unwrap, int other_node_id) {
        return id_to_unwrap>other_node_id?id_to_unwrap:id_to_unwrap+256;
    }
}


