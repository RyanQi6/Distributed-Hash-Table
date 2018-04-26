package mp;
import java.io.IOException;
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

    public void add_figure_table(int index, NodeEntry node_entry) {
        figure_table.put(index, node_entry);
    };

    public void add_key(int key) {
        key_container.add(key);
    }

    protected void startListen() {
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

    public abstract void listen() throws InterruptedException, IOException;

    public abstract void join();

    public abstract void find(int k);

    public abstract void crash();

    public abstract void show();

    public void addTestData() {
        add_figure_table(0,new NodeEntry(node_entry.id + 1,"222.222.111.111", 9999));
        add_figure_table(1,new NodeEntry(node_entry.id + 2,"222.222.111.121", 9989));
        add_figure_table(2,new NodeEntry(node_entry.id + 3,"222.222.111.111", 9999));
        add_figure_table(3,new NodeEntry(node_entry.id + 4,"222.222.111.121", 9989));
        add_figure_table(4,new NodeEntry(node_entry.id + 5,"222.222.111.111", 9999));
        add_figure_table(5,new NodeEntry(node_entry.id + 6,"222.222.111.121", 9989));
        add_figure_table(6,new NodeEntry(node_entry.id + 7,"222.222.111.111", 9999));
        add_figure_table(7,new NodeEntry(node_entry.id + 8,"222.222.111.121", 9989));

        add_key(node_entry.id + 1);
        add_key(node_entry.id + 2);
        add_key(node_entry.id + 3);
        add_key(node_entry.id + 4);
        add_key(node_entry.id + 5);
        add_key(node_entry.id + 6);
    }

}


