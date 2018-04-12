# ORS

## Ontology Repository Services

The Ontology Repository Services (ORS) is a tool for generating web services for storing and managing information of a domain and its metadata.
The information stored in ORS is based on a formal data model that can be designed and built using either an XML schema description (XSD) or an OWL-RDF ontology model. 
The repository provides a RESTful web services API that acts as a back-end for Web-UI components and enables complex queries on the stored data. The REST API services are designed to assist discovery, interlinking and exchange of the stored information. 

The Ontology Repository core and its REST API are designed to be domain independent. One can easily extend or customize the stored data model without requiring any source code changes on the ORS. Addtionally the generated web service REST API can be easily extended and customised according to the use case requirements.

## 1 Features

### 1.1 Supported Features

* Two-way approach for building the Data Model
	- from its Ontology formal description (OWL) using the [ORS-Protege-plugin](https://github.com/cetic/ORS-Protege-plugin).
	- from XML Schema (XSD) 
* Dynamically update the Data Model by changing the OWL or XSD description 
	- avoids the need to recompile the code
* Web Services front-end for the RDF-Repository
* Support for complex queries independently of the Data model using a totally generic querying schema.
* Easily extensible web service API for customizing the repository according to project needs.
* Support for knowledge-base (ontology and data) reasoning using the Jena RDFS and Transitive reasoenrs. Support for more reasoners will be added in future releases.
* Support for RDF4J repository and ONTOP plugin for accessing individuals from relational databases.
* Configurable serialization of documents to XML or JSON.
* API for Frontend support.

### 1.2 Next release features

* Improved Frontend support.
* Imporitng graphs from JSON-LD.
* Exporitng graphs to JSON-LD.

## 2 Architecture

The ORS consists of the following services and supporting porjects: 
* The core Ontology Repository Services project that consists of the ORS WS Application, the RDF repository and SPARQL server. 
* The [ORS-Protege-plugin](/https://github.com/cetic/ORS-Protege-plugin) component that generates the model from the ontology.
* The [ORS-GUI](https://github.com/cetic/ORS-GUI) project which is the component that provides the frontend services.

The following architecture diagram provides a component view of the ORS.

![alt text](https://github.com/cetic/ORS/blob/master/model-resources/resources/ORS-20180412.png "ORS Architecture")

### 2.1 The WS Application 

Allows the management of domain information and exposes a RESTful interface. The service exchanges with its clients XML documents (support for other serialization formats, such as JSON, will be added) according to the schema that is defined by the XSD document. The WS Application accepts RESTful requests and provides methods that store domain data, receive one specific data item in JSON/XML, search for data in the given domain by providing an JSON/XML query and delete requests.  This application uses the Jena API to submit requests to the Fuseki Application and is composed of:
1. The Generator component, which generates java classes from Java POJOs.
2. The Analyzer component that parses the java model and provides a hash map of java classes and their attributes. It is effectively the Data model that is used for conversions to RDF and XML.
3. The RDF Convertor component that uses Jena library to convert the model to RDF.
4. The RDF parser component that converts RDF to Data model, also uses Jena.
5. The Web services generator.

### 2.2 The RDF repository

The Apache Jena Application is the interface to the RDF repository. This application accepts Jena requests from the WS Application that can be a query, a request to add an RDF item, to update an item, etc. It also exposes a SPARQL endpoint with REST interface that allows querying the repository directly using SPARQL queries.
The Jena Application as well as the WS Application are both integrated into the same web service container (back-end container).

The ORS has been developed and tested to use two alternative RDF repoisotries, the Fuseki Repository and the RDF4J Repository

### 2.3 Libraries and Technologies

* Apache Jena (http://jena.apache.org). A free and open source Java framework for building Semantic Web and Linked Data applications.
* Apache Jena Fuseki (http://jena.apache.org/documentation/fuseki2).
* RDF4J Repository (http://rdf4j.org) with ONTOP plugin (https://github.com/ontop/ontop)
* Jersey framework (http://jersey.java.net). A JAX-RS API implementation and framework for developing RESTful web services
* JavaParser (http://javaparser.org). Simple and lightweight set of tools for processing java code programmatically.
* JCodeModel (http://github.com/phax/jcodemodel). A code generation library.

### 2.4 License 

Copyright © CETIC 2018, www.cetic.be 

Authors: Nikolaos Matskanis, Fabian Steels

The ORS is free open source software available under the Apache v2 license. See the [LICENSE](https://github.com/cetic/ORS/blob/master/LICENSE) file.

### 2.5 Releases

#### Release 0.3 - 18 April 2018

* New features include:
* ORS-Protege-plugin support
* Improved GUI support
* Hierarchical views of the Classes
* Support for more RDF types
* Improvements in POST operations

#### Release 0.2 - 20 February 2018

First public release of ORS at tag v0.2.

This version supports:

* GET operations of individuals by Class name and by URI
* POST operations of individuals
* Custom user queries
* Fuseki and RDF4J repositories
* Ontop plugin support for mapping and accessing individuals data in an RDBMS
* Reasoner support

#### Release 0.4 - Planned for June 2018

Release with refinements and some new features required for the Evidence2e-CODEX project.

Such features are:

* JSON-LD support

## 3 Build & Deploy

The procedure for building an ORS project is as follows:
1. Deploy servlet container
	- Tomcat: http://tomcat.apache.org/tomcat-8.5-doc/
2. Deploy the repository 
	- Fuseki: https://jena.apache.org/documentation/fuseki2/
	- RDF4J : http://docs.rdf4j.org/
3. Load Ontology in the repository
	- by following instructions in repository documentation.
4. Generate Ontology POJOs
5. Configuration
	- RDF namespaces and ORS configuration
	- repository deployment configuration
6. Build ORS & generate project
7. Deployment of generated project.

### 3.1 Directory structure

The top level structure of ORS is:

 src/  	-> the sources
 
 model-resources/generated-sources/ -> the generated POJOs and query schema files
 
 model-sources/resources/ 		-> deployment configuration files with example configurations
 
 example-resources/	-> examples for testing the ORS.

### 3.2 POJO generation

* From XSD: if the project has developed an XSD (some cases useful for defining the WS API), it can be used for generating the POJOS using [xjc](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/xjc.html)
* From OWL: Use the [ORS-Protege-plugin](https://github.com/cetic/ORS-Protege-plugin) 
* Manually: create a POJO for each OWL class with all the prooperties as java class members and write the get/set operations. Add the @XMLRootElement annotation on each java class definition.

NOTE: Class names of POJOS must follow the java class name conventions and rules (for example cannot use reserved java names). If conventions and rules are not followed the POJOs will not be used correctly in the generated project or not at all! This is likely to affect the names used in XSD schema elements or OWL classes.

### 3.3 Configuration

#### 3.3.1 RDF Namespaces and ORS configuration

The properties file `src/main/resources/namespaceConfig.properties` configures the ORS. 
This file is composed of:

* Generator section.
	- The location of the directories that contain the generated files in the _target_ project directory. The default configuration should be sufficient.
    	* POJO_directory 	-> generated POJOs (default value should be sufficient for most cases)
    	* query_directory 	-> generated query POJOs (default value should be sufficient)
    	* target_directory	-> directoroy of the generated project sources (default value should be sufficient)
    	* target_package	-> the package the generator is going to generate data (default value should be sufficient)
    	* serialization		-> two options are currently supported: application/json or application/xml.
* RDF configuration section. This section defines the RDF <-> POJO conversion rules
	- RDF namespace configuration. It is necessary to edit this section
 		* defaultNamespace -> the domain ontology namespace 
        * POJO class name and RDF namespace mapping table
 		* Which POJO field must be considered as an RDF label and the language field
 
The ORS-Protege-plugin generates a namespaceconfiguration properties file. More specifically it creates the RDF configuration section while using the default values for the other sections. 

#### 3.3.2 Repository deployment configuration

The configuration file 'model-resources/resources/config.properties' contains the configuration of (templates are available for fuseki and RDF4J repositories)
* Repositorty Endpoints:
	- rdfDataEndpoint = http://[host]:[port]/fuseki/[dataset]/data
 	- rdfDataEndpointUpdate = http://[host]:[port]/fuseki/[dateset]/update
 	- rdfDataEndpointQuery = http://[hotst]:[port]/fuseki/[dataset]?query
* repositoryModel -> repository serialisation model class. Use the ones in the templates
* reasoner -> reasoner type. Currently supporting the jena default reasoners: 
 	- 'RDFS' value for RDFSReasoner
 	- 'Transitive' value for TransitiveReasoner 

### 3.4 Build ORS & generate project

 >mvn clean package

 >cd target

 >chmod 755 generate.sh

 >./generate.sh
 
 You will find a directory named `generated_model`.
 This directory contains the sources of the web application.
 
 >cd generated_model

 >mvn package

### 3.5 Deployment

The webservices have been tested primarily with Apache Tomcat/8.5

After compiling the sources of 'generated_model' you will find inside its target directory (/path_to/generated_model/target) a `war` file named `repository.war`
The last step is to deploy the generated war by importing it into the servlet container (e.g. tomcat 8.5).

### 3.6 Using the REST API (with examples) 

The following example files are available for trying out the ORS:

* ./example-resources/redirnet-simple/redirnet-simple-example.owl -> Simple and small ontology with some individuals for deploying in Fuseki repository.
* ./example-resources/redirnet-simple/(java files) -> the POJOs generated from the ontology
* Make sure that the namespace.config contains the correct class and namespace mapping.
* Follow the instructions of this chapter (3) to build and deploy an ORS project using this example files.

#### 3.6.1 Get Requests

* Get all individuals of a certain concept (for example get all Events):

>GET http://[host]:[port]/repository/api/Event

* Get an individual of a certain concept by URI:

>GET http://[host]:[port]/repository/api/Event?id=http://www.loa-cnr.it/ontologies/DUL.owl%23Event/e2457b 

#### 3.6.2 POST of new Individual with an example

For posting new Individuals on the repository for this redirent-simple example:

POST at the endpoint http://[host]:[port]/repository/api/Event
With message body:
```xml
<event>
	<id>http://www.loa-cnr.it/ontologies/DUL.owl#Event/e2457b</id>
</event>
```

POST at the endpoint http://[host]:[port]/repository/api/Sensor
With JSON message body:
```json
{
"id":"http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#T_CCTV_NE-TEST3",
"administeredBy":{
	"id":"http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#TunnelAgency"
	},
"deployedAt":{
	"id":"http://www.semanticweb.org/ontologies/2015/redirnet-core-ontology#AubangeTunnel_Event20150520"
	},
"operatedBy":{
	"id":"http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#TunnelAgency"
	},
"resourceLocation":{
	"id":"http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#AubangeTunnel_Event20150520"
	} 
}
```

#### 3.6.3 Making a Query with an example

For making a complex query client applications can use the schema defined in [query.xsd](https://github.com/cetic/ORS/blob/master/model-resources/generated-sources/query.xsd).
For building the query applications can retireve the available predicates:

GET http://[host]:[port]/repository/api/query/predicates

and fields: 

GET http://[host]:[port]/repository/api/query/fields

And one of the operators currently supported: {textsearch, in, not in }

GET http://[host]:[port]/repository/api/query/operators

On the endpoint http://[host]:[port]/repository/api/query

POST with message body:
```xml
<?xml version="1.0" encoding="UTF-8"?>
 <query xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:noNamespaceSchemaLocation="file:/C:/Users/redirnet/Documents/OntologyRepositoryServices/trunk/model-resources/generated-sources/query.xsd">
     <field>http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology/Resource</field>
     <filter>
         <expression>
             <predicate>http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology/deployedAt</predicate>
             <operator>textsearch</operator>
             <value>Waterloo</value>
         </expression>
     </filter>
 </query>
```

```json
{
"field": "http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology/Resource",
"filter": {
	"expression": {
		"predicate": "http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology/deployedAt",
		"operator": "textsearch",
		"value": "Waterloo"
		}
	}
}
```

### 3.7 ORS GUI

For feature status and instructions see [ORS-GUI](https://github.com/cetic/ORS-GUI)

### 3.8 Known issues

1. Current delete implementation is not enabled at the REST API.
