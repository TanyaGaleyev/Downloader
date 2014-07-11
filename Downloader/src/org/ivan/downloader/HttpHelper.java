package org.ivan.downloader;

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
        return (baseGetHeader(url) +
                String.format("Range: bytes=%d-%d\r\n\r\n", offset, offset + length - 1))
                .getBytes();
    }

    @Override
    public byte[] getRequestMessage(URL url, int offset) {
        return (baseGetHeader(url) +
                String.format("Range: bytes=%d-\r\n\r\n", offset))
                .getBytes();
    }

    @Override
    public byte[] getRequestMessage(URL url) {
        return (baseGetHeader(url) + "\r\n"
//                + "Range: bytes=0-\r\n\r\n"
        ).getBytes();
    }

    private int totalRead = 0;
    private boolean headerRead = false;
    private int contentLength = 0;
    private boolean chunked = false;
    @Override
    public int readDownloadBytes(byte[] buffer, IOWrapper ioWrapper) throws IOException {
        if(!headerRead) {
            headerRead = true;
            for (String header : readHeaders(ioWrapper)) {
                if (header.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(header.substring("Content-Length:".length()).trim());
                } else if(header.startsWith("Transfer-Encoding:") && header.contains("chunked")) {
                    chunked = true;
                }
            }
        }
        if(chunked) {
            return readChunked(buffer, ioWrapper);
        } else {
            return readLength(buffer, ioWrapper);
        }
    }

    private int readChunked(byte[] buffer, IOWrapper ioWrapper) {
        throw new IllegalStateException("Chunks not supported now");
    }

    private int readLength(byte[] buffer, IOWrapper ioWrapper) throws IOException {
        if(totalRead >= contentLength) return -1;
        int nRead;
        // TODO really should be while
//        if(readPosition < internalLenght - 1) {
//            int remainBytes = internalLenght - readPosition - 1;
//            System.arraycopy(internalBuffer, readPosition + 1, buffer, 0, remainBytes);
//            byte[] trim = new byte[internalLenght - remainBytes];
//            nRead = ioWrapper.read(trim);
//            if(nRead > 0) {
//                System.arraycopy(trim, 0, buffer, readPosition + 1, nRead);
//                nRead += remainBytes;
//            } else {
//                nRead = remainBytes;
//            }
//            readPosition = internalBuffer.length;
//        } else {
            nRead = ioWrapper.read(buffer);
//        }
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
        readPosition = internalBuffer.length;
        return false;
    }


    private byte[] internalBuffer = new byte[2048];
    int readPosition = internalBuffer.length;
    int internalLenght = 0;

    private List<String> readHeaders(IOWrapper ioWrapper) throws IOException {
        List<String> ret = new ArrayList<>();
        byte[] aux = new byte[1];
        int nRead;
        StringBuilder sb = new StringBuilder();
        while (ioWrapper.read(aux) != -1) {
            if(aux[0] == '\r') {
                ioWrapper.read(aux);
                if(sb.length() == 0) break;
                ret.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append((char) aux[0]);
            }
        }
//        readLoop:
//        while ((internalLenght = ioWrapper.read(internalBuffer)) != -1) {
//            boolean done = false;
//            for (readPosition = 0; readPosition < internalLenght; readPosition++) {
//                if(internalBuffer[readPosition] == '\r') {
//                    if(sb.length() == 0) done = true;
//                    ret.add(sb.toString());
//                    sb = new StringBuilder();
//                    byte nextByte;
//                    if (readPosition == internalLenght - 1)
//                        nextByte = ioWrapper.read(aux) > 0 ? aux[0] : -1;
//                    else
//                        nextByte = internalBuffer[readPosition+1];
//                    if(nextByte == '\n') {
//                        readPosition++;
//                    } else if(nextByte != -1) {
//                        sb.append((char) nextByte);
//                    }
//                } else if(internalBuffer[readPosition] == '\n') {
//                    if(sb.length() == 0) done = true;
//                    ret.add(sb.toString());
//                    sb = new StringBuilder();
//                } else {
//                    sb.append((char) internalBuffer[readPosition]);
//                }
//                if(done) break readLoop;
//            }
//        }
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
