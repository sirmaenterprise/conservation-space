<#import "../../../common/common.ftl" as common>
<#assign isSubMenu>${(isSubMenu?c)!'false'}</#assign>
<div id="${id}" class="menu-item dropdown-menu-item ${name}">
	<span class="icon"></span>
	<span class="menu-item-trigger">${label}<b class="caret"></b></span>
	<#if (isSubMenu == "true" && submenus??)>
	<ul class="submenu dropdown-menu-list">
		<@common.renderPageFragments submenus/>
	</ul>
	</#if>
</div>