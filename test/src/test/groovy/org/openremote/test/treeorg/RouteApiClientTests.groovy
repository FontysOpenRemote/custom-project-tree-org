package org.openremote.test.treeorg

import org.openremote.manager.treeorg.RouteApiClient
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RouteApiClientTests extends Specification implements ManagerContainerTrait {

    def "RouteApiClient should call OpenRouteService and return response"() {
        setup:
        // Mock the Container
        def container = Mock(org.openremote.container.Container)

        // Initialize RouteApiClient
        RouteApiClient routeApiClient = new RouteApiClient()
        routeApiClient.init(container)

        // Mock the HttpClient and its behavior
        def client = Mock(HttpClient)
        def request = Mock(HttpRequest)
        def response = Mock(HttpResponse)
        HttpClient.newHttpClient() >> client
        HttpRequest.newBuilder() >> request
        request.uri(_) >> request
        request.header(_, _) >> request
        request.POST(_) >> request
        client.send(_, _) >> response
        response.body() >> '{"code":0,"summary":{"cost":0,"routes":1,"unassigned":0,"setup":0,"service":0,"duration":0,"waiting_time":0,"priority":0,"violations":[],"computing_times":{"loading":67,"solving":0,"routing":0}},"unassigned":[],"routes":[{"vehicle":1,"cost":0,"setup":0,"service":0,"duration":0,"waiting_time":0,"priority":0,"steps":[{"type":"start","location":[5.453487298268298,51.45081456926727],"setup":0,"service":0,"waiting_time":0,"arrival":0,"duration":0,"violations":[]},{"type":"job","location":[5.45348729826831,51.4508145692673],"id":3,"setup":0,"service":0,"waiting_time":0,"job":3,"arrival":0,"duration":0,"violations":[]},{"type":"job","location":[5.4534872982683,51.45081456926729],"id":2,"setup":0,"service":0,"waiting_time":0,"job":2,"arrival":0,"duration":0,"violations":[]},{"type":"job","location":[5.453487298268298,51.45081456926727],"id":1,"setup":0,"service":0,"waiting_time":0,"job":1,"arrival":0,"duration":0,"violations":[]},{"type":"end","location":[5.453487298268298,51.45081456926727],"setup":0,"service":0,"waiting_time":0,"arrival":0,"duration":0,"violations":[]}],"violations":[]}]}'
        response.headers() >> [
                "X-Ratelimit-Remaining": ["437"],
                "X-Ratelimit-Reset"    : ["1716021976"]
        ]

        // Mock request payload
        def query = """
        {
            "vehicles": [
                {
                    "id": 1,
                    "start": [5.453487298268298, 51.45081456926727],
                    "return_to_depot": true,
                    "profile": "driving-car"
                }
            ],
            "jobs": [
                {
                    "id": 1,
                    "location": [5.453487298268298, 51.45081456926727]
                },
                {
                    "id": 2,
                    "location": [5.453487298268300, 51.45081456926729]
                },
                {
                    "id": 3,
                    "location": [5.453487298268310, 51.45081456926730]
                }
            ]
        }
        """

        when:
        // Call the method under test
        def result = routeApiClient.callOpenRouteService(query)

        then:
        // Verify the response structure
        result.contains('"vehicle":1')
        result.contains('"location":[5.453487298268298,51.45081456926727]')
    }
}
