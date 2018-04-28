package mp;
import java.io.IOException;
import java.util.*;

public class SlaveNode extends Node {

    NodeEntry master_info;

    public SlaveNode(NodeBuilder builder) {
        this.self_info = builder.self_info;
        this.client_info = builder.client_info;
        this.master_info = builder.master_info;
        this.finger_table = builder.finger_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
        this.u = builder.u;
        startListen();
//        sendHeartbeatTimer(5000);
//        receiveHeartbeatTimer(10000);
//        join();
    }

    public static class NodeBuilder {
        private final NodeEntry self_info;
        private final NodeEntry client_info;
        private final NodeEntry master_info;
        private Map<Integer, NodeEntry> finger_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        private final Unicast u;

        public NodeBuilder(NodeEntry self_info, NodeEntry client_info, NodeEntry master_into, Unicast u){
            this.self_info = self_info;
            this.client_info = client_info;
            this.master_info = master_into;
            this.u = u;
            this.finger_table = new HashMap<Integer, NodeEntry>();
            this.key_container = new ArrayList<Integer>();
        }
        public NodeBuilder predecessorPointer(NodeEntry predecessor_pointer){
            this.predecessor_pointer = predecessor_pointer;
            return this;
        }
        public SlaveNode build(){
            return new SlaveNode(this);
        }
    }



    // send heartbeat

    //communicate with client

    // join itself to the network
    public void join() {
        init_fingure_table();
        update_others();
        ask_transfer_keys(finger_table.get(0), predecessor_pointer.id, self_info.id);
    }

    public void init_fingure_table() {
        finger_table.put(0, ask_find_successor(master_info, get_start(0)));
        predecessor_pointer = ask_return_predecessor(finger_table.get(0));
        ask_set_predecessor(finger_table.get(0), self_info);
        ask_alter_finger_table(predecessor_pointer, 0, self_info);
        for(int i = 0; i <= 6; ++i) {
            if(unwrap_id(get_start(i+1)) <= unwrap_id(finger_table.get(i).id))
                finger_table.put(i+1, finger_table.get(i));
            else
                finger_table.put(i+1, ask_find_successor(master_info, get_start(i+1)));
        }
    }

    public void update_others() {
        for(int i = 0; i < 8; ++i) {
            int possible_id_to_update = mod((self_info.id - (int)Math.pow(2, i)), 256);
            System.out.println("Possible id to update: " + possible_id_to_update);
            NodeEntry successor = find_successor(possible_id_to_update);
            NodeEntry node_to_update;
            if(successor.id == possible_id_to_update)
                node_to_update = successor;
            else
                node_to_update = find_predecessor(possible_id_to_update);
            System.out.println("Node preceding this id: " + node_to_update.id);
            ask_update_finger_table(node_to_update, self_info, i);
        }
    }

    public int mod(int a, int b)
    {
        int r = a % b;
        return r < 0 ? r + b : r;
    }

    public static void main(String[] args) {

    }
}
