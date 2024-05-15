package org.openremote.manager.setup.treeorg;

import org.openremote.manager.setup.AbstractKeycloakSetup;
import org.openremote.model.Container;
import org.openremote.model.security.ClientRole;
import org.openremote.model.security.Realm;
import org.openremote.model.util.TextUtil;

import static org.openremote.container.util.MapAccess.getString;

public class TreeOrgKeycloakSetup extends AbstractKeycloakSetup {

    public static final String TREEORG_USER_PASSWORD = "TREEORG_USER_PASSWORD";
    public static final String TREEORG_USER_PASSWORD_DEFAULT = "PASSWORD";
    protected final String treeOrgUserPassword;
    public Realm treeOrgRealm;
    public TreeOrgKeycloakSetup(Container container, boolean isProduction) {
        super(container);
        treeOrgUserPassword = getString(container.getConfig(),TREEORG_USER_PASSWORD, TREEORG_USER_PASSWORD_DEFAULT);

        if(isProduction && TextUtil.isNullOrEmpty(treeOrgUserPassword)){
            throw new IllegalStateException("Password must be supplied in production");
        }
    }

    @Override
    public void onStart() {
        // Create custom realm
        treeOrgRealm = createRealm("treeorg", "TreeOrg", true);
        // make sure the realmname is lowercase, or you will get an error saying realm is null

        // Create user(s) for custom realm
        createUser("treeorg",
                "DevUser",
                treeOrgUserPassword,
                "Dev,",
                "User",
                "dev@tree.org",
                true,
                new ClientRole[] {
                ClientRole.READ,
                ClientRole.WRITE
        });
    }
}
