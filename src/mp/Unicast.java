package mp;

import mp.TCP.Client;
import mp.TCP.Server;

import java.io.IOException;
import java.util.*;

public class Unicast {
    String address;
    int port;
    Config hostInfo;

    Server s;
    Client c;

    //buffer to store the messages received but not delivered yet.
    //it is a fifo queue
    Queue<String> messageBuffer;

    public Unicast(String address, int port, Config hostInfo) throws IOException {
        this.address = address;
        this.port = port;
        this.hostInfo = hostInfo;
        s = new Server(address, port);
        c = new Client();

        messageBuffer = new LinkedList<>();
        startListen();
    }

    public void startListen() {
        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    s.startServer(messageBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(server).start();
    }

    public void unicast_send(String destAddress, int destPort, String message) {
        int delay = (int) (hostInfo.minDelay + (hostInfo.maxDelay - hostInfo.minDelay) * Math.random());
        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run() {
                synchronizedSend.send(address, port, c, hostInfo, destAddress, destPort, message);
                t.cancel();
            }
        }, delay);
        //System.out.println( "Sent \"" + message + "\" to process " + destination + ", system time is " + System.currentTimeMillis() );
    }

    public String unicast_receive(){
        return messageBuffer.poll();
    }
}

class synchronizedSend {
    public static synchronized void send(String senderAddress, int senderPort, Client c, Config hostInfo, String destAddress, int destPort, String message){
        try {
            c.startClient(destAddress, destPort);
            c.sendMessage(senderAddress + "||" + senderPort + "||" + message + "||");
            System.out.println("    Msg to: " + destAddress + ":" + destPort + "   Content: " + senderAddress +"||" + senderPort + "||" + message + "||");
            c.closeClient();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}