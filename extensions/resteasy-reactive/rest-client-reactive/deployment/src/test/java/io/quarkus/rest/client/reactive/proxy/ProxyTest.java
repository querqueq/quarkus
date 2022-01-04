package io.quarkus.rest.client.reactive.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.rest.client.reactive.runtime.RestClientBuilderImpl;
import io.quarkus.test.QuarkusUnitTest;

/**
 * client1 and client2 are configured to use 8181 as a proxy, global configuration says to use 8182
 */
public class ProxyTest extends ProxyTestBase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(
                    jar -> jar.addClasses(Client1.class, Client2.class, Client3.class, Client4.class,
                            ViaHeaderReturningResource.class))
            .withConfigurationResource("proxy-test-application.properties");

    @RestClient
    Client1 client1;
    @RestClient
    Client2 client2;
    @RestClient
    Client3 client3;
    @RestClient
    Client4 client4;

    @Test
    void shouldProxyCDIWithPerClientSettings() {
        assertThat(client1.get().readEntity(String.class)).isEqualTo(PROXY_8181);
        assertThat(client2.get().readEntity(String.class)).isEqualTo(PROXY_8181);
        assertThat(client3.get().readEntity(String.class)).isEqualTo(PROXY_8182);
        assertThat(client4.get().readEntity(String.class)).isEqualTo(AUTHENTICATED_PROXY);
    }

    @Test
    void shouldProxyBuilderWithPerClientSettings() {
        Response response1 = RestClientBuilder.newBuilder().baseUri(appUri).proxyAddress("localhost", 8181)
                .build(Client1.class).get();
        assertThat(response1.readEntity(String.class)).isEqualTo(PROXY_8181);
        Response response2 = RestClientBuilder.newBuilder().baseUri(appUri).build(Client2.class).get();
        assertThat(response2.readEntity(String.class)).isEqualTo(PROXY_8182);

        RestClientBuilderImpl restClientBuilder = (RestClientBuilderImpl) RestClientBuilder.newBuilder();
        Response response3 = restClientBuilder.baseUri(appUri).proxyAddress("localhost", 8183)
                .proxyUser("admin").proxyPassword("r00t")
                .build(Client1.class).get();
        assertThat(response3.readEntity(String.class)).isEqualTo(AUTHENTICATED_PROXY);
    }
}
