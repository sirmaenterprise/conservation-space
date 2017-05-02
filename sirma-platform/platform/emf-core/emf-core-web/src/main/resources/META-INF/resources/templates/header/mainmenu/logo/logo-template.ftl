<#assign icon>${(icon)!""}</#assign>
<span id="${id}" class="${name}">
	<a id="cmfLogoLink" href="${href}">
	<#if (icon?length > 0)>
		<img src="${icon}" />
	</#if>
	</a>
</span>