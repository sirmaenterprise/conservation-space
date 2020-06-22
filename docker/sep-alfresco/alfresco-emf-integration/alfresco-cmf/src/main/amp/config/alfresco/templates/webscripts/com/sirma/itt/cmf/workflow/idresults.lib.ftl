<#import "../../../../../org/alfresco/repository/generic-paged-results.lib.ftl" as genericPaging />
<#macro renderParent node indent="   ">
	<#escape x as jsonUtils.encodeJSONString(x)>
	${indent}"parent":
	${indent}{
		${indent}"site": "${node.getSiteShortName()!""}",
		${indent}"nodeRef": "${node.nodeRef}"
	${indent}},
	</#escape>
</#macro>
<#macro nodeProcess row>
<#escape x as jsonUtils.encodeJSONString(x)>
<#if row.siteShortName??>"site": "${row.siteShortName}",</#if>
"nodeRef": "${row.nodeRef}",
<#if row.properties['bpm:workflowInstanceId']??>"workflowInstance": "${row.properties['bpm:workflowInstanceId']}",</#if>
"id": "${row.name}"
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