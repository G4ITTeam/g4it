
---
title: "G4IT Backend code architecture"
pre: 🔣
weight: 1
toc_hide: true
---

## Global description

The backoffice is a Springboot 3 application.

It's composed of several API. Each API has its own package and responsability.

Each API interface and model are generated from openapi specification maven plugin.

## Code base

[Consult code base of the main branch](https://github.com/G4ITTeam/g4it/tree/main/services/backend)

The project is a maven Springboot 3 project.

The code base is organized as follow:


The main package is `com.soprasteria.g4it.backend` It contains the following subpackages:

- `apixxxx` : package of the api xxx. Each api has it's own package which begin with the prefix `api`
- `config` : contains the spring configuration of the application
- `common` : contains main technical classes which handle transversal concerns like task management, main database model and some utilities
- `exception` : contain the application exception. The one that are thrown by the application
- `external` : contain the client services to use external services like boavizta estimation engine and numecoeval client access engine
- `scheduler` : local scheduler to manage loacal scheduled tasks like cache refresh, data or filesystem deletion
- `schedulerlocked` : distributed schedulers which run only on one instance of the application. It's used to manage tasks that must be run only once in the cluster like restarting stucked tasks.
- BackendApplication.java : the main class of the application

### API code base

Each API has its own package and is organized as follow:
- `controller` : The delegate of the API. It's the functional entry point of the API. It's responsible to handle the request and to delegate the processing to business services. The technical entry of the endpoint is done by the generated API interface [see below](#api-interface-generation). This component take the input API model and send it to the business services.
- `business` : The business services of the API. It's responsible to handle the business logic of the API. It's the component that handle the business rules and the data processing. It's the component that handle the data access to the database. It returns either directly DTO api model or Business model object. The choice between BO or DTO depend on the business complexity and it's also intended to manipulate data without risking to generate necessary sql requests.
- `model` : the business objects used by the business layer. It's the object that represent the manipulated business data. if they are needed, theses objects make it easy to manipulate data by the business.
- `modeldb` : the entities which represent a database table. Here we find the entities which are under the responsibility of the API. The entities are used by the business layer to manipulate data. The entities are mapped to the database by the ORM (Object Relational Mapping) layer HIBERNATE.
- `repository` : the database access layer. We use Spring data JPA as interface declaration. The repositories are used by the business layer to request data and get modeldb objects.
- `mapper` : the mapper layer. It's the layer that transform the modeldb object to business object and vice versa. It's also used to transform the business object to DTO object and vice versa. The mapper layer is used to separate the business object from the database object and translate them to API DTO objects.

{{< mermaid align="center">}}

sequenceDiagram
Controller->>Mapper: transform DTO to BO
Mapper -->>Controller: return BO
Controller->>Business: call business service using BO as input

Business->>Repository: call repository to get data
Repository -->>Business: return modeldb entities
Business->>Mapper: transform modeldb to BO
Mapper -->>Business: return BO
Business->>Business: process BO data
Business-->>Controller: return BO to the controller
Controller->>Mapper: transform BO to DTO
Mapper-->>Controller: return DTO to the controller

{{</ mermaid >}}

>[!WARNING] Don't use BO if it's not necessary.
If the business process does not need to have it's own data format, no need to use BO. In this case, the business layer can directly use the modeldb entities and return DTO objects.

>[!WARNING] Be aware of the transactional context.
Be aware that each time you manipulate modeldb entities, you are manipulating the database. So, if you manipulate modeldb entities, you must be sure that you are in a transactional context.

>[!WARNING] Be aware of the lazy loading of OneToMany relation.
Avoid to manipulate List of entities of a modeldb. As the OneToMany relation is lazy loaded, you can have a lot of database requests if you manipulate a list of entities. Prefer to use a query to get the list of entities you need with OneToMany relation already loaded.

### API Interface generation

The API interface is generated using openapi-generator-maven-plugin (see: [See the pom.xml](https://github.com/G4ITTeam/g4it/blob/main/services/backend/pom.xml)

The plugin is configured to generate the API interface from the openapi specification file located in the `src/main/resources/swagger/greenit` folder.
It generates the API interface in the package `com.soprasteria.g4it.backend.server.gen.api` and the model in the package `com.soprasteria.g4it.backend.server.gen.api.dto`.
The API is generated using springboot3 annotations with using role authorization and applying the Delegate Pattern.


