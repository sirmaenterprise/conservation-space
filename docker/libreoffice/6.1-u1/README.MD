# Base LibreOffice image

Contains the full LibreOffice suite and a handful of preinstalled fonts.

Can be used as a base image for other functionalities or directly starting it:

`docker run -d docker-reg.sirmaplatform.com libreoffice -accept=socket,host=localhost,port=8100;urp;`

See https://help.libreoffice.org/Common/Starting_the_Software_With_Parameters for reference.