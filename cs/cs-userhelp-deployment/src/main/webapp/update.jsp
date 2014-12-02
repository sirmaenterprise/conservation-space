<?xml version="1.0" encoding="UTF-8"?>
<%@page import="com.sirma.itt.emf.help.history.PageUrl"%>
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker" scope="session" />
<%@ page import="java.net.URL,javax.help.HelpSet,javax.help.Map.ID" %>
<!DOCTYPE html PUBLIC 
  "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<jsp:useBean id="historyManager" scope="session" class="com.sirma.itt.emf.help.history.HistoryManagerImpl"/>
<script>
<%
//only a "url" or an "id" should be set.
//If for some reason both are set the url overrides the id 

String url = request.getParameter("url");
String id = request.getParameter("id");

String historyUrl = request.getParameter("historyUrl");

if (historyUrl != null ) {	
	
	String shema = pageContext.getRequest().getScheme();
	int serverPort = pageContext.getRequest().getServerPort();
	String serverName = pageContext.getRequest().getServerName();
	String contextName = request.getContextPath();	
	String navigationState = shema + "://" + serverName + ":" + serverPort + contextName + "/";
	navigationState = navigationState + helpBroker.getCurrentNavigatorView().getClass().getName() + ".jsp";
	
	//check if page allready added into history manager.
	if (!(historyUrl.equals(historyManager.getCurrentPageContentUrl())&&(navigationState.equals(historyManager.getCurentPageNavigatorState())))) {		
		PageUrl pageInformation = new PageUrl();
		pageInformation.setContentUrl(historyUrl);
		pageInformation.setNavigationState(navigationState);	
		pageInformation.setId(id);
		historyManager.addPageUrl(pageInformation);
	}
}

if (url == null && id == null) {
 // nothing to do
 // in regular java code we would return.
 // we'll just else here
} else {
 // Try the URL first.
 // If a parameter has been past then there has been
 // a change in the contentframe that needs to be reflected in the
 // helpBroker and the navigator
 if (url != null) {
	URL curURL = helpBroker.getCurrentURL();
	URL testURL = new URL(url);
	if (!testURL.sameFile(curURL)) {
	    ID currentid = helpBroker.getCurrentID();
	    helpBroker.setCurrentURL(testURL);
	    ID mapid = helpBroker.getCurrentID();
	    // if the changed url translates into an id'
	    // update the navigatorframe 
	    // otherwise make sure that nothing is selected
	    // in the navigator frame
	    if (mapid != null && mapid != currentid) {
		%>
             top.findHelpID("<%= mapid.id %>");
		<%
	    } else {
		if (currentid != null) {
		    %>
		    top.setSelected("<%= currentid.id %>", false);
		    <%
		}
	    }
	}
 } else {
	// no URL was specified how about an id?
	if (id != null) {
	    // Yep, just update the helpBroker
	    // The contentsframe has already been updated
	    helpBroker.setCurrentID(id);
	}
 }
}
%>
  //  top.setTimeout("top.checkContentsFrame( );", 2000);
</script>
</head>
<body>
</body>
</html>
 
