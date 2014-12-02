<#import "../common/common.ftl" as common>
<#list menus as menu>
<@common.importDependencies menu/>
<#assign label>${(menu.label)!""}</#assign>
<#assign id>${(menu.id)!""}</#assign>
<#assign prefix>${(menu.prefix)!""}</#assign>
<#assign itemId = id + "_" + prefix + "_" + menu.name>
<#assign itemMenuId = id + "_" + prefix + "_menu_" + menu.name>
<#assign icon>${(menu.icon)!""}</#assign>
<#assign isSubMenu>${(menu.isSubMenu?c)!'false'}</#assign>
<#assign menuItem>${(menu.menuItem)!""}</#assign>
<#assign menuItemSpan>${(menu.menuItemSpan)!""}</#assign>
<#assign menuItemTrigger>${(menu.menuItemTrigger)!""}</#assign>
<#assign menuItemList>${(menu.menuItemList)!""}</#assign>
<#assign href>${(menu.href)!""}</#assign>
<div id="${itemMenuId}" class="menu-item ${menuItem}">
	<span class="icon sprite ${menuItemSpan}"></span>
	<#if (icon?length > 0)>
		<img src="${icon}" />
	</#if>
	<#if (href?length > 0)>
		<a href="${href}" />
	</#if>
	<span class="menu-item-trigger ${menuItemTrigger}"><span>${label}</span></span>
		<#if (isSubMenu == "true" && menu.submenus??)>
		<#assign subMenus = menu.submenus>
		<span class="menu-arrow"></span>
		<ul class="submenu ${menuItemList}">
			<#list subMenus as subMenu>
			<#assign subMenuHref>${(subMenu.href)!""}</#assign>
			<#assign subMenuIcon>${(subMenu.icon)!""}</#assign>
			<#assign subMenuClass>${(subMenu.cl)!""}</#assign>
			<li class="submenu-item">
				<a href="${subMenuHref}" id="${itemId}-Link" value="" class="icon-action-link glyphicon ${subMenuClass}">
					<#if (subMenuIcon?length > 0)>
						<img src="${subMenuIcon}" />
					</#if>
					<span class="menu-link-value" style="margin-left: 6px;">${(subMenu.label)!}</span>
				</a>
			</li>
			</#list>
		</ul>
		</#if>
</div>
 </#list>