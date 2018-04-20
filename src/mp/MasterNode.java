package mp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MasterNode extends Node {
    // builder pattern
    Config hostInfo;
    Unicast u;
    public MasterNode(NodeBuilder builder) throws IOException {
        hostInfo= Config.parseConfig("configFile");
        u = new Unicast(builder.node_entry.address, node_entry.port, hostInfo);
        this.node_entry = builder.node_entry;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    public static class NodeBuilder {
        private final NodeEntry node_entry;
        private  List<NodeEntry> figure_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        public NodeBuilder(NodeEntry node_entry){
            this.node_entry = node_entry;
        }
        public NodeBuilder figureTable(List<NodeEntry> figure_table){
            this.figure_table = figure_table;
            return  this;
        }
        public NodeBuilder predecessorPointer(NodeEntry predecessor_pointer){
            this.predecessor_pointer = predecessor_pointer;
            return this;
        }
        public NodeBuilder keyContainer(List<Integer> key_container){
            this.key_container = key_container;
            return this;
        }
        public MasterNode build() throws IOException{
            return new MasterNode(this);
        }
    }
    // failure detector

    //communicate with client

    //initalize figure table

    // join p
    @ Override
    public void join() {};

    // find p k
    @ Override
    public void find(int k) {};

    // crash p
    @ Override
    public void crash() {};

    // show p
    @ Override
    public void show() {};

    public static void main(String[] args) throws IOException {
        NodeEntry node_entry = new NodeEntry(0, "127.0.0.1", 3000);
        List<NodeEntry> figure_table = new ArrayList<>();
        for(int i = 0; i < 8; ++i)
            figure_table.add(node_entry);
        MasterNode masterNode = new NodeBuilder(node_entry).figureTable(figure_table).build();
    }
}
