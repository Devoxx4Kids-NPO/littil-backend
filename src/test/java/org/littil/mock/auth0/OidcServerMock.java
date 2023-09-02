package org.littil.mock.auth0;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
public class OidcServerMock implements QuarkusTestResourceLifecycleManager {

    private static final String ENCODED_X5C = "MIIC+zCCAeOgAwIBAgIGAXx/E9rgMA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yMTEwMTQxMzUzMDBaFw0yMjEwMTQxMzUzMDBaMBQxEjAQBgNVBAMMCWxvY2FsaG9zdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIicN95dXlQLBqEZUsqPhQopnjnPgGmW80NohEgNzZLqN0xW9cyJJrdJM5Z1lRrePHZGiJdd1XXn4fYasP6/cjRfMWal9X6dD5wlnOTP01/4beX5vctE6W4lZrI3kTFmZ+I69w7BaLsUPWgV1CYrtuldL3dr6xAnngK3hU+JraB2Ndw9llXib26HOZhCXKedCTYcUQieVJGPI0f8H1JNk88+PnwI+cUGgXHF56iTLv9QujI6AhIgextXdd21T0XiHgBkSlSSBeqIKAjfCW6zoXP+PJU+Lso24J3duG3mrbilqHZlmIWnLRaG0RmKOeedXIDHvAaMaVUOLaN9HBgNKo0CAwEAAaNTMFEwHQYDVR0OBBYEFMYGoBNHBTMvMT4DwClVHVVwn+5VMB8GA1UdIwQYMBaAFMYGoBNHBTMvMT4DwClVHVVwn+5VMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAFulB0DKhykXGbGPIBPcj63ItLNilgl1i8i43my8fYdV6OBWLIhZ4InhpX1+XmYCNPNtu94Jy1csS00K2/Hhn4ByBd+6nd5DSr0W0VdVQyhLz3GW1nf0J3X2N+tD818O0KtKKPTq4p9reg/XtV+DNv7DeDAGzlfgRL4E4fQx6OYeuu35kGrPvAddIA70leJMELJRylCLfEcl2ne/Bht8cZVp7ZCxnfXnsc+7hCW84mhzGjJycA3E6TnZPD3pD+q9FoIAQMxMQqUCH71u9vTvz1Q5JdokuJJY2eTHSUKyHA9MwSFq8DFDICJFBoQuFyDlK5yxSUcQpR3mBwKdimj6oA0=";

    WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.NEVER));
        wireMockServer.start();

        wireMockServer.stubFor(get("/auth/.well-known/openid-configuration")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "issuer":"$server_uri/",
                                    "authorization_endpoint":"$server_uri/authorize",
                                    "token_endpoint":"$server_uri/oauth/token",
                                    "device_authorization_endpoint":"$server_uri/oauth/device/code",
                                    "userinfo_endpoint":"$server_uri/userinfo",
                                    "mfa_challenge_endpoint":"$server_uri/mfa/challenge",
                                    "jwks_uri":"$server_uri/.well-known/jwks.json",
                                    "registration_endpoint":"$server_uri/oidc/register",
                                    "revocation_endpoint":"$server_uri/oauth/revoke",
                                    "scopes_supported":["openid","profile","offline_access","name","given_name","family_name","nickname","email","email_verified","picture","created_at","identities","phone","address"],
                                    "response_types_supported":["code","token","id_token","code token","code id_token","token id_token","code token id_token"],
                                    "code_challenge_methods_supported":["S256","plain"],
                                    "response_modes_supported":["query","fragment","form_post"],
                                    "subject_types_supported":["public"],
                                    "id_token_signing_alg_values_supported":["HS256","RS256"],
                                    "token_endpoint_auth_methods_supported":["client_secret_basic","client_secret_post"],
                                    "claims_supported":["aud","auth_time","created_at","email","email_verified","exp","family_name","given_name","iat","identities","iss","name","nickname","phone_number","picture","sub"],
                                    "request_uri_parameter_supported":false,
                                    "request_parameter_supported":false}
                                """.replace("$server_uri", wireMockServer.baseUrl())
                        )));

        wireMockServer.stubFor(get("/auth/.well-known/jwks.json")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"keys\" : [\n" +
                                "    {\n" +
                                "      \"kid\": \"1\",\n" +
                                "      \"kty\":\"RSA\",\n" +
                                "      \"n\":\"iJw33l1eVAsGoRlSyo-FCimeOc-AaZbzQ2iESA3Nkuo3TFb1zIkmt0kzlnWVGt48dkaIl13Vdefh9hqw_r9yNF8xZqX1fp0PnCWc5M_TX_ht5fm9y0TpbiVmsjeRMWZn4jr3DsFouxQ9aBXUJiu26V0vd2vrECeeAreFT4mtoHY13D2WVeJvboc5mEJcp50JNhxRCJ5UkY8jR_wfUk2Tzz4-fAj5xQaBccXnqJMu_1C6MjoCEiB7G1d13bVPReIeAGRKVJIF6ogoCN8JbrOhc_48lT4uyjbgnd24beatuKWodmWYhactFobRGYo5551cgMe8BoxpVQ4to30cGA0qjQ\",\n"
                                +
                                "      \"e\":\"AQAB\"\n" +
                                "    },\n" +
                                "    {" +
                                "      \"kty\": \"RSA\"," +
                                "      \"alg\": \"RS256\"," +
                                "      \"n\":\"iJw33l1eVAsGoRlSyo-FCimeOc-AaZbzQ2iESA3Nkuo3TFb1zIkmt0kzlnWVGt48dkaIl13Vdefh9hqw_r9yNF8xZqX1fp0PnCWc5M_TX_ht5fm9y0TpbiVmsjeRMWZn4jr3DsFouxQ9aBXUJiu26V0vd2vrECeeAreFT4mtoHY13D2WVeJvboc5mEJcp50JNhxRCJ5UkY8jR_wfUk2Tzz4-fAj5xQaBccXnqJMu_1C6MjoCEiB7G1d13bVPReIeAGRKVJIF6ogoCN8JbrOhc_48lT4uyjbgnd24beatuKWodmWYhactFobRGYo5551cgMe8BoxpVQ4to30cGA0qjQ\",\n"
                                +
                                "      \"e\":\"AQAB\",\n" +
                                "      \"x5c\": [" +
                                "          \"" + ENCODED_X5C + "\""
                                +
                                "      ]" +
                                "    }" +
                                "  ]\n" +
                                "}")));

        wireMockServer.stubFor(post("/oauth/token")
                .withRequestBody(containing("\"grant_type\":\"client_credentials\""))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(
                                "{\"access_token\":\"access_token_1\", \"expires_in\":4, \"refresh_token\":\"refresh_token_1\"}")));


        //override the tenant_uri config with the url the mock is available on.
        return Collections.singletonMap("org.littil.auth.tenant_uri", wireMockServer.baseUrl());
    }

    @Override
    public synchronized void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
            wireMockServer = null;
            log.info("Oidc mock server stopped.");
        }
    }
}
