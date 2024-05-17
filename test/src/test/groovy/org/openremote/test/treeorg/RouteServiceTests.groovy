package org.openremote.test.treeorg

import org.openremote.container.Container
import org.openremote.manager.asset.AssetStorageService
import org.openremote.manager.treeorg.RouteApiClient
import org.openremote.manager.treeorg.RouteService
import org.openremote.model.geo.GeoJSONPoint
import org.openremote.model.query.AssetQuery
import org.openremote.model.treeorg.TreeAsset
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

class RouteServiceTests extends Specification implements ManagerContainerTrait {

    def "RouteService should optimize route for sorted assets"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def assetStorageService = Mock(AssetStorageService)
        def routeApiClient = Mock(RouteApiClient)
        container.getService(AssetStorageService) >> assetStorageService
        container.getService(RouteApiClient) >> routeApiClient

        // Create actual TreeAsset instances with location attributes
        def assetA = new TreeAsset()
        def assetB = new TreeAsset()
        def assetC = new TreeAsset()

        def pointA = new GeoJSONPoint(5.453487298268298, 51.45081456926727)
        def pointB = new GeoJSONPoint(5.453487298268300, 51.45081456926729)
        def pointC = new GeoJSONPoint(5.453487298268310, 51.45081456926730)

        assetA.setId("1")
        assetA.setName("Asset A")
        assetA.getAttributes().getOrCreate(TreeAsset.LOCATION).setValue(pointA)

        assetB.setId("2")
        assetB.setName("Asset B")
        assetB.getAttributes().getOrCreate(TreeAsset.LOCATION).setValue(pointB)

        assetC.setId("3")
        assetC.setName("Asset C")
        assetC.getAttributes().getOrCreate(TreeAsset.LOCATION).setValue(pointC)

        // Mock the behavior of the AssetStorageService
        assetStorageService.findAll(_ as AssetQuery) >> [assetA, assetB, assetC]

        // Mock the behavior of the RouteApiClient
        routeApiClient.callOpenRouteService(_ as String) >> '{"routes": [{"steps": [{"job": 1, "location": [5.453487298268298, 51.45081456926727]}, {"job": 2, "location": [5.453487298268300, 51.45081456926729]}, {"job": 3, "location": [5.453487298268310, 51.45081456926730]}]}]}'

        // Initialize RouteService with the mocked services
        RouteService routeService = new RouteService()
        routeService.init(container)

        when:
        // Call the method under test
        def result = routeService.optimizeRouteForSortedAssets([assetA, assetB, assetC], "location")

        then:
        // Verify the result is not null and has the expected assets
        result != null
        result.getOrderedAssets().size() == 3
        result.getOrderedAssets()[0].getId() == "1" // assetA with location pointA
        result.getOrderedAssets()[1].getId() == "2" // assetB with location pointB
        result.getOrderedAssets()[2].getId() == "3" // assetC with location pointC
    }

    def "RouteService should handle empty asset list"() {
        setup:
        // Mock the Container and services
        def container = Mock(Container)
        def assetStorageService = Mock(AssetStorageService)
        def routeApiClient = Mock(RouteApiClient)
        container.getService(AssetStorageService) >> assetStorageService
        container.getService(RouteApiClient) >> routeApiClient

        // Mock the behavior of the AssetStorageService
        assetStorageService.findAll(_ as AssetQuery) >> []

        // Mock the behavior of the RouteApiClient
        routeApiClient.callOpenRouteService(_ as String) >> '{"routes": [{"steps": []}]}'

        // Initialize RouteService with the mocked services
        RouteService routeService = new RouteService()
        routeService.init(container)

        when:
        // Call the method under test
        def result = routeService.optimizeRouteForSortedAssets([], "location")

        then:
        // Verify the result is not null and has no assets
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteService should handle no coordinates"() {
        setup:
        def container = Mock(Container)
        def assetStorageService = Mock(AssetStorageService)
        container.getService(AssetStorageService) >> assetStorageService

        RouteService routeService = new RouteService()
        routeService.init(container)

        when:
        def result = routeService.optimizeRouteForSortedAssets([], "location")

        then:
        result != null
        result.getOrderedAssets().isEmpty()
    }

    def "RouteService should handle one coordinate"() {
        setup:
        def container = Mock(Container)
        def assetStorageService = Mock(AssetStorageService)
        container.getService(AssetStorageService) >> assetStorageService

        def assetA = new TreeAsset()
        def pointA = new GeoJSONPoint(5.453487298268298, 51.45081456926727)
        assetA.setId("1")
        assetA.setName("Asset A")
        assetA.getAttributes().getOrCreate(TreeAsset.LOCATION).setValue(pointA)

        RouteService routeService = new RouteService()
        routeService.init(container)

        when:
        def result = routeService.optimizeRouteForSortedAssets([assetA], "location")

        then:
        result != null
        result.getOrderedAssets().size() == 1
        result.getOrderedAssets()[0].getId() == "1"
    }

    def "RouteService should correctly calculate distance"() {
        setup:
        RouteService routeService = new RouteService()

        expect:
        routeService.calculateDistance([0, 0] as double[], [3, 4] as double[]) == 5.0
        routeService.calculateDistance([0, 0] as double[], [0, 0] as double[]) == 0.0
        routeService.calculateDistance([1, 1] as double[], [4, 5] as double[]) == 5.0
    }
}
