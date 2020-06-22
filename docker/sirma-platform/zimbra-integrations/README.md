# Zimbra integration files 
Repo contains needed files for sep to integrate with zimbra like skins, zimlets and pre-authenticators.

## Installation/Deployment guides: 
* Skin deployment guide can be found here: https://ses.sirmaplatform.com/#/idoc/emf:c42afc2a-2470-4350-a665-b6845ee29dfd?mode=preview#8ec3d635-fc9c-4847-de92-0c9b8334b159
* Zimlet deployment guide can be found here: https://ses.sirmaplatform.com/#/idoc/emf:c42afc2a-2470-4350-a665-b6845ee29dfd?mode=preview#b53d7960-f57e-4d74-b1dd-ff740b612ca3

# Vagrant VM
The repo supports running Zimbra and its SEP integrations within a virtual machine. The used OS is CentOS 7 but it supports Ubuntu 16 as well.
Exposed ports:
* 8443 - User dashboard
* 7071 - Admin panel

## Refreshing shared folders during development
If the vagrant box is CentOS then shared folders need to be synchronized by executing `vagrant rsync`. Read about rsync [here](https://www.vagrantup.com/docs/synced-folders/rsync.html)

# Ansible Provisioning
The vagrant machine is provisioned via [Ansible](https://www.ansible.com/) but the playbooks are made to be easily used on bare metal with minor modifications.
There is an external variables file [vagrant-vars.yml](./provision-vagrant/vagrant-vars.yml) which sets the vagrant specific options for the playbook.

The [playbook](./provision/playbook.yml) is the main entry point of the Zimbra provisioning and allows setting some global variables before running it:
* Hostname
* Domain name
* Admin password
See the file for the rest.

The playbooks support:
* Unbuntu 14 & 16
* CentOS 6 & 7

## Changing the default password
By default the administration password is `admin123`. To change it:
* Set a new value in playbook.yml
* Or use ansible-vault to encrypt new password in [passwords.vault](./provision-vagrant/passwords.vault). 
    * Format should be key: value
    * Read about Vault [here](https://www.vaultproject.io/)

## Securing Zimbra with CA certificates
The provisioning contains a variable `certificates_path` which should be a directory containing:
* commercial.key - the private key
* commercial.crt - root certificate, could be a chain
* commercial_ca.crt - intermediate certificate

# Accessing Zimbra
To be able to open Zimbra running within the VM, there are several options:
* Refer to the machine's IP and port or to the mapped ones on the host - this works but Zimbra will route to the configured <host>.<domain>
* Configure a DNS record to correctly route <host>.<domain> to the machine's IP & port - good for local development
* Or DNS record to a reverse proxy that does the above - good for production environment