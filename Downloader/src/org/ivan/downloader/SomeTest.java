package org.ivan.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public class SomeTest {
    public static void main(String[] args) throws Exception {
        DownloadController dc = new DownloadController();
        try {
//            URL url = new URL("http://norvig.com/big.txt");
//            URL url = new URL("http://tutorials.jenkov.com/images/java-nio/buffers-modes.png");
//            URL url = new URL("http://heanet.dl.sourceforge.net/project/keepass/KeePass%202.x/2.25/KeePass-2.25.zip");
            URL url = new URL("http://download-cf.jetbrains.com/idea/ideaIU-13.1.3.exe");
            DownloadDescriptor dd = dc.startDownload(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String str;
            Thread.sleep(500);
            dc.pauseDownload(dd);
            while (!(str = br.readLine().trim().toLowerCase()).equals("q")) {
                if(str.equals("s"))
                    for (DownloadDescriptor d : dc.getDescriptors()) {
                        dc.requestState(d, new Callback<DownloadState>() {
                            @Override
                            public void process(DownloadState result) {
                                System.out.println(result);
                            }
                        });
                    }
                else if(str.equals("r"))
                    dc.resumeDownload(dd);
                else if(str.equals("p"))
                    dc.pauseDownload(dd);
                else if(str.equals("c"))
                    dc.stopDownload(dd);
            }
        } finally {
            dc.release();
        }
    }
}
