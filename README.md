conservation-space
==================

For more info look at: http://www.conservationspace.org

The source code is distributed under GPLv3 license (http://www.gnu.org/copyleft/gpl.html).

# Building

## Required tools
1. docker
1. docker registry instance

## Steps to build

Run [docker/build.sh](docker/build.sh).
This script will build all artefacts and docker images. It thakes a single argument which is the docker registry address, which is also used in the image tag e.g. 192.168.0.100/vsftpd where 192.168.0.100 should be used as the script's argument.

The UI of the platform is built using jspm. Because jspm downloads packages from github it is very likely that github's rate limit will be hit. To avoid this authentication to github should be provided using the variables `JSPM_USERNAME` which is your github username and `JSPM_TOKEN` which is your password or access token (recommended). If access token is used it should have the `public_repo` permission.

Example:
```bash
JSPM_USERNAME=example JSPM_TOKEN=1234567 ./docker/build.sh 192.168.0.100
```

# Deploying

Install `gulp` and `jspm` globally by running (this requires root or sudo permissions):
```bash
npm install -g gulp jspm
```

Because jspm downloads (a lot) from github.com it is very likely to hit github's rate limit. To avoid this you need to create an API token or log into github. To do so execute `jspm config registries.github` and follow the instructions.

To build all artefacts and docker images run:
```bash
./build.sh
```

# Deployment

The platform is deployed using a docker in swarm mode and a compose file.
Automation of the process is provided using ansible playbooks.
Thy are provided in the `ansible` directory.

## Configure ansible

Inside the ansible directory create a new directory named `inventories`. This is the root directory in which all you environments live. Each sub directory is the name of an environment.
Now create the following structure inside the inventories directory:
```
example - directory which holds all the variables and configurations for the 'example' environment
  - files - directory which hold arbitrary files needed for the deployment
  -- certs - directory which holds the TLS certificate and key required by the proxy service and java keystores containing same certificate and key, also a dhparam.pem file
  - host_vars - directory which holds variables for the individual hosts (machines) in the environment/docker swarm
  - hosts - file which is the ansible inventory file - this file describes all the hosts and groups of hosts which ansible will manage
```

### Inventory file

The inventory file needs to describe three host groups:
1. swarm-managers - hosts that are docker swarm manager nodes
2. swarm-workers - hosts that are docker swarm worker nodes
3. swarm-nodes - the hosts from both the manager and worker groups i.e. all hosts in the swarm regardless of their role

### TLS and Keystores

Inside the example/files/certs folder, place your tls certificate and key and name them nginx.crt and nginx.key. These files can be encoded with ansible vault if you wish.
Import the certificate and key inside two java keystores one named wso2carbon.jks and another client-truststore.jks.
Also generate  a new dhparam.pem file.

### Host Variables

Inside the `host_vars` directory create a sub directory for each host in the swarm. As a name for the directory use the same value you used in the ansible inventory file.
Then for each host directory:

Create a file called 01-ssh.yml
Inside it provide the needed by ansible variables to connect to the host using ssh e.g.
```yaml
ansible_host: 172.17.8.102
ansible_user: vagrant
ansible_ssh_pass: vagrant
```

Create a file 02-docker.yml
This file provides:
1. the address of the docker registry
2. the name of the docker user. This user must exist on the host and will be added to the `docker` group.
3. the name of the network interface the docker should initialize swarm on
4. node labes. each service in the swarm is placed to specific hosts using these labels - see the compose file for all labels

Create a file called 03-sep.yml
This file describes where to find the graphdb license file.
Where the compose file is.
Environment variables for the services.
And which stacks to deploy.

### Running the ansible playbooks

Inside the ansible directory there is convenient script called `run-playbook.sh`.
It takes two arguments:
1. the playbook to run - `provision`, which copies all configuration files, installs docker and initializes the docker swarm, and `deploy`, which deploys the compose file
2. the name of the environment to run against - this is a name of a directory under `ansible/inventories`

The playbooks need to be run in this order i.e. first provision and then deploy.

By default the script expects that something with encrypted using ansible vault. To provide the password for the vault either modify [ansible/ansible-vault-pass.sh](ansible/ansible-vault-pass.sh) or  export the variable `ANSIBLE_VAULT_PASS`.
Another option is to provide the `VAULT_PASS` variable with value `ask`. This will cause ansible to ask for the password each time it needs it.
And finally if you don't have anything encrypted set the `VAULT_PASS` variable to `none`.

## Example

Inside the ansible directory there is a `Vagrantfile` which will create two virtual machines (one master and one slave) which can be used for testing.
The inventory is described in the `inventory/test` directory and can be used as a reference.
Because the `run-playbook.sh` script looks for inventories in a different directory you need to specify where the inventory is using the variable `INVENTORY_BASE_PATH`.

```bash
INVENTORY_BASE_PATH=inventory ./run-playbook.sh provision test
INVENTORY_BASE_PATH=inventory ./run-playbook.sh deploy test
```
