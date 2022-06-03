package org.littil.api;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle {
    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting with profile " + ProfileManager.getActiveProfile());
    }
}
