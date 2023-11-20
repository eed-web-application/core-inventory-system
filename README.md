# code-inventory-system (CIS)
CIS stands as a new inventory management system designed specifically for managing large and complex equipment. 
It breaks away from traditional inventory management systems by not only encompassing hardware, connectors, and 
cables but also enabling users to create new classes for defining new inventory items on the fly. This flexibility
caters to the ever-evolving demands of managing sophisticated equipment.

CIS extends its purview beyond mere hardware inventory to encompass the physical locations where this equipment 
resides. This comprehensive approach ensures seamless tracking of equipment movement and placement, providing a 
holistic view of the physical infrastructure.

Furthermore, CIS diligently maintains a detailed history of each inventory item throughout its lifecycle, 
providing valuable insights into its usage patterns, maintenance records, and overall lifespan. This 
historical data proves instrumental in making informed decisions regarding equipment upgrades, 
replacement cycles, and potential areas of improvement.

# Table of Contents
1. [Overview](#Overview)
2. [ClassType Entity](#classtype-entity)
3. [InventoryItem Entity](#inventoryitem-entity)
4. [CableItem Entity](#cableitem-entity)
5. [Example global overview](#example-global-overview)

## Overview
This document provides detailed information about the InventoryItem and ClassType entities 
used in the Inventory Management System designed for tracking and managing various items, 
including equipment and locations within a nuclear accelerator facility. 

### ClassType Entity
The ClassType entity defines the classes of items, cables, and connectors in the system, specifying their attributes, 
mandatory requirements, and connectivity rules.
```json lines
{
  "_id": "String",
  "name": "String",
  "classType": "String",
  "attributes": "Object",
  "connectableClasses": "Array"
}
```
Fields
* _id: Unique identifier for the class type.
* name: Name of the class (e.g., 'ServerClass', 'EthernetCable').
* classType: Nature of the class (Item, Cable, or Connector).
* attributes: Object defining attributes specific to the class, including mandatory/optional status, data type, and unit.
* connectableClasses: Array of class IDs that can be connected to this class (relevant for items and connectors).

#### Usage
The ClassType documents are essential for defining the structure and rules for various items in the inventory. 
They provide a template for creating and validating InventoryItem documents, ensuring consistency and integrity 
in inventory management.

#### Example
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
  },
  "connectableClasses": ["powerSupplyConnectorClass", "ethernetCableClass"]
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
  },
  "connectableClasses": ["powerCordCableClass"]
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
  },
  "connectableClasses": ["ethernetCableClass"]
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
  },
  "connectableClasses": ["ethernetCableClass"]
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
  },
  "connectableClasses": ["powerCordCableClass", "powerSupplyClass"]
}
```
```json
{
  "_id": "powerSupplyMaleConnectorClass",
  "name": "PowerSupplyConnector",
  "classType": "Connector",
  "attributes": {
    "VoltageRating": {"mandatory": false, "type": "number", "unit": "Volts"}
  },
  "connectableClasses": ["powerCordCableClass", "powerSupplyClass"]
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
  },
  "connectableClasses": ["rj45ConnectorClass", "serverClass"]
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
  },
  "connectableClasses": ["powerSupplyConnectorClass", "powerSupplyClass"]
}
```

### InventoryItem Entity
The InventoryItem entity represents individual items in the inventory. This includes all
physical items and locations, such as servers, racks, and connectors.
```json lines
{
  "_id": String,
  "name": String,
  "type": String,
  "classType": String,
  "parent_id": String,
  "attributes": Object,
  "connector_class": Array,
  "history": Array
}
```
Fields
* _id: Unique identifier for the item.
* name: Human-readable name of the item.
* type: Type of the item (e.g., 'Server', 'Connector').
* parent_id: ID of the parent item, indicating the item's location or grouping.
* attributes: Key-value pairs representing item-specific attributes.
* connector_class: An array of objects, each specifying a type of connector class the item supports and the count of such connectors. This helps in identifying the connectivity capabilities of the item.
* history: Array of historical records, including actions like 'Installed', 'Moved', and their corresponding details.

#### Usage
InventoryItem documents are pivotal for tracking and managing the physical and logical aspects of each inventory component. 
They provide crucial insights into the item's capabilities, its location within the facility, and its lifecycle events, 
ensuring effective and informed inventory management.

#### Example, using the above class examples

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

### CableItem Entity
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

#### Examples
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
## Example global overview
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