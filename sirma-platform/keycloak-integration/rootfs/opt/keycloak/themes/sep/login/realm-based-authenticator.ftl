<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle","SEP")}
    <#elseif section = "header">
        ${msg("enterUsernameTitle")}
    <#elseif section = "form">
		<br>
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="fullUsername" class="${properties.kcLabelClass!}">${msg("userNameFieldPlaceholder")}</label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input id="fullUsername" name="fullUsername" type="text" class="${properties.kcInputClass!}" required placeholder="${msg("userNameFieldPlaceholder")}" />
                </div>
            </div>
			
			<p>${msg("enterUsernameDescrCommon")}</p>
			
			<div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <div class="${properties.kcFormButtonsWrapperClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="Continue"/>
                    </div>
                </div>
            </div>
		</form>
    </#if>
</@layout.registrationLayout>