<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC 
  "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<style type="text/css">

.tabbedBodyStyle {
	margin-top: 2px;
}

.tableStyle {
	width: 100%;
	border-left-width: 5px;
	border-right-width: 5px;
}

.anchorStyle {
	text-decoration: none;
	color: black;
}

.anchorSunBlueStyle {
	text-decoration: underline;
	font-size: 1.2em;
	background-color: transparent;
	color: transparent;
	text-align: justify;
}

.boldStyle {
	margin-left: 30px;
	margin-top: 8px;
	background-image: url("images/nav-bg-2.png");
	background-repeat: repeat-x;
	background-position: 0px 49%;
}

.enableButtons {
	color: black;
}

.disableButtons {
	color: gray;
}

.showLoading {

}

.hideLoading {
	display: none;
}

</style>
<script type="text/javascript" src="navigation.js"></script>
<script src="jQuery.js"></script>
<script type="text/javascript">


function doFoward() {
	var historyRorwardPage = location.protocol + '//' + location.host + "/userhelp/historyForward.jsp"; 	
	$.get(historyRorwardPage, function(data) {

		var backPageInfo = data.substring((data.indexOf("starttrats") + 10));
		backPageInfo = backPageInfo.substring(0, backPageInfo.indexOf("enddne"));

		var info = backPageInfo.split(":::");

		var url = info[0];
		var state = info[1];
		var id = info[2];
		var hasMoreForward = info[3];
		var hasMoreBack = info[4];

		var currentStateHref = top.treeframe.location.href;
		var curentNavigatorHref = top.navigatorframe.location.href;
		var currentContentHref = top.contentsFrame.location.href;

		var backButon = $("a[href*='doBack()']", top.toolbarframe.documetn);
		if ("false" == hasMoreBack) {
			backButon.removeClass("enableButtons").addClass("disableButtons");
			$("img",backButon).attr("src" , "images/backDisable.png");
		} else {
			backButon.removeClass("disableButtons").addClass("enableButtons");
			$("img",backButon).attr("src" , "images/back.gif");
		}
		var forwardButon = $("a[href*='doFoward()']", top.toolbarframe.documetn);
		if ("false" == hasMoreForward) {
			forwardButon.removeClass("enableButtons").addClass("disableButtons");
			$("img",forwardButon).attr("src" , "images/forwardDisable.png");
		} else {
			forwardButon.removeClass("disableButtons").addClass("enableButtons");
			$("img",forwardButon).attr("src" , "images/forward.gif");
		}
			
			if (state != currentStateHref) {
				var id = "";
				if (state.indexOf("TOC") != -1) {
					showLoading();
					var lin = $("a[href*='TOC']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
				} else if (state.indexOf("Index") != -1) {
					showLoading();
					var lin = $("a[href*='Index']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
				} else if (state.indexOf("Search") != -1) {
					//showLoading();
					var lin = $("a[href*='Search']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
			} 
		}
			if (currentContentHref != url) {
				top.contentsFrame.location.href = url;
			}
			//  showButtons();
	});
}

function doBack() {	
	
	var historyRorwardPage = location.protocol + '//' + location.host + "/userhelp/historyBack.jsp"; 	
	$.get(historyRorwardPage, function(data) {

		var backPageInfo = data.substring((data.indexOf("starttrats") + 10));
		backPageInfo = backPageInfo.substring(0, backPageInfo.indexOf("enddne"));

		var info = backPageInfo.split(":::");

		var url = info[0];
		var state = info[1];
		var id = info[2];
		var hasMoreForward = info[3];
		var hasMoreBack = info[4];

		var currentStateHref = top.treeframe.location.href;
		var curentNavigatorHref = top.navigatorframe.location.href;
		var currentContentHref = top.contentsFrame.location.href;

		var backButon = $("a[href*='doBack()']", top.toolbarframe.documetn);
		if ("false" == hasMoreBack) {
			backButon.removeClass("enableButtons").addClass("disableButtons");
			$("img",backButon).attr("src" , "images/backDisable.png");
		} else {
			backButon.removeClass("disableButtons").addClass("enableButtons");
			$("img",backButon).attr("src" , "images/back.gif");
		}
		var forwardButon = $("a[href*='doFoward()']", top.toolbarframe.documetn);
		if ("false" == hasMoreForward) {
			forwardButon.removeClass("enableButtons").addClass("disableButtons");
			$("img",forwardButon).attr("src" , "images/forwardDisable.png");
		} else {
			forwardButon.removeClass("disableButtons").addClass("enableButtons");
			$("img",forwardButon).attr("src" , "images/forward.gif");
		}
		
			if (state != currentStateHref) {
				var id = "";
				if (state.indexOf("TOC") != -1) {
					showLoading();
					var lin = $("a[href*='TOC']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
				} else if (state.indexOf("Index") != -1) {
					showLoading();
					var lin = $("a[href*='Index']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
				} else if (state.indexOf("Search") != -1) {
					//showLoading();
					var lin = $("a[href*='Search']", top.navigatorframe.document);
					lin.each(function() {
						   this.click();
					});
			}
		}
			if (currentContentHref != url) {
				top.contentsFrame.location.href = url;
			}
			// showButtons();
	});
}

function hideLoading() {
	$("*", top.toolbarframe.documetn).show();
}

function showLoading() {
	$("*", top.toolbarframe.documetn).hide();
}

function doPrint() {

	var frameurl = top.contentsFrame.location.href;
	var pageName = frameurl.substring((frameurl.lastIndexOf("/") + 1));
	var historyRorwardPage = location.protocol + '//' + location.host + "/userhelp/print.jsp?fileName=" + pageName; 	
	var NewWin = window.open(historyRorwardPage);	
}
</script>
<%@ page language="java" import="java.util.*" %>
<%
	ResourceBundle resource = ResourceBundle.getBundle("message");
/// commonVariable.properties file will be in WEB-INF/classess  folder
%>
<jsp:useBean id="historyManager" scope="session" class="com.sirma.itt.emf.help.history.HistoryManagerImpl"/>
</head>
<body class="boldStyle">
	<table CLASS="tableStyle" > 
		<tr>
			<td style="padding-right: 10px; text-align: center"><a class='anchorStyle<%=(historyManager.hasMoreBack()? " enableButtons": " disableButtons") %> '
				onfocus="this.blur()" href="javascript:doBack();"update.jsp?> <img
					src="<%=(historyManager.hasMoreBack()? "images/back.gif": "images/backDisable.png") %>" alt="Back" BORDER=0 title=<%=resource.getString("javax.help.BackAction")%>>
					<%=resource.getString("javax.help.BackAction.label")%>
			</a>
			</td>
			<td style="padding-right: 10px; text-align: center"><a class='anchorStyle<%=(historyManager.hasMoreForward()? " enableButtons": " disableButtons") %>'
				onfocus="this.blur()" href="javascript:doFoward();"update.jsp?> <img
					src="<%=(historyManager.hasMoreForward()? "images/forward.gif": "images/forwardDisable.png") %>" alt="Forward" BORDER=0 title=<%=resource.getString("javax.help.ForwardAction")%>>
					<%=resource.getString("javax.help.ForwardAction.label")%>
			</a>
			</td>
			<td style="padding-right: 10px; text-align: center"><a class='anchorStyle'
				href="javascript:doPrint();" onfocus="this.blur()"> <img
					src="images/print.gif" alt="Print" BORDER=0 title=<%=resource.getString("javax.help.PrintAction")%>>
					<%=resource.getString("javax.help.PrintAction.label")%>
			</a>
			</td>
			<td align="right" width=99%><div><a class='anchorStyle'
				onfocus="this.blur()" href="javascript:top.close();"update.jsp?>
					<img style="padding-right: 5px" src="images/close.png" alt="Close" BORDER=0 title=<%=resource.getString("javax.help.Close")%>><br>
					<%=resource.getString("javax.help.Close.label")%>
			</a>
			</td>
		</tr>
	</table>
</body>
</html>