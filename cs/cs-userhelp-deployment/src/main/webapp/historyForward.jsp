<%@ page language="Java"%>
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker" scope="session" />
<jsp:useBean id="historyManager" scope="session" class="com.sirma.itt.emf.help.history.HistoryManagerImpl"/>
<%
	response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 0);
com.sirma.itt.emf.help.history.PageUrl forward = historyManager.forward();

String view = "TOC";
if (forward.getNavigationState().contains("Index")){
	view = "Index";
} else if (forward.getNavigationState().contains("Search")) {
	view = "Search";
}
helpBroker.setCurrentView(view);

if (forward.getId() != null) {
	helpBroker.setCurrentID(forward.getId());
}


out.println("starttrats" + forward.getContentUrl() + ":::" + forward.getNavigationState() + ":::" + forward.getId() +  ":::" + historyManager.hasMoreForward() +  ":::" + historyManager.hasMoreBack() + "enddne");
%>