package org.ivan.downloader.components;

import org.ivan.downloader.connection.IOComponent;
import org.ivan.downloader.protocols.ProtocolHelperProvider;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 13.07.2014.
 */
public class GenericComponentsFactory implements ComponentsFactory {
    private final IOComponent ioComponent;
    private final ProtocolHelperProvider helperProvider;

    public GenericComponentsFactory(IOComponent ioComponent, ProtocolHelperProvider helperProvider) {
        this.ioComponent = ioComponent;
        this.helperProvider = helperProvider;
    }

    @Override
    public Components createComponents(URL url) throws IOException {
        return new Components(ioComponent.createAdapter(url), helperProvider.getHelper(url));
    }
}
