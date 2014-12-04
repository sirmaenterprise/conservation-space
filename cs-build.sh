#!/bin/bash
work_dir=$(readlink -f $(dirname "$0"))
home_dir=$(getent passwd $SUDO_USER | cut -d: -f6)

#install owlim jars
cd ${work_dir}/third-party
mvn install:install-file -Dfile=owlim-se-5.4.jar -DpomFile=owlim-se-5.4.pom
mvn install:install-file -Dfile=owlim-lite-5.3.jar -DpomFile=owlim-lite-5.3.pom

#main parent poms
cd ${work_dir}/poms/sirma
mvn install

cd ${work_dir}/poms/jee6
mvn install

#itt-commons
cd ${work_dir}/itt-commons module
mvn install

#codelist-utils
cd ${work_dir}/codelist-utils
mvn install

#sirma-faces
cd ${work_dir}/sirma-faces
mvn install

#sep-bundle
cd ${work_dir}/sep-bundle
mvn install -P unit-test

#package deployables
cd ${work_dir}/cs
mvn package
