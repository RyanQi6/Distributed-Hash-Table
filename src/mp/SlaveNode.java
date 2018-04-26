package mp;
import java.io.IOException;
import java.util.*;

public class SlaveNode extends Node {

    NodeEntry master_info;

    public SlaveNode(NodeBuilder builder) {
        this.self_info = builder.self_info;
        this.client_info = builder.client_info;
        this.master_info = builder.master_info;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
        this.u = builder.u;
        startListen();
    }

    public static class NodeBuilder {
        private final NodeEntry self_info;
        private final NodeEntry client_info;
        private final NodeEntry master_info;
        private Map<Integer, NodeEntry> figure_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        private final Unicast u;

        public NodeBuilder(NodeEntry self_info, NodeEntry client_info, NodeEntry master_into, Unicast u){
            this.self_info = self_info;
            this.client_info = client_info;
            this.master_info = master_into;
            this.u = u;
            this.figure_table = new HashMap<Integer, NodeEntry>();
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
                        response += "||" + figure_table.get(i).id;
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
            }
            Thread.sleep(100);
        }
    }

    // send heartbeat

    //communicate with client

    // join itself to the network
    public void join() {

    }

    public void init_fingure_table() {
        figure_table.put(0, ask_find_successor(master_info, get_start(0)));
        predecessor_pointer = ask_return_predecessor(figure_table.get(0));
        ask_set_predecessor(figure_table.get(0), self_info);
        for(int i = 0; i <= 6; ++i) {
            if(unwrap_id(get_start(i+1)) <= unwrap_id(figure_table.get(i).id))
                figure_table.put(i+1, figure_table.get(i));
            else
                figure_table.put(i+1, ask_find_successor(master_info, get_start(i+1)));
        }
    }

    // find p k
    @ Override
    public void find(int k) {

    };

    // crash p
    @ Override
    public void crash() {};

    public static void main(String[] args) {

    }
}
