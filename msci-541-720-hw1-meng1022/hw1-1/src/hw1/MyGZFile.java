package hw1;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class MyGZFile {
    private InputStream fileStream;
    private InputStream gzipStream;
    public MyGZFile(String filename)
            throws IOException {
        fileStream = new FileInputStream(filename);
        gzipStream = new GZIPInputStream(fileStream);
    }
    public void close() throws IOException{
        gzipStream.close();
        fileStream.close();
    }
    public BufferedReader read() throws IOException {
        Reader decoder = new InputStreamReader(gzipStream);
        BufferedReader buffered = new BufferedReader(decoder);
        return buffered;
    }
}
