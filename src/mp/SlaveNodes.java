package mp;
import java.util.List;

public class SlaveNodes extends Node{
    private SlaveNodes(NodeBuilder builder){
        this.node_entry = builder.node_entry;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    private static class NodeBuilder{
        private final NodeEntry node_entry;
        private List<NodeEntry> figure_table;
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
        public SlaveNodes build(){
            return new SlaveNodes(this);
        }
    }
    // send heartbeat

    //communicate with client

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

    public static void main(String[] args){

    }
}
