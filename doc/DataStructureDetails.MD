![logo](../logos/SLAC-lab-hires.png)
# Core Inventory System Data Structure

## Table of Contents
<!-- TOC -->
* [Core Inventory System Data Structure](#core-inventory-system-data-structure)
  * [Table of Contents](#table-of-contents)
  * [Overview](#overview)
  * [Class Type Entity](#class-type-entity)
    * [Usage](#usage)
    * [Example](#example)
  * [Inventory Element Entity](#inventory-element-entity)
    * [Usage](#usage-1)
    * [Example, using the above class examples](#example-using-the-above-class-examples)
  * [Cable Item Entity](#cable-item-entity)
    * [Examples](#examples)
* [Example global overview](#example-global-overview)
<!-- TOC -->

## Overview
This document provides detailed information about the InventoryItem and ClassType entities
used in the Inventory Management System designed for tracking and managing various items,
including equipment and locations within a nuclear accelerator facility.

## Class Type Entity
The ClassType entity defines the classes of items, cables, and connectors in the system, specifying their attributes,
mandatory requirements, and connectivity rules.
```json lines
{
  "_id": "String",
  "name": "String",
  "classType": "String",
  "attributes": [
  {
    "name": "string",
    "description": "string",
    "mandatory": "true|false",
    "type": "string|integer|double|boolean|date|date-time"
    "unit": "watt|m/s|ampere|etc..."
  }
  ],
}
```
Fields
* _id: Unique identifier for the class type.
* name: Name of the class (e.g., 'ServerClass', 'EthernetCable').
* classType: Nature of the class (Item, Cable, or Connector).
* attributes: Object defining attributes specific to the class, including mandatory/optional status, data type, and unit.

### Usage
The ClassType documents are essential for defining the structure and rules for various items in the inventory.
They provide a template for creating and validating InventoryItem documents, ensuring consistency and integrity
in inventory management.

### Example
This section provides detailed documentation for various ClassTypes used in the Inventory Management System.
These class types define the characteristics and connectivity options for different items such as servers, connectors,
and cables within the system.

Description: Represents servers within the inventory, detailing essential attributes like CPU, RAM, and storage.
```json
{
  "_id": "serverClass",
  "name": "Server",
  "classType": "Item",
  "attributes": {
    "CPU": {"mandatory": true, "type": "string"},
    "RAM": {"mandatory": true, "type": "string", "unit": "GB"},
    "Storage": {"mandatory": true, "type": "string", "unit": "TB"},
    "NetworkPorts": {"mandatory": true, "type": "number"}
  }
}

```
Description: Defines power supplies, including attributes like wattage and efficiency.
```json
{
  "_id": "powerSupplyClass",
  "name": "PowerSupply",
  "classType": "Item",
  "attributes": {
    "Wattage": {"mandatory": true, "type": "number", "unit": "Watts"},
    "EfficiencyRating": {"mandatory": false, "type": "string"}
  }
}
```
Description: Specifies RJ45(M/F) connectors used primarily for Ethernet connections.
```json
{
  "_id": "rj45MaleConnectorClass",
  "name": "RJ45Connector",
  "classType": "Connector",
  "attributes": {
    "Type": {"mandatory": true, "type": "string"},
    "Compatibility": {"mandatory": true, "type": "string"}
  }
}
```
```json
{
  "_id": "rj45FemaleConnectorClass",
  "name": "RJ45Connector",
  "classType": "Connector",
  "attributes": {
    "Type": {"mandatory": true, "type": "string"},
    "Compatibility": {"mandatory": true, "type": "string"}
  }
}
```
Description: Details connectors used for connecting power supplies.
```json
{
  "_id": "powerSupplyFemaleConnectorClass",
  "name": "PowerSupplyConnector",
  "classType": "Connector",
  "attributes": {
    "VoltageRating": {"mandatory": false, "type": "number", "unit": "Volts"}
  }
}
```
```json
{
  "_id": "powerSupplyMaleConnectorClass",
  "name": "PowerSupplyConnector",
  "classType": "Connector",
  "attributes": {
    "VoltageRating": {"mandatory": false, "type": "number", "unit": "Volts"}
  }
}
```
Description: Covers Ethernet cables, defining length, category, and other relevant attributes.
```json 
{
  "_id": "ethernetCableClass",
  "name": "EthernetCable",
  "classType": "Cable",
  "attributes": {
    "Length": {"mandatory": true, "type": "number", "unit": "meters"},
    "Category": {"mandatory": true, "type": "string"}
  }
}
```
Description: Represents power cord cables(F/M), specifying length and plug type.
```json
{
  "_id": "powerCordCableClass",
  "name": "PowerCordCable",
  "classType": "Cable",
  "attributes": {
    "Length": {"mandatory": true, "type": "number", "unit": "meters"},
    "PlugType": {"mandatory": true, "type": "string"}
  }
}
```

## Inventory Element Entity
The InventoryElement entity represents individual element in the inventory. This includes all
physical item and locations and software, such as servers, racks, connectors and software program.
```json lines
{
  "_id": "String",
  "name": "String",
  "classId": "String",
  "parent_id": "String",
  "full_three_path": "String",
  "attributes": "Object",
  "connector_class": "Array",
  "history": "Array"
}
```
Fields
* _id: Unique identifier for the element.
* name: Human-readable name of the element.
* classId: the id of the class type of the element (e.g., 'Server', 'Connector').
* parent_id: ID of the parent item, indicating the item's location or grouping.
* full_three_path: represent the full three path from the root to this element that is the leaf
* attributes: Key-value pairs representing item-specific attributes.
* connector_class: An array of objects, each specifying a type of connector class the item supports and the count of such connectors. This helps in identifying the connectivity capabilities of the item.
* history: Array of historical records, including actions like 'Installed', 'Moved', and their corresponding details.

### Usage
InventoryElement documents are pivotal for tracking and managing the physical and logical aspects of each inventory component.
They provide crucial insights into the item's capabilities, its location within the facility, and its lifecycle events,
ensuring effective and informed inventory management.

### Example, using the above class examples

```json
{
  "_id": "server001",
  "name": "Main Server",
  "type": "Server",
  "classType": "serverClass",
  "parent_id": "dataCenter1",
  "attributes": {
    "CPU": "Intel Xeon E5",
    "RAM": "64GB",
    "Storage": "4TB",
    "NetworkPorts": 4
  },
  "connector_class": [
        {
          "count": 1,
          "type": "powerSupplyConnectorClass"
        },
        {
        "count": 4,
        "type": "rj45FemaleConnectorClass"
        }
  ],
  "history": [
    {
      "date": "2023-04-10",
      "action": "Installed",
      "description": "Server installed in Data Center 1."
    }
  ]
}
```

```json
{
  "_id": "powerSupply001",
  "name": "Server Power Supply",
  "type": "PowerSupply",
  "classType": "powerSupplyClass",
  "parent_id": "server001",
  "attributes": {
    "Wattage": 800,
    "EfficiencyRating": "Gold"
  },
  "connectors": [
    {
      "count": 1,
      "type": "powerSupplyMaleConnectorClass"
    }
  ],
  "history": [
    {
      "date": "2023-04-12",
      "action": "Added",
      "description": "Power supply added to Main Server."
    }
  ]
}
```

## Cable Item Entity
Cable items are an essential part of the inventory management system, especially in environments like data centers or
technical facilities. They are used to represent various types of cables, such as Ethernet cables, power cords, and others,
each serving as a crucial link between different equipment or locations.
```json
{
  "_id": "String",
  "name": "String",
  "classType": "String",
  "attributes": ["Object"],
  "connector1_id": "String",
  "connector2_id": "String",
  "history": "Array"
}
```

* _id: Unique identifier for the cable.
* name: Descriptive name of the cable.
* classType: Reference to the ClassType defining this cable's category (e.g., Ethernet, power cord).
* attributes: Key-value pairs describing the cable's physical and technical characteristics, such as length, category, or bandwidth.
* connector1_id, connector2_id: The IDs of the connectors at each end of the cable. These reference specific connector items, indicating what the cable is connected to.
* history: Records of significant events in the cable's lifecycle, including installation, movement, or maintenance.

### Examples
Using the two example above we are now modelling the connector for the server and power supply, then we will model the cable .
this is the connector attached to the power supply connector of the ***server001***
```json
{
  "_id": "powerSupplyMaleConnectorOnServer",
  "name": "Male Power Supply Connector on Main Server",
  "type": "Connector",
  "classType": "powerSupplyMaleConnectorClass",
  "parent_id": "server001",
  "attributes": {
    "Type": "Standard Power Connector",
    "VoltageRating": 240
  },
  "history": [
    {
      "date": "2023-04-10",
      "action": "Installed",
      "description": "Male power supply connector installed on Main Server."
    }
  ]
}

```
Tis is the male connector attached to the female connector of the ***powerSupply001***
```json
{
  "_id": "powerConnector001",
  "name": "Power Supply Male Connector",
  "type": "Connector",
  "classType": "powerSupplyMaleConnectorClass",
  "parent_id": "powerSupply001",
  "attributes": {
    "Type": "Standard Power Connector",
    "VoltageRating": 240
  },
  "history": [
    {
      "date": "2023-04-13",
      "action": "Created",
      "description": "Male connector for power supply created."
    }
  ]
}
```

Now we are going to model the cable item that is attache to the two male connector for bring current to the powerSupply001 to the server001
```json lines
{
  "_id": "powerCordCable001",
  "name": "Power Cord Cable for Main Server",
  "type": "Cable",
  "classType": "powerCordCableClass",
  "connector1_id": "powerSupplyMaleConnectorOnServer", // Male connector on the server
  "connector2_id": "powerConnector001", // Corresponding power connector
  "attributes": {
    "Length": 2,
    "PlugType": "Type-F"
  },
  "history": [
    {
      "date": "2023-04-10",
      "action": "Connected",
      "description": "Connected Main Server to Power Supply."
    }
    // Additional historical records...
  ]
}
```
# Example global overview
Below is a graphical representation for the above exmples
```text
Building
│
├─── [item] Floor 1
│    │
│    ├─── [item] Server (Main Server)
│         [Attributes: CPU, RAM, Storage, NetworkPorts]
│         │
│         │ [Connector] Male Power Supply Connector on Main Server] -----│ 
│                                                                        │ 
│                                                                        │ 
│                                                                        │ 
│                                                                        │ 
└─── [item] Floor 2                                                      │ [Power Cord Cable for Main Server]
     │                                                                   │ 
     ├─── [item] Power Supply (Server Power Supply)                      │ 
          │                                                              │ 
          │ [Connector] Power Connector 001------------------------------│ 
         
```