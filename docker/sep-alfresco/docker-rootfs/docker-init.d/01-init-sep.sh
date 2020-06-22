#!/usr/bin/env sh

# needed for security.sso.idpUrl.${ALFRESCO_CONTAINER_IP} config
export ALFRESCO_CONTAINER_IP="$(awk 'END{print $1}' /etc/hosts)"

# CMF-16253
echo >> /opt/tomcat/shared/classes/alfresco-global.properties
extensions="txt doc docx docm dotx dotm ppt pptx pptm ppsx ppsm potx potm ppam sldx sldm vsd xls xlsx xltx xlsm xltm xlam xlsb"
for ext in $extensions; do
	echo "content.transformer.OpenOffice.mimeTypeLimits.$ext.pdf.maxSourceSizeKBytes=15728640" >> /opt/tomcat/shared/classes/alfresco-global.properties
done

replace-env.sh /opt/tomcat/shared/classes/sep-alfresco-global.properties

cat /opt/tomcat/shared/classes/sep-alfresco-global.properties >> /opt/tomcat/shared/classes/alfresco-global.properties