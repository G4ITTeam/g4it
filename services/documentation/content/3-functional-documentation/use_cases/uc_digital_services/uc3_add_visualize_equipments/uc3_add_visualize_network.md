---
title: '2.3.2 Add or Visualize Network'
description: "This use case describes how to add network equipments to a digital service"
weight: 20
mermaid: true
---
## Table of contents
- [Table of contents](#table-of-contents)
- [Description](#description)
- [Network visualization](#network-visualization)
- [Network add / edit](#network-add--edit)

## Description

This usecase allows a project team to add network equipment into a digital service previously created.

**Navigation Path**
- My Digital Services / My Digital Service / Terminals / Add Network
- My Digital Services / My Digital Service / Terminals / Modify Network

**Access Conditions**
The connected user must have the write access for that module on the selected organization.

## Network visualization

![uc3_add_visualize_equipments_networksTab.png](../../images/uc3_add_visualize_equipments_networksTab.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group      | Elements                        | Type   | Description                                                                                                                                                                                |
|-----------|------------|---------------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           | Tab Header |                                 | group  |                                                                                                                                                                                            |
| 1         |            | Networks                        | title  |                                                                                                                                                                                            |
| 2         |            | + Add Network                   | button | <li><u>*initialization rules*</u>: That button is displayed if the connected user have the write right.<br><br><li><u>*action rules*</u>: That button open the window Network details.<br> |
|           | Tab        |                                 |        |                                                                                                                                                                                            |
| 3         |            | Type                            | column |                                                                                                                                                                                            |
| 4         |            | Yearly quantity of GB exchanged | column |                                                                                                                                                                                            |
| 8         |            | Edit                            | button | <li><u>*action rules*</u>: That button open the window Network details.<br>                                                                                                                |
| 9         |            | Delete                          | button | <li><u>*action rules*</u>: Delete the network from the current Digital Service.<br> Note : The user must click on Calculate to update the footprint estimation.                            |

{{% /expand %}}

## Network add / edit

![uc3_add_visualize_equipments_NetworksAdd.png](../../images/uc3_add_visualize_equipments_NetworksAdd.png)

{{% expand title="Show the behavior detail" expanded="false" center="true"%}}

| Reference | Group           | Elements                                             | Type                 | Description                                                                                          |
|-----------|-----------------|------------------------------------------------------|----------------------|------------------------------------------------------------------------------------------------------|
| 1         | Title           |                                                      |                      | <li><u>*initialization rules*</u>: Coming from "Add Network", the title is "New Network" else "xxx". |
| 2         |                 | Type                                                 | dropdown             |                                                                                                      |
| 3         |                 | Total quantity of GB exchanged by year for all users | Decimal number input |                                                                                                      |
| 4         | Cancel          |                                                      | button               | <li><u>*action rules*</u>: That button close the window Network details.<br>                         |
| 5         | Create / Update |                                                      | button               |                                                                                                      |
{{% /expand %}}
