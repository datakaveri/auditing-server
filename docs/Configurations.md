<p align="center">
<img src="./cdpg.png" width="300">
</p>

# Modules
This document contains the information of the configurations to setup various services and dependencies in order to bring up the DX Auditing Server. 
Please find the example configuration file [here](https://github.com/datakaveri/auditing-server/blob/main/example-configs/config-dev.json). While running the server, config.json file could
be added [secrets](https://github.com/datakaveri/auditing-server/tree/main/secrets/all-verticles-configs).

## Other Configuration

| Key Name   | Value Datatype | Value Example | Description                                                 |
|:-----------|:--------------:|:--------------|:------------------------------------------------------------|
| version    |     Float      | 1.0           | config version                                              |
| zookeepers |     Array      | zookeeper     | zookeeper configuration to deploy clustered vert.x instance |
| clusterId  |     String     | audit-cluster | cluster id to deploy clustered vert.x                       |
| host       |     String     | audit-host    | the hostname for clustering                                 |


## RabbitMq Verticle

| Key Name                 | Value Datatype | Value Example      | Description                                                                                            |
|:-------------------------|:--------------:|:-------------------|:-------------------------------------------------------------------------------------------------------|
| verticleInstances        |    integer     | 1                  | Number of instances required for the verticle                                                          |
| dataBrokerIP             |     string     | localhost          | RMQ IP address                                                                                         |
| dataBrokerPort           |    integer     | 24568              | RMQ port number                                                                                        |
| dataBrokerUserName       |     string     | rmqUserName        | User name for RMQ                                                                                      |
| dataBrokerPassword       |     string     | rmqPassword        | Password for RMQ                                                                                       |
| dataBrokerManagementPort |    integer     | 28041              | Port on which RMQ Management plugin is running                                                         |
| connectionTimeout        |    integer     | 6000               | Setting connection timeout as part of RabbitMQ config options to set up webclient                      |
| requestedHeartbeat       |    integer     | 60                 | Defines after what period of time the peer TCP connection should be considered unreachable by RabbitMQ |
| handshakeTimeout         |    integer     | 6000               | To increase or decrease the default connection time out                                                |
| requestedChannelMax      |    integer     | 5                  | Tells no more that 5 (or given number) could be opened up on a connection at the same time             |
| networkRecoveryInterval  |    integer     | 500                | Interval to restart the connection between rabbitmq node and clients                                   |
| automaticRecoveryEnabled |    boolean     | true               | Enables or disables automatic connection recovery.                                                     |
| prodVhost                |     string     | prodVhostValue     | Production vHost used for consuming audit information.                                                 |
| internalVhost            |     string     | internalVhostValue | Internal vHost used for consuming audit information.                                                   |
| externalVhost            |     string     | externalVhostValue | External vHost used for consuming audit information.                                                   |

## Processor Verticle

| Key Name                    | Value Datatype | Value Example         | Description                                                     |
|:----------------------------|:--------------:|:----------------------|:----------------------------------------------------------------|
| verticleInstances           |    integer     | 1                     | Number of instances required for the verticle                   |
| immudbRsTableName           |     string     | rsAuditing            | Table name for auditing Resource Server in Immudb               |
| immudbCatTableName          |     string     | catAuditing           | Table name for auditing Catalogue Server in Immudb              |
| immudbAuthTableName         |     string     | aaaAuditing           | Table name for auditing Auth Server in Immudb                   |
| postgresRsTableName         |     string     | rsAuditing            | Table name for auditing Resource Server in postgres             |
| postgresCatTableName        |     string     | catAuditing           | Table name for auditing Catalogue Server in postgres            |
| postgresAuthTableName       |     string     | aaaAuditing           | Table name for auditing Auth Server in postgres                 |
| postgresOgcTableName        |     string     | ogcAuditing           | Table name for auditing Ogc Server in postgres                  |
| postgresConsentLogTableName |     string     | consentLogAuditing    | Table name for auditing Consent log in postgres                 |
| immudbConsentLogTableName   |     string     | consentLogAuditing    | Table name for auditing Consent log in immudb                   |
| immudbOgcTableName          |     string     | ogcAuditing           | Table name for auditing Ogc Server in immudb                    |
| postgresAclApdTableName     |     string     | apdAuditing           | Table name for auditing ACL-APD Server in postgres              |
| immudbApdTableName          |     string     | apdAuditing           | Table name for auditing ACL-APD Server in Immudb                |
| postgresDmpApdTableName     |     string     | dmpApdAuditing        | Table name for auditing DataMarketPlace in postgres             |
| immudbDmpApdTableName       |     string     | apdAuditing           | Table name for auditing DataMarketPlace Server in Immudb        |


## Postgres Verticle

| Key Name          | Value Datatype | Value Example | Description                                   |
|:------------------|:--------------:|:--------------|:----------------------------------------------|
| verticleInstances |    integer     | 1             | Number of instances required for the verticle |
| databaseIP        |     String     | localhost     | Postgres Database IP address                  |
| databasePort      |    integer     | 5433          | Postgres Port number                          |
| databaseName      |     String     | auditing      | Postgres Database name                        |
| databaseUserName  |     String     | dbUserName    | Postgres Database user name                   |
| databasePassword  |     String     | dbPassword    | Password for Postgres DB                      |
| poolSize          |    integer     | 25            | Pool size for postgres client                 |

## Immudb Verticle

| Key Name                    | Value Datatype | Value Example | Description                                           |
|:----------------------------|:--------------:|:--------------|:------------------------------------------------------|
| verticleInstances           |    integer     | 1             | Number of instances required for the verticle         |
| meteringDatabaseIP          |     String     | localhost     | Postgres Database IP address                          |
| meteringDatabasePort        |    integer     | 5433          | Postgres Port number                                  |
| meteringRSDatabaseName      |     string     | rsDbName      | Database name for auditing Resource Server in Immudb  |
| meteringRSDatabaseUserName  |     string     | rsDbUser      | UserName for auditing Resource Server in Immudb       |
| meteringRSDatabasePassword  |     string     | rsDbPassword  | Password for auditing Catalogue Server in Immudb      |
| meteringAAADatabaseName     |     string     | aaaDbName     | Database name for auditing Auth Server in Immudb      |
| meteringAAADatabaseUserName |     string     | aaaDbUser     | UserName for auditing Auth Server in Immudb           |
| meteringAAADatabasePassword |     string     | aaaDbPassword | Password for auditing Auth Server in Immudb           |
| meteringCATDatabaseName     |     string     | catDbName     | Database name for auditing Catalogue Server in Immudb |
| meteringCATDatabaseUserName |     string     | catDbUser     | Username for auditing Catalogue Server in Immudb      |
| meteringCATDatabasePassword |     string     | catDbPassword | Password name for auditing Catalogue Server in Immudb |
| meteringOgcDatabaseName     |     string     | ogcDbName     | Database name for auditing Ogc Server in Immudb       |
| meteringOgcDatabaseUserName |     string     | ogcDbUser     | UserName for auditing Ogc Server in Immudb            |
| meteringOgcDatabasePassword |     string     | ogcDbPassword | Password for auditing Ogc Server in Immudb            |
| meteringPoolSize            |    integer     | 25            | Connection pool size for each Immudb database         |

## Cache Verticle

| Key Name          | Value Datatype | Value Example | Description                                   |
|:------------------|:--------------:|:--------------|:----------------------------------------------|
| verticleInstances |    integer     | 1             | Number of instances required for the verticle |