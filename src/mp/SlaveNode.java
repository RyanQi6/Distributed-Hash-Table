package mp;
import javax.management.RuntimeMBeanException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.sql.Time;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimerTask;

// message types used:
//1. heartbeat message, from successor to predecessor, line 61
//2. node crash report, from predecessor to client

public class SlaveNode extends Node {
    Unicast u;
    public boolean successor_alive = true;

    public SlaveNode(NodeBuilder builder) {
        this.node_entry = builder.node_entry;
        this.figure_table = builder.figure_table;
        this.predecessor_pointer = builder.predecessor_pointer;
        this.key_container = builder.key_container;
    }

    static class NodeBuilder {
        private final NodeEntry node_entry;
        private List<NodeEntry> figure_table;
        private NodeEntry predecessor_pointer;
        private List<Integer> key_container;

        private final Unicast u;

        public NodeBuilder(NodeEntry node_entry, Unicast u){
            this.node_entry = node_entry;
            this.u = u;
            this.figure_table = new ArrayList<NodeEntry>();
            this.key_container = new ArrayList<Integer>();
        }
        public NodeBuilder predecessorPointer(NodeEntry predecessor_pointer){
            this.predecessor_pointer = predecessor_pointer;
            return this;
        }
        public SlaveNode build(){
            return new SlaveNode(this);
        }
    }

    // Failure Detection
    // Timer setter
    public Timer sendHeartbeatTimer(int delay) {
        this.send_timer = new Timer();
        this.send_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    destroySendTimer();
                    send_timer = sendHeartbeatTimer(delay);

                    u.unicast_send(predecessor_pointer.address, predecessor_pointer.port, "successor is alive");
                } catch (IOException i) {
                    System.out.println("IO Exception");
                } catch (InterruptedException i) {
                    System.out.println("Interrupted");
                }
            }
        }, delay);
        return send_timer;
    }
    public Timer receiveHeartbeatTimer(int delay) {
        this.receive_timer = new Timer();
        this.receive_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    destroyReceiveTimer();
                    receive_timer = receiveHeartbeatTimer(delay);

                    successor_alive = false;
                    u.unicast_send(client_info.address, client_info.port, "node is down");

                    failureRecovery();
                } catch (IOException i) {
                    System.out.println("IO Exception");
                } catch (InterruptedException i) {
                    System.out.println("Interrupted");
                }
            }
        }, delay);
        return receive_timer;
    }
    // Timer destroyer
    public void destroySendTimer(){
        this.send_timer.cancel();
    }
    public void destroyReceiveTimer(){
        this.receive_timer.cancel();
    }

    // When the hearbeat from successor is received, clear the timer, and time again
    public void receivedHeartbeat(){
        successor_alive = true;
        destroyReceiveTimer();
        this.receive_timer = receiveHeartbeatTimer(10);
    }

    // Failure recovery: To be implemented
    public void failureRecovery(){

    }
    //communicate with client

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

    private void startListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                }catch (IOException e){
                    e.printStackTrace();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        new Thread(listener).start();
    }
    private void listen() throws IOException, InterruptedException{
        String message;
        int command_mode = 1;
        if(command_mode == 1){
            receivedHeartbeat();
        }
    }

    public static void main(String[] args) {

    }
}
