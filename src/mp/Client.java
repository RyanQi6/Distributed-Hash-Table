package mp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    Map<Integer, NodeEntry> figure_table;
    Unicast u;

    public Client(Unicast u) {
        figure_table = new HashMap<>();
        this.u = u;
    }

    // key board command

    //communicate with ndoes

    // join
    public void join(int p) throws IOException {
//        Unicast u = new Unicast("127.0.0.1", 3000 + p, Config.parseConfig("configFile"));
//        SlaveNode n = new SlaveNode.NodeBuilder(new NodeEntry(1, u.address, u.port), u).build();
        figure_table.put(p, new NodeEntry(p, "127.0.0.1", 3000 + p));
    }

    // find p k
    public void find(int p, int k) {}

    // crash p
    public void crash(int p) {}

    // show p
    public void show(int p) {}

    //show all
    public void showAll() {}
}