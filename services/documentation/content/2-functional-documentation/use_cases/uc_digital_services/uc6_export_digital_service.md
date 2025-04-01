---
title: "2.6 Export a digital service"
description: "This use case describes how to export a digital service"
weight: 60
mermaid: true
---

## Table of contents

-   [Table of contents](#table-of-contents)
-   [Description](#description)
-   [State Diagram](#state-diagram)
-   [Mockup](#mockup)
-   [Sequence Diagram](#sequence-diagram)

## Description

This use case allows a project team to generate and download the created terminals, networks, cloud services and non-cloud servers files, as well as their evaluated impacts, in .csv format.

**Navigation Path**

My Digital Services / Digital Service view / Export button

**Access Conditions**

The connected member must have the 'write' role for the digital service module one the selected organization.

## State Diagram

{{< mermaid align="center">}}
graph TD;
Step1[Digital service view] -->|Click on 'Export' button| Step2
Step2[Retrieve  CSV files stored<br> in azure stoage] --> Step3[Files downloaded]

{{</ mermaid >}}

## Mockup

![uc6_export_digitalServiceView.png](../images/uc6_export_digitalServiceView.png)

{{% expand title="Behavior rules" expanded="false" center="true"%}}

| Management rules | Title         | Rule description                                                                                                                                                                                                                                                                                  |
| ---------------- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                | Export button | The button is displayed only for users with “write” role access.                                                                                                                                                                                                                                  |
| 2                | Export files  | The files produced are :<br> - 6 files which represent the inventory (datacenter.csv, network.csv, terminal.csv, server.csv, virtual_machines.csv, cloud_instances.csv), <br> - 2 files which represent the impact indicator of the inventory (ind_physical_equipment.csv, ind_cloud_instances) . |

{{% /expand %}}

## Sequence Diagram

{{< mermaid >}}
sequenceDiagram
actor RND as Project Team
participant front as G4IT Front-End
participant back as G4IT Back-End
participant Azure storage

    RND ->> front: Click on 'Export' button on the digital service view
    front ->> back: POST /api/GET /subscribers/{subscriber}/organizations/{organization}/digital-services/{digitalServiceUid}/export
    Azure storage -->> back: Retrieve the zipped files
    back ->> front: zip file downloaded in user's local
    front ->> RND: Exported zipped CSV files

{{</ mermaid >}}
