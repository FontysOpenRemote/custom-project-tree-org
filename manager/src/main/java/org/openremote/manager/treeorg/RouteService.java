package org.openremote.manager.treeorg;

import org.openremote.manager.asset.AssetStorageService;
import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.treeorg.TreeAsset;

import java.util.*;
import java.util.logging.Logger;

import static org.openremote.model.asset.Asset.NOTES;

public class RouteService implements ContainerService {

    private AssetStorageService assetStorageService;
    private static final Logger LOG = Logger.getLogger(RouteService.class.getName());
    public RouteService() {
    }

    @Override
    public void init(Container container) throws Exception {
        this.assetStorageService = container.getService(AssetStorageService.class);
    }

    @Override
    public void start(Container container) throws Exception {
    }

    @Override
    public void stop(Container container) throws Exception {
    }

    /**
     * Optimizes the route for a list of sorted assets based on a specified attribute.
     *
     * @param sortedAssets  A list of sorted assets to optimize the route for.
     * @param attributeName The name of the attribute used for sorting.
     * @return A RouteResponse containing the Google Maps URLs for the optimized routes and the ordered assets.
     */
    public RouteResponse optimizeRouteForSortedAssets(List<Asset<?>> sortedAssets, String attributeName) {
        List<double[]> coordinates = extractCoordinates(sortedAssets);
        double[] startingPosition = {5.453487298268298, 51.45081456926727};

        // Generate the new closest-next-point route
        List<double[]> newOptimalRoute = findOptimalRoute(coordinates, startingPosition);
        String newGoogleMapsURL = generateGoogleMapsURL(newOptimalRoute);
        LOG.info("View new route on Google Maps: " + newGoogleMapsURL);

        // Update the parent asset with the Google Maps URL
        updateParentAssetWithGoogleMapsURL(newGoogleMapsURL, sortedAssets);

        // Update route IDs for the assets
        updateRouteIds(sortedAssets);

        return new RouteResponse(newGoogleMapsURL, sortedAssets);
    }

    /**
     * Finds the optimal route using the closest-next-point algorithm.
     *
     * @param coordinates      List of coordinates.
     * @param startingPosition Starting position.
     * @return List of coordinates representing the optimal route.
     */
    private List<double[]> findOptimalRoute(List<double[]> coordinates, double[] startingPosition) {
        List<double[]> route = new ArrayList<>();
        boolean[] visited = new boolean[coordinates.size()];
        double[] currentPosition = startingPosition;
        route.add(currentPosition);

        for (int i = 0; i < coordinates.size(); i++) {
            double minDistance = Double.MAX_VALUE;
            int closestPointIndex = -1;
            for (int j = 0; j < coordinates.size(); j++) {
                if (!visited[j]) {
                    double distance = calculateDistance(currentPosition, coordinates.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestPointIndex = j;
                    }
                }
            }
            if (closestPointIndex != -1) {
                visited[closestPointIndex] = true;
                currentPosition = coordinates.get(closestPointIndex);
                route.add(currentPosition);
            }
        }
        route.add(startingPosition); // Return to start
        return route;
    }

    /**
     * Calculates the distance between two coordinates.
     *
     * @param point1 First point.
     * @param point2 Second point.
     * @return Distance between the two points.
     */
    private double calculateDistance(double[] point1, double[] point2) {
        double dx = point1[0] - point2[0];
        double dy = point1[1] - point2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Updates the Google Maps URL attribute for the relevant assets and clears it for others.
     *
     * @param googleMapsURL The Google Maps URL to set.
     */
    private void updateParentAssetWithGoogleMapsURL(String googleMapsURL, List<Asset<?>> sortedAssets) {
        Asset<?> parentAsset = findParentAsset(sortedAssets);
        if (parentAsset != null) {
            parentAsset.getAttributes().getOrCreate(NOTES).setValue(googleMapsURL);
            assetStorageService.merge(parentAsset);
            LOG.info("Updated parent asset " + parentAsset.getName() + " ID: " + parentAsset.getId() + " with Google Maps URL");
        }
    }

    private Asset<?> findParentAsset(List<Asset<?>> sortedAssets) {
        for (Asset<?> asset : sortedAssets) {
            String parentId = asset.getParentId();
            if (parentId != null) {
                return assetStorageService.find(parentId);
            }
        }
        LOG.severe("Parent asset not found");
        return null;
    }

    /**
     * Updates the routeId attribute of each asset in the ordered list and resets routeId to 0 for other assets.
     *
     * @param orderedAssets The list of assets in the order of the optimized route.
     */
    private void updateRouteIds(List<Asset<?>> orderedAssets) {
        Map<String, Integer> assetIdToRouteIdMap = new HashMap<>();
        for (int i = 0; i < orderedAssets.size(); i++) {
            assetIdToRouteIdMap.put(orderedAssets.get(i).getId(), i + 1);
        }

        // Convert the list of asset IDs to an array
        String[] assetIdsArray = assetIdToRouteIdMap.keySet().toArray(new String[0]);

        // Find the relevant assets by their IDs
        AssetQuery query = new AssetQuery().types(TreeAsset.class).ids(assetIdsArray);
        List<Asset<?>> relevantAssets = assetStorageService.findAll(query);

        // Update routeId attributes for relevant assets
        for (Asset<?> asset : relevantAssets) {
            Integer newRouteId = assetIdToRouteIdMap.get(asset.getId());
            asset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID).setValue(newRouteId);
            assetStorageService.merge(asset);
            LOG.info("Setting route ID " + newRouteId + " for asset: " + asset.getName());
        }

        // Reset routeId for assets not in the ordered list
        for (String assetId : assetIdToRouteIdMap.keySet()) {
            if (!assetIdToRouteIdMap.containsKey(assetId)) {
                Asset<?> asset = assetStorageService.find(assetId);
                if (asset != null) {
                    asset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID).setValue(0);
                    assetStorageService.merge(asset);
                    LOG.info("Resetting route ID to 0 for asset ID: " + asset.getId());
                }
            }
        }
    }

    /**
     * Extracts coordinates from a list of assets.
     *
     * @param assets List of assets to extract coordinates from.
     * @return List of coordinates in [longitude, latitude] format.
     */
    private List<double[]> extractCoordinates(List<Asset<?>> assets) {
        List<double[]> coordinates = new ArrayList<>();
        assets.forEach(asset -> {
            Optional<Attribute<?>> locationAttribute = asset.getAttributes().get("location");
            locationAttribute.ifPresent(attr -> {
                GeoJSONPoint point = (GeoJSONPoint) attr.getValue().orElse(null);
                if (point != null) {
                    coordinates.add(new double[]{point.getX(), point.getY()});
                }
            });
        });
        return coordinates;
    }

    /**
     * Prints the ordered list of assets.
     *
     * @param assets List of ordered assets.
     */
    private void printOrderedAssets(List<Asset<?>> assets, String attributeName) {
        LOG.info("Ordered visitation list:");
        assets.forEach(asset -> LOG.info("Visit Asset ID: " + asset.getId() + " - "
                + asset.getName() + " - " + asset.getAttribute(attributeName)));
    }

    /**
     * Generates a Google Maps URL for the given list of coordinates.
     *
     * @param coordinates List of coordinates to include in the URL.
     * @return Google Maps URL for the route.
     */
    public String generateGoogleMapsURL(List<double[]> coordinates) {
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/");
        coordinates.forEach(coord -> url.append(coord[1]).append(",").append(coord[0]).append("/"));
        return url.toString();
    }
}
