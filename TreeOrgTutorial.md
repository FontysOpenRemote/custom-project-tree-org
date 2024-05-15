# TreeOrg Tutorial: Setting Up a Custom Project with OpenRemote

Welcome to the TreeOrg tutorial! This guide will help you set up a custom project using OpenRemote and customize it to fit the needs of our fictional company, TreeOrg.

## Table of Contents
1. [Introduction](#introduction)
2. [Setting Up the Development Environment](#setting-up-the-development-environment)
3. [Creating the TreeOrg Realm](#creating-the-treeorg-realm)
4. [Customizing TreeOrg](#customizing-treeorg)
5. [Advanced Topics](#advanced-topics)
6. [FAQ and Troubleshooting](#faq-and-troubleshooting)

## Introduction

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


## Customizing TreeOrg

### Adding Custom Services
*How to add custom services to the TreeOrg project.*

### Managing Users and Roles
*Guide to managing users and roles in the TreeOrg realm.*

### Custom APIs
*Instructions for creating custom APIs.*

## Advanced Topics

### Integration with External Services
*Guide to integrating the TreeOrg project with external services.*

### Performance Optimization
*Tips and techniques for optimizing the performance of the TreeOrg project.*

## FAQ and Troubleshooting

### FAQ
*Frequently asked questions about the TreeOrg project.*

### Troubleshooting
*Common issues and their solutions.*



