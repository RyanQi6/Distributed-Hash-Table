package mp;
import java.io.IOException;
import java.util.*;

public class SlaveNode extends Node {

    public SlaveNode(NodeBuilder builder) {
        this.node_entry = builder.node_entry;
        this.client_info = builder.client_info;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
        this.u = builder.u;
        startListen();
    }

    public static class NodeBuilder {
        private final NodeEntry node_entry;
        private final NodeEntry client_info;
        private Map<Integer, NodeEntry> figure_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        private final Unicast u;

        public NodeBuilder(NodeEntry node_entry, NodeEntry client_info, Unicast u){
            this.node_entry = node_entry;
            this.client_info = client_info;
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
                u.unicast_send(client_info.address, client_info.port, message);
            }
            Thread.sleep(1000);
        }
    }

    // send heartbeat

    //communicate with client

    // join p
    @ Override
    public void join() {};

    // find p k
    @ Override
    public  void find(int k) {};

    // crash p
    @ Override
    public void crash() {};

    // show p
    @ Override
    public void show() {};

    public static void main(String[] args) {

    }
}
