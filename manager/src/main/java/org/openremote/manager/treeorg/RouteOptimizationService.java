package org.openremote.manager.treeorg;

import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.treeorg.TreeAsset;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class RouteOptimizationService implements ContainerService {
    private static final Logger LOG = Logger.getLogger(RouteService.class.getName());
    private SortingService sortingService;
    private RouteService routeService;

    @Override
    public void init(Container container) throws Exception {
        this.sortingService = container.getService(SortingService.class);
        this.routeService = container.getService(RouteService.class);
    }

    @Override
    public void start(Container container) {
        // Perform initial optimization
        optimizeRouteForSensors(TreeAsset.class, "waterLevel");
        optimizeRouteForSensors(TreeAsset.class, "soilTemperature");
    }

    @Override
    public void stop(Container container) {
        // Any cleanup logic if needed
    }

    public RouteResponse optimizeRouteForSensors(Class<?> assetType, String attributeName) {
        if (assetType == null || attributeName == null || attributeName.isEmpty()) {
            LOG.severe("Asset type or attribute name is null or empty. Unable to optimize route.");
            return new RouteResponse(null, Collections.emptyList());
        }

        List<Asset<?>> sortedSensors = sortingService.findAllAssetsSortedByAttributeAndType(assetType, attributeName);
        if (sortedSensors == null || sortedSensors.isEmpty()) {
            LOG.severe("No sorted sensors found for the given attribute. Unable to optimize route.");
            return new RouteResponse(null, Collections.emptyList());
        }

        // Delegate route optimization to RouteService
        return routeService.optimizeRouteForSortedAssets(sortedSensors, attributeName);
    }

}
