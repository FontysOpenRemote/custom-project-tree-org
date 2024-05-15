# TreeOrg Tutorial: Setting Up a Custom Project with OpenRemote

Welcome to the TreeOrg tutorial! This guide will help you set up a custom project using OpenRemote and customize it to fit the needs of our fictional company, TreeOrg.

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
