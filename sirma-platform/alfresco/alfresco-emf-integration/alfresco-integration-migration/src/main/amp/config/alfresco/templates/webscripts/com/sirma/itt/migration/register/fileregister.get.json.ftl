<#-- Renders a paging object. -->
<#macro pagingJSON paging>
	"paging": {
      		"totalItems": ${paging.totalItems?c},
      		"pageSize": <#if !paging.pageSize?? || paging.pageSize lt 0>${paging.totalItems?c}<#else>${paging.pageSize?c}</#if>,
      		"skipCount": ${paging.skipCount?c}
	 }
</#macro>

<#macro registryEntryPrint entry>
{
	"source": "<#if entry.sourcePath??>${entry.sourcePath?js_string?replace("\\", "\\\\")}</#if>",
	"target": "<#if entry.targetPath??>${entry.targetPath}</#if>",
	"fileName": "<#if entry.fileName??>${entry.fileName}</#if>",
	"destFileName": "<#if entry.destFileName??>${entry.destFileName}</#if>",
	"crc": "<#if entry.crc??>${entry.crc}</#if>",
	"status": <#if entry.status??>${entry.status?c}<#else>-1</#if>,
	"modifiedBy": "<#if entry.modifiedBy??>${entry.modifiedBy}</#if>",
	"modifiedDate": "<#if entry.modifiedDate??>${entry.modifiedDate?string("yyyy-MM-dd HH:mm:ss")}</#if>",
	"nodeId": "<#if entry.nodeId??>workspace://SpacesStore/${entry.nodeId}</#if>"
}
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":	[
		<#if data??>
			<#list data as item>
				<@registryEntryPrint entry=item />
				<#if item_has_next>,</#if>
			</#list>
		</#if>
	]<#if paging??>,
		<@pagingJSON paging=paging />
	</#if> 
} 
</#escape>