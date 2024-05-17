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

    public void setGoogleMapsURL(String googleMapsURL) {
        this.googleMapsURL = googleMapsURL;
    }

    public List<Asset<?>> getOrderedAssets() {
        return orderedAssets;
    }

    public void setOrderedAssets(List<Asset<?>> orderedAssets) {
        this.orderedAssets = orderedAssets;
    }
}
