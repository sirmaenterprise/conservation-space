<#assign icon>${(icon)!""}</#assign>
<span id="${id}" class="additional-logo ${name}">
	<#if (icon?length > 0)>
		<img src="${icon}" />
	</#if>
</span>