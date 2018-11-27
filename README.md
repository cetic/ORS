# Ontology Repository Services

The Ontology Repository Services (ORS) is a tool for generating web services for storing and managing information of a domain and its metadata.
The information stored in ORS is based on a formal data model that can be designed and built using an OWL-RDF ontology model. 
The repository provides a RESTful web services API that acts as a back-end for Web-UI components and enables complex queries on the stored data. 
The REST API services are designed to assist discovery, interlinking and exchange of the stored information. 

The Ontology Repository core and its REST API are designed to be domain independent. 
One can easily extend or customize the stored data model without requiring any source code changes on the ORS. 
Addtionally the generated web service REST API can be easily extended and customised according to the use case requirements.

## 1 Features

### 1.1 Supported Features

* Building the Data Model from the Ontology formal description (OWL) using the [ORS-Protege-plugin](https://github.com/cetic/ORS-Protege-plugin).
    - Easily update the Data Model by changing the OWL description 
	- Generic code (not bound to ontology concepts)  does not need to be changed  
* Web Services REST API for the RDF-Repository
* Support for complex queries independently of the Data model using a totally generic querying schema.
* Easily extensible web service API for customizing the repository according to project needs.
* Support for knowledge-base (ontology and data) reasoning using the Jena RDFS and Transitive reasoenrs. Support for more reasoners will be added in future releases.
* Support for RDF4J repository and ONTOP plugin for accessing individuals from relational databases.
* Configurable serialization of documents to XML or JSON.
* Generated schema for Frontend support.
* Importing graphs from JSON-LD.
* Exporting graphs to JSON-LD.

## 2 Architecture

The ORS consists of the following services and supporting porjects: 
* The core Ontology Repository Services project that consists of the ORS WS Application, the RDF repository and SPARQL server. 
* The [ORS-Protege-plugin](/https://github.com/cetic/ORS-Protege-plugin) component that generates the model from the ontology.
* The [ORS-GUI](https://github.com/cetic/ORS-GUI) project which is the component that provides the frontend services.

The following architecture diagram provides a component view of the ORS.

![alt text](https://github.com/cetic/ORS/blob/master/src/resources/ORS-20181123.png "ORS Architecture")

### 2.1 The WS Application 

Allows the management of domain information and exposes a RESTful interface. The service exchanges with its clients XML documents (support for other serialization formats, such as JSON, will be added) according to the schema that is defined by the XSD document. The WS Application accepts RESTful requests and provides methods that store domain data, receive one specific data item in JSON/XML, search for data in the given domain by providing an JSON/XML query and delete requests.  This application uses the Jena API to submit requests to the Fuseki Application and is composed of:
1. The RDF Convertor component that uses Jena library to convert the model to RDF.
2. The RDF parser component that converts RDF to Data model, also uses Jena.
3. The Rest Services component that provides WS services and resources.
4. The Document Services component that handles the json-ld import/export operations.

### 2.2 The RDF repository

The Apache Jena Application is the interface to the RDF repository. This application accepts Jena requests from the WS Application that can be a query, a request to add an RDF item, to update an item, etc. It also exposes a SPARQL endpoint with REST interface that allows querying the repository directly using SPARQL queries.
The Jena Application as well as the WS Application are both integrated into the same web service container (back-end container).

The ORS has been developed and tested to use two alternative RDF repoisotries, the Fuseki Repository and the RDF4J Repository. It is foreseen to add support for the Jena TDB repository.

### 2.3 Libraries and Technologies

* Apache Jena (http://jena.apache.org). A free and open source Java framework for building Semantic Web and Linked Data applications.
* Apache Jena Fuseki (http://jena.apache.org/documentation/fuseki2).
* RDF4J Repository (http://rdf4j.org) with ONTOP plugin (https://github.com/ontop/ontop)
* Jersey framework (http://jersey.java.net). A JAX-RS API implementation and framework for developing RESTful web services
* JSONLD-Java component (https://github.com/jsonld-java/jsonld-java) for handling json-ld documents

### 2.4 License 

Copyright © CETIC 2018, www.cetic.be 

Authors: Nikolaos Matskanis, Fabian Steels

The ORS is free open source software available under the Apache v2 license. See the [LICENSE](https://github.com/cetic/ORS/blob/master/LICENSE) file.

### 2.5 Releases

#### Release 0.4 - 23 November 2018

* New features include:
* Improved GUI support
* Support for more RDF types
* Improvements in POST operations
* Import/export of JSON-LD operations
* Graph URI management operations
* Delete/update operatons


#### Release 0.3 - 18 April 2018

* New features include:
* ORS-Protege-plugin support
* Improved GUI support
* Hierarchical views of the Classes
* Support for more RDF types
* Improvements in POST operations

#### Release 0.2 - 20 February 2018

First public release of ORS at tag v0.2.
* GET operations of individuals by Class name and by URI
* POST operations of individuals
* Custom user queries
* Fuseki and RDF4J repositories
* Ontop plugin support for mapping and accessing individuals data in an RDBMS
* Reasoner support

#### Release 0.5 - Planned for December 2018

* Include protege-plugin in this project

## 3 Build & Deploy

The procedure for building an ORS project is as follows:
1. Deploy servlet container
	- Tomcat: http://tomcat.apache.org/tomcat-8.5-doc/
2. Deploy the repository 
	- Fuseki: https://jena.apache.org/documentation/fuseki2/
	- RDF4J : http://docs.rdf4j.org/
3. Optionally Load Ontology in the repository
	- by following instructions in repository documentation.
4. Generate Ontology POJOs using ORS-Protege-Plugin
5. Configuration
	- repository deployment configuration
6. Build ORS & generate project
7. Deployment of generated project.

### 3.1 Directory structure

The top level structure of ORS is:

 src/  	-> the sources

 src/generated -> generated sources and resources

 src/main -> generic sources 
 
 src/resources -> configuration resources
 
### 3.2 POJO generation

* From OWL: Use the [ORS-Protege-plugin](https://github.com/cetic/ORS-Protege-plugin) 

NOTE: Class names of POJOS must follow the java class name conventions and rules (for example cannot use reserved java names). If conventions and rules are not followed the POJOs will not be used correctly in the generated project or not at all! This is likely to affect the names used in OWL classes.

### 3.3 Configuration

Repository deployment configuration

The configuration file 'src/main/resources/config.properties' contains the configuration of (templates are available for fuseki and RDF4J repositories)
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

 >cd target/ors_project

 >mvn package

### 3.5 Deployment

The webservices have been tested primarily with Apache Tomcat/8.5

After compiling the sources of 'ors_project' you will find inside its target directory a `war` file named `repository.war`
The last step is to deploy the generated war by importing it into the servlet container (e.g. tomcat 8).

### 3.6 Using the REST API (with examples) 


#### 3.6.1 Get Requests

* Get all individuals of a certain concept (Pizza):

>GET http://[host]:[port]/repository/api/resource/Pizza

* Get an individual of a certain concept by URI:

>GET http://[host]:[port]/repository/api/resource/Pizza?id=http://www.example.org/margarita1 

#### 3.6.2 POST of new Individual with an example

For posting new Individuals on the repository for this redirent-simple example:

POST at the endpoint http://[host]:[port]/repository/api/resource/Pizza
With a JSON message body an example is given bellow.
```json
{
    "typeClass": "Margarita",
    "type": [
      "http://example.org/pizza#Margarita"
    ],
    "id": "http://example.org/margarita1",
    "name":"Margarigta"
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

PUT with message body:

```json
{
"field": "http://example.org/pizza#Pizza",
"filter": {
	"expression": {
		"predicate": "http://www.example.org/pizza#name",
		"operator": "textsearch",
		"value": "Margarita"
		}
	}
}
```

### 3.7 ORS GUI

For feature status and instructions see [ORS-GUI](https://github.com/cetic/ORS-GUI)
