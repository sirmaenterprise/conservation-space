<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<div id="loginTable1" class="center-content">
    <%
        loginFailed = CharacterEncoder.getSafeText(request.getParameter("loginFailed"));
        if (loginFailed != null) {

    %>
            <div class="alert alert-error">
                <fmt:message key='<%=CharacterEncoder.getSafeText(request.getParameter
                ("errorMessage"))%>'/>
            </div>
    <% } %>

    <% if (CharacterEncoder.getSafeText(request.getParameter("username")) == null || "".equals
    (CharacterEncoder.getSafeText(request.getParameter("username")).trim())) { %>

        <!-- Username -->
        <div class="bottom-space">
			<input class="input-xlarge" placeholder="<fmt:message key='username'/>" type="text" id='username' name="username"/>
        </div>

    <%} else { %>

        <input type="hidden" id='username' name='username' value='<%=CharacterEncoder.getSafeText
        (request.getParameter("username"))%>'/>

    <% } %>

    <!--Password-->
    <div class="bottom-space">
		<input type="password" placeholder="<fmt:message key='password'/>" id='password' name="password"  class="input-xlarge"/>
		<input type="hidden" name="sessionDataKey" value='<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>'/>
    </div>

	<div class="bottom-space fix-checkbox">
		<label class="checkbox">
			<input type="checkbox" id="chkRemember" name="chkRemember">
			<fmt:message key='remember.me'/>
		</label>
	</div>

    <div class="bottom-space right-align">
        <input type="submit" value='<fmt:message key='login'/>' class="btn login-btn">
    </div>

</div>

