package org.openremote.manager.treeorg;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.openremote.model.asset.Asset;
import org.openremote.model.treeorg.TreeOrgResource;

import java.util.List;

@Path("/")
public class TreeOrgResourceImplementation implements TreeOrgResource {

    private final SortingService sortingService;
    private final RouteOptimizationService routeOptimizationService;

    public TreeOrgResourceImplementation(SortingService sortingService, RouteOptimizationService routeOptimizationService) {
        this.sortingService = sortingService;
        this.routeOptimizationService = routeOptimizationService;
    }

    @GET
    @Path("sortbyattribute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sortAssetsByAttribute(@QueryParam("assetType") String assetType, @QueryParam("attribute") String attributeName) {
        Class<?> type = null;
        try {
            type = Class.forName(assetType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<Asset<?>> sortedAssets = sortingService.findAllAssetsSortedByAttributeAndType(type, attributeName);
        return Response.ok(sortedAssets).build();
    }

    @GET
    @Path("optimizeRoute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response optimizeRouteForSensors(@QueryParam("assetType") String assetType, @QueryParam("attribute") String attributeName) {
        Class<?> type = null;
        try {
            type = Class.forName(assetType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        RouteResponse routeResponse = routeOptimizationService.optimizeRouteForSensors(type, attributeName);
        return Response.ok(routeResponse).build();
    }
}
