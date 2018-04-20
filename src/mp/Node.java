package mp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public abstract class Node{
    NodeEntry client_info;
    NodeEntry node_entry;
    List<NodeEntry> figure_table;
    NodeEntry predecessor_pointer;
    List<Integer> key_container;

    Timer send_timer;
    Timer receive_timer;

    public abstract void join();

    public abstract void find(int k);

    public abstract void crash();

    public abstract void show();

}


