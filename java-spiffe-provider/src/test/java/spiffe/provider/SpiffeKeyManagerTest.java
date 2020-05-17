package spiffe.provider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spiffe.exception.X509SvidException;
import spiffe.internal.CertificateUtils;
import spiffe.svid.x509svid.X509Svid;
import spiffe.svid.x509svid.X509SvidSource;

import javax.net.ssl.X509KeyManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static spiffe.provider.SpiffeProviderConstants.DEFAULT_ALIAS;

public class SpiffeKeyManagerTest {

    @Mock
    X509SvidSource x509SvidSource;

    X509KeyManager keyManager;
    X509Svid x509Svid;

    @BeforeEach
    void setup() throws X509SvidException, URISyntaxException {
        MockitoAnnotations.initMocks(this);
        keyManager = (X509KeyManager) new SpiffeKeyManagerFactory().engineGetKeyManagers(x509SvidSource)[0];
        x509Svid = X509Svid
                .load(
                        Paths.get(loadResource("testdata/cert.pem")),
                        Paths.get(loadResource("testdata/key.pem")));
    }

    @Test
    void getCertificateChain_returnsAnArrayOfX509Certificates() throws CertificateException {
        when(x509SvidSource.getX509Svid()).thenReturn(x509Svid);

        val certificateChain = keyManager.getCertificateChain(DEFAULT_ALIAS);
        val spiffeId = CertificateUtils.getSpiffeId(certificateChain[0]);

        assertAll(
                () -> assertEquals(1, certificateChain.length),
                () -> assertEquals("spiffe://example.org/test", spiffeId.toString())
        );
    }

    @Test
    void getPrivateKey_aliasIsSpiffe_returnAPrivateKey() {
        when(x509SvidSource.getX509Svid()).thenReturn(x509Svid);

        val privateKey = keyManager.getPrivateKey(DEFAULT_ALIAS);

        assertNotNull(privateKey);
    }

    private URI loadResource(String path) throws URISyntaxException {
        return getClass().getClassLoader().getResource(path).toURI();
    }
}
