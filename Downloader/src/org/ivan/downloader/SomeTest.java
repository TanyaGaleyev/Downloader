package org.ivan.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
//            URL url = new URL("http://download-cf.jetbrains.com/idea/ideaIU-13.1.3.exe");
//            DownloadDescriptor dd = dc.startDownload(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String str;
//            Thread.sleep(500);
//            dc.pauseDownload(dd);
            while (!(str = br.readLine().trim().toLowerCase()).equals("q")) {
                if (str.equals("l"))
                    for (DownloadDescriptor dd : dc.getDescriptors().values()) {
                        final int uid = dd.getUid();
                        dc.requestState(dd, new Callback<DownloadState>() {
                            @Override
                            public void process(DownloadState result) {
                                System.out.println(uid + ": " + result);
                            }
                        });
                    }
                else if (str.startsWith("r ")) {
                    for (DownloadDescriptor dd : descriptors(str.substring(1), dc))
                        dc.resumeDownload(dd);
                } else if (str.startsWith("p ")) {
                    for (DownloadDescriptor dd : descriptors(str.substring(1), dc))
                        dc.pauseDownload(dd);
                } else if (str.startsWith("c ")) {
                    for (DownloadDescriptor dd : descriptors(str.substring(1), dc))
                        dc.stopDownload(dd);
                } else if(str.startsWith("d ")) {
                    try {
                        dc.startDownload(new URL(str.substring(1)));
                    } catch (MalformedURLException e) {
                        System.out.println("Please enter url");
                    }
                } else {
                    System.out.println("Unrecognized command");
                }
            }
        } finally {
            dc.release();
        }
    }

    private static Collection<DownloadDescriptor> descriptors(String req, DownloadController dc) {
        if(req.trim().equals("*"))
            return dc.getDescriptors().values();
        try {
            DownloadDescriptor dd = dc.getDescriptors().get(Integer.parseInt(req.trim()));
            if(dd != null)
                return Collections.singletonList(dd);
            else {
                System.out.println("specify download from list");
                return Collections.emptyList();
            }
        } catch (NumberFormatException e) {
            System.out.println("Request download by uid");
            return Collections.emptyList();
        }
    }
}
