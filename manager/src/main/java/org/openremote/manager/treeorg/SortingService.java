package org.openremote.manager.treeorg;

import org.openremote.manager.asset.AssetStorageService;
import org.openremote.manager.web.ManagerWebService;
import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.filter.AttributePredicate;
import org.openremote.model.treeorg.TreeAsset;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SortingService implements ContainerService {
    private AssetStorageService assetStorageService;
    private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());

    /**
     * Finds all assets of a specific type sorted by the specified attribute.
     * @param attributeName The name of the attribute to sort on.
     * @param assetType The type of assets to filter.
     * @param <T> The type of the attribute to sort on.
     * @return List of assets of the specified type sorted by the specified attribute.
     */
    public <T extends Comparable<T>> List<Asset<?>> findAllAssetsSortedByAttributeAndType(Class<?> assetType, String attributeName) {
        AssetQuery query = new AssetQuery()
                .types((Class<? extends Asset<?>>) assetType)
                .attributes(new AttributePredicate(attributeName, null));

        List<Asset<?>> assets = assetStorageService.findAll(query).stream()
                .filter(asset -> asset.getAttributes().get(attributeName).isPresent())
                .filter(asset -> {
                    Optional<Attribute<?>> attribute = asset.getAttributes().get(attributeName);
                    return attribute.map(attr -> attr.getValue().orElse(null)).orElse(null) != null;
                })
                .sorted(Comparator.comparing(asset -> {
                    Optional<Attribute<?>> attribute = asset.getAttributes().get(attributeName);
                    return (T) attribute.get().getValue().get();
                }))
                .limit(10)
                .collect(Collectors.toList());

        if (assets.isEmpty()) {
            LOG.info("No assets with non-null values found for attribute: " + attributeName);
        } else {
            assets.forEach(asset -> {
                Optional<Attribute<?>> attribute = asset.getAttributes().get(attributeName);
                attribute.ifPresent(attr -> {
                    Optional<?> value = attr.getValue();
                    if (value.isPresent() && value.get() instanceof Number) {
                        Integer attributeValue = ((Number) value.get()).intValue();
                        LOG.info("Asset ID: " + asset.getId() + asset.getName() + " - " + attributeName + ": " + attributeValue);
                    } else {
                        LOG.info("Asset ID: " + asset.getId() + " - " + attributeName + " is not a number or not present.");
                    }
                });
            });
        }
        return assets;
    }

    /**
     * All services are initialized in the order they have been added to the container (if container started with
     * explicit list of services) otherwise they are initialized in order of {@link #getPriority}.
     *
     * @param container
     */
    @Override
    public void init(Container container) throws Exception {
        this.assetStorageService = container.getService(AssetStorageService.class);
    }

    /**
     * After initialization, services are started in the order they have been added to the container (if container
     * started with explicit list of services) otherwise they are started in order of {@link #getPriority}.
     *
     * @param container
     */
    @Override
    public void start(Container container) throws Exception {
        Class<?> assetType = TreeAsset.class;
        var attributeName = new Attribute<>("waterLevel").getName();
        findAllAssetsSortedByAttributeAndType(assetType, attributeName);
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
