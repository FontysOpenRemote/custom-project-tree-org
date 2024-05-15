package org.openremote.manager.setup.treeorg;

import groovyjarjarantlr4.runtime.tree.Tree;
import org.openremote.container.util.UniqueIdentifierGenerator;
import org.openremote.manager.setup.ManagerSetup;
import org.openremote.model.Container;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.impl.ThingAsset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.security.Realm;
import org.openremote.model.treeorg.TreeAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.openremote.model.value.MetaItemType.READ_ONLY;
import static org.openremote.model.value.MetaItemType.RULE_STATE;

public class TreeOrgManagerSetup extends ManagerSetup {

    public static final double BASE_LATITUDE = 51.43848672819468;
    public static final double BASE_LONGITUDE = 5.47967205919616;
    public static final double MAX_RADIUS = 4;
    private static final int BATCH_SIZE = 20;

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

        int amountOfAssets = 100;
        CreateTreeAssets(amountOfAssets, treeOrgAssets);

    }

    private void CreateTreeAssets(int amountOfAssets, Asset<?> parentAsset) {
        List<TreeAsset> assetList = new ArrayList<>();

        for (int i = 0; i < amountOfAssets; i++) {
            GeoJSONPoint randomLocation = generateRandomLocation();

            TreeAsset treeAsset = new TreeAsset("TreeAsset " + (i + 1));
            treeAsset.setParent(parentAsset);
            treeAsset.getAttributes().addOrReplace(new Attribute<>(Asset.LOCATION, randomLocation));

            // Generate a random water level between 1 and 10000
            Random random = new Random();
            int waterLevel = 1 + random.nextInt(10000);

            treeAsset.getAttributes().getOrCreate(treeAsset.WATER_LEVEL)
                    .addMeta(new MetaItem<>(RULE_STATE, true)).addMeta(new MetaItem<>(READ_ONLY, false))
                    .setValue(waterLevel);

            treeAsset.getAttributes().getOrCreate(TreeAsset.SOIL_TEMPERATURE)
                    .addMeta(new MetaItem<>(RULE_STATE, true)).addMeta(new MetaItem<>(READ_ONLY, false))
                    .setValue(21.0);  // Assuming a default value of 21.0 for soil temperature

            treeAsset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID)
                    .addMeta(new MetaItem<>(RULE_STATE, true))
                    .addMeta(new MetaItem<>(READ_ONLY, true))
                    .setValue(0);

            treeAsset.getAttributes().getOrCreate(TreeAsset.PRIORITY)
                    .addMeta(new MetaItem<>(RULE_STATE, true))
                    .addMeta(new MetaItem<>(READ_ONLY, true))
                    .setValue(false);
            treeAsset.setId(UniqueIdentifierGenerator.generateId(treeAsset.getName()));
            assetList.add(treeAsset);

            // If the batch size is reached, merge the batch and clear the list
            if (assetList.size() >= BATCH_SIZE) {
                for (TreeAsset asset : assetList) {
                    assetStorageService.merge(asset);
                }
                assetList.clear();
            }
        }
    }

    /**
     * Generates a random location within a specified radius from the base location.
     *
     * @return The generated GeoJSONPoint location.
     */
    private GeoJSONPoint generateRandomLocation() {
        Random random = new Random();
        double angle = 2 * Math.PI * random.nextDouble();
        double radius = MAX_RADIUS * random.nextDouble();
        double dx = radius * Math.cos(angle);
        double dy = radius * Math.sin(angle);
        return calculateNewLocation(dx, dy);
    }

    private GeoJSONPoint calculateNewLocation(double dx, double dy) {
        // Adjust the Cartesian coordinates to be offsets from the base location
        double latitude = BASE_LATITUDE + (dy / 111);
        double longitude = BASE_LONGITUDE + (dx / (111 * Math.cos(Math.toRadians(BASE_LATITUDE))));
        return new GeoJSONPoint(longitude, latitude);
    }
}
