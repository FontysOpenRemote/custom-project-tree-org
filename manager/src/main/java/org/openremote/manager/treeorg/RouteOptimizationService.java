package org.openremote.manager.treeorg;

import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.treeorg.TreeAsset;

import java.util.List;

public class RouteOptimizationService implements ContainerService {

    private SortingService sortingService;
    private RouteService routeService;

    @Override
    public void init(Container container) throws Exception {
        this.sortingService = container.getService(SortingService.class);
        this.routeService = container.getService(RouteService.class);
    }

    @Override
    public void start(Container container) throws Exception {
        // Perform initial optimization
        optimizeRouteForSensors(TreeAsset.class, "waterLevel");
        optimizeRouteForSensors(TreeAsset.class, "soilTemperature");
    }

    @Override
    public void stop(Container container) {
        // Any cleanup logic if needed
    }

    public RouteResponse optimizeRouteForSensors(Class<?> assetType, String attributeName) {
        // Delegate finding and sorting to SortingService
        List<Asset<?>> sortedSensors = sortingService.findAllAssetsSortedByAttributeAndType(assetType, attributeName);

        // Delegate route optimization to RouteService
        return routeService.optimizeRouteForSortedAssets(sortedSensors, attributeName);
    }
}
