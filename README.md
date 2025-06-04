# e-Move: Intelligent Recharging Platform and Route Optimizer

## Project Description

e-Move is a platform specialized in strategic route planning for electric vehicles. The objective is to assist drivers in optimizing their journeys based on pre-registered charging stations. Unlike systems that rely on real-time data, our solution uses its own database with manually validated and updated information, ensuring accuracy and reliability.

The platform allows users to input their destinations and receive personalized routes, calculated based on their vehicle's autonomy and the location of available charging stations. The system indicates strategic stopping points, estimates the remaining battery level upon arrival at the destination, and, when necessary, suggests charging stations during the trip, including the estimated additional time for each stop.

Currently, the project functions as a terminal-based application, with plans to evolve into a web platform.

## Main Features (Current Terminal Version)

* User registration and login.
* Vehicle registration and listing associated with the user.
* Personalized route calculation:
    * Considers vehicle autonomy (including partially charged battery as informed by the user).
    * Utilizes an internal grid to simulate distances between pre-registered cities.
    * Suggests cities for recharging stops if autonomy is insufficient for the next leg or final destination.
    * Displays the total route distance (based on the grid).
* Uses a MySQL database for data persistence (users, vehicles, cities, etc.).

## Technologies Used

* Java
* MySQL
* JDBC for database connection
* Dijkstra's Algorithm for calculating the shortest distance in the city grid.

## Environment Setup

### Prerequisites

* Java JDK (e.g., version 17 or higher)
* MySQL Server installed and running (e.g., version 8.0 or higher).
* Git (for cloning the repository).
* [Optional] IntelliJ IDEA or another Java IDE.

### Database Setup

1.  **Create the Database:**
    Ensure your MySQL server is running. Create a database for the project (the name used in `Conexao.java` is `EMOVE`):
    ```sql
    CREATE DATABASE EMOVE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  **Create Tables:**
    Execute the `schema.sql` script (you will need to create this file with the `CREATE TABLE` commands for `USUARIOS`, `VEICULOS`, `CIDADES`, `USUARIOS_VEICULOS`, etc., based on your logical model) to create all necessary tables in the `EMOVE` database.
3.  **Populate with Initial Data (Optional):**
    Execute the `data.sql` script (you will need to create this file with `INSERT` commands for cities, popular vehicles, etc.) to populate the database with initial data.
4.  **Credentials Configuration:**
    * Copy the `db.properties.template` file to a new file named `db.properties` in the root of your classpath (usually the `src` folder or `src/main/resources` if using a Maven/Gradle structure).
    * Edit the `db.properties` file with your database URL, MySQL username, and password.
        ```properties
        db.url=jdbc:mysql://localhost:3306/EMOVE
        db.username=YOUR_MYSQL_USERNAME
        db.password=YOUR_MYSQL_PASSWORD
        ```
    * **IMPORTANT:** The `db.properties` file should not be committed to Git and should be listed in your `.gitignore`.

## How to Compile and Run

### Via IDE (e.g., IntelliJ IDEA)
1.  Clone or import the project.
2.  Configure the Java SDK.
3.  Add the `mysql-connector-j-X.Y.Z.jar` to the project libraries.
4.  Create and configure the `db.properties` file in the `src` (or `src/main/resources`) folder.
5.  Run the `Main.Main` class.

### Running the Compiled JAR
1.  Compile the project to generate a JAR file (e.g., `EMOVE.jar`).
2.  Ensure the `mysql-connector-j-X.Y.Z.jar` is accessible (either included in the JAR as a "fat JAR" or in a `lib` folder referenced in the JAR's `MANIFEST.MF`).
3.  Ensure the `db.properties` file is in the root of the JAR's classpath.
4.  Run via terminal:
    ```bash
    java -jar EMOVE.jar
    ```
    (If the driver is not in a fat JAR, you might need to adjust the classpath: `java -cp "EMOVE.jar:lib/mysql-connector-j-X.Y.Z.jar" Main.Main`)

## Authors

* Felipe Giacomini Cocco - RA: 116526
* Fernando Gabriel Perinotto - RA: 115575
* Jhonatas Kévin de Oliveira Braga - RA: 116707
* Lucas dos Santos Souza - RA: 116852
* Samuel Wilson Rufino - RA: 117792

---
*This is an Interdisciplinary Project for the Information Systems course at FHO - Hermínio Ometto Foundation.*
