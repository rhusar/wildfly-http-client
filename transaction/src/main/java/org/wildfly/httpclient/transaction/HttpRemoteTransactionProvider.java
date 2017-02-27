package org.wildfly.httpclient.transaction;

import java.net.URI;
import javax.transaction.SystemException;

import org.wildfly.httpclient.common.WildflyHttpContext;
import org.wildfly.transaction.client.spi.RemoteTransactionPeer;
import org.wildfly.transaction.client.spi.RemoteTransactionProvider;

/**
 * @author Stuart Douglas
 */
public class HttpRemoteTransactionProvider implements RemoteTransactionProvider {

    @Override
    public RemoteTransactionPeer getPeerHandle(URI uri) throws SystemException {
        return new HttpRemoteTransactionPeer(WildflyHttpContext.getCurrent().getTargetContext(uri));
    }

    @Override
    public boolean supportsScheme(String s) {
        switch (s) {
            case "http":
            case "https":
                return true;
        }
        return false;
    }
}
