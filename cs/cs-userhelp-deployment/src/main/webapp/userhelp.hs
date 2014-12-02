<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "helpset_2_0.dtd">

<helpset version="2.0">

	<!-- title -->
	<title>SEP Bundle User Guide</title>

	<!-- maps -->
	<maps>
		<homeID>id</homeID>
		<mapref location="userhelpMap.jhm"/>
	</maps>

	<!-- views -->
	<view mergetype="javax.help.AppendMerge">
		<name>TOC</name>
		<label>Table Of Contents</label>
		<type>javax.help.TOCView</type>
		<data>userhelpTOC.xml</data>
	</view>
	<view mergetype="javax.help.SortMerge">
		<name>Index</name>
		<label>Index</label>
		<type>javax.help.IndexView</type>
		<data>userhelpIndex.xml</data>
	</view>

	<view mergetype="javax.help.SortMerge">
		<name>Search</name>
		<label>Search</label>
		<type>javax.help.SearchView</type>
		<data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
	</view>
	<presentation default="true" displayviewimages="true">
		<name>main window</name>
		<title>SEP Bundle User Guide</title>
		<image>helpimage</image>
		<toolbar>
			<helpaction>javax.help.BackAction</helpaction>
			<helpaction>javax.help.ForwardAction</helpaction>
			<helpaction>javax.help.SeparatorAction</helpaction>
			<helpaction>javax.help.HomeAction</helpaction>
			<helpaction>javax.help.SeparatorAction</helpaction>
			<helpaction>javax.help.PrintAction</helpaction>
			<helpaction>javax.help.PrintSetupAction</helpaction>
		</toolbar>
	</presentation>
</helpset>

