# Base image for OpenLDAP on 🐳

The image includes:

* openldap - OpenLDAP itself with its slapd daemon and related utilities
* rsyslog - Used to intercept & store OpenLDAP logs
* logrotate - Used to rotate OpenLDAP logs
* openldap-clients - helpful utilities to query & manage OpenLDAP


While the image includes all the required libraries, its main purpose is to be extended with custom models.

## Provisioning

Provisioning scripts make sure rsyslog daemon is running and OpenLDAP is initialized before running the slapd daemon.

See `/docker-init.d/` directory for more information.

## Volumes

* `/var/lib/openldap` - the main directory where OpenLDAP is reading & storing data + configurations
  * `/var/lib/openldap/openldap-data/` - here is the actual data base 
  * `/var/lib/openldap/slapd.d/` - this folder contains the slapd daemon schema configuration
* /var/log/openldap - all logs from OpenLDAP

Other directories

* `/etc/openldap` - base schemas & configurations
* `/var/run/openldap` - directory where OpenLDAP will store its .pid file 

## Health check

The image contains a basic health check script that connects to the slapd daemon making sure the service is responding.

## Running

The OpenLDAP daemon should be started by executing the `start-openldap.sh`
Make sure the script is executed as the `dockeru` user and the schema configuration is present in `/var/lib/openldap/slapd.d/`

### Configurable environment

The slapd daemon's logging level can be configured by specifying the ` LDAP_DEBUG_LEVEL` environment variable. 
The default is 256, see the official documentation for different levels.

## Extending

1. Overwrite /docker-init.d/10-provision-schema.sh
2. Place schema config in /var/lib/openldap/slap.d/
3. Store data in /var/lib/openldap/openldap-data/
4. Make sure /var/lib/openldap/ has proper permissions for dockeru:dockeru

## Example

```bash
docker run -d --name openldap -p 10389:10389 docker-reg.sirmaplatform.com/openldap:<version>
```

### Docker compose

```yaml

---
version: '3'

services:
  ldap:
    image: docker-reg.sirmaplatform.com/openldap:<version>
    volumes:
      - ldap_data:/var/lib/openldap
      - ldap_logs:/var/log/openldap
    ports:
      - 10389:10389

volumes:
  ldap_data:
  ldap_logs:

```