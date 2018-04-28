package mp;
import java.io.IOException;
import java.util.*;

public class MasterNode extends Node {
    // builder pattern
    public MasterNode(NodeBuilder builder) throws IOException {
        super();
        this.self_info = builder.self_info;
        this.client_info = builder.client_info;
        this.finger_table = builder.finger_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
        this.u = builder.u;
        for(int i = 0; i < 8; ++i)
            this.finger_table.put(i, self_info);
        predecessor_pointer = self_info;
        //initially put all keys in master node
        for(int i = 0; i < 256; ++i) {
            this.key_container.add(i);
        }

        startListen();
    }

    public static class NodeBuilder {
        private final NodeEntry self_info;
        private final NodeEntry client_info;
        private Map<Integer, NodeEntry> finger_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;
        private final Unicast u;

        public NodeBuilder(NodeEntry self_info, NodeEntry client_info, Unicast u) {
            this.self_info = self_info;
            this.client_info = client_info;
            this.u = u;
            this.finger_table = new HashMap<Integer, NodeEntry>();
            this.key_container = new ArrayList<Integer>();
        }
        public MasterNode build() throws IOException {
            return new MasterNode(this);
        }
    }

    // failure detector

    //communicate with client

    //initalize finger table
}
