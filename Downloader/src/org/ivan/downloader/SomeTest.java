package org.ivan.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public class SomeTest {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://yandex.st/weather/1.2.50/i/icons/48x48/skc_d.png");
        DownloadController dc = new DownloadController();
        dc.startDownload(url);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str;
        while (!(str = br.readLine().trim().toLowerCase()).equals("q")) {
            if(str.equals("s"))
                for (DownloadDescriptor d : dc.getDescriptors()) {
                    System.out.println(dc.getState(d));
                }
        }
        dc.release();
    }
}
