<h1 align="center" id="#g4it" style="margin-top: 0px;">G4IT</h1>

<p align="center" style="margin-bottom: 0px !important;">
  <img width="200" src="services/frontend/src/assets/images/logos/logo-overgreen-small.svg" alt="G4IT logo" align="center">
</p>


G4IT is a platform that facilitates the assessment, definition and management of plans to reduce the impact of digital technology, at an organizational scale

[![LICENSE : APACHE LICENSE 2.0](services/frontend/src/assets/images/logos/apache-license.svg)](LICENSE.txt)

## Features

- Create a reliable view of the components of an IS or a digital service
- Initiate assessments on the defined perimeter to assess the environmental impact of the IS or digital service.
- Understand and target key impacts to prioritise efforts on what really matters
- Define and manage the plan to reduce the footprint of IS and its digital services
- Impact assessment according to international standards: multi-stage, multi-criteria and multi-component
- Take your analysis further by extracting data from the platform

## License  


G4IT is licensed. See [license file](LICENSE.txt) in the same directory.


## Building


### Overview

**G4IT** is the service developed by **Sopra Steria** based on the **NumEcoEval calculation engine**.

More information on this subject can be found in the documentation, but in this page, it will only be about its
deployment on a developer station.

### Prerequisites

**G4IT** relies on **environment variables** to run. Therefore, you **must declare** the following variables (windows
env properties):

- SPRINGDOC_SWAGGERUI_OAUTH_API
- SPRINGDOC_SWAGGERUI_OAUTH_CLIENTID
- AZURE_TENANT_ID
- AZURE_SUBSCRIPTION_ID
- AZURE_CLIENT_ID
- AZURE_CLIENT_SECRET
- SPRING_CLOUD_AZURE_KEYVAULT_SECRET_ENDPOINT

Retrieve these values on the gitlab CI/CD configuration.

### Retrieving source code

Proposed path : **C:\G4IT\\_repo**

```shell
# open git bash
mkdir -p /c/G4IT/_repo
cd /c/G4IT/_repo
# G4IT code
git clone https://github.com/G4ITTeam/g4it.git

# NumEcoEval code
git clone https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval.git
```

### Kafka and Postgresql containers

- NumEcoEval needs kafka and postgresql to work.
- G4IT needs postgresql to work.

To run kafka and postgresql, open a command prompt:
```shell
# open git bash or any prompt
cd /c/G4IT/_repo/g4it/workspace/numecoeval
podman-compose up -d
podman-compose ps
# It should start kafka and postgresdb containers
# You should see the containers in Podman Desktop > Containers
```

### IntelliJ Workspace 

Launch IntelliJ Idea, and open the folder **C:\G4IT\\_repo\g4it\\workspace**

The workspace comes with :

- Launch Configurations:
  - All (G4IT + NumEcoEval)
  - individual launch

- Folders: backend, NumEcoEval components

## Further information 

Feel free to read README.md files in backend and frontend folders.

[Backend README](services/backend/README.md)

[Frontend README](services/frontend/README.md)


## Contact 

For any request you can contact the G4IT Team <a href="mailto:support.g4it@soprasteria.com"> support.g4it@soprasteria.com</a> 


[Back to top](#g4it)
