package mp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Node {
    NodeEntry node_entry;
    NodeEntry client_info;
    Map<Integer, NodeEntry> figure_table;
    NodeEntry predecessor_pointer;
    List<Integer> key_container;
    Unicast u;

    public abstract void join();

    public abstract void find(int k);

    public abstract void crash();

    public abstract void show();

}


