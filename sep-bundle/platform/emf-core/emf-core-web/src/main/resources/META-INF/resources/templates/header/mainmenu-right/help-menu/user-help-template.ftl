<div class="user-menu user-help-menu">
	<a class="user-menu-trigger dontBlockUI user-help-menu-trigger glyphicons circle_question_mark" title="${name}" href="javascript: void(0);"></a>
	<div id="userHelpMenuOptions" class="user-menu-options" style="display: none;">
		<#list submenus as menu>
			<span class="user-menu-option">
				${(menu)!""}
			</span>
		 </#list>
	</div>
</div>
