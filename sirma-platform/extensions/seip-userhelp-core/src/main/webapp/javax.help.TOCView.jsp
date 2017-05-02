<?xml version="1.0" ?>
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker" scope="session" />
<%@ page import="javax.help.TOCView, javax.help.Map.ID" %>
<%@ taglib uri="/jhlib.tld" prefix="jh" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/TR/1999/REC-html-in-xml" xml:lang="en"
	lang="en">
<head>
<style type="text/css">
    .anchorStyle { text-decoration:none; color:black;}
    .anchorBoldStyle { text-decoration:none; color:black; font-weight: bold;}
</style>
<script language="JavaScript1.3" src="tree.js">
</script>
<script src="jQuery.js"></script>
<script type="text/javascript">
	function hideLoading() {
		$("*", top.toolbarframe.documetn).show();
	}
</script>
</head>
<body onload="hideLoading()">
<div>
<SCRIPT type="text/javascript">
tocTree = new Tree("tocTree", 22, "ccccff", true, false);
<% TOCView curNav = (TOCView)helpBroker.getCurrentNavigatorView(); %>
<jh:tocItem helpBroker="<%= helpBroker %>" tocView="<%= curNav %>" >
tocTree.addTreeNode("<%= parentID %>","<%= nodeID %>","<%= iconURL!=""?iconURL:"null" %>","<%= name %>","<%= helpID %>","<%= contentURL!=""?contentURL:"null" %>","<%= expansionType%>" );
</jh:tocItem>
tocTree.drawTree();
tocTree.refreshTree();
<% 
ID id = helpBroker.getCurrentID();
if (id != null) {
%> 
    tocTree.selectFromHelpID("<%= id.id%>");
<%
}
%>
</SCRIPT>
</div>
</BODY>


