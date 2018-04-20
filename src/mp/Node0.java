package mp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Node0 extends Node {
    // builder pattern
    Config hostInfo;
    Unicast u;
    private Node0(NodeBuilder builder) throws IOException{
        hostInfo= Config.parseConfig("configFile");
        u = new Unicast(builder.node_entry.address, node_entry.port, hostInfo);
        this.node_entry = builder.node_entry;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    private static class NodeBuilder{
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
        public Node0 build() throws IOException{
            return new Node0(this);
        }
    }
    // failure detector

    //communicate with client

    //initalize figure table

    // join p
    @ Override
    public  void join(){};

    // find p k
    @ Override
    public  void find(int k){};

    // crash p
    @ Override
    public void crash(){};

    // show p
    @ Override
    public void show(){};

    public static void main(String[] args) throws IOException{
        NodeEntry node_entry = new NodeEntry(0, "127.0.0.1", 3002);
        List<NodeEntry> figure_table = new ArrayList<>();
        figure_table.add(node_entry);
        Node0 node0 = new NodeBuilder(node_entry).figureTable(figure_table).build();
    }
}
