import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.sql.Time;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimerTask;
public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client(new Unicast("127.0.0.1", 2999, Config.parseConfig("configFile")));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String s = br.readLine();
            if(s.equals("join")) {
                client.join(1);
                client.join(2);
                client.join(3);
            }
            else
                System.out.println("Invalid command.");
        }
//        class TimerHelper {
//            Timer timer;
//
//            public TimerHelper(int delay) {
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        System.out.println("Waited too much time, operation aborted");
//                        timer.cancel();
//                        renew(delay);
//                    }
//                }, delay);
//            }
//
//            public void destroyTimer(){
//                this.timer.cancel();
//            }
//            public void renew(int delay){
//                this.timer = new TimerHelper(delay).timer;
//            }
//        }
//        TimerHelper t = new TimerHelper(1000);
    }
}