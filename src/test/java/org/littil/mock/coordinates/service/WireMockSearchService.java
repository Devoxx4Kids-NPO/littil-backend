package org.littil.mock.coordinates.service;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WireMockSearchService implements QuarkusTestResourceLifecycleManager {

    public static final String NOT_FOUND = "NotFound";

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(options().dynamicPort()
                .extensions(new ResponseTemplateTransformer(false)));
        
        wireMockServer.start();
        stubbing();
        return Collections.singletonMap("quarkus.rest-client.\"org.littil.api.coordinates.service.SearchService\".url",
                wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if (Objects.nonNull(wireMockServer))
            wireMockServer.stop();
    }

    private void stubbing() {
        String coordinates = "[ { \"lat\" : 52.5254012, \"lon\" : 13.4054681, \"display_name\" : \"street city {{request.query.q.[0]}} etcetera\" } ]";
        String regex = "(?:(?!(" + NOT_FOUND + ")).)*$"; // all except "NotFound"

        wireMockServer.stubFor(get(WireMock.urlMatching("/.*"))
                .withQueryParam("q", WireMock.matching(regex))
                .withQueryParam("format", WireMock.equalTo("json"))
                .willReturn(aResponse()
                        .withHeader("content-type", "application/json")
                    .withBody(coordinates)
                    .withTransformers("response-template")));

        wireMockServer.stubFor(get(WireMock.urlMatching("/.*"))
                .withQueryParam("q", WireMock.equalTo(NOT_FOUND))
                .withQueryParam("format", WireMock.equalTo("json"))
                .willReturn(okJson("[]")));
    }
}