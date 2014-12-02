<#import "../../../common/common.ftl" as common>
<div class="user-menu user-options-menu">
	<span class="user-menu-trigger user-options-menu-trigger">${currentUserName}</span>
	<div id="userMenuOptions" class="user-menu-options" style="display: none;">
		<@common.renderPageFragments submenus/>
	</div>
</div>