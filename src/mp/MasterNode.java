package mp;
import java.io.IOException;
import java.util.*;

public class MasterNode extends Node {
    // builder pattern
    public MasterNode(NodeBuilder builder) throws IOException {
        this.self_info = builder.self_info;
        this.client_info = builder.client_info;
        this.finger_table = builder.finger_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
        this.u = builder.u;
        for(int i = 0; i < 8; ++i)
            this.finger_table.put(i, self_info);
        startListen();
    }

    public static class NodeBuilder {
        private final NodeEntry self_info;
        private final NodeEntry client_info;
        private Map<Integer, NodeEntry> finger_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;
        private final Unicast u;

        public NodeBuilder(NodeEntry self_info, NodeEntry client_info, Unicast u) {
            this.self_info = self_info;
            this.client_info = client_info;
            this.u = u;
            this.finger_table = new HashMap<Integer, NodeEntry>();
            this.key_container = new ArrayList<Integer>();
        }
        public NodeBuilder predecessorPointer(NodeEntry predecessor_pointer) {
            this.predecessor_pointer = predecessor_pointer;
            return this;
        }
        public MasterNode build() throws IOException {
            return new MasterNode(this);
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
            }
            Thread.sleep(100);
        }
    }

    // failure detector

    //communicate with client

    //initalize finger table


    // find p k
    @ Override
    public void find(int k) {

    };

    // crash p
    @ Override
    public void crash() {};
}
