# Greyhound BusTracker API Integration

This is a standalone Java application that sends a request to the Greyhound BusTracker API at
https://pegasus.greyhound.com/busTracker/dispatch/driverBusAssignment and parses the complete itinerary data obtained
from the response into a MySQL database. 
Subsequent executions check for changes in the API response and update the records in the database accordingly. 
A configurable polling mechanism is provided for scheduled execution. The entire data flow is managed with the
Spring Integration Java DSL. The application build and dependencies are managed with Maven.
The source code includes full unit and integration test coverage with an H2 in-memory database. 


### Frameworks and libraries

- Spring Boot 2.1.2
- Spring Integration 5.1.2
- Spring Data JPA 2.1.4
- Hibernate 5.3.7
- Ehcache 2.10.6
- JUnit 5.4.0
- Mockito 2.23.4
- WireMock 2.21.0

## Setting up and running the application locally

1. Make sure you have `JDK 1.8+` installed.
2. Make sure you have `MySQL Server 5.1+` installed and running.
3. Open `/src/main/resources/application.properties` and adjust your database connection settings as necessary.
4. Compile and run the source code in your IDE.

### Notes

* Hibernate creates the database automatically at first run;
to create it manually, the supplied `bus-tracker-ddl.sql` script can be used.
* The following parameters can be configured inside `/src/main/resources/application.properties`:
    * API endpoint URL
    * Number of retries in case of API failure
    * Delay between retries (in milliseconds)
    * API response timeout (in milliseconds)
    * API polling interval (in minutes)