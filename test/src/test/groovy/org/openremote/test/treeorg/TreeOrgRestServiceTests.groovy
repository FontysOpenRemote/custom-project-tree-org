package org.openremote.test.treeorg

import org.openremote.manager.asset.AssetStorageService
import org.openremote.manager.treeorg.*
import org.openremote.manager.web.ManagerWebService
import org.openremote.model.Container
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

class TreeOrgRestServiceTests extends Specification implements ManagerContainerTrait {

    def "TreeOrgRestService should initialize and register API classes"() {
        setup:
        // Mock the Container and services
        Container container = Mock(Container)
        ManagerWebService managerWebService = Mock(ManagerWebService)
        AssetStorageService assetStorageService = Mock(AssetStorageService)
        SortingService sortingService = Mock(SortingService)
        RouteOptimizationService routeOptimizationService = Mock(RouteOptimizationService)
        RouteService routeService = Mock(RouteService)
        RouteApiClient routeApiClient = Mock(RouteApiClient)
        container.getService(ManagerWebService) >> managerWebService
        container.getService(AssetStorageService) >> assetStorageService
        container.getService(SortingService) >> sortingService
        container.getService(RouteOptimizationService) >> routeOptimizationService
        container.getService(RouteService) >> routeService
        container.getService(RouteApiClient) >> routeApiClient

        // Register the API singleton
        managerWebService.addApiSingleton(_) >> { args ->
            assert args[0] instanceof TreeOrgResourceImplementation
        }

        // Initialize TreeOrgRestService with the mocked services
        TreeOrgRestService treeOrgRestService = new TreeOrgRestService()
        treeOrgRestService.init(container)

        expect:
        // Verify the services are initialized correctly
        treeOrgRestService.assetStorageService == assetStorageService
        treeOrgRestService.sortingService == sortingService
        treeOrgRestService.routeService == routeService
        treeOrgRestService.routeOptimizationService == routeOptimizationService
        treeOrgRestService.routeApiClient == routeApiClient
    }
}
