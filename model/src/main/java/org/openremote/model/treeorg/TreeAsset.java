package org.openremote.model.treeorg;

import jakarta.persistence.Entity;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.MetaItemType;
import org.openremote.model.value.ValueFormat;
import org.openremote.model.value.ValueType;

import java.util.Optional;

import static org.openremote.model.Constants.UNITS_CELSIUS;

@Entity
public class TreeAsset extends Asset<TreeAsset> {

    public static final AttributeDescriptor<Double> SOIL_TEMPERATURE = new AttributeDescriptor<>("soilTemperature", ValueType.NUMBER,
            new MetaItem<>(MetaItemType.READ_ONLY)
    ).withUnits(UNITS_CELSIUS).withFormat(ValueFormat.NUMBER_1_DP());
    public static final AttributeDescriptor<Integer> WATER_LEVEL = new AttributeDescriptor<>("waterLevel", ValueType.POSITIVE_INTEGER,
            new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<String> TREE_TYPE = new AttributeDescriptor<>("treeType", ValueType.TEXT).withOptional(true);
    public static final AttributeDescriptor<Integer> ROUTE_ID = new AttributeDescriptor<>("routeId", ValueType.POSITIVE_INTEGER,
            new MetaItem<>(MetaItemType.READ_ONLY, true)
    );
    public static final AttributeDescriptor<Boolean> PRIORITY = new AttributeDescriptor<>("priority", ValueType.BOOLEAN,
            new MetaItem<>(MetaItemType.READ_ONLY, true)
    );

    public static final AssetDescriptor<TreeAsset> DESCRIPTOR = new AssetDescriptor<>("tree", "396d22", TreeAsset.class);

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    protected TreeAsset() {
    }
    public TreeAsset(String name) {
        super(name);
    }
    public Optional<Double> getTemperature() {
        return getAttributes().getValue(SOIL_TEMPERATURE);
    }
    public Optional<Integer> getWaterLevel() {
        return getAttributes().getValue(WATER_LEVEL);
    }
    public Optional<String> getTreeType() { return getAttributes().getValue(TREE_TYPE);}
    public Optional<Integer> getRouteId() { return getAttributes().getValue(ROUTE_ID);}
    public Optional<Boolean> getPriority() { return getAttributes().getValue(PRIORITY);}
}