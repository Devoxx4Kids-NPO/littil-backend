package org.littil.mock.auth0;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

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
        wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                    {
                                        "access_token": "accessToken",
                                        "id_token": "idToken",
                                        "refresh_token": "refreshToken",
                                        "token_type": "type",
                                        "scope": "scope",
                                        "expires_in": $expires_in
                                    }
                                """.replace("$expires_in", String.valueOf(Instant.now().plusSeconds(600).getEpochSecond())))));

        //override the tenant_uri config with the url the mock is available on.
        return Collections.singletonMap("org.littil.auth.tenant_uri", wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}
