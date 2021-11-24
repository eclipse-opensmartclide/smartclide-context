# smartclide-context
SmartCLIDE Context Handling Component

## Preconditions to build and run Context Handling

To build and run Context Handling, the following software is required:#

- Java (at least version ...)
- ...

## How to build Context Handling

1. ... TO DO

## How to configure Context Handling

### Monitoring Config

In the following the xml schema for the monitoring configuration is listed. Below the xsd the configuration elements are described in detail.

**monitoring-config.xsd**

```
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" targetNamespace="http://www.atb-bremen.de" >
  <xs:element name="config">
    <xs:complexType>
      <xs:sequence>
        <xs:element ref="indexes"/>
        <xs:element ref="datasources"/>
        <xs:element ref="interpreters"/>
        <xs:element ref="monitors"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="indexes">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" minOccurs="1" ref="index"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="index">
    <xs:complexType>
      <xs:attribute name="id" use="required" type="xs:ID"/>
      <xs:attribute name="location" use="required" type="xs:anyURI"/>
    </xs:complexType>
  </xs:element>
  <xs:element name="monitors">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" minOccurs="1" ref="monitor"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="monitor">
    <xs:complexType>
      <xs:attribute name="id" use="required" type="xs:ID"/>
      <xs:attribute name="datasource" use="required" type="xs:IDREF"/>
      <xs:attribute name="index" use="required" type="xs:IDREF"/>
      <xs:attribute name="interpreter" use="required" type="xs:IDREF"/>
    </xs:complexType>
  </xs:element>
  <xs:element name="datasources">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" minOccurs="1" ref="datasource"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="datasource">
    <xs:complexType>
      <xs:attribute name="class" use="required"/>
      <xs:attribute name="id" use="required" type="xs:ID"/>
      <xs:attribute name="monitor" use="required"/>
      <xs:attribute name="options" use="required"/>
      <xs:attribute name="type" use="required" type="xs:NCName"/>
      <xs:attribute name="uri" use="required" type="xs:anyURI"/>
    </xs:complexType>
  </xs:element>
  <xs:element name="interpreters">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" minOccurs="1" ref="interpreter"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
  <xs:element name="interpreter">
    <xs:complexType>
      <xs:sequence>
        <xs:element maxOccurs="unbounded" minOccurs="1" ref="configuration"/>
      </xs:sequence>
      <xs:attribute name="id" use="required" type="xs:ID"/>
    </xs:complexType>
  </xs:element>
  <xs:element name="configuration">
    <xs:complexType>
      <xs:attribute name="analyser" use="required"/>
      <xs:attribute name="parser" use="required"/>
      <xs:attribute name="type" use="required"/>
    </xs:complexType>
  </xs:element>
</xs:schema>
```

**monitoring-config.xml**
```
<?xml version="1.0" encoding="utf-8"?>
<config xmlns="http://www.atb-bremen.de"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.atb-bremen.de monitoring-config.xsd">

    <indexes>
        <index id="index-git" location="target/indexes/git"/>
    </indexes>

    <datasources>
        <datasource id="datasource-git" type="messagebroker"
                    monitor="de.atb.context.monitoring.monitors.GitMonitor"
                    uri=""
                    options="server=localhost&amp;port=5672&amp;exchange=smartclide-monitoring&amp;topic=monitoring.git.*&amp;dle-topic=dle.git.commits"
                    class="de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource"/>
    </datasources>

    <interpreters>
        <interpreter id="interpreter-git">
            <configuration type="*"
                           parser="de.atb.context.monitoring.parser.GitParser"
                           analyser="de.atb.context.monitoring.analyser.GitAnalyser"/>
        </interpreter>
    </interpreters>

    <monitors>
        <monitor id="monitor-git" datasource="datasource-git" interpreter="interpreter-git" index="index-git"/>
    </monitors>
</config>
```

The "monitoring-config.xml" configuration file can also be changed by using the Admin UI.


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
    - package de.atb.context.monitoring.monitors.webservice.WebServiceMonitor
- options: Options for the datasource can be entered using this value. The options are dependant on the datasource to be used
- uri:The uri of the data source to be monitored
- class:The following datasource implementations are available
    - package de.atb.context.monitoring.config.models.datasources.DatabaseDataSource
    - package de.atb.context.monitoring.config.models.datasources.FilePairSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.FileSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.FileTripletSystemDataSource
    - package de.atb.context.monitoring.config.models.datasources.WebServiceDataSource

#### interpreters

Each interpreter entry has the following mandatory attributes

- id: The unique name of the interpreter
- configuration
    - analyser: The analyser class to be used. The following implementations are available:
        - package de.atb.context.monitoring.analyser.database.DatabaseAnalyser
        - package de.atb.context.monitoring.analyser.file.FileAnalyser
        - package de.atb.context.monitoring.analyser.file.FilePairAnalyser
        - package de.atb.context.monitoring.analyser.file.FileTripletAnalyser
        - package de.atb.context.monitoring.analyser.webservice.WebServiceAnalyser
    - parser: The parser class to be used. The following implementations are available:
        - package de.atb.context.monitoring.parser.database.DatabaseParser
        - package de.atb.context.monitoring.parser.file.FileParser
        - package de.atb.context.monitoring.parser.file.FilePairParser
        - package de.atb.context.monitoring.parser.file.FileTripletParser
        - package de.atb.context.monitoring.parser.webservice.WebServiceParser
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

The "services-config.xml" configuration file can also be changed by using the Admin UI.


## How to run Context Handling

1. ... TO DO describe how the docker containers are started

