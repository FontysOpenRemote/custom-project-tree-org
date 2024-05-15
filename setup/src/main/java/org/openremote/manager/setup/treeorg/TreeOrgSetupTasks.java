package org.openremote.manager.setup.treeorg;

import org.openremote.model.Container;
import org.openremote.model.setup.Setup;
import org.openremote.model.setup.SetupTasks;

import java.util.Arrays;
import java.util.List;

public class TreeOrgSetupTasks implements SetupTasks {

    public static final String PRODUCTION = "production";

    @Override
    public List<Setup> createTasks(Container container, String setupType, boolean keycloakEnabled) {

        boolean isProduction = PRODUCTION.equalsIgnoreCase(setupType);

        // Add custom Setup task implementations here with tasks optionally dependent on setupType
        return Arrays.asList(
                new TreeOrgKeycloakSetup(container, isProduction),
                new TreeOrgManagerSetup(container)
        );
    }
}
