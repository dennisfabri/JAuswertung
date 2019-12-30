<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" encoding="ISO-8859-1" />
<xsl:include href="css.xsl"/>
<xsl:template match="/">
<html>
  <head>
    <META NAME="GENERATOR" CONTENT="JAuswertung"/>
    <title><xsl:value-of select="index/@name"/><xsl:text> - JAuswertung</xsl:text></title>
    <xsl:call-template name="cascadingstylesheet"/>
  </head>
  <body>
<div style="width:99%;margin:auto;padding:10px 0px">
<div class="corner">
<b class="cornertop"><xsl:text> </xsl:text><b class="b1"><xsl:text> </xsl:text></b><b class="b2"><xsl:text> </xsl:text></b><b class="b3"><xsl:text> </xsl:text></b><b class="b4"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
<div class="cornercontent">
  	<h1><xsl:value-of select="/index/competitioninfos/@name"/></h1>
    <p>
      <xsl:text>Ort: </xsl:text>
      <xsl:value-of select="/index/competitioninfos/@location"/>
    </p>
    <p>
      <xsl:text>Datum: </xsl:text>
      <xsl:value-of select="/index/competitioninfos/@date"/>
    </p>
    <p>
      <xsl:text>Stand: </xsl:text>
      <xsl:value-of select="/index/competitioninfos/@dateoflastchange"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="/index/competitioninfos/@timeoflastchange"/>
    </p>

    <h1><xsl:value-of select="index/titles/@results"/></h1>
    <table>
      <xsl:attribute name="width">99%</xsl:attribute>
      <xsl:attribute name="summary">Ergebnisse</xsl:attribute>
      <tr>
        <th>
          <xsl:value-of select="index/titles/@agegroup"/>
        </th>
        <th>
          <xsl:attribute name="colspan">2</xsl:attribute>
          <xsl:value-of select="index/titles/@sex"/>
        </th>
      </tr>
      <xsl:for-each select="index/index/agegroup">
        <tr>
          <td class="center">
            <xsl:value-of select="@name"/>
          </td>
          <td class="center">
            <xsl:variable name="femalesupport" select="@femalesupported"/>
            <xsl:choose>
              <xsl:when test='@femalesupported="true"'>
                <a>
                  <xsl:attribute name="href"><xsl:value-of select="@femalelink"/></xsl:attribute>
                  <xsl:value-of select="/index/titles/@female"/>
                </a>  
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="/index/titles/@female"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
          <td class="center">
            <xsl:variable name="malesupport" select="@malesupported"/>
            <xsl:choose>
              <xsl:when test='@malesupported="true"'>
                <a>
                  <xsl:attribute name="href"><xsl:value-of select="@malelink"/></xsl:attribute>
                  <xsl:value-of select="/index/titles/@male"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="/index/titles/@male"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="index/index/full">
        <tr>
          <td>
            <xsl:attribute name="class">center</xsl:attribute>
            <xsl:attribute name="colspan">3</xsl:attribute>            
            <a>
              <xsl:attribute name="href">groupevaluation.html</xsl:attribute>
              <xsl:value-of select="/index/titles/@groupevaluation"/>
            </a>  
          </td>
        </tr>
      </xsl:for-each>
    </table>
	<h1><xsl:value-of select="/index/titles/@download"/></h1>
    <table>
      <xsl:attribute name="width">99%</xsl:attribute>
      <xsl:attribute name="summary">Herunterladen</xsl:attribute>
      <tr>
        <th><xsl:value-of select="index/titles/@name"/></th>
        <xsl:for-each select="index/index/format">
          <th><xsl:value-of select="@name"/></th>
        </xsl:for-each>
      </tr>
      <xsl:for-each select="index/index/type">
      <tr>
        <td><xsl:value-of select="@name"/></td>
        <xsl:for-each select="format">
          <td>
          <xsl:attribute name="class">center</xsl:attribute>
          <xsl:variable name="support"
                    select="@supported"/>
        <xsl:choose>
          <xsl:when test='@supported="true"'>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="@link"/></xsl:attribute>
              <xsl:value-of select="/index/titles/@download"/>
            </a>  
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>-</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
          </td>
        </xsl:for-each>
      </tr>
      </xsl:for-each>
  </table>
<!--
  <p></p>
  <xsl:for-each select="/index/index/graphics">
    <table>
      <xsl:attribute name="width">99%</xsl:attribute>
      <tr>
        <td>
          <xsl:attribute name="colspan">2</xsl:attribute>
          <xsl:value-of select="@name"/>
        </td>
      </tr>
      <xsl:for-each select="image">
        <tr>
          <td>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="@link1"/></xsl:attribute>
              <xsl:value-of select="@name1"/>
            </a>
          </td>
          <td>
            <a>
              <xsl:attribute name="href"><xsl:value-of select="@link2"/></xsl:attribute>
              <xsl:value-of select="@name2"/>
            </a>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:for-each>
-->
  <p>
    <xsl:attribute name="align">right</xsl:attribute>
    <a>
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:attribute name="href">http://<xsl:value-of select="/index/infos/@homepage"/></xsl:attribute>
      <xsl:value-of select="/index/infos/@name"/>
    </a>  
    <xsl:text> </xsl:text>
    <xsl:value-of select="/index/infos/@copyright"/>
  </p>
</div>
<b class="cornerbottom"><xsl:text> </xsl:text><b class="b4b"><xsl:text> </xsl:text></b><b class="b3b"><xsl:text> </xsl:text></b><b class="b2b"><xsl:text> </xsl:text></b><b class="b1b"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
</div>
</div>  
</body>
</html>
</xsl:template>
</xsl:stylesheet>