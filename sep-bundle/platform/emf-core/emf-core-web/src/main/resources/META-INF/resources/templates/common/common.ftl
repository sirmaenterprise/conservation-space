<#macro importDependencies item>
	<#if (item?? && item.css??)>
      <#list item.css as cssFile>
         <link rel="stylesheet" type="text/css" href="${cssFile}"/>
      </#list>
	</#if>
	<#if (item?? && item.js??)>
      <#list item.js as jsFile>
      	 <script type="text/javascript" src="${jsFile}"></script>
      </#list>
	</#if>
</#macro>

<#macro renderPageFragments pageFragments>
	<#list pageFragments as pageFragment>
		${(pageFragment)!""}
	 </#list>
 </#macro>