package org.ivan.downloader;

import org.ivan.downloader.connection.GenericConnectionFactory;
import org.ivan.downloader.connection.UrlConnectionFactory;
import org.ivan.downloader.io.NIOFactory;
import org.ivan.downloader.protocols.SimpleProtocolConnectionProvider;
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
 * CLI to test Download Manager. Usage will be printed to System.out stream.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class SomeTest {
    public static void main(String[] args) throws Exception {
        Logger.getGlobal().setLevel(Level.WARNING);
        DownloadManager dc = new DownloadController(new UrlConnectionFactory(), new PoolWorkersController());
//        DownloadController dc = new DownloadController(
//                new GenericConnectionFactory(new NIOFactory(), new SimpleProtocolConnectionProvider()),
//                new PoolWorkersController());
        printUsage();
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
            while (!(str = br.readLine().trim().toLowerCase()).equals("q")) {
                if (str.matches("l(\\s+-v)?")) {
                    boolean verbose = str.contains("-v");
                    Collection<DownloadDescriptor> descriptors = dc.getDescriptors().values();
                    if(descriptors.isEmpty()) {
                        System.out.println("Empty downloads list");
                    } else {
                        for (DownloadDescriptor dd : descriptors) {
                            final int uid = dd.getUid();
                            String url = verbose ? "(" + dd.getUrl() + ")" : "";
                            System.out.println(uid + url + ": " + dc.requestState(dd) + ", time: " + dd.getDownloadTime());
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

    private static void printUsage() {
        System.out.println("Test console for Download Manager");
        System.out.println("Usage:");
        System.out.println("l [-v]");
        System.out.println("    prints list of submitted downloads");
        System.out.println("    with -v option provides verbose output");
        System.out.println("OR");
        System.out.println("d <url>");
        System.out.println("    start download from specified url");
        System.out.println("OR");
        System.out.println("<command> <id>");
        System.out.println("    where <command>::=r|p|c");
        System.out.println("    r -- means resume");
        System.out.println("    p -- means pause");
        System.out.println("    c -- means cancel");
        System.out.println("    and <id> could be unique single download identifier or *");
        System.out.println("    * means that action will be taken to every download");
        System.out.println("Hint: to determine download id call 'l' command");
        System.out.println("OR");
        System.out.println("q");
        System.out.println("    leave console");
    }
}
