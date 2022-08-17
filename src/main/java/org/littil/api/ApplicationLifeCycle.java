package org.littil.api;

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

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {

    @Inject
    @ConfigProperty(name = "org.littil.devservices.devdata", defaultValue = "false")
    boolean insertDevData;

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting with profile " + ProfileManager.getActiveProfile());

        if (insertDevData)
            persistDevData();
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
