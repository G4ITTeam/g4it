---
title: "How to load the collected data in G4IT?"
description: "This section is a user guide to explain where the data need to be loaded in the application"
weight: 20
---

<!-- TOC -->
  * [Accessing the Inventory Module in G4IT](#accessing-the-digital-service-module-in-g4it)
    * [**Step 1:** Access the Inventory Module](#step-1-access-the-digital-service-module)
  * [Creating a New Inventory](#creating-a-new-digital-service)
    * [**Step 2:** Start the Evaluation](#step-2-start-the-evaluation)
  * [Adding the Collected Data](#adding-the-collected-data)
    * [**End-user Devices**](#end-user-devices)
      * [**Step 3:** Add a New Device](#step-3-add-a-new-device)
      * [**Step 4:** Complete Device Information](#step-4-complete-device-information)
      * [**Step 5:** Edit or Delete Devices](#step-5-edit-or-delete-devices)
    * [**Data Exchange**](#data-exchange)
      * [**Step 6:** Add a Network](#step-6-add-a-network)
      * [**Step 7:** Complete Network Information](#step-7-complete-network-information)
    * [**Servers**](#servers)
      * [If You Know the Details About Your Infrastructure Configuration](#if-you-know-the-details-about-your-infrastructure-configuration)
        * [**Step 8:** Create a Server](#step-8-create-a-server)
        * [**Step 9:** Choose the Type of Server](#step-9-choose-the-type-of-server)
        * [**Step 10:** Complete Server Information](#step-10-complete-server-information)
        * [**Step 11:** Create a New Datacenter](#step-11-create-a-new-datacenter)
        * [**Step 12:** Create a Virtual Machine (VM)](#step-12-create-a-virtual-machine-vm)
      * [**Cloud Hosting (IaaS - Infrastructure as a Service)**](#cloud-hosting-iaas---infrastructure-as-a-service)
        * [**Step 13:** Create a Cloud Service](#step-13-create-a-cloud-service)
        * [**Step 14:** Complete Cloud Services Information](#step-14-complete-cloud-services-information)
  * [Scenario Used as an Example](#scenario-used-as-an-example)
  * [Functional Documentation](#functional-documentation)
<!-- TOC -->

---

## Load inventory data in G4IT

### **Step 1:** Access the Inventory Module

- **Action**: Click on the @ icon to access the module.

![Screenshot showing the user interface with an icon for accessing the Inventory Module.](../media/01_ds_module_access.png "Screenshot of the user interface with an icon for accessing the Inventory Module.")

---

## Creating a New Inventory

### **Step 2:** Start the Evaluation

- **Action**: Click the "Evaluate New Service" button to begin creating a new Inventory.

![Screenshot showing the "Evaluate New Service" button highlighted on the interface.](../media/02_create_a_ds.png "Screenshot of the 'Evaluate New Service' button highlighted on the interface.")

---

## Adding the Collected Data

### **End-user Devices**

#### **Step 3:** Add a New Device

- **Action**: Click the "Add Device" button to create a new device entry.

![Screenshot showing the "Add Device" button for adding new device information.](../media/03_edit_name_create_device.png "Screenshot showing the 'Add Device' button for adding new device information.")

#### **Step 4:** Complete Device Information

- **Action**: Fill in the required fields for your end-user device:
    1. **Device Type**
    2. **Country of Use**: This information will help determine which electricity mix to use when evaluating the impact of usage.
    3. **Number of Users**
    4. **Average Usage Per Year**
    5. **Device Lifespan**
    6. Click **Add Device** to save your input.

![Screenshot showing the form to fill in device information, with fields such as device type, country, and usage.](../media/04_add_device.png "Screenshot showing the form to fill in device information, including device type, country, and usage.")

#### **Step 5:** Edit or Delete Devices

- **Action**: If necessary, click **Edit (1)** or **Delete (2)** to modify or remove an end-user device.

![Screenshot showing the options to edit or delete a device entry, with icons for editing and deleting.](../media/05_edit_device.png "Screenshot showing the options to edit or delete a device entry, with icons for editing and deleting.")

---

### **Data Exchange**

#### **Step 6:** Add a Network

- **Action**: Click the "Add Network" button to create a new network.

![Screenshot showing the "Add Network" button to create a new network entry.](../media/06_add_network.png "Screenshot showing the 'Add Network' button to create a new network entry.")

#### **Step 7:** Complete Network Information

- **Action**: Fill in the necessary details about the terminal network:
    1. **Network Type and Location**
    2. **Annual Data Exchange Volume**
    3. Click **Add Network** to save.

![Screenshot showing the form to fill in network details, including network type, location, and data volume.](../media/07_add_network.png "Screenshot showing a form to fill in network details, including network type, location, and data volume.")

---

### **Servers**

#### If You Know the Details About Your Infrastructure Configuration

##### **Step 8:** Create a Server

- **Action**: Click the "Add Server" button to begin adding your server.

![Screenshot showing the "Add Server" button for creating a new server entry.](../media/08_create_server.png "Screenshot showing the 'Add Server' button for creating a new server entry.")

##### **Step 9:** Choose the Type of Server

- **Action**: Select the type of server you want to create:
    1. **Name**: Assign a unique name to your server.
    2. **Server Type**: Choose between **Dedicated** (a physical server dedicated to your service) or **Shared** (a server shared with other services, managed via virtual machines that you’ll configure later).
    3. **Server Purpose**: Choose **Compute** (for processing power) or **Storage** (for storing data).

![Screenshot showing the options for selecting server type and server purpose, with a form for entering server details.](../media/09_add_server_step_1.png "Screenshot showing the options for selecting server type and server purpose, with a form for entering server details.")

##### **Step 10:** Complete Server Information

- **Action:**
    1. **Select the Reference of the Physical Server**: You can use one of the first three options for rough sizing (S = Small, M = Medium, L = Large).
    2. **Datacenter Selection**: Select a datacenter from the list.
    3. **Create a New Datacenter**: Use the "Add Datacenter" button if necessary.
    4. **Server Characteristics**:
        - Adjust these for a more accurate footprint evaluation based on your knowledge.
        - For example, annual operating time affects the impact of usage.
    5. Complete the server creation or move to the next step.

![Screenshot showing the form to select server reference, datacenter selection, and input for server characteristics.](../media/10_add_server_step_2.png "Screenshot showing the form to select server reference, datacenter selection, and input for server characteristics.")

##### **Step 11:** Create a New Datacenter

- **Action:** Add the characteristics of the datacenter:
    1. **Name**
    2. **Location**: This information will help determine which electricity mix to use when evaluating the impact of usage.
    3. **PUE (Power Usage Effectiveness)**: A measure of energy efficiency in the datacenter. Lower PUE indicates better efficiency, as it compares the energy used by infrastructure versus cooling and power distribution.

![Screenshot showing the form for adding a new datacenter, with fields for name, location, and PUE value.](../media/11_add_server_add_datacenter.png "Screenshot showing the form for adding a new datacenter, with fields for name, location, and PUE value.")

##### **Step 12:** Create a Virtual Machine (VM)

1. **Action:** Add the characteristics of the VM:
    - **Name**
    - **Quantity**
    - **Size**: The resource allocation for each virtual machine (e.g., CPU cores, storage).
    - **Operating Time**: The total uptime of virtual machines throughout the year, measured in hours.
2. **Action:** Edit or delete the VM as needed.
3. **Action:** Finalize the creation of the server and associated virtual machine.

![Screenshot showing the form to add virtual machine characteristics, including size, quantity, and operating time.](../media/12_add_server_step_3.png "Screenshot showing the form to add virtual machine characteristics, including size, quantity, and operating time.")

---

#### **Cloud Hosting (IaaS - Infrastructure as a Service)**

##### **Step 13:** Create a Cloud Service

- **Action**: Click the "Add Cloud Service" button to start adding your cloud infrastructure.

![Screenshot showing the "Add Cloud Service" button to create a new cloud instance.](../media/13_add_cloud_instance.png "Screenshot showing the 'Add Cloud Service' button to create a new cloud instance.")

##### **Step 14:** Complete Cloud Services Information

1. **Action:** Configure the parameters for the cloud services:
    - **Name**
    - **Provider**: e.g., AWS, Azure
    - **Type of Instance**: Based on the available instances for AWS and Azure.
2. **Action:** Describe how the service is used:
    - **Quantity**
    - **Location**: This information will help determine which electricity mix to use when evaluating the impact of usage.
    - **Operating Time**: The total uptime of cloud services throughout the year, measured in hours.
    - **Average Workload**: As a percentage of CPU usage.
3. **Action:** Click **Add** to save your input.

![Screenshot showing the form to configure a cloud service, with fields for provider, instance type, location, and operating time.](../media/14_create_cloud_instance.png "Screenshot showing the form to configure a cloud service, with fields for provider, instance type, location, and operating time.")

---

This current page just show you sample files with minimal data permit you to make a first assessment.
In the datamodel file (cf. picture below to find it) you will explore all the rules necessary to integrate files that permit to estimate environmental impact of an inventory.
![datamodel file](../images/DataModel_Loading.png)

### Datacenter

> NOTE : name of each datacenter must be unique

A Datacenter file is needed for all physical equipment (mainly servers) located in data centres. It permit for now to take into account the efficiency of this datacenter that is measure with PUE indicator. Power usage effectiveness (PUE) is a ratio that describes how much energy is used by the computing equipment in contrast to cooling and other overhead that supports the equipment.

#### Sample template
| Templates                                                                    | Comment                                                                    |
|------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| [Example with mandatory field](../documents/datacenter_sampleMandatory1.csv) | the separator for PUE is a "."<br/>the country must exist in the plateform |

### Physical Equipment
A Physical Equipement file is the most important file in G4IT. It often come from an extract of a  CMDB (Configuration management database).  
you can play with the quantity for a mutualized equipement (example : a TV share with several teams half time, you can put 0,5 in the quantity to take intro account only the part dedicated to your perimeter)

> NOTE : name of each physical equipement must be unique

- **Equipement tpye** : must be in the list of authorize type (a default equipment for each type will be use if )

#### Physical equipment pre-filled template
| Template                                                   | Input                                                     |
|------------------------------------------------------------|-----------------------------------------------------------|
| [Laptop](../documents/User_Devices_01_Laptop_Template.csv) | Serial number; Model; Purchase date; Laptop usage country |

### Virtual Equipment
> NOTE 1:  
Virtual Equipement is larger than Virtual Machine. it represent all it is needed for an application (include a dedicated server. In that last case see here)
When you know the hosted technical architecture. 

> NOTE 2 : Name of each physical equipement must be unique

#### host characteristics under control
#### Cloud Services (host characteristics not under control)
### Application

## Functional Documentation

For detailed information about this module, refer to the [Functional Documentation for Digital Services](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md).
