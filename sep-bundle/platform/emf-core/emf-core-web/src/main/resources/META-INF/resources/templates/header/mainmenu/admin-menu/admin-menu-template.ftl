<#import "../../../common/common.ftl" as common>
<#assign isSubMenu>${(isSubMenu?c)!'false'}</#assign>
<div id="${id}" class="menu-item dropdown-menu-item ${name}">
	<span class="icon"></span>
	<span class="menu-item-trigger">${label}<b class="caret"></b></span>
	<#if (isSubMenu == "true" && submenus??)>
	<span class="menu-arrow"></span>
	<ul class="submenu dropdown-menu-list pm-menu">
		<@common.renderPageFragments submenus/>
	</ul>
	</#if>
</div>