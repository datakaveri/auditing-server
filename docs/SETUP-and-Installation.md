<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Setup and Installation Guide
This guide provides all necessary steps for installing and configuring the DX Auditing Server to enable auditing of data exchanges.

## Configuration
To configure the DX Auditing Server for connection with Immudb, PostgreSQL, and RabbitMQ, refer to [Configurations](./Configurations.md) for deployment-specific settings.

## Dependencies
This section outlines the required dependencies and their scope. All dependencies must be met before deploying the DX Auditing Server.

### External Dependencies
| Software Name | Purpose                                                                                                                     | 
|:--------------|:----------------------------------------------------------------------------------------------------------------------------|
| PostgreSQL    | Stores and manages audit data for fast querying of audit logs.                                                              |
| Immudb        | Provides immutable storage for audit logs, ensuring data integrity and security by preventing tampering with audit records. |
| RabbitMQ      | Consumes and writes audit-related messages, enabling efficient processing and logging.                                      |

## Prerequisites

### RabbitMQ
- **Setup:** Follow the RabbitMQ setup instructions [here](https://github.com/datakaveri/iudx-deployment/blob/master/Docker-Swarm-deployment/single-node/databroker).
- **Configuration:** After deploying RabbitMQ, ensure the following settings:

#### Create vHost

| Type  | Name          | Details                                                                                                                                      |   
|-------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| vHost | IUDX          | The `IUDX` vhost in RabbitMQ facilitates subscription monitoring by binding exchanges to the auditing queue for accurate message tracking.   |
| vHost | IUDX-INTERNAL | The `IUDX-INTERNAL` vhost in RabbitMQ facilitates audit monitoring by binding exchanges to the auditing queue for accurate message tracking. |


#### Create Exchange

| Exchange Name                     | Type of exchange | features | Details                                                                                                                 |   
|-----------------------------------|------------------|----------|-------------------------------------------------------------------------------------------------------------------------|
| Default binding with All adaptors | direct           | durable  | This exchange in the `IUDX` vHost is used to route subscription-monitoring information to the auditing-message queue.   |  
| auditing                          | direct           | durable  | This exchange in the `IUDX-INTERNAL` vHost is used to route audit-monitoring information to the auditing-message queue. |  


#### Create Queue and Bind to Exchange
| Exchange Name            | Queue Name              | vHost   | routing key                | Details                                                                                                                                           |  
|--------------------------|-------------------------|---------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| <name-of-resource-group> | subscription-monitoring | durable | managed by resource server | This queue in the `IUDX` vHost is used to consume subscription-monitoring information.                                                            |
| auditing                 | auditing-message        | durable | #                          | This queue in the `IUDX-INTERNAL` vHost is used to consume audit-monitoring information, receiving messages routed from the `auditing` exchange . |

#### User and Permissions
Create a DX Auditing user using the RabbitMQ management UI and set write permission. This user will be used by DX Auditing server to publish audit data

| API                                 | cURL command   | Details                                                |   
|-------------------------------------|----------------|--------------------------------------------------------|
| /api/users/<userName>               | As shown below | To create a new user with admin role                   |
| /api/permissions/<vhost>/<userName> | As shown below | Set permission for a user to publish audit information | 

cURL for the user creation request
``` bash
curl --location --request PUT 'http://<host>:<port>/api/users/new_user' \
--header 'content-type: application/json' \
--header 'Authorization: ****' \
--data '{"password":"new_password", "tags":"administrator"}'
```


cURL to Set Permissions for the IUDX Virtual Host
``` bash
curl -u admin:admin_password -X PUT -H "content-type:application/json" \
-d '{"configure":"^$", "write":"^subscriptions-monitoring$", "read":"^subscriptions-monitoring$"}' \
http://<host>:<port>/api/permissions/IUDX/dx-auditing-user
```

cURL to Set Permissions for the IUDX-INTERNAL Virtual Host
``` bash
curl -u admin:admin_password -X PUT -H "content-type:application/json" \
-d '{"configure":"^$", "write":"^auditing$", "read":"^auditing-messages$"}' \
http://<host>:<port>/api/permissions/IUDX-INTERNAL/dx-auditing-user
```

### Auditing
#### Immudb
1. **Installation**: Follow the [Immudb setup guide](https://github.com/datakaveri/iudx-deployment/tree/master/docs/immudb-setup).
2. **Schema**: Load initial schemas and indexes for Immudb tables as outlined in [here](https://github.com/datakaveri/auditing-server/blob/main/src/main/resources/immudb/migration/V1_1__init-tables.sql).


| Table Name           | Purpose                                                                                                        |
|----------------------|----------------------------------------------------------------------------------------------------------------|
| **rsaudit**          | Stores auditing data for resource servers, data ingestion, GIS, file servers, and resource server proxy (rsp). |
| **auditingtable**    | Stores auditing data for catalogue services.                                                                   |
| **auditing_acl_apd** | Stores auditing data related to acl-apd.                                                                       |
| **ogcaudit**         | Stores auditing data for Open Geospatial Consortium (OGC) resource servers.                                    |
| **auditing_dmp**     | Stores auditing data for Data Market Place (DMP) server.                                                       |
| **auditing_consent** | Stores auditing data related to user consent logs.                                                             |

#### Postgres
- **Installation**: Follow the [Postgres setup guide](https://github.com/datakaveri/iudx-deployment/blob/master/Docker-Swarm-deployment/single-node/postgres)

- In PostgreSQL, the following tables support efficient querying and retrieval of audit logs across services. This complements the immutable storage in Immudb by offering faster, query-optimized storage.

| Table Name           | Purpose                                                                                                        |
|----------------------|----------------------------------------------------------------------------------------------------------------|
| **auditing_rs**      | Stores auditing data for resource servers, data ingestion, GIS, file servers, and resource server proxy (rsp). |
| **auditing_cat**     | Stores auditing data for catalogue services.                                                                   |
| **auditing_acl_apd** | Stores auditing logs for acl-apd.                                                                              |
| **auditing_ogc**     | Stores auditing logs for OGC resource servers.                                                                 |
| **auditing_dmp**     | Stores auditing logs for the Data Market Place  (DMP) server.                                                  |
| **auditing_consent** | Stores auditing logs related to user consent events.                                                           |

## Installation Steps

### Prerequisite - Make configuration
Create a configuration file from `./configs/config-example.json`.

### Docker based
1. **Install Docker and Docker Compose.**
2. **Clone the repository** and **build the Docker images**:
    ```
   ./docker/build.sh
   ```
3. Modify `docker-compose.yml` to reference the created configuration file.
4. Start the server in production mode:

   ```
   docker-compose up prod
   ```

### JAR
1. Install java 11 and maven
2. Set Environment variables
    ```
    export AUDIT_URL=https://<auditing-domain-name>
    export LOG_LEVEL=INFO
    ```
3. Use maven to package the application as a JAR. Goto the root folder where the pom.xml file is present and run the below command.
   `mvn clean package -Dmaven.test.skip=true`
4. 2 JAR files would be generated in the `target/` directory
    - `iudx.auditing.server-cluster-1.0.0-SNAPSHOT-fat.jar` - clustered vert.x containing micrometer metrics
    - `iudx.auditing.server-dev-1.0.0-SNAPSHOT-fat.jar` - non-clustered vert.x and does not contain micrometer metrics

#### Running the clustered JAR
**Note**: The clustered JAR requires Zookeeper to be installed. Refer [here](https://zookeeper.apache.org/doc/r3.3.3/zookeeperStarted.html) to learn more about how to set up Zookeeper. Additionally, the `zookeepers` key in the config being used needs to be updated with the IP address/domain of the system running Zookeeper.
The JAR requires 3 runtime arguments when running:

* --config/-c : path to the config file
* --host/-i : the hostname for clustering
* --modules/-m : comma separated list of module names to deploy

e.g. `java -jar target/iudx.auditing.server-cluster-1.0.0-SNAPSHOT-fat.jar  --host $(hostname) -c configs/config.json -m iudx.auditing.server.rabbitmq.RabbitMqVerticle,iudx.auditing.server.processor.ProcessorVerticle,iudx.auditing.server.postgres.PostgresVerticle,iudx.auditing.server.metering.ImmudbVerticle,iudx.auditing.server.cache.CacheVerticle
Use the `--help/-h` argument for more information. You may additionally append an `RS_JAVA_OPTS` environment
variable containing any Java options to pass to the application.

e.g.
```
$ export AUDIT_JAVA_OPTS="-Xmx4096m"
$ java $AUDIT_JAVA_OPTS -jar target/iudx.auditing.server-cluster-1.0.0-SNAPSHOT-fat.jar ...

```

#### Running the non-clustered JAR
The JAR requires 1 runtime argument when running

* --config/-c : path to the config file

e.g. `java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory -jar target/iudx.auditing.server-dev-1.0.0-SNAPSHOT-fat.jar -c configs/config.json`

Use the `--help/-h` argument for more information. You may additionally append an `AUDIT_JAVA_OPTS` environment variable containing any Java options to pass to the application.

e.g.
```
$ export AUDIT_JAVA_OPTS="-Xmx1024m"
$ java $AUDIT_JAVA_OPTS -jar target/iudx.auditing.server-dev-1.0.0-SNAPSHOT-fat.jar ...
```


### Maven based
1. Install java 11 and maven
2. Use the maven exec plugin-based starter to start the server
   `mvn clean compile exec:java@auditing-server`

## Logging and Monitoring
### Log4j 2
- For asynchronous logging, logging messages to the console in a specific format, Apache's log4j 2 is used
- For log formatting, adding appenders, adding custom logs, setting log levels, log4j2.xml could be updated : [link](https://github.com/datakaveri/dx-acl-apd/blob/main/src/main/resources/log4j2.xml)
- Please find the reference to log4j 2 : [here](https://logging.apache.org/log4j/2.x/manual/index.html)

### Micrometer
- Micrometer is used for observability of the application
- Reference link: [vertx-micrometer-metrics](https://vertx.io/docs/vertx-micrometer-metrics/java/)
- The metrics from micrometer is stored in Prometheus which can be used to alert, observe,
  take steps towards the current state of the application
- The data sent to Prometheus can then be visualised in Grafana
- Reference link: [vertx-prometheus-grafana](https://how-to.vertx.io/metrics-prometheus-grafana-howto/)
- DX Deployment repository references for [Prometheus](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/prometheus), [Loki](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/loki), [Grafana](https://github.com/datakaveri/iudx-deployment/tree/master/K8s-deployment/K8s-cluster/addons/mon-stack/grafana)

## Testing
### Unit Testing
1. Run the server through either docker, maven or redeployer
2. Run the unit tests and generate a surefire report
   `mvn clean test-compile surefire:test surefire-report:report`
3. Jacoco reports are stored in `./target/`