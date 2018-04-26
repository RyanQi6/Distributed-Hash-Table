package mp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Node {
    NodeEntry self_info;
    NodeEntry client_info;
    Map<Integer, NodeEntry> figure_table;
    NodeEntry predecessor_pointer;
    List<Integer> key_container;
    Unicast u;

    public void alter_figure_table(int index, NodeEntry node_info) {
        figure_table.put(index, node_info);
    };

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

    public abstract void listen() throws InterruptedException, IOException;

    public abstract void find(int k);

    public abstract void crash();

    public void addTestData() {
        alter_figure_table(0,new NodeEntry(self_info.id + 1,"222.222.111.111", 9999));
        alter_figure_table(1,new NodeEntry(self_info.id + 2,"222.222.111.121", 9989));
        alter_figure_table(2,new NodeEntry(self_info.id + 3,"222.222.111.111", 9999));
        alter_figure_table(3,new NodeEntry(self_info.id + 4,"222.222.111.121", 9989));
        alter_figure_table(4,new NodeEntry(self_info.id + 5,"222.222.111.111", 9999));
        alter_figure_table(5,new NodeEntry(self_info.id + 6,"222.222.111.121", 9989));
        alter_figure_table(6,new NodeEntry(self_info.id + 7,"222.222.111.111", 9999));
        alter_figure_table(7,new NodeEntry(self_info.id + 8,"222.222.111.121", 9989));

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
        if(unwrap_id(figure_table.get(0).id)  > unwrap_id(id))
            return figure_table.get(0);
        else {
            for(int i = 0; i < 8; ++i) {
                if(unwrap_id(figure_table.get(i).id) > unwrap_id(id))
                    return ask_find_successor(figure_table.get(i-1), id);
            }
        }
        //should not reach here
        return new NodeEntry(0, "0", 0);
    }

    //ask node to return its predecessor
    public NodeEntry ask_return_predecessor(NodeEntry performer) {
        return new NodeEntry(0, "1",0);
    }

    //ask node to set its predecessor
    public NodeEntry ask_set_predecessor(NodeEntry performer, NodeEntry predecessor) {
        return new NodeEntry(0, "1",0);
    }

    public int get_start(int i) {
        return (self_info.id + (int)Math.pow(2, i))%256;
    }

    public int unwrap_id(int id) {
        return id>self_info.id?id:id+256;
    }
}


