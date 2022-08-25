package org.littil.mock.auth0;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Slf4j
public class APIManagementMock implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        /*
         * Mock the auth0 api server, so we can run tests without
         * having to call the actual auth API server.
         */
        wireMockServer.stubFor(get(urlPathEqualTo(""))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                   {"mocked" : "The Auth0 API server :)"}
                                """)));

        //override the tenant_uri config with the url the mock is available on.
        return Collections.singletonMap("org.littil.auth.provider_api", wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
