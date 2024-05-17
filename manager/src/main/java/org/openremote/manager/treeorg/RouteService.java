package org.openremote.manager.treeorg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.manager.asset.AssetStorageService;
import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.treeorg.TreeAsset;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.openremote.model.asset.Asset.NOTES;
public class RouteService implements ContainerService {

    private AssetStorageService assetStorageService;
    private static final Logger LOG = Logger.getLogger(RouteService.class.getName());
    private RouteApiClient routeApiClient;
    private Map<Integer, String> jobIdToAssetIdMap = new HashMap<>();

    public RouteService() {
    }

    @Override
    public void init(Container container) throws Exception {
        this.assetStorageService = container.getService(AssetStorageService.class);
        this.routeApiClient = container.getService(RouteApiClient.class);
    }

    @Override
    public void start(Container container) throws Exception {
        // Any start logic if needed
    }

    @Override
    public void stop(Container container) throws Exception {
        // Any cleanup logic if needed
    }

    /**
     * Optimizes the route for a list of sorted assets based on a specified attribute.
     *
     * @param sortedAssets  A list of sorted assets to optimize the route for.
     * @param attributeName The name of the attribute used for sorting.
     * @return A RouteResponse containing the Google Maps URL for the optimized route and the ordered assets.
     */
    public RouteResponse optimizeRouteForSortedAssets(List<Asset<?>> sortedAssets, String attributeName) {
        List<double[]> coordinates = extractCoordinates(sortedAssets);

        double[] startingPosition = {5.453487298268298, 51.45081456926727};
        String tspQuery = prepareTSPQuery(coordinates, sortedAssets, startingPosition);
        try {
            String routeResponse = routeApiClient.callOpenRouteService(tspQuery);
            LOG.info("Route Response: " + routeResponse);
            List<Asset<?>> orderedAssets = orderAssetsByRouteResponse(routeResponse, sortedAssets);
            updateRouteIds(orderedAssets);
            printOrderedAssets(orderedAssets, attributeName);

            List<double[]> routeCoordinates = extractCoordinates(orderedAssets);
            routeCoordinates.add(0, startingPosition); // Add starting position at the beginning
            routeCoordinates.add(startingPosition); // Add starting position at the end

            String googleMapsURL = generateGoogleMapsURL(routeCoordinates);
            LOG.info("View route on Google Maps: " + googleMapsURL);

            updateParentAssetWithGoogleMapsURL(googleMapsURL, sortedAssets);

            return new RouteResponse(googleMapsURL, orderedAssets);
        } catch (IOException | InterruptedException e) {
            LOG.severe("Failed to call OpenRouteService: " + e.getMessage());
            return null;
        }
    }


    /**
     * Prepares the TSP (Travelling Salesperson Problem) query for the OpenRouteService API.
     *
     * @param coordinates      A list of coordinates for the assets in [longitude, latitude] format.
     * @param sortedSensors    A list of sorted assets that represent the sensors.
     * @param startingPosition The starting position for the route in [longitude, latitude] format.
     * @return A JSON string representing the TSP query.
     */
    private String prepareTSPQuery(List<double[]> coordinates, List<Asset<?>> sortedSensors, double[] startingPosition) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        ArrayNode jobs = mapper.createArrayNode();
        ArrayNode vehicles = mapper.createArrayNode();

        ObjectNode vehicle = mapper.createObjectNode();
        vehicle.put("id", 1);
        vehicle.set("start", mapper.createArrayNode().add(startingPosition[0]).add(startingPosition[1]));
        vehicle.put("return_to_depot", true);
        vehicle.put("profile", "driving-car");
        vehicles.add(vehicle);

        for (int i = 0; i < coordinates.size(); i++) {
            ObjectNode job = mapper.createObjectNode();
            int jobId = i + 1;
            job.put("id", jobId); // Use a unique integer as job ID
            job.set("location", mapper.createArrayNode().add(coordinates.get(i)[0]).add(coordinates.get(i)[1]));
            jobs.add(job);

            // Map job ID to original asset ID for reference
            jobIdToAssetIdMap.put(jobId, sortedSensors.get(i).getId());
        }

        payload.set("vehicles", vehicles);
        payload.set("jobs", jobs);
        return payload.toString();
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
     * Orders assets based on the route response from the OpenRouteService API.
     *
     * @param routeResponse JSON response from the OpenRouteService API.
     * @param assets        List of assets to order.
     * @return Ordered list of assets based on the optimized route.
     * @throws IOException throws exception
     */
    private List<Asset<?>> orderAssetsByRouteResponse(String routeResponse, List<Asset<?>> assets) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(routeResponse);

        LOG.info("Root Node: " + rootNode.toPrettyString());

        JsonNode stepsNode = rootNode.path("routes").get(0).path("steps");

        Map<Integer, double[]> orderedCoordinates = new LinkedHashMap<>();
        if (stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                int jobId = stepNode.path("job").asInt();
                double[] coord = new double[2];
                coord[0] = stepNode.path("location").get(0).asDouble();
                coord[1] = stepNode.path("location").get(1).asDouble();
                orderedCoordinates.put(jobId, coord);
            }
        }

        return mapCoordinatesToAssets(orderedCoordinates, assets);
    }

    /**
     * Maps the ordered coordinates to their corresponding assets.
     *
     * @param orderedCoordinates Map of job ID to coordinates.
     * @param assets             List of assets to map.
     * @return List of assets ordered by the provided coordinates.
     */
    private List<Asset<?>> mapCoordinatesToAssets(Map<Integer, double[]> orderedCoordinates, List<Asset<?>> assets) {
        Map<String, Asset<?>> coordinatesToAssetsMap = new HashMap<>();
        for (Asset<?> asset : assets) {
            double[] assetCoordinates = extractCoordinatesFromAsset(asset);
            String key = Arrays.toString(assetCoordinates);
            coordinatesToAssetsMap.put(key, asset);
        }

        List<Asset<?>> orderedAssets = new ArrayList<>();
        for (Map.Entry<Integer, double[]> entry : orderedCoordinates.entrySet()) {
            String key = Arrays.toString(entry.getValue());
            if (coordinatesToAssetsMap.containsKey(key)) {
                Asset<?> asset = coordinatesToAssetsMap.get(key);
                if (!orderedAssets.contains(asset)) {
                    orderedAssets.add(asset);
                }
            }
        }

        // Ensure no duplicates and exactly 10 assets
        Set<Asset<?>> uniqueAssets = new LinkedHashSet<>(orderedAssets);
        if (uniqueAssets.size() < 10) {
            assets.stream()
                    .filter(asset -> !uniqueAssets.contains(asset))
                    .limit(10 - uniqueAssets.size())
                    .forEach(uniqueAssets::add);
        }

        LOG.info("Total assets after mapping: " + uniqueAssets.size());
        return new ArrayList<>(uniqueAssets);
    }

    /**
     * Extracts coordinates from a single asset.
     *
     * @param asset The asset to extract coordinates from.
     * @return Coordinates of the asset in [longitude, latitude] format.
     */
    private double[] extractCoordinatesFromAsset(Asset<?> asset) {
        Optional<Attribute<?>> locationAttribute = asset.getAttributes().get("location");
        if (locationAttribute.isPresent()) {
            GeoJSONPoint point = (GeoJSONPoint) locationAttribute.get().getValue().orElse(null);
            if (point != null) {
                return new double[]{point.getX(), point.getY()};
            }
        }
        return null;
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

