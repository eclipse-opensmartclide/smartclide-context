# smartclide-context
SmartCLIDE Context Handling Component

## Preconditions to build and run Context Handling

To build and run Context Handling, the following software is required:

- Java (at least version 11)
- Apache Maven (at least version 3.5.4)
- Docker (for deploying Context Handling on the SmartCLIDE cluster)

## How to build Context Handling

Context Handling can be built using maven with the following command:

  `mvn package`

In order to build a docker container image that can be deployed to a docker host, the following command can be used:

  `mvn clean ...`

Note: Currently the configuration files are packed into the container images, which means that on each configuration change a new container has to be created and deployed.

## How to run Context Handling

There are two possibilites to execute Context Handling. 

1. Building and runnning the locally built Context Handling can be done using the following command:
   ```
   buildAndRunCHLocally.sh
   ```

2. Executing Context Handling using the Docker container image from the ATB container registry:
   ```
   docker run ....
   ```
   
## How to configure Context Handling

The Context Handling configuration files are store in the following folder:
`./resources/`

### Monitoring Config

In the following the xml schema for the monitoring configuration is listed. Below the xsd the configuration elements are described in detail.

**monitoring-config.xsd**

The xsd file can be found here: [monitoring-config.xsd](https://github.com/eclipse-researchlabs/smartclide-context/blob/main/smartclide-monitoring/src/test/resources/config/monitoring-config.xsd)

**monitoring-config.xml**

The current monitoring config can be found here: [monitoring-config.xml](https://github.com/eclipse-researchlabs/smartclide-context/blob/main/smartclide-monitoring/src/test/resources/config/monitoring-config.xml)

### Description

#### indexes

Each index entry has the following mandatory attributes

- id: The unique name of the index
- location: The URI of the location the index is stored

#### datasources

Each datasource entry has the following mandatory attributes

- id:The unique name of the datasource
- type:The type of the datasource. Possible values are: file, webservice, database
- monitor:The class of the monitor to be used. Possible values are:
    - package de.atb.context.monitoring.monitors.database.DatabaseMonitor
    - package de.atb.context.monitoring.monitors.file.FileSystemMonitor
    - package de.atb.context.monitoring.monitors.file.FilePairSystemMonitor
    - package de.atb.context.monitoring.monitors.file.FileTripletSystemMonitor
    - package de.atb.context.monitoring.monitors.webservice.MessageBrokerMonitor
    - package de.atb.context.monitoring.monitors.webservice.WebServiceMonitor
    - package de.atb.context.monitoring.monitors.GitlabCommitMonitor
    - package de.atb.context.monitoring.monitors.GitMonitor
- options: Options for the datasource can be entered using this value. The options are dependant on the datasource to be used
- uri:The uri of the data source to be monitored
- class:The following datasource implementations are available
    - package de.atb.context.monitoring.config.models.datasources.DatabaseDataSource
    - package de.atb.context.monitoring.config.models.datasources.FilePairSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.FileSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.FileTripletSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource
    - package de.atb.context.monitoring.config.models.datasources.WebServiceDataSource
    - package de.atb.context.monitoring.config.models.datasources.GitlabDataSource

#### interpreters

Each interpreter entry has the following mandatory attributes

- id: The unique name of the interpreter
- configuration
    - analyser: The analyser class to be used. The following implementations are available:
        - package de.atb.context.monitoring.analyser.database.DatabaseAnalyser
        - package de.atb.context.monitoring.analyser.file.FileAnalyser
        - package de.atb.context.monitoring.analyser.file.FilePairAnalyser
        - package de.atb.context.monitoring.analyser.file.FileTripletAnalyser
        - package de.atb.context.monitoring.analyser.webservice.MessageBrokerAnalyser
        - package de.atb.context.monitoring.analyser.webservice.WebServiceAnalyser
        - package de.atb.context.monitoring.analyser.webserviceGitAnalyser
        - package de.atb.context.monitoring.analyser.webservice.GitlabCommitAnalyser
    - parser: The parser class to be used. The following implementations are available:
        - package de.atb.context.monitoring.parser.database.DatabaseParser
        - package de.atb.context.monitoring.parser.file.FileParser
        - package de.atb.context.monitoring.parser.file.FilePairParser
        - package de.atb.context.monitoring.parser.file.FileTripletParser
        - package de.atb.context.monitoring.parser.webservice.MessageBrokerParser
        - package de.atb.context.monitoring.parser.webservice.WebServiceParser
        - package de.atb.context.monitoring.parser.GitlabCommitParser
        - package de.atb.context.monitoring.parser.GitParser

    - type: Currently only used for File analyser and parser. Defines the file extensions to be used.

#### monitors

...

### service configuration

**services-config.xml**
```
<?xml version="1.0" encoding="utf-8"?>
<config xmlns="http://www.atb-bremen.de" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <services>
      <service id="AmIMonitoring">
         <host>localhost</host>
         <location>http://localhost:19001</location>
         <name>AmIMonitoringService</name>
         <server>de.atb.context.services.AmIMonitoringService</server>
         <proxy>de.atb.context.services.IAmIMonitoringService</proxy>
      </service>
      <service id="AmI-repository">
         <host>localhost</host>
         <location>http://localhost:19002</location>
         <name>AmIMonitoringDataRepositoryService</name>
         <server>de.atb.context.services.AmIMonitoringDataRepositoryService</server>
         <proxy>de.atb.context.services.IAmIMonitoringDataRepositoryService</proxy>
      </service>
      <service id="PersistenceUnitService">
         <host>localhost</host>
         <location>http://localhost:19004</location>
         <name>PersistenceUnitService</name>
         <server>de.atb.context.services.PersistenceUnitService</server>
         <proxy>de.atb.core.services.IPersistenceUnitService</proxy>
      </service>
   </services>
</config>
```
