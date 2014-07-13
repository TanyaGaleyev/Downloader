package org.ivan.downloader.threading;

/**
 * Callback interface to perform actions after {@link org.ivan.downloader.threading.messages.Message}
 * are processed in message loop
 * <p>
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 12:54
 */
public interface Callback<T> {
    void process(T result);
}
