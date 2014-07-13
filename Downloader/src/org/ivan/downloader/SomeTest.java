package org.ivan.downloader;

import org.ivan.downloader.components.GenericComponentsFactory;
import org.ivan.downloader.components.UrlConnectionComponentsFactory;
import org.ivan.downloader.connection.NIOComponent;
import org.ivan.downloader.protocols.SimpleProtocolHelperProvider;
import org.ivan.downloader.threading.PoolWorkersController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ivan on 10.07.2014.
 */
public class SomeTest {
    public static void main(String[] args) throws Exception {
        Logger.getGlobal().setLevel(Level.WARNING);
        DownloadManager dc = new DownloadController(new UrlConnectionComponentsFactory(), new PoolWorkersController());
//        DownloadController dc = new DownloadController(
//                new GenericComponentsFactory(new NIOComponent(), new SimpleProtocolHelperProvider()),
//                new PoolWorkersController());
        try {
//            URL url = new URL("http://norvig.com/big.txt");
//            URL url = new URL("http://tutorials.jenkov.com/images/java-nio/buffers-modes.png");
//            URL url = new URL("http://heanet.dl.sourceforge.net/project/keepass/KeePass%202.x/2.25/KeePass-2.25.zip");
//            URL url = new URL("http://download-cf.jetbrains.com/idea/ideaIU-13.1.3.exe");
//            URL url = new URL("http://get.videolan.org/vlc/2.1.3/win32/vlc-2.1.3-win32.exe");
//            URL url = new URL("http://ftp.acc.umu.se/mirror/videolan.org/vlc/2.1.3/win32/vlc-2.1.3-win32.exe");
//            DownloadDescriptor dd = dc.startDownload(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String str;
//            Thread.sleep(500);
//            dc.pauseDownload(dd);
            while (!(str = br.readLine().trim().toLowerCase()).equals("q")) {
                if (str.equals("l")) {
                    Collection<DownloadDescriptor> descriptors = dc.getDescriptors().values();
                    if(descriptors.isEmpty()) {
                        System.out.println("Empty downloads list");
                    } else {
                        for (DownloadDescriptor dd : descriptors) {
                            final int uid = dd.getUid();
                            System.out.println(uid + ": " + dc.requestState(dd) + ", time: " + dd.getDownloadTime());
                        }
                    }
                } else if (str.startsWith("r ")) {
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

    private static Collection<DownloadDescriptor> descriptors(String req, DownloadManager dc) {
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
