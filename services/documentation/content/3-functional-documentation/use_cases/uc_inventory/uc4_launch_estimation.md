---
title: "1.4. Launch an estimate"
description: "This use case describes how to launch an estimate"
weight: 40
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a sustainable IT leader to launch the calculation for the assessment of the impact of the application of the information system.
The calculation is based on different indicators that contextualize the impacts observed.

More information about the production of indicators can be found here:
https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/blob/develop/docs/MoteurDeCalculG4IT_V1.1.adoc

**Navigation Path**
Input :
- My Information System / Visualize my inventories / My inventory / Estimate history / ("Estimate" or "Update Estimate")
Output :
- My Information System / Visualize my inventories / My inventory / Estimate history

**Access Conditions**  
The connected user must have the write access for that module one the selected organization.

## State Diagram
{{< mermaid align="center">}}
graph TD;
Step1[List of inventory view] --> Step2[Load files in the inventory] --> Decision1{Is there valid physical equipment file ?}
Decision1 -->|No|Step3[Button 'Launch estimate' disable]
Decision1 -->|Yes|Decision2{Is there already an integration running ?}
Decision2 -->|No|Step3
Decision2 -->|Yes|Decision3{Is there already an estimation running ?}
Decision3 -->|No|Step3
Decision3 -->|Yes|Step4[Button 'Launch estimate' enable] -->|Click on 'Launch estimate' button|Step5[Confirm the action] -->|No|Step4
Step5 -->|Yes|Step6[Estimation in progress] -->|Estimation done|Step7['Update estimate' button available and estimate history is updated]

{{< /mermaid >}}

## Mockup

### Loading History
![uc4_launch_estimation_estimate_history](../images/uc4_launch_estimation_estimate_history.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

#### Estimate History
| Reference | Group                             | Elements  | Sub-Elements   | Type   | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|-----------|-----------------------------------|-----------|----------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Estimate History                  |           |                | button | Launch/ Update inventory footprint estimate. [Environmental Footprint Assessment methodology](../../global_concepts/environmental_footprint_assessment_methodology/_index.md)<br>*NB* : Before launching the first estimate, check that the referential data available for your organization suit your needs. <br><br><li><u>*initialization rules*</u>: The button is available only if a valid physical equipment file is already upload and an integration isn't already running and an estimation isn't already running.<br><br><li><u>*action rules*</u>: Trigger the request of an estimate. |
| 1         |                                   | Estimates |                | List   | <li><u>*initialization rules*</u>: The list is ordered by try date desc                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 2         |                                   |           | Status icon    | Label  | <li><u>*initialization rules*</u>: 3 existing status : <br>Error (Estimation could not be performed),<br> Completed (Inventory have been successfully submitted to NumEcoEval for estimation but it does not mean that the estimation is ready, wait around 10 min to let the compute finish).)<br>                                                                                                                                                                                                                                                                                                |
| 3         |                                   |           | Estimate dates | button |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 4         | Choose criteria for the inventory |           |                | button | Details of the behaviour is described in  [3.2.5 Choose criteria](../uc_administration/uc_administration_manage_organizations/uc5_choose_criteria.md)                                                                                                                                                                                                                                                                                                                                                                                                                                              |

#### Estimate control and process

| Management rules | Title | Rule description |
|------------------|-------|------------------|
| 1                |       |                  |
| 2                |       |                  |
| 3                |       |                  |

{{% /expand %}}


## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant NumEcoEval
participant BOAVIZTAPI
participant DataBase
participant Azure storage

RND ->> front: Click on "Launch estimate" button
front ->> back: POST /api/{subscriber}/{organization}/inventories/{inventory_id}/evaluation
back ->> BOAVIZTAPI: Estimate the impact of the cloud services via POST /api/v1/cloud/instance
BOAVIZTAPI ->> DataBase: Send cloud indicators data in out_virtual_equipment table
back --> Azure storage: Send cloud_virtual_equipment.csv and cloud_virtual_equipment.csv to Azure storage.
back -> NumEcoEval: Estimate the impact of the inventory via POST /api/entrees/calculs/soumission
NumEcoEval ->> DataBase: Send indicators data in ind_indicateur_impact table
NumEcoEval ->> DataBase: Send cloud indicators data in out_application table
back --> Azure storage: Send cloud_application.csv and ind_cloud_application.csv to Azure storage.
back --> DataBase: Aggregate indicators data in agg_application_indicator and agg_equipment_indicator
front ->> back: GET /api/{subscriber}/{organization}/inventories/{inventory_id}
back ->> DataBase: Get the inventory
back -->> front: Display that the estimation is finished

{{< /mermaid >}}
