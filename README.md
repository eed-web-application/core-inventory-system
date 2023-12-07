![logo](./logos/SLAC-lab-hires.png)

# Code Inventory System (CIS)
<!-- TOC -->
* [Code Inventory System (CIS)](#code-inventory-system-cis)
  * [SLAC National Accelerator Laboratory](#slac-national-accelerator-laboratory)
  * [License](#license)
  * [Overview](#overview)
  * [Data Structure](#data-structure)
  * [Configuration](#configuration)
  * [Demo](#demo)
    * [Starting the Demo with Docker-Compose Files](#starting-the-demo-with-docker-compose-files)
    * [Demo Data](#demo-data)
    * [Rest API Documentation](#rest-api-documentation)
      * [Demo Mode Features](#demo-mode-features)
    * [Demo LDAP configuration](#demo-ldap-configuration)
<!-- TOC -->

## SLAC National Accelerator Laboratory
The SLAC National Accelerator Laboratory is operated by Stanford University for the US Departement of Energy.  
[DOE/Stanford Contract](https://legal.slac.stanford.edu/sites/default/files/Conformed%20Prime%20Contract%20DE-AC02-76SF00515%20as%20of%202022.10.01.pdf)

## License
Copyright (c) 2017-2023, The Board of Trustees of the Leland Stanford Junior University, through SLAC National Accelerator Laboratory... the complete license is [here](LICENSE.md)

## Overview
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

## Data Structure
a detailed data structure can be viewed [here](doc/DataStructureDetails.md)

## Configuration

below is the standard configuration of the CIS backend application
```yaml
logging:
  level:
    edu.stanford.slac.code_inventory_system: ${CIS_LOG_LEVEL:DEBUG}

server:
  tomcat:
    mbeanregistry:
      enabled: true

spring:
  application:
    name: 'CIS'
  cache:
    type: hazelcast
  ldap:
    urls: ${CIS_LDAP_URI:ldap://localhost:8389}
    base: ${CIS_LDAP_BASE:dc=sdf,dc=slac,dc=stanford,dc=edu}
  data:
    mongodb:
      uri: ${CIS_MONGODB_URI:mongodb://cis:cis@localhost:27017/cis?authSource=cis}
  servlet:
    multipart:
      enabled: true
      file-size-threshold: 1MB
      max-file-size: ${CIS_MAX_POST_SIZE:100MB}
      max-request-size: ${CIS_MAX_POST_SIZE:100MB}

edu:
  stanford:
    slac:
      ad:
        eed:
          baselib:
            app-token-prefix: ${spring.application.name}
            app-token-jwt-key: ${CIS_APP_TOKEN_JWT:token-header-key}
            user-header-name: ${CIS_AUTH_HEADER:x-vouch-idp-accesstoken}
            oauth-server-discover: ${CIS_OIDC_CONFIGURATION_ENDPOINT:https://dex.slac.stanford.edu/.well-known/openid-configuration}
            root-user-list: ${CIS_ROOT_USERS}
            root-authentication-token-list-json: ${CIS_ROOT_AUTHENTICATION_TOKEN_JSON:[]}
          mongodb:
            db_admin_uri: ${CIS_ADMIN_MONGODB_URI:mongodb://admin:admin@localhost:27017/?authSource=admin}

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: ${spring.application.name}

# swagger-ui custom path
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    path: /api-docs

mongock:
  migration-scan-package:
    - edu.stanford.slac.code_inventory_system.migration
  throw-exception-if-cannot-obtain-lock: true #Default true
  track-ignored: false #Default true
  transaction-enabled: false
  runner-type: initializingbean
  enabled: true #Default true
```

## Demo

### Starting the Demo with Docker-Compose Files

To initiate the demo, use the provided docker-compose files. The `docker-compose.yml` is the default file 
for starting the necessary services for the CIS backend to conduct unit and integration tests. Alongside, 
the `docker-compose-app.yml` is used to enable the CIS backend in demo mode.

```shell
docker compose -f docker-compose.yml -f docker-compose-app.yml up
```
in case of application updates the docker image need to be rebuilt so in this case this command can be used:
```shell
docker compose -f docker-compose.yml -f docker-compose-app.yml up --build backend
```

### Demo Data
A demo database is established at startup according to the rules specified in the following [file](src/main/resources/demo-structure.yml):


### Rest API Documentation
The backend seamlessly provides access to the OpenAPI 3.0 specification for the REST API at the endpoint '/api-docs'. This endpoint serves the OpenAPI 3.0 documentation in JSON format.

To access the documentation when the backend is operating in demo mode, use the following shell command:
```shell #in case the backend is run in demo mode
curl http://localhost:8080/api-docs
```



#### Demo Mode Features
In demo mode, the system employs an embedded LDAP server to mimic users and groups. Moreover, 
this mode introduces a feature for mock users, incorporating a suite of REST APIs. These APIs 
facilitate the retrieval of mock user data, which is essential for accessing the full range of other A
PIs in the system.

### Demo LDAP configuration

Below is the default ldap configuration, the content configure three user and two groups:
* as users: *users1*, *user2* and *user3*.
* as groups: 
  * group-1: user1
  * group-2: user1, user2

User3 doesn't belong ot any groups.

```text
dn: dc=sdf,dc=slac,dc=stanford,dc=edu
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: kpn

# Create the Groups organizational unit
dn: ou=Group,dc=sdf,dc=slac,dc=stanford,dc=edu
objectClass: top
objectClass: organizationalUnit
ou: Group

# Create the People organizational unit
dn: ou=People,dc=sdf,dc=slac,dc=stanford,dc=edu
objectClass: top
objectClass: organizationalUnit
ou: People


# Create People
dn: uid=user1,ou=People, dc=sdf,dc=slac,dc=stanford,dc=edu
uid: user1
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
gecos: Name1 Surname1
cn: Surname1
sn: Surname1
mail: user1@slac.stanford.edu

dn: uid=user2,ou=People, dc=sdf,dc=slac,dc=stanford,dc=edu
uid: user2
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
gecos: Name2 Surname2
cn: Surname2
sn: Surname2
mail: user2@slac.stanford.edu

dn: uid=user3,ou=People, dc=sdf,dc=slac,dc=stanford,dc=edu
uid: user3
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
gecos: Name3 Surname3
cn: Surname3
sn: Surname3
mail: user3@slac.stanford.edu

# Create Group
dn: cn=group-1,ou=Group, dc=sdf,dc=slac,dc=stanford,dc=edu
cn: group-1
objectClass: posixGroup
objectClass: top
objectClass: importedObject
gidNumber: 3591
memberUid: user1@slac.stanford.edu

dn: cn=group-2,ou=Group, dc=sdf,dc=slac,dc=stanford,dc=edu
cn: group-2
objectClass: posixGroup
objectClass: top
objectClass: importedObject
gidNumber: 3592
memberUid: user1@slac.stanford.edu
memberUid: user2@slac.stanford.edu
```

For get the mock users the /v1/mock/users-auth api should be called:
```shell # example for retrieve mock users
curl http://localhost:8080/v1/mock/users-auth
```

and something like that is returned:
```json
{
  "errorCode":0,
  "payload":
  {
    "Name1 Surname1":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InVzZXIxQHNsYWMuc3RhbmZvcmQuZWR1IiwiaWF0IjoxNzAwNzAzMTIzLCJleHAiOjE3MDA3MDY3MjN9.LZgUleeSkzL3-m_FBY7KAsXGrg-OBco0Ltzwe5Cal68",
    "Name3 Surname3":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InVzZXIzQHNsYWMuc3RhbmZvcmQuZWR1IiwiaWF0IjoxNzAwNzAzMTIzLCJleHAiOjE3MDA3MDY3MjN9.A2upZMIN7le31RzNWwlKbk0J7jTpavcH24odJ8j97Jo",
    "Name2 Surname2":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InVzZXIyQHNsYWMuc3RhbmZvcmQuZWR1IiwiaWF0IjoxNzAwNzAzMTIzLCJleHAiOjE3MDA3MDY3MjN9.lUKIxch_ifr1pzNQXH22mRr_Ak-qQJJdXJEcvfg-yms"
  }
}
```
Each key of the payload content is a different user, have been mocked three different user 
belonging to different groups. The content of each user is the JWT that need to be used too authentication the
call to the rest API. The content of the JWT need to be put into the http header in the field indicated by 
the configuration key <span style="color:orange">**user-header-name**</span>, that for the default 
configuration is: <span style="color:orange">**x-vouch-idp-accesstoken**</span>