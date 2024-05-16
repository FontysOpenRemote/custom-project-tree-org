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
    @Override
    public void init(Container container) throws Exception {
        ManagerWebService webService = container.getService(ManagerWebService.class);
        assetStorageService = container.getService(AssetStorageService.class);
        sortingService = container.getService(SortingService.class);
        webService.addApiSingleton(new TreeOrgResourceImplementation(sortingService));
    }

    /**
     * After initialization, services are started in the order they have been added to the container (if container
     * started with explicit list of services) otherwise they are started in order of {@link #getPriority}.
     *
     * @param container
     */
    @Override
    public void start(Container container) throws Exception {

    }

    /**
     * When the container is shutting down, it stops all services in the reverse order they were started.
     *
     * @param container
     */
    @Override
    public void stop(Container container) throws Exception {

    }

}
