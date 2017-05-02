<#import "../../../../org/alfresco/repository/generic-paged-results.lib.ftl" as genericPaging />
<#macro renderParent node indent="   ">
	<#escape x as jsonUtils.encodeJSONString(x)>
	${indent}"parent":
	${indent}{
		${indent}"type": "${node.typeShort}",
		${indent}"isContainer": ${node.isContainer?string},
		${indent}"site": "${node.getSiteShortName()!""}",
		${indent}"cm:name": "${node.properties.name!""}",
		${indent}"cm:title": "${node.properties.title!""}",
		${indent}"cm:description": "${node.properties.description!""}",
		<#if node.properties.modified??>${indent}"cm:modified": "${xmldate(node.properties.modified)}",</#if>
		<#if node.properties.modifier??>${indent}"cm:modifier": "${node.properties.modifier}",</#if>
		${indent}"displayPath": "${node.displayPath!""}",
		${indent}"nodeRef": "${node.nodeRef}"
	${indent}},
	</#escape>
</#macro>
<#macro nodeProcess row>
<#escape x as jsonUtils.encodeJSONString(x)>
"type": "${row.typeShort}",
"cm:name": "${row.properties.name!""}",
<#if row.siteShortName??>"site": "${row.siteShortName}",</#if>
"displayPath": "${row.displayPath!""}",
"nodeRef": "${row.nodeRef}",
"properties": <@propertiesJSON node=row properties=versionProperties />
		
</#escape>
</#macro>
<#macro resultJSON results>
	<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
<#if parent??>
	<@renderParent parent />
</#if>
		"items":
		[
		<#list results as row>
			{
			    <@nodeProcess row />	
			}<#if row_has_next>,</#if>
		</#list>
		]
	}
	<@genericPaging.pagingJSON pagingVar="paging" />
}
	</#escape>
</#macro>
<#-- Renders a map of properties -->
<#macro propertiesJSON node properties>
{
<#list properties?keys as key>
		"${key}":<#if properties[key]??><#assign val=properties[key]><#if val?is_boolean == true>${val?string}<#elseif val?is_date == true>"${xmldate(val)}"<#elseif val?is_number == true> ${val?c}<#elseif val?is_sequence>[<#list val as element>"${element?string}"<#if (element_has_next)>,</#if></#list>]<#else>"${shortQName(val?string)}"</#if><#else>null</#if><#if (key_has_next)>,</#if>
</#list>
<#if (properties?size>0)>,</#if>"nodeRef":"${node.nodeRef}"
}
</#macro>

