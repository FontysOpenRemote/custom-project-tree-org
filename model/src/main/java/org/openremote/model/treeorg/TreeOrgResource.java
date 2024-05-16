package org.openremote.model.treeorg;

import jakarta.ws.rs.core.Response;


public interface TreeOrgResource {

    Response sortAssetsByAttribute(String assetType, String attributeName);
}
