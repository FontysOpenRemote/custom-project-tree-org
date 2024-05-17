package org.openremote.manager.treeorg;

import org.openremote.model.Container;
import org.openremote.model.ContainerService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class RouteApiClient implements ContainerService {

    private static final Logger LOG = Logger.getLogger(RouteApiClient.class.getName());
    private static final String ORS_API_KEY = "5b3ce3597851110001cf6248a3a8675f23d74b67a8e01dfe64f8d363";

    @Override
    public void init(Container container) throws Exception {

    }

    @Override
    public void start(Container container) throws Exception {

    }

    @Override
    public void stop(Container container) throws Exception {

    }

    public String callOpenRouteService(String query) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/optimization"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ORS_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String rateLimitRemaining = response.headers().firstValue("X-Ratelimit-Remaining").orElse("unknown");
        String rateLimitReset = response.headers().firstValue("X-Ratelimit-Reset").orElse("unknown");

        LOG.info("Rate Limit Remaining: " + rateLimitRemaining);
        LOG.info("Rate Limit Resets At: " + rateLimitReset);

        return response.body();
    }

}
