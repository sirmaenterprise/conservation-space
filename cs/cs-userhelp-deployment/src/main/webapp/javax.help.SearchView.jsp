
<?xml version="1.0" ?>
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker" scope="session" />
<%@ page import="javax.help.SearchView" %>
<%@ taglib uri="/jhlib.tld" prefix="jh" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/TR/1999/REC-html-in-xml" xml:lang="en"
	lang="en">
<head>
<% String query = request.getParameter("searchQuery"); %>
<STYLE type="text/css">
    .anchorStyle { text-decoration:none; color:black; margin-left:8pt; }
    .anchorBoldStyle { text-decoration:none; color:black; font-weight: bold; margin-left:5pt;}
</STYLE>
<script language="JavaScript1.3" src="searchlist.js">
</script>
<script src="jQuery.js"></script>
<script type="text/javascript">
	function hideLoading() {
		$("*", top.toolbarframe.documetn).show();
	}
</script>
</head>
<BODY  onload="hideLoading()">

<FORM METHOD="GET" NAME="search" ACTION="javax.help.SearchView.jsp">
<P>Find:
<INPUT TYPE="text" NAME="searchQuery" VALUE="<%= query!=null?query:"" %>" >
<input type="submit" name="search" value="Search" />
</FORM>

<%
if (query != null) {
    SearchView curNav = (SearchView)helpBroker.getCurrentNavigatorView(); 
%>
<SCRIPT>
searchList = new SearchList("searchList", 22, "ccccff");
<jh:searchTOCItem searchView="<%= curNav %>" helpBroker="<%= helpBroker %>" query="<%= query %>" >
searchList.addNode("<%= name.replaceAll("\"", "") %>","<%= confidence %>","<%= hits %>","<%= helpID %>","<%= contentURL %>" );
</jh:searchTOCItem>
searchList.drawList();
searchList.refreshList();
searchList.select(0);
</SCRIPT>
<%
}
%>
</BODY>
</HTML>
