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
    - [Managing Users and Roles](#managing-users-and-roles)
    - [Custom APIs](#custom-apis)
5. [Advanced Topics](#advanced-topics)
    - [Integration with External Services](#integration-with-external-services)
    - [Performance Optimization](#performance-optimization)
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
   - Preferences/Build,Execution,Deployment/Build Tools/Gradle -> set Build and Run & Run tests using to Intellij if they are not and apply.

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
   - This class defines the setup tasks for the TreeOrg realm, including initializing the Keycloak, Manager, and Rules setups. It determines which tasks to run based on the environment (e.g., production or development).
   - In the `treeorg` package, create a new class called `TreeOrgSetupTasks`
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

### Step 3: Create TreeOrgKeycloakSetup Class

1. **Create TreeOrgKeycloakSetup Class**
   - This class handles the creation of the TreeOrg Keycloak realm and user setup. It configures authentication settings and creates a default user with specified roles for the TreeOrg realm.
   - In the `treeorg` package, create a new class called `TreeOrgKeycloakSetup`.
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

### Step 4: Create TreeOrgManagerSetup Class

1. **Create TreeOrgManagerSetup Class**
   - This class sets up the TreeOrg Manager environment, including creating initial assets for the realm.
   - In the `treeorg` package, create a new class called `TreeOrgManagerSetup`
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
### Integrating TreeAssets into the Realm

With the `TreeAsset` class in place, the next step is to integrate these assets into our realm. In the `TreeOrgManagerSetup` class, we implement logic to create `TreeAssets` around a specific location, such as Eindhoven. Here is a detailed look at the process:

#### TreeOrgManagerSetup Class

The `TreeOrgManagerSetup` class includes methods to create and manage `TreeAssets` dynamically. This implementation results in a group of 100 TreeAssets, all associated under the ThingAsset "TreeOrg Assets". These assets are randomly placed around the city based on the specified base location, creating a realistic deployment pattern.

By utilizing this setup, we can efficiently simulate a realistic environment where each TreeAsset represents a physical tree, equipped with sensors, within the city of Eindhoven. Below is the code and explanation of its functionality:

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

### Updating The Map for Enhanced Visualization

Once our `TreeAssets` are in place, the default map may no longer suffice due to the lack of detailed MBTiles for the Eindhoven area, especially at the appropriate zoom levels. To address this, you will need to update your map data. Comprehensive guidance on acquiring and setting up the appropriate MBTiles can be found in the OpenRemote [Developer Guide: Working on Maps](https://github.com/openremote/openremote/wiki/Developer-Guide%3A-Working-on-maps). After obtaining the `mapdata.mbtiles` file that fits our needs, place it in the `deployment/map` directory. Subsequently, you should update the `mapsettings.json` file to reflect the new map configuration, specifically adding some options for your Realm. This ensures that the map accurately displays the geographic distribution and details of our `TreeAssets`, enhancing the user interface and interaction.

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

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
### Adding Custom Services
*How to add custom services to the TreeOrg project.*

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
### Managing Users and Roles
*Guide to managing users and roles in the TreeOrg realm.*

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
### Custom APIs
*Instructions for creating custom APIs.*

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
## Advanced Topics

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
### Integration with External Services
*Guide to integrating the TreeOrg project with external services.*

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
### Performance Optimization
*Tips and techniques for optimizing the performance of the TreeOrg project.*


[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)
## FAQ and Troubleshooting

[Back to Top](#treeorg-tutorial-setting-up-a-custom-project-with-openremote)



