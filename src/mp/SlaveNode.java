package mp;
import java.util.ArrayList;
import java.util.List;

public class SlaveNode extends Node {

    public SlaveNode(NodeBuilder builder) {
        this.node_entry = builder.node_entry;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    public static class NodeBuilder {
        private final NodeEntry node_entry;
        private List<NodeEntry> figure_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        private final Unicast u;

        public NodeBuilder(NodeEntry node_entry, Unicast u){
            this.node_entry = node_entry;
            this.u = u;
            this.figure_table = new ArrayList<NodeEntry>();
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
