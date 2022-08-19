package org.littil.api;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.provider.Provider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;
import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {

    @Inject
    @ConfigProperty(name = "org.littil.devservices.devdata", defaultValue = "false")
    boolean insertDevData;

    @Inject
    DataSource dataSource;
    @Inject
    ManagementAPI managementAPI;

    void onStart(@Observes StartupEvent ev) throws Auth0Exception {
        log.info("The application is starting with profile " + ProfileManager.getActiveProfile());

        if (insertDevData) {
            persistDevData();
            cleanLittilAuth0User();
        }
    }

    private void cleanLittilAuth0User() throws Auth0Exception {
        log.info("Removing all authorizations from Auth0 appMetadata, this might cause issues when 2 developers are testing at once.");

        User user = new User();
        String userId = "auth0|62fd3224f76949850e9eb264";
        Map<String, Object> appMetadata = Map.of(USER_ID_TOKEN_CLAIM, "e87cda48-9a98-41a7-89bb-9ee62441c84c",
                AUTHORIZATIONS_TOKEN_CLAIM, Collections.emptyMap());
        user.setAppMetadata(appMetadata);
        managementAPI.users().update(userId, user).execute();

        log.info("Done all authorizations from Auth0 appMetadata, this might cause issues when 2 developers are testing at once.");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private void persistDevData() {
        log.info("Persisting development data to datasource, this should not be happening in staging nor production.");

        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(String.format("""
                        INSERT INTO `user` (
                            user_id,
                            provider,
                            provider_id,
                            email_address
                        ) VALUES (
                            %s,
                            %s,
                            %s,
                            %s
                        )""", "X'e87cda489a9841a789bb9ee62441c84c'", "'" + Provider.AUTH0.name() + "'", "'auth0|62fd3224f76949850e9eb264'", "'info@littil.org'"));

                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM `user`");
                rs.next();

                log.info("Created " + rs.getInt("rowcount") + " users for development purposes");

            } catch (SQLException e) {
                throw new PersistenceException("Unable to persist development data", e);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Unable to persist development data", e);
        }

        log.info(String.format("Added general LiTTiL user for development purposes. You can login via email-address %s and the default password to the dev tenant.", "'info@littil.org'"));
    }
}
