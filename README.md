# ORS

## Ontology Repository Services

The Ontology Repository Services (ORS) is a tool for generating web services for storing and managing information of a domain and its metadata.
The information stored in ORS is based on a formal data model that can be designed and built using either an XML schema description (XSD) or an OWL-RDF ontology model. 
The repository provides a RESTful web services API that acts as a back-end for Web-UI components and enables complex queries on the stored data. The REST API services are designed to assist discovery, interlinking and exchange of the stored information. 

The Ontology Repository core and its REST API are designed to be domain independent. One can easily extend or customize the stored data model without requiring any source code changes on the ORS. Addtionally the generated web service REST API can be easily extended and customised according to the use case requirements.

## 1 Features

### 1.1 Supported Features

* Two-way approach for building the Data Model
	- from XML Schema (XSD) - designed to be extended to supporting other popular front-end formats (JSON)
	- from its Ontology formal description (OWL)
* Dynamically update the Data Model by changing the OWL or XSD description 
	- avoids the need to recompile the code
* Web Services front-end for the RDF-Repository
* Support for complex queries independently of the Data model using a totally generic querying schema.
* Easily extensible web service API for customizing the repository according to project needs.
* Support for knowledge-base (ontology and data) reasoning using the Jena RDFS and Transitive reasoenrs. Support for more reasoners will be added in future releases.
* Support for RDF4J repository and ONTOP plugin for accessing individuals from relational databases.

### 1.2 Next release features

* Protege plugin 
* Frontend generation
* JSON-LD support

## 2 Architecture

The ontology repository consists of two services: the WS Application and the RDF repository and SPARQL server. The attached architecture diagram provides a component view of the ORS.

### 2.1 The WS Application 

Allows the management of domain information and exposes a RESTful interface. The service exchanges with its clients XML documents (support for other serialization formats, such as JSON, will be added) according to the schema that is defined by the XSD document. The WS Application accepts RESTful requests and provides methods that store domain data, receive one specific data item in XML, search for data in the given domain by providing an XML query and delete requests. All the XML requests and data descriptions must follow a XSD file (generated from the ontology). This application uses the Jena API to submit requests to the Fuseki Application and is composed of:
1. The Generator component, which generates java classes from XML schema for parsing the XML documents.
2. The Analyzer component that parses the java model and provides a hash map of java classes and their attributes. It is effectively the Data model that is used for conversions to RDF and XML.
3. The RDF Convertor component that uses Jena library to convert the model to RDF.
4. The RDF parser component that converts RDF to Data model, also uses Jena
5. The Web services generator.

### 2.2 The RDF repository

The Apache Jena Fuseki Application is the interface to the Fuseki RDF repository. This application accepts Jena requests from the WS Application that can be a query, a request to add an RDF item, to update an item, etc. It also exposes a SPARQL endpoint with REST interface that allows querying the repository directly using SPARQL queries.

The Fuseki Application as well as the WS Application are both integrated into the same web service container (back-end container).

### 2.3 Libraries and Technologies

* Apache Jena (jena.apache.org). A free and open source Java framework for building Semantic Web and Linked Data applications.
* Apache Jena Fuseki (jena.apache.org/documentation/fuseki2).
* RDF4J Repository (rdf4j.org) with ONTOP plugin (https://github.com/ontop/ontop)
* Jersey framework (jersey.java.net). A JAX-RS API implementation and framework for developing RESTful web services
* JavaParser (javaparser.org). Simple and lightweight set of tools for processing java code programmatically.
* JCodeModel (github.com/phax/jcodemodel). A code generation library

### 2.4 License 

Copyright © CETIC 2018, www.cetic.be 

Authors: Nikolaos Matskanis, Fabian Steels

The ORS is free open source software available under the Apache v2 license. See LICENSE file.

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

From XSD: if the project ha developed an XSD (some cases useful for defining the WS API), it can be used for generating the POJOS using xjc
From OWL: Use the protege plugin ... (TODO)
Manually: create a POJO for each OWL class with all the prooperties as java class members and write the get/set operations. Add the @XMLRootElement annotation on each java class definition.

NOTE: Class names of POJOS must follow the java class name convention otherwise the POJO packages will not be imported in the generated classes! In some cases this may lead to XSD schema elements or OWL classes must also follow java class name convention.

### 3.3 Configuration

#### 3.3.1 RDF Namespaces and ORS configuration

The properties file `src/main/resources/namespaceConfig.properties` configures the ORS. 
This file is composed of:
 
 * Generator section.
 	- The location of the directories that contain the generated files in the _target_ project directory. The default configuration should be sufficient.
    	* POJO_directory 	-> generated POJOs
    	* query_directory 	-> generated query POJOs
    	* target_directory	-> directoroy of the generated project sources
    	* target_package	-> the package the generator is going to generate data
    - The generator generates 3 subpackages
    	* rs -> rest resources
    	* rdf -> converting POJO to rdf
    	* manager -> business logic
 * RDF configuration section. This section defines the RDF <-> POJO conversion rules
 	- RDF namespace configuration. It is necessary to edit this section
 		* defaultNamespace -> the domain ontology namespace 
        * POJO class name and RDF namespace mapping table
 		* Which POJO field must be considered as an RDF label and the language field
 
#### 3.3.2 Repository deployment configuration

The configuration file 'model-resources/resources/config.properties' contains the configuration of (templates are available for fuseki and RDF4J repositories)
	* Repositorty Endpoints:
		- rdfDataEndpoint = http://localhost:18080/fuseki/redirnet/data
 		- rdfDataEndpointUpdate = http://localhost:18080/fuseki/redirnet/update
 		- rdfDataEndpointQuery = http://localhost:18080/fuseki/redirnet?query
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

### 3.6 Example files

TODO 

### 3.7 Known issues

1. Current delete implementation is not enabled at the REST API.
2. POJO generation implementation using OWL files (Protege Plugin support) is not completed, no alternative currenlty available. 