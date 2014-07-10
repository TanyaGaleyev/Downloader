package org.ivan.downloader;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 10.07.2014.
 */
public class HttpHelper implements ProtocolHelper {
    @Override
    public byte[] getRequestMessage(URL url, int offset, int length) {
        return null;
    }

    @Override
    public byte[] getRequestMessage(URL url, int offset) {
        return new byte[0];
    }

    @Override
    public byte[] getRequestMessage(URL url) {
        return (baseGetHeader(url) + "\n").getBytes();
    }

    private int totalRead = 0;
    private boolean headerRead = false;
    private int contentLength = 0;
    @Override
    public int readDownloadBytes(byte[] buffer, IOWrapper ioWrapper) throws IOException {
        if(!headerRead) {
            headerRead = true;
            for (String header : readHeaders(ioWrapper)) {
                if (header.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(header.substring("Content-Length:".length()).trim());
                }
            }
        }
        if(totalRead >= contentLength) return -1;
        int nRead = ioWrapper.read(buffer);
        totalRead += nRead;
        return nRead;
    }

    @Override
    public byte[] checkRangeDownloadMessage(URL url) {
        return headHeader(url).getBytes();
    }

    @Override
    public boolean isRangeSupported(IOWrapper ioWrapper) throws IOException {
        for(String header : readHeaders(ioWrapper))
            if(header.startsWith("Accept-Ranges:") && header.contains("bytes"))
                return true;
        return false;
    }

    private List<String> readHeaders(IOWrapper ioWrapper) throws IOException {
        List<String> ret = new ArrayList<>();
        byte[] buffer = new byte[2048];
        byte[] aux = new byte[1];
        int nRead;
        StringBuilder sb = new StringBuilder();
        readLoop:
        while ((nRead = ioWrapper.read(buffer)) != -1) {
            for (int i = 0; i < nRead; i++) {
                if(buffer[i] == '\r') {
                    if(sb.length() == 0) break readLoop;
                    ret.add(sb.toString());
                    sb = new StringBuilder();
                    byte nextByte;
                    if (i == nRead - 1)
                        nextByte = ioWrapper.read(aux) > 0 ? aux[0] : -1;
                    else
                        nextByte = buffer[i+1];
                    if(nextByte == '\n') {
                        i++;
                    } else if(nextByte != -1) {
                        sb.append((char) nextByte);
                    }
                } else if(buffer[i] == '\n') {
                    ret.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append((char) buffer[i]);
                }
            }
        }
        for (String s : ret) {
            System.out.println(s);
        }
        return ret;
    }

    private String baseGetHeader(URL url) {
        return  "GET " + url.getPath() + " HTTP/1.1\r\n" +
                "Host: " + url.getHost() + "\r\n" +
                "Connection: close\r\n" +
                "Cache-Control: no-cache\r\n" +
                "User-Agent: Java\r\n" +
                "Accept: */*\r\n";
    }

    private String headHeader(URL url) {
        return  "HEAD " + url.getPath() + " HTTP/1.1\r\n" +
                "Host: " + url.getHost() + "\r\n" +
                "Connection: close\r\n" +
                "Cache-Control: no-cache\r\n" +
                "User-Agent: Java\r\n" +
                "Accept: */*\r\n" +
                "Range: bytes=0-\r\n\r\n";
    }
}
