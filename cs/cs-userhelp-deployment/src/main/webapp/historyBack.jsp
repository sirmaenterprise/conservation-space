<%@ page language="Java"%>
<jsp:useBean id="helpBroker" class="javax.help.ServletHelpBroker" scope="session" />
<jsp:useBean id="historyManager" scope="session" class="com.sirma.itt.emf.help.history.HistoryManagerImpl"/>
<%
	response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 0);
com.sirma.itt.emf.help.history.PageUrl back = historyManager.back();

String view = "TOC";
if (back.getNavigationState().contains("Index")){
	view = "Index";
} else if (back.getNavigationState().contains("Search")) {
	view = "Search";
}
helpBroker.setCurrentView(view);

if (back.getId() != null) {
	helpBroker.setCurrentID(back.getId());
}

out.println("starttrats" + back.getContentUrl() + ":::" + back.getNavigationState() + ":::" + back.getId() +  ":::" + historyManager.hasMoreForward() +  ":::" + historyManager.hasMoreBack() + "enddne");
%>