# smartclide-context
SmartCLIDE Context Handling Component

## Preconditions to build and run Context Handling

To build and run Context Handling, the following software is required:

- Java (at least version 11)
- Apache Maven (at least version 3.5.4)
- Docker (for running tests and deploying Context Handling on the SmartCLIDE cluster)
- docker-compose (for running local sample instance only)

## How to build Context Handling

Context Handling can be built using maven with the following command:

  ```shell
  mvn install
  ```

In order to build and push a container image that can be deployed, the following command can be used:

  ```shell
  mvn install
  mvn jib:build -pl smartclide-monitoring -Djib.to.image="${IMAGE_NAME:IMAGE_TAG}" -Djib.to.auth.username="${CONTAINER_REGISTRY_USERNAME}" -Djib.to.auth.password="${CONTAINER_REGISTRY_TOKEN}"
  ```

## How to run Context Handling

A sample configuration and docker-compose file can be found in the [samples folder](https://github.com/eclipse-opensmartclide/smartclide-context/tree/main/samples).

You can run the sample with the following command: 

   ```shell
   docker-compose -f samples/docker-compose.yml up
   ```
   
## How to configure Context Handling

### Monitoring Config

**monitoring-config.xml**

An example monitoring configuration can be found here: [monitoring-config.xml](https://github.com/eclipse-opensmartclide/smartclide-context/blob/main/samples/config/monitoring-config.xml)

**monitoring-config.xsd**

The corresponding XSD file can be found here: [monitoring-config.xsd](https://github.com/eclipse-opensmartclide/smartclide-context/blob/main/samples/config/monitoring-config.xsd)

### Description

#### indexes

Each index entry has the following mandatory attributes

- id: The unique name of the index
- location: The URI of the location the index is stored

#### datasources

Each datasource entry has the following mandatory attributes

- id:The unique name of the datasource
- type:The type of the datasource. Possible values are: filesystem, webservice, database, messageBroker
- monitor:The class of the monitor to be used. Possible values are:
    - package DatabaseMonitor
    - package FileSystemMonitor
    - package FilePairSystemMonitor
    - package FileTripletSystemMonitor
    - package org.eclipse.opensmartclide.context.monitoring.monitors.webservice.MessageBrokerMonitor
    - package WebServiceMonitor
    - package GitlabCommitMonitor
    - package org.eclipse.opensmartclide.context.monitoring.monitors.GitMonitor
- options: Options for the datasource can be entered using this value. The options are dependent on the datasource to be used
- uri:The uri of the data source to be monitored
- class:The following datasource implementations are available
    - package DatabaseDataSource
    - package FilePairSystemDataSource
    - package FileSystemDataSource
    - package FileTripletSystemDataSource
    - package MessageBrokerDataSource
    - package WebServiceDataSource
    - package GitlabDataSource

#### interpreters

Each interpreter entry has the following mandatory attributes

- id: The unique name of the interpreter
- configuration
    - analyser: The analyser class to be used. The following implementations are available:
        - package DatabaseAnalyser
        - package FileAnalyser
        - package FilePairAnalyser
        - package FileTripletAnalyser
        - package org.eclipse.opensmartclide.context.monitoring.analyser.webservice.MessageBrokerAnalyser
        - package WebServiceAnalyser
        - package org.eclipse.opensmartclide.context.monitoring.analyser.webserviceGitAnalyser
        - package org.eclipse.opensmartclide.context.monitoring.analyser.webservice.GitlabCommitAnalyser
    - parser: The parser class to be used. The following implementations are available:
        - package DatabaseParser
        - package FileParser
        - package FilePairParser
        - package FileTripletParser
        - package org.eclipse.opensmartclide.context.monitoring.parser.webservice.MessageBrokerParser
        - package WebServiceParser
        - package GitlabCommitParser
        - package org.eclipse.opensmartclide.context.monitoring.parser.GitParser

    - type: Currently only used for File analyser and parser. Defines the file extensions to be used.

#### monitors

Each monitor entry has the following mandatory attributes

- id: The unique name of the monitor
- datasource: The id of one previously defined datasource (see above)
- interpreter: The id of one previously defined interpreter (see above)
- index: The id of one previously defined index (see above)
