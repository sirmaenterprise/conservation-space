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
"cm:title": "${row.properties.title!""}",
"cm:description": "${row.properties.description!""}",
<#if row.properties.modified??>"cm:modified": "${xmldate(row.properties.modified)}",</#if>
<#if row.properties.modifier??>"cm:modifier": "${row.properties.modifier}",</#if>
<#if row.siteShortName??>"site": "${row.siteShortName}",</#if>
"displayPath": "${row.displayPath!""}",
"nodeRef": "${row.nodeRef}"
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