package org.openremote.test.treeorg

import org.openremote.container.Container
import org.openremote.manager.treeorg.RouteOptimizationService
import org.openremote.manager.treeorg.RouteResponse
import org.openremote.manager.treeorg.RouteService
import org.openremote.manager.treeorg.SortingService
import org.openremote.model.asset.Asset
import org.openremote.model.treeorg.TreeAsset
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

class RouteOptimizationServiceTests extends Specification implements ManagerContainerTrait {

    def "RouteOptimizationService should optimize route for sensors"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
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
        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, _ as String) >> [assetA, assetC, assetB]

        // Mock the behavior of the RouteService
        routeService.optimizeRouteForSortedAssets(_ as List<Asset<?>>, _ as String) >> new RouteResponse("url", [assetA, assetC, assetB])

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

    def "RouteOptimizationService should handle empty asset list"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Mock the behavior of the SortingService
        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, _ as String) >> []

        // Mock the behavior of the RouteService
        routeService.optimizeRouteForSortedAssets(_ as List<Asset<?>>, _ as String) >> new RouteResponse("url", [])

        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "waterLevel")

        then:
        // Verify the result is not null and has no assets
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteOptimizationService should handle no assets"() {
        setup:
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, _ as String) >> []

        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "waterLevel")

        then:
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteOptimizationService should handle null attribute name"() {
        setup:
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, _ as String) >> []

        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, null)

        then:
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteOptimizationService should handle one asset"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Create a single TreeAsset instance with a waterLevel attribute
        def assetA = new TreeAsset()
        assetA.setId("1")
        assetA.setName("Asset A")
        assetA.getAttributes().getOrCreate(TreeAsset.WATER_LEVEL).setValue(10)

        // Mock the behavior of the SortingService
        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, _ as String) >> [assetA]

        // Mock the behavior of the RouteService
        routeService.optimizeRouteForSortedAssets(_ as List<Asset<?>>, _ as String) >> new RouteResponse("url", [assetA])

        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "waterLevel")

        then:
        // Verify the result is not null and has the expected asset
        result != null
        result.getOrderedAssets().size() == 1
        result.getOrderedAssets()[0].getId() == "1" // assetA with waterLevel 10
    }

    def "RouteOptimizationService should handle different attributes"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Create TreeAsset instances with different attributes
        def assetA = new TreeAsset()
        def assetB = new TreeAsset()
        assetA.setId("1")
        assetA.setName("Asset A")
        assetA.getAttributes().getOrCreate(TreeAsset.SOIL_TEMPERATURE).setValue(15.5)

        assetB.setId("2")
        assetB.setName("Asset B")
        assetB.getAttributes().getOrCreate(TreeAsset.SOIL_TEMPERATURE).setValue(18.3)

        // Mock the behavior of the SortingService
        sortingService.findAllAssetsSortedByAttributeAndType(_ as Class<?>, "soilTemperature") >> [assetA, assetB]

        // Mock the behavior of the RouteService
        routeService.optimizeRouteForSortedAssets(_ as List<Asset<?>>, "soilTemperature") >> new RouteResponse("url", [assetA, assetB])

        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "soilTemperature")

        then:
        // Verify the result is not null and has the expected assets
        result != null
        result.getOrderedAssets().size() == 2
        result.getOrderedAssets()[0].getId() == "1" // assetA with soilTemperature 15.5
        result.getOrderedAssets()[1].getId() == "2" // assetB with soilTemperature 18.3
    }

    class InvalidAssetType {}

    def "RouteOptimizationService should handle invalid asset type"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Define a custom invalid class within the test


        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test with the invalid asset type
        def result = routeOptimizationService.optimizeRouteForSensors(InvalidAssetType, "waterLevel")

        then:
        // Verify the result is not null and has no assets
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteOptimizationService should handle invalid attribute name"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def sortingService = Mock(SortingService)
        def routeService = Mock(RouteService)
        container.getService(SortingService) >> sortingService
        container.getService(RouteService) >> routeService

        // Initialize RouteOptimizationService with the mocked services
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService()
        routeOptimizationService.init(container)

        when:
        // Call the method under test with an invalid attribute name
        def result = routeOptimizationService.optimizeRouteForSensors(TreeAsset, "invalidAttributeName")

        then:
        // Verify the result is not null and has no assets
        result != null
        result.getOrderedAssets().isEmpty()
    }


}
