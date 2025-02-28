---
title: "1.3. Load files"
description: "This use case describes how to load files"
weight: 30
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

The use case allows a sustainable IT leader to upload new files into an inventory.
The files are either datacenter, physical equipment, virtual equipment or application file and should be in .csv format.
The loading of the files can be completed, completed with errors, failed or failed because of headers depending on the success of the loading.

**Navigation Path**  
Input :

-   My Information System / Visualize my inventories / New Inventory / Add button
-   My Information System / Visualize my inventories / My inventory / Add files / Upload button
    Output :
-   My Information System / Visualize my inventories / My inventory / Loading history

**Access Conditions**  
The connected user must have the write access for that module on the selected organization.

## State Diagram

### Checking files loading

{{< mermaid align="center">}}

graph TD;
Step1[Loading files] --> |Click on 'UPLOAD' button|Step2[Checking Files] --> Decision1{Errors in the headers ?}
Decision1 --> |Yes|Step3[Display Error: 'Failed headers']
Decision1 --> |No|Decision2{Any validation errors ?}
Decision2 --> |Yes|Step4[Display Warning: 'Completed with errors']
Decision2 --> |No|Step5[Display Validated :'Completed']

{{</ mermaid >}}

## Mockup

### Loading History

![uc3_load_files_loading_history.png](../images/uc3_load_files_loading_history.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

#### Loading History

| Reference | Group           | Elements          | Sub-Elements | Type   | Description                                                                                                                                                                                                                                                                               |
| --------- | --------------- | ----------------- | ------------ | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|           | Loading History |                   |              | button | Access inventory footprint for the application<br><br><li><u>_initialization rules_</u>: Details of the behaviour is described in [1.2. Create or update an inventory (IS version or simulation)](uc2_create_inventory.md).                                                               |
| 1         |                 | Load files        |              | list   | <li><u>_initialization rules_</u>: The list is ordered by try date desc                                                                                                                                                                                                                   |
|           |                 | Loading files try |              | label  |                                                                                                                                                                                                                                                                                           |
| 2         |                 |                   | Status icon  | label  | <li><u>_initialization rules_</u>: 3 existing types: Error, Completed with errors and Completed.                                                                                                                                                                                          |
| 3         |                 |                   | Try dates    | label  |                                                                                                                                                                                                                                                                                           |
| 4         |                 |                   | Download     | button | <li><u>_initialization rules_</u>: The button is displayed in case of "Completed with errors" which means some items could not be loaded on the inventory. That trigger the download of a file containing the items in error. Items can be then corrected in the file and uploaded later. |

#### Loading control and process

| Management rules | Title | Rule description |
| ---------------- | ----- | ---------------- |
| 1                |       |                  |
| 2                |       |                  |
| 3                |       |                  |

{{% /expand %}}

## Sequence Diagram

### Loading files

{{< mermaid >}}

sequenceDiagram
actor RND as Sustainable IT Leader
participant front as G4IT Front-End
participant back as G4IT Back-End
participant DataBase

RND ->> front: Click on "UPLOAD" button in the loading files view
front ->> back: POST /api/{subscriber}/{organization}/inventories/{inventory_id}/files
back -->> DataBase: Uploading files in the chosen inventory
front ->> back: POST /api/{subscriber}/{organization}/inventories/{inventory_id}/loading
back --> DataBase: Split the virtualEquipment records by the typeInfracture 'NON_CLOUD_SERVERS' and 'CLOUD_SERVICES'.
back -->> DataBase: Load 'CLOUD_SERVICES' records in the in_virtual_equipment table and 'NON_CLOUD_SERVERS' in the equipement_virtuel table.
back --> DataBase: Split the application records by the type 'associeAvecUnEquipmentCloud'.
back -->> DataBase: Load application associated to cloud in the in_application table are rest to the application table.
back -->> DataBase: Update the loading history
front ->> back: GET /api/{subscriber}/{organization}/inventories/{inventory_id}
back -->> DataBase: Get the updated inventory
back ->> front: Display the updated loading history

{{< /mermaid >}}
