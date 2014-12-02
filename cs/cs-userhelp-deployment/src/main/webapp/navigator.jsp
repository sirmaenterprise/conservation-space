<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC 
  "-//W3C//DTD XHTML 1.0 transitional//EN"
  "http://www.w3.org/tr/xhtml1/DTD/xhtml1-transitional.dtd">
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker"
	scope="session" />
<%@ page import="javax.help.NavigatorView"%>
<%@ taglib uri="/jhlib.tld" prefix="jh"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
<style type="text/css">
.tabbedBodyStyle {
	//margin-top: 2px;
}

.tableStyle {
	width: 100%;
	border-left-width: 5px;
	border-right-width: 5px;
}

.tabbedAnchorStyle {
	text-decoration: none;
	color: black;
	margin-left: auto;
	margin-right: auto;
}

.tableActiveStyle {
	height: 40px;
	width: 50px;
	background-image: url("images/btn-active-bg.png");
	background-repeat: repeat-x;
	vertical-align: baseline;
}

.tableInactiveStyle {
	height: 40px;
	width: 50px;
	background-image: url("images/btn-inactive-bg.png");
	background-repeat: repeat-x;
	vertical-align: baseline;
	
}

.toolTipLabel {
	text-align: center;
}
</style>
<script type="text/javascript" src="navigation.js"></script>
<%@ page language="java" import="java.util.*" %>
<%
	ResourceBundle resource = ResourceBundle.getBundle("message");
/// commonVariable.properties file will be in WEB-INF/classess  folder
%>
</head>
<body class="tabbedBodyStyle">
	<table CLASS="tableStyle">
		<tr>
			<jh:navigators helpBroker="<%= helpBroker %>">
				<td align="center" class="<%=isCurrentNav.booleanValue()
						? "tableActiveStyle"
						: "tableInactiveStyle"%>">
						<a class=tabbedAnchorStyle href="navigator.jsp?nav=<%=name%>"> 
							<img src="<%=iconURL != "" ? iconURL : "images/" + className + ".png"%>"
									alt="<%=tip%>" border=0 title=<%=resource.getString(className)%>>
						</img>
						<div clss="toolTipLabel"><%=resource.getString(className + ".label")%></div>
					</a>
				</td>
			</jh:navigators>
		</tr>
	</table>
	<%@ page import="javax.help.HelpSet,javax.help.NavigatorView"%>
	<%
		NavigatorView curNav = helpBroker.getCurrentNavigatorView();
		if (curNav != null) {
	%>
	<SCRIPT>
		top.treeframe.location = "<%=curNav.getClass().getName()%>.jsp"
    </SCRIPT>
	<%
		} else {
	%>
	<SCRIPT>
		top.treeframe.location = "nonavigator.jsp"
	</SCRIPT>
	<%
		}
	%>
</body>
</html>



