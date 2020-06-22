<#escape x as jsonUtils.encodeJSONString(x)>
{
	"result":	[
		<#if result??>
			<#list result as item>
				{
					"crc": "<#if item.first??>${item.first}</#if>",
					"updated": <#if item.second??>${item.second?string}<#else>false</#if>
				}
				<#if item_has_next>,</#if>
			</#list>
		</#if>
	]
} 
</#escape>