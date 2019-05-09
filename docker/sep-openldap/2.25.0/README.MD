# Image for SEP's OpenLDAP on 🐳

Extended OpenLDAP image for serving SEP's identity provider needs.

## Provisioning

OpenLDAP can be provisioned by using specific LDIF (LDAP Data Interchange Format) provided to the slapd daemon.

Any container of this image will look into `/var/lib/openldap/slapd.d/` directory and if it's empty it will perform 
the schema and database provisioning from `/etc/openldap/slapd.ldif` only once.

See `/docker-init.d/10-provision-schema.sh` for more information about the process.

## Schemas

The image will provision the following schemas:

**Base schemas**

* core - the bare minimum to actually run slapd daemon
* cosine - Cosine and Internet X.500 model
* inetorgperson - needed for proper user and group management

**External schemas**

Required for proper claim management by WSO2IDP
* wso2person
* scimperson
* identityperson

## Data bases

### SEP MDB

The whole reason for this extended image is to provide and configure an OpenLDAP MDB (Memory-Mapped Database).
This is achieved by specifying custom MDB entry in the provisioning `/etc/openldap/slapd.ldif`

See the official documentation for examples.

### Internal

* frontend - an internal database from which all others inherit configurations
* config - an internal database containing all schemas and database configurations. 
Only the root container user can access and manage this database on the OpenLDAP's system endpoint `ldapi://`

## Access

* External - services should use the `ldap://<ip or dns name>:10389` exposed endpoint 
* Internal - usable only within the container, the system `ldapi://` endpoint is configured for management of the OpenLDAP service.

## Environment variables

* `SEP_MDB_SUFFIX` - this is the suffix of the SEP database. Default is **dc=SIRMAITT,dc=BG**
* `SEP_MDB_ROOT_DN` - this is the administration DN of the SEP database. Default value is **cn=Manager,dc=SIRMAITT,dc=BG**. 
                      External services wishing to manage SEP DN should authenticate with this DN
* `SEP_MDB_DEFAULT_PASS` - the default password for the administration DN
* `PASSWORD_HASH_METHOD` - **SSHA** is the default password hashing method. For more info on supported algorithms: see 
                           https://www.openldap.org/doc/admin24/security.html

All of the variables are used only in the initial provisioning.

## Root password provisioning

There are two ways of providing a root password during initial provisioning.

* By specifying the `SEP_MDB_DEFAULT_PASS` environment variable - This is not recommended for production.
* By using Docker secrets functionality and mounting a secret named `sep-mdb-password`. 

The latter will result in exposing it only for root at `/run/secrets/sep-mdb-password` in the container. 
The provisioning will pickup the decrypted secret and use it in the SEP MDB definition. 
This is more production ready.

## Examples

### Running OpenLDAP image

```bash
docker run -d --name openldap -p 10389:10389 docker-reg.sirmaplatform.com/sep-openldap:<version>
```

## Docker compose

```yaml

---
version: '3'

services:
  ldap:
    image: docker-reg.sirmaplatform.com/sep-openldap:<version>
    volumes:
      - ldap_data:/var/lib/openldap
      - ldap_logs:/var/log/openldap
    ports:
      - 10389:10389
    environment:
      - SEP_MDB_DEFAULT_PASS=password

volumes:
  ldap_data:
  ldap_logs:

```

### Useful requests

Given you have root access to the container:

Dumping current configuration
```bash
slapcat -F /var/lib/openldap/slapd.d/ -b "cn=config"
```

Querying current configuration
```bash
ldapsearch -Y EXTERNAL -Q -H ldapi:// -b "cn=config" -LLL
```
