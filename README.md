# MyRetail

MyRetail is a microservice that provides following abilities to the client,

    1. Retrieve Product and Price information as JSON by Product ID using GET endpoint
    2. Modify Price of a product by Product ID using PUT endpoint

## Accesing the App

1. Use Swagger-ui http://localhost:8080/swagger-ui.html
2. GET MyRetail/v1/products/{id} returns Product Information for given product ID
3. PUT MyRetail/v1/products/{id} allows user to update Price (request payload) for the given Product ID 

## Technologies Used

1. Spring Boot - version: 2.0.1.RELEASE - REST service
2. Cassandra - version 3.11.2  - NoSQL Data storage
3. Swagger-UI - version 2.8.0 - Api documentation
4. Gradle - version 4.6 - Build

## Instructions to Setup

1. Install Cassandra by following steps on - https://medium.com/@areeves9/cassandras-gossip-on-os-x-single-node-installation-of-apache-cassandra-on-mac-634e6729fad6
2. Install Gradle 
3. Clone the code from the repository
4. Start Cassandra using `cassandra -f` command
5. Create KeySpace and table using the cql script in the path src/main/resources/myretail_scripts.cql
6. Run `gradle bootRun` command on the Project directory
7. Open browser and visit Swagger.
`http://localhost:8080/swagger-ui.html`
8. Swagger-UI shows the required requests and sample responses for the MyRetail endpoints.
