import mp.*;

import java.io.IOException;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Config hostInfo = Config.parseConfig("configFile");
        Unicast u = new Unicast("127.0.0.1", 3001, hostInfo);
        u.unicast_send("127.0.0.1", 3000, "hello world!");
        String message;
        while( true ) {
            message = u.unicast_receive();
            if(message != null)
                System.out.println(message);
            Thread.sleep(1000);
        }
    }
}
