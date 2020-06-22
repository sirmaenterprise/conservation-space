<#import "../results.lib.ftl" as resultJSONLib />
{
"data":
	{
<#if parent??>
	<@resultJSONLib.renderParent parent  />
</#if>
		"items":
		[
		<#list results as row>
			{
			   <@resultJSONLib.nodeProcess row /><#if versionInfo??>, 
			   "version":"${versionInfo[row.nodeRef]}"</#if>	
			}<#if row_has_next>,</#if>
		</#list>
		]
	}
}