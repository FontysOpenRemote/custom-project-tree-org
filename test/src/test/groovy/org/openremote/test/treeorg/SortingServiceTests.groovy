package org.openremote.test.treeorg

import org.openremote.manager.asset.AssetStorageService
import org.openremote.manager.treeorg.SortingService
import org.openremote.model.query.AssetQuery
import org.openremote.model.treeorg.TreeAsset
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

class SortingServiceTests extends Specification implements ManagerContainerTrait {

    def "SortingService should find and sort assets by attribute and type"() {
        setup:
        // Mock the Container and services
        def container = Mock(org.openremote.container.Container)
        def assetStorageService = Mock(AssetStorageService)
        container.getService(AssetStorageService) >> assetStorageService

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

        // Mock the behavior of the AssetStorageService
        assetStorageService.findAll(_ as AssetQuery) >> [assetB, assetC, assetA]

        // Initialize SortingService with the mocked AssetStorageService
        SortingService sortingService = new SortingService()
        sortingService.init(container)

        when:
        // Call the method under test
        def sortedAssets = sortingService.findAllAssetsSortedByAttributeAndType(TreeAsset.class, "waterLevel")

        then:
        // Verify the assets are sorted by waterLevel
        sortedAssets.size() == 3
        sortedAssets[0].getId() == "1" // assetA with waterLevel 10
        sortedAssets[1].getId() == "3" // assetC with waterLevel 20
        sortedAssets[2].getId() == "2" // assetB with waterLevel 30
    }
}
