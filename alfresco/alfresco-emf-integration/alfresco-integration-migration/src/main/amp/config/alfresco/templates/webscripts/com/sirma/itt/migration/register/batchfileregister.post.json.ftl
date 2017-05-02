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
		<#if result??>
			<#list result as item>
				<@registryEntryPrint entry=item />
				<#if item_has_next>,</#if>
			</#list>
		</#if>
	]
} 
</#escape>