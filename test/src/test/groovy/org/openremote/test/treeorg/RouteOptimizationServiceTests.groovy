package org.openremote.test.treeorg

import org.openremote.manager.treeorg.RouteOptimizationService
import org.openremote.manager.treeorg.RouteResponse
import org.openremote.manager.treeorg.RouteService
import org.openremote.manager.treeorg.SortingService
import org.openremote.model.treeorg.TreeAsset
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

class RouteOptimizationServiceTests extends Specification implements ManagerContainerTrait {

    def "RouteOptimizationService should optimize route for sensors"() {
        setup:
        // Mock the Container and services
        def container = Mock(org.openremote.container.Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Create actual TreeAsset instances with waterLevel attributes
        def assetA = new TreeAsset()
        def assetB = new TreeAsset()
        def assetC = new TreeAsset()

        assetA.setId("1")
        assetA.setName("Asset A")
        assetA.getAttributes().getOrCreate(TreeAsset.WATER_LEVEL).setValue(10)

        assetB.setId("2")
        assetB.setName("Asset B")
        assetB.getAttributes().getOrCreate(TreeAsset.WATER_LEVEL).setValue(30)

        assetC.setId("3")
        assetC.setName("Asset C")
        assetC.getAttributes().getOrCreate(TreeAsset.WATER_LEVEL).setValue(20)

        // Mock the behavior of the SortingService
        sortingService.findAllAssetsSortedByAttributeAndType(_, _) >> [assetA, assetC, assetB]

        // Mock the behavior of the RouteService
        routeService.optimizeRouteForSortedAssets(_, _) >> new RouteResponse("url", [assetA, assetC, assetB])

        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "waterLevel")

        then:
        // Verify the result is not null and has the expected assets
        result != null
        result.getOrderedAssets().size() == 3
        result.getOrderedAssets()[0].getId() == "1" // assetA with waterLevel 10
        result.getOrderedAssets()[1].getId() == "3" // assetC with waterLevel 20
        result.getOrderedAssets()[2].getId() == "2" // assetB with waterLevel 30
    }
}
