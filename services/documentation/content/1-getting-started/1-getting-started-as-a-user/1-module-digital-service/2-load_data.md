---
title: "How to load the collected data in G4IT?"
description: "This section is a user guide to explain where the data need to be loaded in the application"
weight: 20
---

<!-- TOC -->

* [Access to the digital service module in G4IT](#access-to-the-digital-service-module-in-g4it)
* [Create a new digital service](#create-a-new-digital-service)
* [Add the data collected](#add-the-data-collected)
    * [End-user devices](#end-user-devices)
    * [Data exchange](#data-exchange)
    * [Servers](#servers)
        * [I know the details about my infrastructure configuration](#i-know-the-details-about-my-infrastructure-configuration)
        * [I subscribed to IaaS to host my infrastructure (e.g., Azure, AWS)](#cloud-hosting-iaas---infrastructure-as-a-service)
* [Scenario used as an example](#scenario-used-as-an-example)

<!-- TOC -->
---

## Access to the Digital Service Module in G4IT

### **Step 1:** Access the Digital Service Module

- **Action**: Click on the @ icon to access the module.

![01_ds_module_access.png](../media/01_ds_module_access.png)

---

## Create a New Digital Service

### **Step 2:** Start the Evaluation

- **Action**: Click the "Evaluate new service" button to begin creating a new digital service.

![02_create_a_ds.png](../media/02_create_a_ds.png)

---

## Add the Data Collected

### **End-user Devices**

#### **Step 3:** Add a New Device

- **Action**: Click the "Add Device" button to create a new device entry.

![03_edit_name_create_device.png](../media/03_edit_name_create_device.png)

#### **Step 4:** Complete Device Information

- **Action**: Fill in the required fields regarding your end-user device:
    1. **Device Type**
    2. **Country of Use** : This information will be used to determine which electricity mix to consider for the impact
       on usage.
    3. **Number of Users**
    4. **Average Usage Per Year**
    5. **Device Lifespan**
    6. Click **Add Device** to save your input.

![04_add_device.png](../media/04_add_device.png)

#### **Step 5:** Edit or Delete Devices

- **Action**: If necessary, click on **Edit (1)** or **Delete (2)** to modify or remove an end-user device.

![05_edit_device.png](../media/05_edit_device.png)

---

### **Data Exchange**

#### **Step 6:** Add a Network

- **Action**: Click the "Add Network" button to create a new network.

![06_add_network.png](../media/06_add_network.png)

#### **Step 7:** Complete Network Information

- **Action**: Fill in the necessary details about the terminal network:
    1. **Network Type and Location**
    2. **Annual Data Exchange Volume**
    3. Click **Add Network** to save.

![07_add_network.png](../media/07_add_network.png)

---

### **Servers**

#### I Know the Details About My Infrastructure Configuration

#### **Step 8:** Create a Server

- **Action**: Click the "Add Server" button to start adding your server.

![08_create_server.png](../media/08_create_server.png)

#### **Step 9:** Choose the Type of Server

- **Action**: Select the type of server you want to create:
    1. **Name**: Assign a unique name to your server.
    2. **Server Type**: Choose between **Dedicated** (physical server dedicated to your service) or **Shared** (server
       shared with other services, managed via virtual machines you’ll configure later).
    3. **Server Purpose**: Choose **Compute** (for processing power) or **Storage** (for storing data).

![09_add_server_step_1.png](../media/09_add_server_step_1.png)

#### **Step 10:** Complete server information

- **Action:**
    1. **Select the reference of the physical server**: The first three can be used if you want to approximate with
       macro-sizing (S = small, M = medium and L = large).
    2. **Datacenter selection**: Select a datacenter in the list
    3. **Create a new datacenter** using "Add datacenter" button
    4. **Server characteristics**:
        - Can be adjusted for a more accurate footprint evaluation based on your knowledge.
        - For instance, annual operating time influences the impact on usage.
    5. Create the server or go to the next step

![10_add_server_step_2.png](../media/10_add_server_step_2.png)

#### **Step 11:** Create a new datacenter

- **Action:** Add the characteristics of the datacenter
    1. **Name**
    2. **Location**: This information will be used to determine which electricity mix to consider for the impact
       on usage.
    3. **PUE (Power Usage Effectiveness)**: A measure of energy efficiency in the data center. Lower PUE indicates
       better efficiency, as it measures how much energy is used by the infrastructure compared to the cooling and power
       distribution.

![11_add_server_add_datacenter.png](../media/11_add_server_add_datacenter.png)

#### **Step 12:** Create a virtual machine (VM)

![12_add_server_step_3.png](../media/12_add_server_step_3.png)
---

#### Cloud Hosting (IaaS - Infrastructure as a Service)

![13_add_cloud_instance.png](../media/13_add_cloud_instance.png)
![14_create_cloud_instance.png](../media/14_create_cloud_instance.png)
---

## Scenario used as an example

Lien vers le DS G4IT de démo ?
