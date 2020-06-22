<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayLoginBody=false displayMessage=false displayWide=(realm.password && social.providers??); section>
	<#if section = "form">
	<div class="login__body">
		<div class="login__group">
			<h2>${msg("contextSwitchTitle",(realm.displayName!''))}</h2>
			
			<p>${msg("contextSwitchDescription",(realm.displayName!''))}</p>
			
			<a href="#" class="${properties.kcButtonClass!} ${properties.btnOutline!} ${properties.kcButtonBlockClass!} login__btn">${msg("contextBackBtn")}</a>
			<i class="fa fa-angle-up"></i>
		  </div>
		  
		  <div class="login__group">
			<h2>${msg("loginSwitchCompanyTitle")}</h2>
			
			<p>${msg("loginSwitchCompanyDescr")}</p>
			
			<a href="#" class="${properties.kcButtonClass!} ${properties.btnOutline!} ${properties.kcButtonBlockClass!} login__btn">${msg("loginSwitchCompanyBtn")}</a>
			<i class="fa fa-angle-up"></i>
		  </div>
	</div>
	
	<div class="login__forms">
		<div class="form form--simple">
			<form onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
				<div class="form__head">
					<h2>${msg("enterUsernameTitle")}</h2>
				</div>
				
				<div class="form__body">
					<div class="${properties.kcFormGroupClass!}">					
						<label for="fullUsername" class="hidden">${msg("userNameFieldPlaceholder")}</label>

						<input type="text" class="field" name="fullUsername" id="fullUsername" placeholder="${msg("userNameFieldPlaceholder")}">
						
						<p>${msg("enterUsernameDescr")}</p>
					</div>
				</div>
				
				<div class="form__actions">
					<input type="submit" value="${msg("continueBtn")}" class="${properties.kcButtonClass} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}">
				</div>
			</form>
		</div>
		
		<div class="form form--simple visible">
			<div id="kc-form" <#if realm.password && social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
			  <div id="kc-form-wrapper" <#if realm.password && social.providers??>class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}"</#if>>
				<#if realm.password>
					<form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
						<div class="form__head">
							<h2>${msg("loginTitle",(realm.displayName!''))}</h2>
						</div>
						
						<#if message?has_content>
							  <div class="alert alert-${message.type}">
								  <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
								  <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
								  <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
								  <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
								  <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
							  </div>
						  </#if>
						
						<div class="form__body">
							<div class="${properties.kcFormGroupClass!} InputAddOn">
								<label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>

								<#if usernameEditDisabled??>
									<input tabindex="1" id="username" class="${properties.kcInputClass!} InputAddOn-field" name="username" value="${(login.username!'')}" type="text" disabled placeholder="${msg("userNameFieldPlaceholder")}" />
								<#else>
									<input tabindex="1" id="username" class="${properties.kcInputClass!} InputAddOn-field" name="username" value="${(login.username!'')}"  type="text" autofocus autocomplete="off" placeholder="${msg("userNameFieldPlaceholder")}" />
								</#if>
								<span class="InputAddOn-item">@${realm.name}</span>
							</div>

							<div class="${properties.kcFormGroupClass!}">
								<label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
								<input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="off" placeholder="${msg("passwordFieldPlaceholder")}" />
							</div>

							<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!} loginOptions">
								<div id="kc-form-options">
									<#if realm.rememberMe && !usernameEditDisabled??>
										<div class="checkbox">                                
											<#if login.rememberMe??>
												<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked>
											<#else>
												<input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox">
											</#if>
											<label for="rememberMe">${msg("rememberMe")}</label>
										</div>
									</#if>
									</div>
									<div class="${properties.kcFormOptionsWrapperClass!}">
										<#if realm.resetPasswordAllowed>
											<span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
										</#if>
									</div>
							  </div>
						</div>

					    <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
						  <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
					    </div>
					</form>
				</#if>
				</div>
				<#if realm.password && social.providers??>
					<div id="kc-social-providers" class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}">
						<ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 4>${properties.kcFormSocialAccountDoubleListClass!}</#if>">
							<#list social.providers as p>
								<li class="${properties.kcFormSocialAccountListLinkClass!}"><a href="${p.loginUrl}" id="zocial-${p.alias}" class="zocial ${p.providerId}"> <span>${p.displayName}</span></a></li>
							</#list>
						</ul>
					</div>
				</#if>
			  </div>
		</div>
	</div>
    
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !usernameEditDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
