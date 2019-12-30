<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN""http://java.sun.com/products/javahelp/helpset_2_0.dtd">
<helpset version="2.0">
  <title>Hilfe von JAuswertung</title>
  <maps>
    <homeID>top</homeID>
    <mapref location="map.jhm" />
  </maps>
  <view xml:lang="de" mergetype="javax.help.UniteAppendMerge">
    <name>TOC</name>
    <label>Inhaltsverzeichnis</label>
    <type>javax.help.TOCView</type>
    <data>toc.xml</data>
  </view>
  <!--
  <view xml:lang="de" mergetype="javax.help.SortMerge">
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>index.xml</data>
  </view>
  -->
  <view xml:lang="de">
    <name>Search</name>
    <label>Suche</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</ data>
  </view>
<!--  <view>
    <name>Favorites</name>
    <label>Favoriten</label>
    <type>javax.help.FavoritesView</type>
  </view>
-->
  <presentation default="true">
    <name>main window</name>
    <size width="800" height="600"/>
    <location x="50" y="50"/>
    <title>JAuswertung - Hilfe</title>
  </presentation>
</helpset>
