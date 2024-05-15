package org.openremote.manager.setup.treeorg;

import org.openremote.container.util.UniqueIdentifierGenerator;
import org.openremote.manager.setup.ManagerSetup;
import org.openremote.model.Container;
import org.openremote.model.asset.impl.ThingAsset;
import org.openremote.model.security.Realm;

public class TreeOrgManagerSetup extends ManagerSetup {

    public TreeOrgManagerSetup(Container container) {
        super(container);
    }

    @Override
    public void onStart() throws Exception{
        super.onStart();
        createAssets();
    }

    private void createAssets() {

        TreeOrgKeycloakSetup treeOrgKeycloakSetup = setupService.getTaskOfType(TreeOrgKeycloakSetup.class);

        Realm treeOrgRealm = treeOrgKeycloakSetup.treeOrgRealm;

        // Add an overarching Asset. This is not mandatory, but it makes it easier to group
        ThingAsset treeOrgAssets = new ThingAsset("TreeOrg Assets");

        // I set the realm here so that the child objects do not have to do it
        treeOrgAssets.setRealm(treeOrgRealm.getName());
        treeOrgAssets.setId(UniqueIdentifierGenerator.generateId(treeOrgAssets.getName()));
        assetStorageService.merge(treeOrgAssets);
    }
}
