package org.ivan.downloader.connection;

import org.ivan.downloader.protocols.ProtocolConnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Adapt {@link java.net.HttpURLConnection} to use with DownloadManager
 * Created by ivan on 13.07.2014.
 */
public class UrlConnectionFactory implements ConnectionFactory {

    @Override
    public ProtocolConnection createConnection(URL url) {
        try {
            return new UrlConnectionConnection((HttpURLConnection) url.openConnection());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class UrlConnectionConnection implements ProtocolConnection {
        private HttpURLConnection urlConnection;

        private UrlConnectionConnection(HttpURLConnection urlConnection) {
            this.urlConnection = urlConnection;
        }

        @Override
        public void connect() throws IOException {
            // here we do not delegate to URLConnection.connect to be able manipulate request properties
        }

        @Override
        public void disconnect() throws IOException {
            urlConnection.disconnect();
        }

        @Override
        public void requestDownload(int offset) throws IOException {
            urlConnection.setRequestProperty("Range", "bytes=" + offset + "-");
        }

        @Override
        public int readDownloadBytes(byte[] buffer) throws IOException {
            return urlConnection.getInputStream().read(buffer);
        }

        @Override
        public boolean isRangeSupported() throws IOException {
            String acceptRanges = urlConnection.getHeaderField("Accept-Ranges");
            return acceptRanges != null && acceptRanges.contains("bytes");
        }

        @Override
        public int getSize() {
            return urlConnection.getContentLength();
        }
    }
}
