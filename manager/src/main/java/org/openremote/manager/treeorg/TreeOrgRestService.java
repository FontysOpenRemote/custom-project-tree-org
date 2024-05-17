package org.openremote.manager.treeorg;

import org.openremote.manager.asset.AssetStorageService;
import org.openremote.manager.web.ManagerWebService;
import org.openremote.model.Container;
import org.openremote.model.ContainerService;

import java.util.logging.Logger;

public class TreeOrgRestService implements ContainerService {
    private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());
    protected AssetStorageService assetStorageService;
    protected SortingService sortingService;
    protected RouteOptimizationService routeOptimizationService;
    protected RouteService routeService;
    protected RouteApiClient routeApiClient;
    @Override
    public void init(Container container) throws Exception {
        ManagerWebService webService = container.getService(ManagerWebService.class);
        assetStorageService = container.getService(AssetStorageService.class);
        sortingService = container.getService(SortingService.class);

        routeService = container.getService(RouteService.class);
        routeOptimizationService = container.getService(RouteOptimizationService.class);
        routeApiClient = container.getService(RouteApiClient.class);
        webService.addApiSingleton(new TreeOrgResourceImplementation(sortingService, routeOptimizationService));
        LOG.info("Registered custom API classes: " + sortingService);
    }

    @Override
    public void start(Container container) throws Exception {

    }

    @Override
    public void stop(Container container) throws Exception {

    }

}
