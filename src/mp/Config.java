package mp;

import java.io.*;

public class Config {
    public int minDelay;
    public int maxDelay;

    public Config(int minDelay, int maxDelay) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }
	
	public static Config parseConfig(String filename) throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(filename));
		String firstLine = file.readLine();
		if(firstLine == null)
			throw new IOException("Empty Configuration File");
		String[] delays = firstLine.split(" ");
		if(delays.length != 2)
			throw new IOException("Wrong Formatted Configuration File");
		Integer minDelay = Integer.parseInt(delays[0]);
		Integer maxDelay = Integer.parseInt(delays[1]);
		if(minDelay == null || maxDelay == null) 
			throw new IOException("Wrong Formatted Configuration File");
		return new Config(minDelay, maxDelay);
	}

}
