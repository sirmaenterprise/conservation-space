<#assign icon>${(icon)!""}</#assign>
<span id="${id}" class="${name}">
	<span id="cmfLogoLink" onclick="return false;" onmousedown="EMF.config.pageBlockerOFF=true;">
	<#if (icon?length > 0)>
		<img src="${icon}" />
	</#if>
	</span>
</span>