package org.ivan.downloader.threading;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 12:54
 */
public interface Callback<T> {
    void process(T result);
}
