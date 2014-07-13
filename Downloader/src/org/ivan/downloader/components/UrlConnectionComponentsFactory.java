package org.ivan.downloader.components;

import org.ivan.downloader.connection.IOAdapter;
import org.ivan.downloader.protocols.ProtocolHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ivan on 13.07.2014.
 */
public class UrlConnectionComponentsFactory implements ComponentsFactory {

    @Override
    public Components createComponents(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        return new Components(new UrlConnectionIO(urlConnection), new UrlConnectionHelper(urlConnection));
    }

    private class UrlConnectionIO implements IOAdapter {
        private HttpURLConnection urlConnection;

        private UrlConnectionIO(HttpURLConnection urlConnection) {
            this.urlConnection = urlConnection;
        }

        @Override
        public void open() throws IOException {}

        @Override
        public int read(byte[] buffer) throws IOException {
            return urlConnection.getInputStream().read(buffer);
        }

        @Override
        public int write(byte[] buffer) throws IOException {
            urlConnection.getOutputStream().write(buffer);
            return buffer.length;
        }

        @Override
        public int write(byte[] buffer, int offset, int length) throws IOException {
            urlConnection.getOutputStream().write(buffer, offset, length);
            return length;
        }

        @Override
        public void close() throws IOException {
            urlConnection.disconnect();
        }
    }

    private class UrlConnectionHelper implements ProtocolHelper {
        private HttpURLConnection urlConnection;

        private UrlConnectionHelper(HttpURLConnection urlConnection) {
            this.urlConnection = urlConnection;
        }

        @Override
        public byte[] getRequestMessage(int offset, int length) {
            return new byte[0];
        }

        @Override
        public byte[] getRequestMessage(int offset) {
            return new byte[0];
        }

        @Override
        public byte[] getRequestMessage() {
            return new byte[0];
        }

        @Override
        public void requestDownload(IOAdapter adapter, int offset) throws IOException {
            urlConnection.setRequestProperty("Range", "bytes=" + offset + "-");
        }

        @Override
        public int readDownloadBytes(byte[] buffer, IOAdapter ioAdapter) throws IOException {
            return urlConnection.getInputStream().read(buffer);
        }

        @Override
        public byte[] checkRangeDownloadMessage() {
            return new byte[0];
        }

        @Override
        public boolean isRangeSupported(IOAdapter ioAdapter) throws IOException {
            String acceptRanges = urlConnection.getHeaderField("Accept-Ranges");
            return acceptRanges != null && acceptRanges.contains("bytes");
        }

        @Override
        public int getSize() {
            return urlConnection.getContentLength();
        }
    }
}
