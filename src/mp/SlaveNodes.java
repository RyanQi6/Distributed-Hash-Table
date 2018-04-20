import java.util.List;

public class SlaveNodes extends Node{
    private SlaveNodes(NodeBuilder builder){
        this.id = builder.id;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    private static class NodeBuilder{
        private final int id;
        private FigureTable figure_table;
        private Node predecessor_pointer;
        private List<Integer> key_container;

        public NodeBuilder(int id){
            this.id = id;
        }
        public NodeBuilder figureTable(FigureTable figure_table){
            this.figure_table = figure_table;
            return  this;
        }
        public NodeBuilder predecessorPointer(Node predecessor_pointer){
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
    public  void join(Node p){};

    // find p k
    @ Override
    public  void find(Node p, int k){};

    // crash p
    @ Override
    public void crash(Node p){};

    // show p
    @ Override
    public void show(Node p){};

    //show all
    @ Override
    public void showAll(){};
}
