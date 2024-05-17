package org.openremote.manager.treeorg;

import org.openremote.model.asset.Asset;

import java.util.List;

public class RouteResponse {
    private String googleMapsURL;
    private List<Asset<?>> orderedAssets;

    public RouteResponse(String googleMapsURL, List<Asset<?>> orderedAssets) {
        this.googleMapsURL = googleMapsURL;
        this.orderedAssets = orderedAssets;
    }

    public String getGoogleMapsURL() {
        return googleMapsURL;
    }

    public List<Asset<?>> getOrderedAssets() {
        return orderedAssets;
    }
}
