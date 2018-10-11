
<#macro registryEntryPrint entry>
<tr>
	<td><#if entry.sourcePath??>${entry.sourcePath}</#if></td>
	<td><#if entry.targetPath??>${entry.targetPath}</#if></td>
	<td><#if entry.destFileName??>${entry.destFileName}</#if></td>
	<td><#if entry.crc??>${entry.crc}</#if></td>
	<td><#if entry.status??>${entry.status?c}<#else>-1</#if></td>
	<td><#if entry.modifiedBy??>${entry.modifiedBy}</#if></td>
	<td><#if entry.modifiedDate??>${entry.modifiedDate?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
	<td><#if entry.nodeId??>workspace://SpacesStore/${entry.nodeId}</#if></td>
</tr>
</#macro>

<html><body>
<#if error?? && !data??><h2>Error:</h2> ${error}<#else>
	<#if data??><h2>Processed files and folders: ${data?size?c}</h2>
<table border="1">
<tr>
<th>Source path</th>
<th>Target path</th>
<th>File name</th>
<th>Check code</th>
<th>Status</th>
<th>Modified by</th>
<th>Modified date</th>
<th>Node Id</th>
</tr>
<#list data as item>
	<@registryEntryPrint entry=item />
</#list>
</table>	
	</#if>
</#if>
</body></html>