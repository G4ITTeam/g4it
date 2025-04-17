---
title: "What are the data to be collected?"
description: "This section explains the kind of data you need to collect before starting an assessment"
weight: 10
---

## Data collected

![Schema that described the data that need to be collected in case inventory impact evaluation : employee equipment, network equipment, servers...](../images/data_collected.png)

4 type of data are expected in G4IT

- Datacenter : permit to take into account efficiency of a datacenter
- Physical Equipment : it is a key component : every IT system is based on physical equipment
- Virtual Equipment : It represents in reality a part of a physical equipment and permit to compute impact of an application. It can be Virtual Machine, Pod, part of an hypervisor or a router... 
- Application : an application is a sum of Virtual Equipment.

This organization in permit to load separately each kind of files, several time and permit to automate the load by API.

In this page you can find all data you will need to evaluate impact of an inventory, some influence the calculation of the impact, others do not and just permit to helps user to understand or categorize in GUI.

### **Data Center**

Data affects the Impact :
- **Location of the datacenter**:
    - The country or region where the data center is located.

- **Power Usage Efectiveness of the datacenter**:
    - PUE measure energy efficiency of a data center. Lower PUE indicates better efficiency, as it measures how much
      energy is used by the infrastructure compared to the cooling and power distribution.

Data informs users
- **nomEntite**

### **Physical Equipment**

Data affects the Impact :
- **Equipment characteristic**: Description of the physical servers used, including model and performance characteristics (e.g., CPU
  type, RAM, storage).
 
- **Quantity** : number of Equipments which have same characteristic 
  
- **Equipment Lifespan** (if accurate data exist): The expected lifespan of physical Equipments 

- **Energy Consumption** (if accurate data exist): The annual average power usage of the Equipments, measured in kilowatts per hour (kWh).

- **Location**: The country where the Equipment is located.

- **Satus**: the objective is to know for example if the equipment is in use, in stock, in transit...

- **Datasource**: 

#### **Specificities for servers**

Data affects the Impact :
- **Operating Time**: The amount of time the servers are actively in operation each day or month, impacting wear and
  energy consumption.

Data affects GUI categorization
Data informs users
- **nomEntite**

### **Virtual Machines (VMs)**

- **Quantity**:The total number of virtual machines utilized in the service infrastructure.

- **VM Size**: The resource allocation for each virtual machine (e.g., CPU cores, storage).

- **Operating Time**: The total uptime of virtual machines throughout the year, measured in hours.

Data affects GUI categorization
Data informs users
- **nomEntite**
---

#### **Cloud Hosting (IaaS - Infrastructure as a Service)**

I use cloud service providers (e.g., AWS, Azure) for certain infrastructure needs, and here are the key configurations:

- **Cloud Provider**:
    - The service provider hosting the infrastructure (e.g., Microsoft Azure, Amazon Web Services).

- **Cloud Subscription Information**:
    - **Instance Type**: Specific configuration of the virtual machines (e.g., instance type in AWS, series of virtual
      machines used in Azure).
    - **Location**: The geographical region where the cloud infrastructure is hosted.

- **Usage of Cloud Instances**:
    - **Operating Time**: The duration for which the cloud instances are actively running.
    - **Average workload**: Percentage of server load.

Data affects GUI categorization
Data informs users

[Detailed documentation about the module](../../../../2-functional-documentation/use_cases/uc_digital_services/_index.md)
