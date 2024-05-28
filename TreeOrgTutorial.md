# TreeOrg Tutorial: Setting Up a Custom Project with OpenRemote

Welcome to the TreeOrg tutorial! In this guide, we will show you how to set up and customize a project using OpenRemote to meet the specific needs of TreeOrg, a forward-thinking company dedicated to urban forestry management.
## Table of Contents
1. [Introduction](#introduction)
2. [Setting Up the Development Environment](#step-2-setting-up-your-development-environment)
3. [Creating the TreeOrg Realm](#creating-the-treeorg-realm)
4. [Customizing TreeOrg](#customizing-treeorg)
    - [Adding Assets](#adding-assets)
    - [Integrating TreeAssets into the Realm](#integrating-treeassets-into-the-realm)
    - [Updating the Map for Enhanced Visualization](#updating-the-map-for-enhanced-visualization)
    - [Adding Custom Services](#adding-custom-services)
    - [Custom APIs](#custom-apis)
5. [Advanced Topics](#advanced-topics)
    - [Integration with External Services](#integration-with-external-services)
   - [Refactoring the services](#refactor-optimizing-routes)
   - [Creating Custom Widgets](#creating-custom-widgets)
6. [FAQ and Troubleshooting](#faq-and-troubleshooting)

## Introduction
TreeOrg is an innovative company specializing in the maintenance and care of large urban trees in Eindhoven. Utilizing advanced IoT sensors, TreeOrg monitors various environmental parameters of city trees, such as soil moisture and temperature. The data collected by these sensors is crucial in determining when and where service workers need to be dispatched to water and maintain the trees.

However, managing the logistics of routing service personnel efficiently from one tree to another poses a significant challenge. As the company grows and the number of trees under management increases, the need for an automated solution to optimize service routes has become apparent. TreeOrg seeks to integrate their system with navigation tools like Google Maps to enable their workers to find the targeted trees quickly and follow the most efficient routes possible.

This tutorial will guide you through setting up a custom project in OpenRemote that not only captures and processes data from IoT sensors but also interfaces with navigation software to streamline the workflow of field personnel. By the end of this guide, you will have a robust system designed to enhance the operational efficiency of tree servicing tasks in urban environments.
## Prerequisites
Before starting, ensure you have the following installed:
- [Git](https://git-scm.com/)
- [Node.js and npm](https://nodejs.org/)
- [Yarn](https://yarnpkg.com/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)

## Step 1: Forking and Cloning the Repository

1. **Fork the Repository**
    - Navigate to the [OpenRemote custom project repository](https://github.com/openremote/custom-project) on GitHub.
    - Click the "Fork" button to create your own copy of the repository.

2. **Clone the Forked Repository**
    - Open your terminal and clone your forked repository:
      ```sh
      git clone https://github.com/yourusername/custom-project-tree-org.git
      cd custom-project-tree-org
      ```
[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
## Step 2: Setting Up Your Development Environment

1. **Load the Repository into IntelliJ**
    - Open IntelliJ IDEA and click `Open` to load the `custom-project-tree-org` directory.

2. **Install Dependencies**
    - Open the terminal within IntelliJ and run:
      ```sh
      yarn install
      ```

3. **Add OpenRemote as a Submodule**
    - Add the OpenRemote repository as a submodule:
      ```sh
      git submodule add
      git submodule update
      ```
4. **Confirm settings**
    - Verify that the Gradle buildtools are set to use Intellij:
    - Preferences/Build,Execution,Deployment/Build Tools/Gradle -> set Build and Run & Run tests using to Intellij if
      they are not and apply.

## Step 3: Running the Application

1. **Run Backend with Custom Deployment Configuration**
    - In IntelliJ, select the `Custom Deployment` run configuration for the backend.
    - Click the run button to start the backend service.

2. **Run the Manager UI**
    - Open a new terminal in IntelliJ.
    - Navigate to the UI directory:
      ```sh
      cd openremote/ui/app/manager
      ```
    - Run the UI using npm:
      ```sh
      npm run serve
      ```

3. **Verify Setup**
    - Open a web browser and navigate to `http://localhost:9000/manager/`.
    - You should see the Manager UI with the default setup, confirming that the application is running correctly.

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
# Creating the TreeOrg Realm

## Introduction

In this section, we will create the TreeOrg setup. This setup will help us manage users, realms, settings, and more, ensuring we can develop features with a fresh slate every time we run the application.

## Creating Packages and Classes

### Step 1: Create TreeOrg Package

1. **Navigate to the Setup Package**
    - From the root of the `custom-project`, navigate to `setup/src/main/java/org/openremote/manager/setup`.

2. **Create TreeOrg Package**
    - Create a new package named `treeorg`.

### Step 2: Create TreeOrgSetupTasks Class

1. **Create TreeOrgSetupTasks Class**
    - This class defines the setup tasks for the TreeOrg realm, including initializing the Keycloak, Manager, and Rules
      setups. It determines which tasks to run based on the environment (e.g., production or development).
    - In the `treeorg` package, create a new class called `TreeOrgSetupTasks`

<details>
<summary>View Code</summary>

```java
     public class TreeOrgSetupTasks implements SetupTasks {

         public static final String PRODUCTION = "production";

         @Override
         public List<Setup> createTasks(Container container, String setupType, boolean keycloakEnabled) {
             boolean isProduction = PRODUCTION.equalsIgnoreCase(setupType);

             // Add custom Setup task implementations here with tasks optionally dependent on setupType
             return Arrays.asList(
                     new TreeOrgKeycloakSetup(container, isProduction),
                     new TreeOrgManagerSetup(container),
                     new TreeOrgRulesSetup(container)
             );
         }
     }
```

</details>

### Step 3: Create TreeOrgKeycloakSetup Class

1. **Create TreeOrgKeycloakSetup Class**
    - This class handles the creation of the TreeOrg Keycloak realm and user setup. It configures authentication
      settings and creates a default user with specified roles for the TreeOrg realm.
    - In the `treeorg` package, create a new class called `TreeOrgKeycloakSetup`.

<details>
<summary>View Code</summary>

```java
     public class TreeOrgKeycloakSetup extends AbstractKeycloakSetup {

         public static final String TREEORG_USER_PASSWORD = "TREEORG_USER_PASSWORD";
         public static final String TREEORG_USER_PASSWORD_DEFAULT = "PASSWORD";
         protected final String treeOrgUserPassword;
         public Realm treeOrgRealm;

         public TreeOrgKeycloakSetup(Container container, boolean isProduction) {
             super(container);
             treeOrgUserPassword = getString(container.getConfig(), TREEORG_USER_PASSWORD, TREEORG_USER_PASSWORD_DEFAULT);

             if (isProduction && TextUtil.isNullOrEmpty(treeOrgUserPassword)) {
                 throw new IllegalStateException("Password must be supplied in production");
             }
         }

         @Override
         public void onStart() {
             // Create custom realm
             treeOrgRealm = createRealm("treeorg", "TreeOrg", true);
             // make sure the realm name is lowercase, or you will get an error saying realm is null

             // Create user(s) for custom realm
             createUser("treeorg", "DevUser", treeOrgUserPassword, "Dev", "User", "dev@tree.org", true, new ClientRole[] {
                             ClientRole.READ,
                             ClientRole.WRITE
                     });
         }
     }
```

</details>

### Step 4: Create TreeOrgManagerSetup Class

1. **Create TreeOrgManagerSetup Class**
    - This class sets up the TreeOrg Manager environment, including creating initial assets for the realm.
    - In the `treeorg` package, create a new class called `TreeOrgManagerSetup`

<details>
<summary>View Code</summary>

```java
     public class TreeOrgManagerSetup extends ManagerSetup {

         public TreeOrgManagerSetup(Container container) {
             super(container);
         }

         @Override
         public void onStart() throws Exception {
             super.onStart();
             createAssets();
         }

         private void createAssets() {
             TreeOrgKeycloakSetup treeOrgKeycloakSetup = setupService.getTaskOfType(TreeOrgKeycloakSetup.class);
             Realm treeOrgRealm = treeOrgKeycloakSetup.treeOrgRealm;

             // Add an overarching Asset. This is not mandatory, but it makes it easier to group
             ThingAsset treeOrgAssets = new ThingAsset("TreeOrg Assets");

             // Set the realm here so that the child objects inherit this realm
             treeOrgAssets.setRealm(treeOrgRealm.getName());
             treeOrgAssets.setId(UniqueIdentifierGenerator.generateId(treeOrgAssets.getName()));
             assetStorageService.merge(treeOrgAssets);
         }
     }
```

</details>

### Step 5: Register TreeOrgSetupTasks

1. **Register TreeOrgSetupTasks Class**
    - Open the file at `setup/src/main/resources/META-INF/services/`.
    - Add the following line to register the `TreeOrgSetupTasks`:
      ```
      org.openremote.manager.setup.treeorg.TreeOrgSetupTasks
      ```

### Step 6: Run the Application

1. **Start Backend**
    - In IntelliJ, select the `Custom Deployment` run configuration for the backend.
    - Click the run button to start the backend service.

2. **Run the Manager UI**
    - Open a new terminal in IntelliJ.
    - Navigate to the UI directory:
      ```sh
      cd openremote/ui/app/manager
      ```
    - Run the UI using npm:
      ```sh
      npm run serve
      ```

3. **Verify Setup**
    - Open a web browser and navigate to `http://localhost:9000/manager/` (ensure the trailing slash is included).
    - You should see your newly built realm and one asset in the asset window.

---

By following these steps, you have successfully set up the TreeOrg environment. Continue to build on this foundation, creating custom services and features for your project.


[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
## Customizing TreeOrg

### Adding Assets

In the TreeOrg project, creating custom assets allows us to model real-world entities and their properties within our system. Here, we'll go through the process of creating a `TreeAsset`, which represents a tree equipped with various sensors.

#### Creating TreeAsset

To create a `TreeAsset`, start by navigating to the `model/src/main/java/org/openremote/model` folder and creating a package named `treeorg`. Inside this package, you'll create the `TreeAsset` class that extends `Asset<TreeAsset>` from OpenRemote's model framework.

Here is a brief overview and the code for the `TreeAsset` class:

This TreeAsset class includes attributes such as soil temperature and water level, which are essential for monitoring the health and maintenance needs of a tree. These attributes are set to be read-only to prevent unauthorized modification. The tree type, route ID, and priority can be used to manage maintenance schedules and routing for care personnel based on the tree's conditions and needs.

By extending the Asset<TreeAsset>, this custom asset leverages the existing framework capabilities like handling names, locations, and other standard properties, allowing for easy integration and management within the OpenRemote environment.

<details>
<summary>View Code</summary>

```java
@Entity
public class TreeAsset extends Asset<TreeAsset> {

    // Attributes that hold sensor data and tree information
    public static final AttributeDescriptor<Double> SOIL_TEMPERATURE = new AttributeDescriptor<>(
        "soilTemperature", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.READ_ONLY)
    ).withUnits(UNITS_CELSIUS).withFormat(ValueFormat.NUMBER_1_DP());

    public static final AttributeDescriptor<Integer> WATER_LEVEL = new AttributeDescriptor<>(
        "waterLevel", ValueType.POSITIVE_INTEGER,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );

    public static final AttributeDescriptor<String> TREE_TYPE = new AttributeDescriptor<>(
        "treeType", ValueType.TEXT
    ).withOptional(true);

    public static final AttributeDescriptor<Integer> ROUTE_ID = new AttributeDescriptor<>(
        "routeId", ValueType.POSITIVE_INTEGER,
        new MetaItem<>(MetaItemType.READ_ONLY, true)
    );

    public static final AttributeDescriptor<Boolean> PRIORITY = new AttributeDescriptor<>(
        "priority", ValueType.BOOLEAN,
        new MetaItem<>(MetaItemType.READ_ONLY, true)
    );

    // Descriptor for use in the manager UI and other parts of the system
    public static final AssetDescriptor<TreeAsset> DESCRIPTOR = new AssetDescriptor<>(
        "tree", "396d22", TreeAsset.class
    );

    protected TreeAsset() {
        // Constructor for JPA and Jackson
    }

    public TreeAsset(String name) {
        super(name);
    }

    // Getter methods to access the attributes
    public Optional<Double> getTemperature() {
        return getAttributes().getValue(SOIL_TEMPERATURE);
    }
    public Optional<Integer> getWaterLevel() {
        return getAttributes().getValue(WATER_LEVEL);
    }
    public Optional<String> getTreeType() {
        return getAttributes().getValue(TREE_TYPE);
    }
    public Optional<Integer> getRouteId() {
        return getAttributes().getValue(ROUTE_ID);
    }
    public Optional<Boolean> getPriority() {
        return getAttributes().getValue(PRIORITY);
    }
}
```

</details>

### Integrating TreeAssets into the Realm

With the `TreeAsset` class in place, the next step is to integrate these assets into our realm. In the `TreeOrgManagerSetup` class, we implement logic to create `TreeAssets` around a specific location, such as Eindhoven. Here is a detailed look at the process:

#### TreeOrgManagerSetup Class

The `TreeOrgManagerSetup` class includes methods to create and manage `TreeAssets` dynamically. This implementation results in a group of 100 TreeAssets, all associated under the ThingAsset "TreeOrg Assets". These assets are randomly placed around the city based on the specified base location, creating a realistic deployment pattern.

By utilizing this setup, we can efficiently simulate a realistic environment where each TreeAsset represents a physical tree, equipped with sensors, within the city of Eindhoven. Below is the code and explanation of its functionality:

<details>
<summary>View Code</summary>

```java
public class TreeOrgManagerSetup extends ManagerSetup {

    private static final double BASE_LATITUDE = 51.43848672819468;
    private static final double BASE_LONGITUDE = 5.47967205919616;
    private static final double MAX_RADIUS = 4;
    private static final int BATCH_SIZE = 20;

    public TreeOrgManagerSetup(Container container) {
        super(container);
    }

    @Override
    public void onStart() throws Exception {
        super.onStart();
        createAssets();
    }

    private void createAssets() {

        TreeOrgKeycloakSetup treeOrgKeycloakSetup = setupService.getTaskOfType(TreeOrgKeycloakSetup.class);
        Realm treeOrgRealm = treeOrgKeycloakSetup.treeOrgRealm;

        // Create a parent asset to group TreeAssets
        ThingAsset treeOrgAssets = new ThingAsset("TreeOrg Assets");
        treeOrgAssets.setRealm(treeOrgRealm.getName());
        treeOrgAssets.setId(UniqueIdentifierGenerator.generateId(treeOrgAssets.getName()));
        assetStorageService.merge(treeOrgAssets);

        // Create a specified number of TreeAssets and associate them with the parent
        int amountOfAssets = 100;
        CreateTreeAssets(amountOfAssets, treeOrgAssets);
    }

    private void CreateTreeAssets(int amountOfAssets, Asset<?> parentAsset) {
        List<TreeAsset> assetList = new ArrayList<>();

        for (int i = 0; i < amountOfAssets; i++) {
            GeoJSONPoint randomLocation = generateRandomLocation();
            TreeAsset treeAsset = new TreeAsset("TreeAsset " + (i + 1));
            treeAsset.setParent(parentAsset);
            treeAsset.getAttributes().addOrReplace(new Attribute<>(Asset.LOCATION, randomLocation));

            // Initialize attributes with random and predefined values
            Random random = new Random();
            treeAsset.getAttributes().getOrCreate(TreeAsset.WATER_LEVEL)
                .setValue(1 + random.nextInt(10000));

            treeAsset.getAttributes().getOrCreate(TreeAsset.SOIL_TEMPERATURE)
                .setValue(21.0); // Default soil temperature

            treeAsset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID)
                .setValue(0);

            treeAsset.getAttributes().getOrCreate(TreeAsset.PRIORITY)
                .setValue(false);

            treeAsset.setId(UniqueIdentifierGenerator.generateId(treeAsset.getName()));
            assetList.add(treeAsset);

            // Merge assets in batches for efficiency
            if (assetList.size() >= BATCH_SIZE) {
                for (TreeAsset asset : assetList) {
                    assetStorageService.merge(asset);
                }
                assetList.clear();
            }
        }
    }
    
    private GeoJSONPoint generateRandomLocation() {
        Random random = new Random();
        double angle = 2 * Math.PI * random.nextDouble();
        double radius = MAX_RADIUS * random.nextDouble();
        double dx = radius * Math.cos(angle);
        double dy = radius * Math.sin(angle);
        return calculateNewLocation(dx, dy);
    }

    private GeoJSONPoint calculateNewLocation(double dx, double dy) {
        double latitude = BASE_LATITUDE + (dy / 111); // Convert offset to degrees latitude
        double longitude = BASE_LONGITUDE + (dx / (111 * Math.cos(Math.toRadians(BASE_LATITUDE)))); // Convert offset to degrees longitude
        return new GeoJSONPoint(longitude, latitude);
    }
}
```

</details>

### Updating The Map for Enhanced Visualization

Once our `TreeAssets` are in place, the default map may no longer suffice due to the lack of detailed MBTiles for the Eindhoven area, especially at the appropriate zoom levels. To address this, you will need to update your map data. Comprehensive guidance on acquiring and setting up the appropriate MBTiles can be found in the OpenRemote [Developer Guide: Working on Maps](https://github.com/openremote/openremote/wiki/Developer-Guide%3A-Working-on-maps). After obtaining the `mapdata.mbtiles` file that fits our needs, place it in the `deployment/map` directory. Subsequently, you should update the `mapsettings.json` file to reflect the new map configuration, specifically adding some options for your Realm. This ensures that the map accurately displays the geographic distribution and details of our `TreeAssets`, enhancing the user interface and interaction.

<details>
<summary>View Code</summary>

```json
    {
    "treeorg" : {
    "center" : [ 5.4534874, 51.450813 ],
    "bounds" : [ 5.01, 51.1, 5.9, 51.8 ],
    "zoom" : 15,
    "minZoom" : 11,
    "maxZoom" : 24,
    "boxZoom" : false,
    "geocodeUrl" : "https://nominatim.openstreetmap.org"
  }
}
```

</details>

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)

### Adding Custom Services

In our ongoing effort to enhance the management and operational efficiency of TreeOrg, we introduce the `SortingService`. This service is strategically designed to manage and sort TreeAssets based on critical attributes such as water level.

#### SortingService Overview

The `SortingService` is part of the new suite of custom services housed within the `treeorg` package in the `manager/src/main/java/org.openremote.manager` directory. This service is initiated upon startup and focuses on retrieving all `TreeAssets`, sorting them according to their water levels. By doing so, it identifies the top 10 assets that require immediate attention, thereby optimizing the maintenance workflows.

The service operates by executing a filtered query that retrieves assets based on their type and the specified attribute (`waterLevel` in this case). It ensures that only assets with non-null attribute values are considered, sorting them in ascending order to prioritize maintenance for trees with the most critical needs first.

Here's a brief look at how the service is implemented:

- **Asset Retrieval and Sorting**: The service queries the `AssetStorageService` to fetch all relevant assets and applies sorting logic based on the water level attribute. It leverages Java’s Stream API to efficiently filter and sort these assets inline with their defined attributes.
- **Logging**: For operational transparency, the service logs the results of the sorting process, providing clear and actionable insights into which assets have the lowest water levels and thus, higher priority for maintenance.
- **Service Registration**: To ensure proper integration, the `SortingService` must be registered within the OpenRemote system. This is done by adding `org.openremote.manager.treeorg.SortingService` to the service manifest located in the `META-INF/services` directory.

<details>
<summary>View Code</summary>

```java
public class SortingService implements ContainerService {
   private AssetStorageService assetStorageService;
   private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());

   @Override
   public void init(Container container) throws Exception {
      this.assetStorageService = container.getService(AssetStorageService.class);
   }

   @Override
   public void start(Container container) throws Exception {
      Class<?> assetType = TreeAsset.class;
      var attributeName = new Attribute<>("waterLevel").getName();
      findAllAssetsSortedByAttributeAndType(assetType, attributeName);
   }

   @Override
   public void stop(Container container) throws Exception {
      // Any cleanup logic if needed
   }


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

}
```

</details>

By implementing the `SortingService`, TreeOrg not only advances its asset management capabilities but also enhances its responsiveness to environmental conditions affecting urban forestry management. This service exemplifies how custom solutions can be tailored to meet specific operational needs within the broader framework of OpenRemote's IoT management platform.

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)

### Custom APIs

As part of our commitment to enhancing TreeOrg's technological infrastructure, we have developed a custom API that allows for dynamic interaction with our asset management system. The `TreeOrgResource` interface defines the functionalities of our custom API, focusing primarily on asset management operations such as sorting assets based on specific attributes.

#### TreeOrgResource Interface

The `TreeOrgResource` interface, located in the model folder, defines the necessary endpoint for sorting assets:

<details>
<summary>View Code</summary>

```java
public interface TreeOrgResource {
    Response sortAssetsByAttribute(String assetType, String attributeName);
}
```

</details>

This interface is implemented in the TreeOrgResourceImplementation class within the manager folder, which provides the functionality to sort assets by attributes through a RESTful service:

<details>
<summary>View Code</summary>

```java
@Path("/")
public class TreeOrgResourceImplementation implements TreeOrgResource {
    private final SortingService sortingService;

    public TreeOrgResourceImplementation(SortingService sortingService) {
        this.sortingService = sortingService;
    }

    @GET
    @Path("sortbyattribute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sortAssetsByAttribute(@QueryParam("assetType") String assetType, @QueryParam("attribute") String attributeName) {
        Class<?> type;
        try {
            type = Class.forName(assetType);
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Asset type not found: " + assetType)
                           .build();
        }
        List<Asset<?>> sortedAssets = sortingService.findAllAssetsSortedByAttributeAndType(type, attributeName);
        return Response.ok(sortedAssets).build();
    }
}
```

</details>

Integration with TreeOrgRestService
To ensure that this API is integrated and available for use, the TreeOrgResourceImplementation is registered within our system through the TreeOrgRestService:

<details>
<summary>View Code</summary>

```java
public class TreeOrgRestService implements ContainerService {
    private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());
    protected AssetStorageService assetStorageService;
    protected SortingService sortingService;

    @Override
    public void init(Container container) throws Exception {
        ManagerWebService webService = container.getService(ManagerWebService.class);
        assetStorageService = container.getService(AssetStorageService.class);
        sortingService = container.getService(SortingService.class);
        webService.addApiSingleton(new TreeOrgResourceImplementation(sortingService));
    }
}
```

</details>

This service setup ensures that our custom API is properly instantiated and made available through the ManagerWebService at the application startup. By creating this dedicated endpoint, TreeOrg enhances its operational capabilities, providing stakeholders with direct, efficient access to real-time data and asset management functionalities.

Accessing the Endpoint
You can now access the endpoint via:

```GET http://localhost:8080/api/treeorg/sortbyattribute?assetType=org.openremote.model.treeorg.TreeAsset&attribute=waterLevel```

This will serve a response containing the 10 TreeAssets with the lowest water levels. It offers a convenient way for maintenance managers and city planners to quickly identify trees that require immediate attention, streamlining the maintenance process and enhancing urban environmental management.

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
## Advanced Topics

In this section, we delve into advanced topics to enhance the functionality and efficiency of the TreeOrg project.

### Integration With External Services

To find the most efficient route between assets, TreeOrg aims to leverage OpenStreetMap's optimize API. To achieve this,
we will create a `RouteOptimizationService` and a `RouteService` to handle route optimization and communication with the
OpenRouteService API.

> **Beware**: While OpenRouteService (ORS) is very powerful for certain use cases and offers vastly greater parameters
> and customization options, it may not always be the optimal solution for all scenarios. After evaluating our specific
> needs, we found that a manual algorithm for route optimization worked better for TreeOrg. This approach provided more
> control and simplicity for our specific use case, especially in scenarios with fewer points and simpler routing
> requirements.
>
> For more details on the manual route optimization method we implemented, see
> the [Refactor: Optimizing Routes](#refactor-optimizing-routes) section.

#### RouteOptimizationService

The `RouteOptimizationService` orchestrates the route optimization process by interacting with the `SortingService` to
retrieve sorted assets and then delegating the route optimization task to the `RouteService`. This service is designed
to initiate upon startup and perform an initial optimization for assets based on critical attributes like water level
and soil temperature. By automating the route optimization process, TreeOrg ensures efficient allocation of resources
and timely maintenance of urban trees.

<details>
<summary>View Code</summary>

```java
public class RouteOptimizationService implements ContainerService {

   private SortingService sortingService;
   private RouteService routeService;

   @Override
   public void init(Container container) throws Exception {
      this.sortingService = container.getService(SortingService.class);
      this.routeService = container.getService(RouteService.class);
   }

   @Override
   public void start(Container container) throws Exception {
      // Perform initial optimization
      optimizeRouteForSensors(TreeAsset.class, "waterLevel");
      optimizeRouteForSensors(TreeAsset.class, "soilTemperature");
   }

   @Override
   public void stop(Container container) {
      // Any cleanup logic if needed
   }

   public RouteResponse optimizeRouteForSensors(Class<?> assetType, String attributeName) {
      // Delegate finding and sorting to SortingService
      List<Asset<?>> sortedSensors = sortingService.findAllAssetsSortedByAttributeAndType(assetType, attributeName);

      // Delegate route optimization to RouteService
      return routeService.optimizeRouteForSortedAssets(sortedSensors, attributeName);
   }
}
```

</details>

#### RouteService

The `RouteService` is responsible for the technical details of communicating with the OpenRouteService API. It prepares
the payload for the API request by compiling a list of coordinates and other necessary data into a format that the API
can understand. Upon receiving a response from the API, the service processes this response to determine the optimal
route between the assets. It then updates the asset information to reflect this optimized route, ensuring that the
maintenance crew has the most efficient path to follow. The `RouteService` thus plays a crucial role in reducing travel
time and operational costs.

<details>
<summary>View Code</summary>

```java
public class RouteService implements ContainerService {

   private AssetStorageService assetStorageService;
   private static final Logger LOG = Logger.getLogger(RouteService.class.getName());
   private RouteApiClient routeApiClient;
   private Map<Integer, String> jobIdToAssetIdMap = new HashMap<>();

   public RouteService() {
   }

   @Override
   public void init(Container container) throws Exception {
      this.assetStorageService = container.getService(AssetStorageService.class);
      this.routeApiClient = container.getService(RouteApiClient.class);
   }

   @Override
   public void start(Container container) throws Exception {
      // Any start logic if needed
   }

   @Override
   public void stop(Container container) throws Exception {
      // Any cleanup logic if needed
   }

   /**
    * Optimizes the route for a list of sorted assets based on a specified attribute.
    *
    * @param sortedAssets  A list of sorted assets to optimize the route for.
    * @param attributeName The name of the attribute used for sorting.
    * @return A RouteResponse containing the Google Maps URL for the optimized route and the ordered assets.
    */
   public RouteResponse optimizeRouteForSortedAssets(List<Asset<?>> sortedAssets, String attributeName) {
      List<double[]> coordinates = extractCoordinates(sortedAssets);

      double[] startingPosition = {5.453487298268298, 51.45081456926727};
      String tspQuery = prepareTSPQuery(coordinates, sortedAssets, startingPosition);
      try {
         String routeResponse = routeApiClient.callOpenRouteService(tspQuery);
         LOG.info("Route Response: " + routeResponse);
         List<Asset<?>> orderedAssets = orderAssetsByRouteResponse(routeResponse, sortedAssets);
         updateRouteIds(orderedAssets);
         printOrderedAssets(orderedAssets, attributeName);

         List<double[]> routeCoordinates = extractCoordinates(orderedAssets);
         routeCoordinates.add(0, startingPosition); // Add starting position at the beginning
         routeCoordinates.add(startingPosition); // Add starting position at the end

         String googleMapsURL = generateGoogleMapsURL(routeCoordinates);
         LOG.info("View route on Google Maps: " + googleMapsURL);

         updateParentAssetWithGoogleMapsURL(googleMapsURL, sortedAssets);

         return new RouteResponse(googleMapsURL, orderedAssets);
      } catch (IOException | InterruptedException e) {
         LOG.severe("Failed to call OpenRouteService: " + e.getMessage());
         return null;
      }
   }


   /**
    * Prepares the TSP (Travelling Salesperson Problem) query for the OpenRouteService API.
    *
    * @param coordinates      A list of coordinates for the assets in [longitude, latitude] format.
    * @param sortedSensors    A list of sorted assets that represent the sensors.
    * @param startingPosition The starting position for the route in [longitude, latitude] format.
    * @return A JSON string representing the TSP query.
    */
   private String prepareTSPQuery(List<double[]> coordinates, List<Asset<?>> sortedSensors, double[] startingPosition) {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode payload = mapper.createObjectNode();
      ArrayNode jobs = mapper.createArrayNode();
      ArrayNode vehicles = mapper.createArrayNode();

      ObjectNode vehicle = mapper.createObjectNode();
      vehicle.put("id", 1);
      vehicle.set("start", mapper.createArrayNode().add(startingPosition[0]).add(startingPosition[1]));
      vehicle.put("return_to_depot", true);
      vehicle.put("profile", "driving-car");
      vehicles.add(vehicle);

      for (int i = 0; i < coordinates.size(); i++) {
         ObjectNode job = mapper.createObjectNode();
         int jobId = i + 1;
         job.put("id", jobId); // Use a unique integer as job ID
         job.set("location", mapper.createArrayNode().add(coordinates.get(i)[0]).add(coordinates.get(i)[1]));
         jobs.add(job);

         // Map job ID to original asset ID for reference
         jobIdToAssetIdMap.put(jobId, sortedSensors.get(i).getId());
      }

      payload.set("vehicles", vehicles);
      payload.set("jobs", jobs);
      return payload.toString();
   }


   /**
    * Updates the Google Maps URL attribute for the relevant assets and clears it for others.
    *
    * @param googleMapsURL The Google Maps URL to set.
    */
   private void updateParentAssetWithGoogleMapsURL(String googleMapsURL, List<Asset<?>> sortedAssets) {
      Asset<?> parentAsset = findParentAsset(sortedAssets);
      if (parentAsset != null) {
         parentAsset.getAttributes().getOrCreate(NOTES).setValue(googleMapsURL);
         assetStorageService.merge(parentAsset);
         LOG.info("Updated parent asset " + parentAsset.getName() + " ID: " + parentAsset.getId() + " with Google Maps URL");
      }
   }

   private Asset<?> findParentAsset(List<Asset<?>> sortedAssets) {
      for (Asset<?> asset : sortedAssets) {
         String parentId = asset.getParentId();
         if (parentId != null) {
            return assetStorageService.find(parentId);
         }
      }
      LOG.severe("Parent asset not found");
      return null;
   }

   /**
    * Updates the routeId attribute of each asset in the ordered list and resets routeId to 0 for other assets.
    *
    * @param orderedAssets The list of assets in the order of the optimized route.
    */
   private void updateRouteIds(List<Asset<?>> orderedAssets) {
      Map<String, Integer> assetIdToRouteIdMap = new HashMap<>();
      for (int i = 0; i < orderedAssets.size(); i++) {
         assetIdToRouteIdMap.put(orderedAssets.get(i).getId(), i + 1);
      }

      // Convert the list of asset IDs to an array
      String[] assetIdsArray = assetIdToRouteIdMap.keySet().toArray(new String[0]);

      // Find the relevant assets by their IDs
      AssetQuery query = new AssetQuery().types(TreeAsset.class).ids(assetIdsArray);
      List<Asset<?>> relevantAssets = assetStorageService.findAll(query);

      // Update routeId attributes for relevant assets
      for (Asset<?> asset : relevantAssets) {
         Integer newRouteId = assetIdToRouteIdMap.get(asset.getId());
         asset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID).setValue(newRouteId);
         assetStorageService.merge(asset);
         LOG.info("Setting route ID " + newRouteId + " for asset: " + asset.getName());
      }

      // Reset routeId for assets not in the ordered list
      for (String assetId : assetIdToRouteIdMap.keySet()) {
         if (!assetIdToRouteIdMap.containsKey(assetId)) {
            Asset<?> asset = assetStorageService.find(assetId);
            if (asset != null) {
               asset.getAttributes().getOrCreate(TreeAsset.ROUTE_ID).setValue(0);
               assetStorageService.merge(asset);
               LOG.info("Resetting route ID to 0 for asset ID: " + asset.getId());
            }
         }
      }
   }


   /**
    * Extracts coordinates from a list of assets.
    *
    * @param assets List of assets to extract coordinates from.
    * @return List of coordinates in [longitude, latitude] format.
    */
   private List<double[]> extractCoordinates(List<Asset<?>> assets) {
      List<double[]> coordinates = new ArrayList<>();
      assets.forEach(asset -> {
         Optional<Attribute<?>> locationAttribute = asset.getAttributes().get("location");
         locationAttribute.ifPresent(attr -> {
            GeoJSONPoint point = (GeoJSONPoint) attr.getValue().orElse(null);
            if (point != null) {
               coordinates.add(new double[]{point.getX(), point.getY()});
            }
         });
      });
      return coordinates;
   }

   /**
    * Orders assets based on the route response from the OpenRouteService API.
    *
    * @param routeResponse JSON response from the OpenRouteService API.
    * @param assets        List of assets to order.
    * @return Ordered list of assets based on the optimized route.
    * @throws IOException throws exception
    */
   private List<Asset<?>> orderAssetsByRouteResponse(String routeResponse, List<Asset<?>> assets) throws IOException {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rootNode = mapper.readTree(routeResponse);

      LOG.info("Root Node: " + rootNode.toPrettyString());

      JsonNode stepsNode = rootNode.path("routes").get(0).path("steps");

      Map<Integer, double[]> orderedCoordinates = new LinkedHashMap<>();
      if (stepsNode.isArray()) {
         for (JsonNode stepNode : stepsNode) {
            int jobId = stepNode.path("job").asInt();
            double[] coord = new double[2];
            coord[0] = stepNode.path("location").get(0).asDouble();
            coord[1] = stepNode.path("location").get(1).asDouble();
            orderedCoordinates.put(jobId, coord);
         }
      }

      return mapCoordinatesToAssets(orderedCoordinates, assets);
   }

   /**
    * Maps the ordered coordinates to their corresponding assets.
    *
    * @param orderedCoordinates Map of job ID to coordinates.
    * @param assets             List of assets to map.
    * @return List of assets ordered by the provided coordinates.
    */
   private List<Asset<?>> mapCoordinatesToAssets(Map<Integer, double[]> orderedCoordinates, List<Asset<?>> assets) {
      Map<String, Asset<?>> coordinatesToAssetsMap = new HashMap<>();
      for (Asset<?> asset : assets) {
         double[] assetCoordinates = extractCoordinatesFromAsset(asset);
         String key = Arrays.toString(assetCoordinates);
         coordinatesToAssetsMap.put(key, asset);
      }

      List<Asset<?>> orderedAssets = new ArrayList<>();
      for (Map.Entry<Integer, double[]> entry : orderedCoordinates.entrySet()) {
         String key = Arrays.toString(entry.getValue());
         if (coordinatesToAssetsMap.containsKey(key)) {
            Asset<?> asset = coordinatesToAssetsMap.get(key);
            if (!orderedAssets.contains(asset)) {
               orderedAssets.add(asset);
            }
         }
      }

      // Ensure no duplicates and exactly 10 assets
      Set<Asset<?>> uniqueAssets = new LinkedHashSet<>(orderedAssets);
      if (uniqueAssets.size() < 10) {
         assets.stream()
                 .filter(asset -> !uniqueAssets.contains(asset))
                 .limit(10 - uniqueAssets.size())
                 .forEach(uniqueAssets::add);
      }

      LOG.info("Total assets after mapping: " + uniqueAssets.size());
      return new ArrayList<>(uniqueAssets);
   }

   /**
    * Extracts coordinates from a single asset.
    *
    * @param asset The asset to extract coordinates from.
    * @return Coordinates of the asset in [longitude, latitude] format.
    */
   private double[] extractCoordinatesFromAsset(Asset<?> asset) {
      Optional<Attribute<?>> locationAttribute = asset.getAttributes().get("location");
      if (locationAttribute.isPresent()) {
         GeoJSONPoint point = (GeoJSONPoint) locationAttribute.get().getValue().orElse(null);
         if (point != null) {
            return new double[]{point.getX(), point.getY()};
         }
      }
      return null;
   }

   /**
    * Prints the ordered list of assets.
    *
    * @param assets List of ordered assets.
    */
   private void printOrderedAssets(List<Asset<?>> assets, String attributeName) {
      LOG.info("Ordered visitation list:");
      assets.forEach(asset -> LOG.info("Visit Asset ID: " + asset.getId() + " - "
              + asset.getName() + " - " + asset.getAttribute(attributeName)));
   }

   /**
    * Generates a Google Maps URL for the given list of coordinates.
    *
    * @param coordinates List of coordinates to include in the URL.
    * @return Google Maps URL for the route.
    */
   public String generateGoogleMapsURL(List<double[]> coordinates) {
      StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/");
      coordinates.forEach(coord -> url.append(coord[1]).append(",").append(coord[0]).append("/"));
      return url.toString();
   }
}
```

</details>

#### RouteApiClient

The `RouteApiClient` manages the HTTP requests to the OpenRouteService API. It constructs the request with appropriate
headers and payloads, sends the request, and handles the response. This class is essential for maintaining the
connection to the external service and ensuring that TreeOrg's routing requests are processed efficiently. The client
also handles rate limiting information provided by the API, ensuring compliance with usage policies.

<details>
<summary>View Code</summary>

````java
public class RouteApiClient implements ContainerService {

   private static final Logger LOG = Logger.getLogger(RouteApiClient.class.getName());
   private static final String ORS_API_KEY = "Your_Api_Key_Here";

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

````

</details>

#### RouteResponse

The `RouteResponse` class encapsulates the data returned from the OpenRouteService API, including the Google Maps URL
for the optimized route and the ordered list of assets. This structured response makes it easy to present the optimized
route to end-users and integrate it with other parts of the system.
<details>
<summary>View Code</summary>

```java
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
```
</details>

#### Update The Resource Interface and Implementation

##### TreeOrgResource Interface

The `TreeOrgResource` interface defines the endpoints for the custom API. These endpoints include methods for sorting
assets by a specified attribute and for optimizing routes based on asset data. By defining these methods, the interface
establishes a contract that the implementation class must fulfill, ensuring consistency and reliability in the API's
behavior.

##### TreeOrgResourceImplementation

The `TreeOrgResourceImplementation` class provides the actual functionality behind the API endpoints defined in
the `TreeOrgResource` interface. It uses the `SortingService` to sort assets and the `RouteOptimizationService` to
optimize routes. By implementing these endpoints, the class allows external clients to request sorted asset lists and
optimized routes dynamically. This flexibility is crucial for integrating TreeOrg's system with other tools and services
used in urban forestry management.

##### Integration with TreeOrgRestService

The `TreeOrgRestService` class registers the `TreeOrgResourceImplementation` with the system, ensuring that the API is
available and accessible. This integration involves adding the implementation to the web service, making it part of the
overall API framework. By doing so, TreeOrg ensures that the custom API endpoints are properly initialized and can
handle requests as soon as the system starts up.
<details>
<summary>View Code</summary>

````java
public interface TreeOrgResource {

   Response sortAssetsByAttribute(String assetType, String attributeName);

   Response optimizeRouteForSensors(String assetType, String attributeName);
}

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

public class TreeOrgRestService implements ContainerService {
   private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());
   protected AssetStorageService assetStorageService;
   protected SortingService sortingService;
   protected RouteOptimizationService routeOptimizationService;
   protected RouteService routeService;
   protected RouteApiClient routeApiClient;

   @Override
   public void init(Container container) throws Exception {
      ManagerWebService webService = container.getService(ManagerWebService.class);
      assetStorageService = container.getService(AssetStorageService.class);
      sortingService = container.getService(SortingService.class);

      routeService = container.getService(RouteService.class);
      routeOptimizationService = container.getService(RouteOptimizationService.class);
      routeApiClient = container.getService(RouteApiClient.class);
      webService.addApiSingleton(new TreeOrgResourceImplementation(sortingService, routeOptimizationService));
      LOG.info("Registered custom API classes: " + sortingService);
   }

   @Override
   public void start(Container container) throws Exception {

   }

   @Override
   public void stop(Container container) throws Exception {

   }

}

````

</details>

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)

## Refactor: Optimizing Routes

### Initial Approach with External Service

Initially, we integrated with the OpenRouteService (ORS) API to optimize routes for TreeOrg's urban forestry management
operations. The ORS API provided capabilities to solve the Traveling Salesperson Problem (TSP), which is crucial for
determining the most efficient route to visit multiple assets (trees) based on their locations. We aimed to leverage ORS
to automate the route planning process and generate optimized routes for our service personnel.

### Challenges Faced

While ORS offered powerful routing algorithms, we encountered several issues that hindered our progress:

1. **Integration Complexity**: Integrating ORS required complex API interactions, including preparing and sending JSON
   payloads, handling responses, and managing rate limits imposed by the service.
2. **Rate Limiting**: The ORS API had strict rate limits, which constrained the number of requests we could make within
   a given timeframe. This limitation posed challenges for real-time route optimization, especially as the number of
   assets grew.
3. **Reliability Concerns**: Reliance on an external service introduced potential points of failure due to network
   issues, service downtime, or API changes, impacting the reliability of our route optimization process.
4. **Response Accuracy**: There were discrepancies in the response data, sometimes resulting in suboptimal routes or
   errors in the generated coordinates, which further complicated our implementation.

### Transition to a Manual Algorithm

Given these challenges, we decided to implement a custom, manual algorithm to optimize routes. This approach aimed to
reduce dependencies on external services and provide a more controlled, efficient, and reliable solution tailored to our
specific needs.

## The Closest-Next-Point Algorithm

### Overview

The algorithm we implemented is a heuristic approach to the Traveling Salesperson Problem known as the
*Closest-Next-Point Algorithm*. This method prioritizes simplicity and efficiency by iteratively selecting the nearest
unvisited asset from the current location until all assets have been visited.

### Algorithm Steps

1. **Initialization**:
   - **Starting Point**: Begin at a fixed starting location, which is the initial position of the service personnel.
   - **Asset List**: Maintain a list of all assets (trees) to be visited.

2. **Route Construction**:
   - **Current Position**: Set the starting point as the current position.
   - **Unvisited Assets**: Create a list of unvisited assets.

3. **Iterative Selection**:
   - **Find Nearest Asset**: From the current position, identify the nearest unvisited asset based on the geographical
     distance.
   - **Update Route**: Move to the nearest asset, mark it as visited, and update the current position.
   - **Repeat**: Continue the process until all assets have been visited.

4. **Return to Start**:
   - After visiting all assets, return to the starting point to complete the route.

5. **Generate Navigation Links**:
   - Compile the sequence of coordinates visited during the route into a Google Maps URL for easy navigation.

### Advantages of the Algorithm

1. **Simplicity**: The algorithm is straightforward to implement and understand, making it easy to debug and maintain.
2. **Efficiency**: By always moving to the closest unvisited asset, the algorithm minimizes travel distance and time,
   which is particularly beneficial for real-time applications.
3. **Control**: Implementing the algorithm internally allows for greater customization and flexibility to adjust to
   specific requirements and constraints of TreeOrg's operations.
4. **Independence**: Eliminating the dependency on external services reduces the risk of outages and rate limiting,
   ensuring more reliable and continuous operation.

### Practical Implementation

In our implementation within the `RouteService`, we:

- Extracted the coordinates of all relevant assets.
- Applied the closest-next-point logic to determine the optimal visitation sequence.
- Generated Google Maps URLs to provide navigational aids for service personnel.
- Updated the asset information with route details to facilitate efficient planning and execution.

This manual algorithm not only improved the efficiency and reliability of our route optimization process but also
provided us with a tailored solution that could scale with TreeOrg's growing needs without external constraints.

<details>
<summary>View Refactored Code</summary>

#### RouteOptimizationService

````java

public class RouteOptimizationService implements ContainerService {
   private static final Logger LOG = Logger.getLogger(RouteService.class.getName());
   private SortingService sortingService;
   private RouteService routeService;

   @Override
   public void init(Container container) throws Exception {
      this.sortingService = container.getService(SortingService.class);
      this.routeService = container.getService(RouteService.class);
   }

   @Override
   public void start(Container container) {
      // Perform initial optimization
      optimizeRouteForSensors(TreeAsset.class, "waterLevel");
      optimizeRouteForSensors(TreeAsset.class, "soilTemperature");
   }

   @Override
   public void stop(Container container) {
      // Any cleanup logic if needed
   }

   public RouteResponse optimizeRouteForSensors(Class<?> assetType, String attributeName) {
      if (assetType == null || attributeName == null || attributeName.isEmpty()) {
         LOG.severe("Asset type or attribute name is null or empty. Unable to optimize route.");
         return new RouteResponse(null, Collections.emptyList());
      }

      List<Asset<?>> sortedSensors = sortingService.findAllAssetsSortedByAttributeAndType(assetType, attributeName);
      if (sortedSensors == null || sortedSensors.isEmpty()) {
         LOG.severe("No sorted sensors found for the given attribute. Unable to optimize route.");
         return new RouteResponse(null, Collections.emptyList());
      }

      // Delegate route optimization to RouteService
      return routeService.optimizeRouteForSortedAssets(sortedSensors, attributeName);
   }

}
````

#### RouteService

````java



public class RouteService implements ContainerService {

   private AssetStorageService assetStorageService;
   private static final Logger LOG = Logger.getLogger(RouteService.class.getName());

   public RouteService() {
   }

   @Override
   public void init(Container container) throws Exception {
      this.assetStorageService = container.getService(AssetStorageService.class);
   }

   @Override
   public void start(Container container) {
   }

   @Override
   public void stop(Container container) {
   }

   /**
    * Optimizes the route for a list of sorted assets based on a specified attribute.
    *
    * @param sortedAssets  A list of sorted assets to optimize the route for.
    * @return A RouteResponse containing the Google Maps URLs for the optimized routes and the ordered assets.
    */
   public RouteResponse optimizeRouteForSortedAssets(List<Asset<?>> sortedAssets, String attributeName) {
      if (sortedAssets.isEmpty()) {
         LOG.severe("Sorted assets list is empty. Unable to optimize route.");
         return new RouteResponse(null, Collections.emptyList()); // Return a default RouteResponse or null
      }

      if (sortedAssets.size() == 1) {
         LOG.warning("Only one asset in the list. Route optimization may not be necessary.");
         updateRouteIds(sortedAssets);
         return new RouteResponse(null, sortedAssets);
      }

      List<double[]> coordinates = extractCoordinates(sortedAssets);
      double[] startingPosition = {5.453487298268298, 51.45081456926727};

      // Generate the new closest-next-point route
      List<double[]> newOptimalRoute = findOptimalRoute(coordinates, startingPosition);
      String newGoogleMapsURL = generateGoogleMapsURL(newOptimalRoute);
      LOG.info("View new route on Google Maps: " + newGoogleMapsURL);

      // Update the parent asset with the Google Maps URL
      updateParentAssetWithGoogleMapsURL(newGoogleMapsURL, sortedAssets);

      // Update route IDs for the assets
      updateRouteIds(sortedAssets);

      return new RouteResponse(newGoogleMapsURL, sortedAssets);
   }


   /**
    * Finds the optimal route using the closest-next-point algorithm.
    *
    * @param coordinates      List of coordinates.
    * @param startingPosition Starting position.
    * @return List of coordinates representing the optimal route.
    */
   private List<double[]> findOptimalRoute(List<double[]> coordinates, double[] startingPosition) {
      List<double[]> route = new ArrayList<>();
      boolean[] visited = new boolean[coordinates.size()];
      double[] currentPosition = startingPosition;
      route.add(currentPosition);

      for (int i = 0; i < coordinates.size(); i++) {
         double minDistance = Double.MAX_VALUE;
         int closestPointIndex = -1;
         for (int j = 0; j < coordinates.size(); j++) {
            if (!visited[j]) {
               double distance = calculateDistance(currentPosition, coordinates.get(j));
               if (distance < minDistance) {
                  minDistance = distance;
                  closestPointIndex = j;
               }
            }
         }
         if (closestPointIndex != -1) {
            visited[closestPointIndex] = true;
            currentPosition = coordinates.get(closestPointIndex);
            route.add(currentPosition);
         }
      }
      route.add(startingPosition); // Return to start
      return route;
   }

   /**
    * Calculates the distance between two coordinates.
    *
    * @param point1 First point.
    * @param point2 Second point.
    * @return Distance between the two points.
    */
   private double calculateDistance(double[] point1, double[] point2) {
      double dx = point1[0] - point2[0];
      double dy = point1[1] - point2[1];
      return Math.sqrt(dx * dx + dy * dy);
   }

   /**
    * Updates the Google Maps URL attribute for the relevant assets and clears it for others.
    *
    * @param googleMapsURL The Google Maps URL to set.
    */
   private void updateParentAssetWithGoogleMapsURL(String googleMapsURL, List<Asset<?>> sortedAssets) {
      Asset<?> parentAsset = findParentAsset(sortedAssets);
      if (parentAsset != null) {
         parentAsset.getAttributes().getOrCreate(NOTES).setValue(googleMapsURL);
         assetStorageService.merge(parentAsset);
         LOG.info("Updated parent asset " + parentAsset.getName() + " ID: " + parentAsset.getId() + " with Google Maps URL");
      }
   }

   private Asset<?> findParentAsset(List<Asset<?>> sortedAssets) {
      for (Asset<?> asset : sortedAssets) {
         String parentId = asset.getParentId();
         if (parentId != null) {
            return assetStorageService.find(parentId);
         }
      }
      LOG.severe("Parent asset not found");
      return null;
   }

   /**
    * Updates the routeId attribute of each asset in the ordered list and resets routeId to 0 for other assets.
    *
    * @param orderedAssets The list of assets in the order of the optimized route.
    */
   private void updateRouteIds(List<Asset<?>> orderedAssets) {
      Map<String, Integer> assetIdToRouteIdMap = new HashMap<>();
      for (int i = 0; i < orderedAssets.size(); i++) {
         assetIdToRouteIdMap.put(orderedAssets.get(i).getId(), i + 1);
      }

      // Convert the list of asset IDs to an array
      String[] assetIdsArray = assetIdToRouteIdMap.keySet().toArray(new String[0]);

      // Find the relevant assets by their IDs
      AssetQuery query = new AssetQuery().ids(assetIdsArray);
      List<Asset<?>> relevantAssets = assetStorageService.findAll(query);

      if (relevantAssets == null || relevantAssets.isEmpty()) {
         LOG.severe("No relevant assets found for the given IDs.");
         return;
      }

      // Update routeId attributes for relevant assets
      for (Asset<?> asset : relevantAssets) {
         Integer newRouteId = assetIdToRouteIdMap.get(asset.getId());
         AttributeDescriptor<Integer> routeIdDescriptor = new AttributeDescriptor<>("routeId", ValueType.POSITIVE_INTEGER);
         asset.getAttributes().getOrCreate(routeIdDescriptor).setValue(newRouteId);
         assetStorageService.merge(asset);
         LOG.info("Setting route ID " + newRouteId + " for asset: " + asset.getName());
      }

      // Reset routeId for assets not in the ordered list
      for (String assetId : assetIdToRouteIdMap.keySet()) {
         if (!assetIdToRouteIdMap.containsKey(assetId)) {
            Asset<?> asset = assetStorageService.find(assetId);
            if (asset != null) {
               AttributeDescriptor<Integer> routeIdDescriptor = new AttributeDescriptor<>("routeId", ValueType.POSITIVE_INTEGER);
               asset.getAttributes().getOrCreate(routeIdDescriptor).setValue(0);
               assetStorageService.merge(asset);
               LOG.info("Resetting route ID to 0 for asset ID: " + asset.getId());
            }
         }
      }
   }

   /**
    * Extracts coordinates from a list of assets.
    *
    * @param assets List of assets to extract coordinates from.
    * @return List of coordinates in [longitude, latitude] format.
    */
   private List<double[]> extractCoordinates(List<Asset<?>> assets) {
      List<double[]> coordinates = new ArrayList<>();
      assets.forEach(asset -> {
         Optional<Attribute<?>> locationAttribute = asset.getAttributes().get("location");
         locationAttribute.ifPresent(attr -> {
            GeoJSONPoint point = (GeoJSONPoint) attr.getValue().orElse(null);
            if (point != null) {
               coordinates.add(new double[]{point.getX(), point.getY()});
            }
         });
      });
      return coordinates;
   }

   /**
    * Generates a Google Maps URL for the given list of coordinates.
    *
    * @param coordinates List of coordinates to include in the URL.
    * @return Google Maps URL for the route.
    */
   public String generateGoogleMapsURL(List<double[]> coordinates) {
      StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/");
      coordinates.forEach(coord -> url.append(coord[1]).append(",").append(coord[0]).append("/"));
      return url.toString();
   }
}
```` 

#### SortingService

````java
public class SortingService implements ContainerService {
   private AssetStorageService assetStorageService;
   private static final Logger LOG = Logger.getLogger(ManagerWebService.class.getName());

   @Override
   public void init(Container container) throws Exception {
      this.assetStorageService = container.getService(AssetStorageService.class);
   }

   @Override
   public void start(Container container) throws Exception {
      Class<?> assetType = TreeAsset.class;
      var attributeName = new Attribute<>("waterLevel").getName();
      findAllAssetsSortedByAttributeAndType(assetType, attributeName);
   }

   @Override
   public void stop(Container container) throws Exception {
      // Any cleanup logic if needed
   }


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

}
````

#### ResourceImplementation

````java

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



````

</details>

### Creating Custom Widgets

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)

## FAQ and Troubleshooting

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)



