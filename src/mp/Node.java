package mp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Node {
    NodeEntry self_info;
    NodeEntry client_info;
    Map<Integer, NodeEntry> finger_table;
    NodeEntry predecessor_pointer;
    List<Integer> key_container;
    Unicast u;

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
            }
            Thread.sleep(100);
        }
    }

    public abstract void find(int k);

    public abstract void crash();

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
        //if even the nearest node is greater than id, then this node is the successor
        if(unwrap_id(finger_table.get(0).id)  > unwrap_id(id))
            return finger_table.get(0);
        else {
            //else, find the farthest node less than or equal to id and ask it to search for successor
            for(int i = 0; i < 8; ++i) {
                if(unwrap_id(finger_table.get(i).id) > unwrap_id(id))
                    return ask_find_successor(finger_table.get(i-1), id);
            }
            //if even the farthest node is less than id, ask the farthest to search for successor
            return ask_find_successor(finger_table.get(7), id);
        }
    }

    //find predecessor of id
    public NodeEntry find_predecessor(int id) {
        NodeEntry n_prime = self_info;
//        System.out.println("find_predecessor:"+n_prime.id + " " + unwrap_id_for_other_node(id, n_prime.id) + " " + unwrap_id_for_other_node(ask_read_finger_table(n_prime, 0).id, n_prime.id));
        while(!(n_prime.id < unwrap_id_for_other_node(id, n_prime.id) && unwrap_id_for_other_node(id, n_prime.id) < unwrap_id_for_other_node(ask_read_finger_table(n_prime, 0).id, n_prime.id))) {
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


