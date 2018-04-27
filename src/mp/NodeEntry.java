package mp;
public class NodeEntry {
    int id;
    String address;
    int port;

    public NodeEntry(int id, String address, int port){
        this.id = id;
        this.address = address;
        this.port = port;
    }

    @Override
    public String toString() {
        String str = "[ID=" + this.id + " IP:PORT=" + this.address + ":" + this.port+"]";
        return str;
    }
}
