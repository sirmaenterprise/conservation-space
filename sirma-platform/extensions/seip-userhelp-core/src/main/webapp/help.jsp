

<?xml version="1.0" ?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker"
	scope="session" />
<jsp:useBean id="sslUtil" class="com.sirma.itt.seip.help.SSLService"
	scope="application" />
<%
	sslUtil = new com.sirma.itt.seip.help.SSLService();
	sslUtil.disableSSLValidation();
%>

<%@ taglib uri="/jhlib.tld" prefix="jh"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

	<jh:validate helpBroker="<%=helpBroker%>"
		helpSetName="userhelp/userhelp.hs" />
	<%
		// only an "id" should be set.
		String id = request.getParameter("id");
		if (id == null) {
			// "no"thing to do
			// in regular java code we would return.
			helpBroker.setCurrentID("id");
		} else {
			helpBroker.setCurrentID(id);
		}
	%>
	<link rel="shortcut icon" type="image/vnd.microsoft.icon"
		href="images/user-guide-16.png" />
	<title>User Guide</title> <script language="JavaScript1.3"
		src="util.js">
		
	
	</script>
	<script src="jQuery.js"></script>
</head>

<frameset rows="0,*" name="top" BORDER=0 framespacing=0>
	<frameset COLS="*,0" name="upperframe" NORESIZE frameborder=NO>
		<frame SRC="update.jsp" name="updateframe">
	</frameset>
	<frameset COLS="30%,70%" name="lowerhelp" BORDER=0 frameborder=NO>
		<frameset id="nav" rows="80,*" name="navigatortop" BORDER=0
			frameborder=NO>
			<frame SRC="navigator.jsp" id="navigatorframe" name="navigatorframe"
				SCROLLING="NO" frameborder=NO></frame>
			<frame SRC="loading.html" id="treeframe" name="treeframe"
				SCROLLING="AUTO" frameborder=NO></frame>
		</frameset>
		<frameset rows="80,*" name="rightpane" framespacing=0>
			<frame NORESIZE SRC="toolbars.jsp" id="toolbarframe"
				name="toolbarframe" SCROLLING="NO" BORDER=0 frameborder=NO></frame>
			<frame
				SRC="<jsp:getProperty name="helpBroker" property="currentURL" />"
				name="contentsFrame" id="contentsFrame" SCROLLING="AUTO"
				frameborder="no"></frame>
		</frameset>
	</frameset>
</frameset>
<noframes></noframes>
</html>
