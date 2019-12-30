<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" encoding="ISO-8859-1" />
<xsl:include href="css.xsl"/>
<xsl:template match="/">
<html>
  <head>
    <META NAME="GENERATOR" CONTENT="JAuswertung"/>
    <title>Meldung</title>
    <xsl:call-template name="cascadingstylesheet"/>
  </head>
  <body>
<div style="width:99%;margin:auto;padding:10px 0px">
<div class="corner">
<b class="cornertop"><xsl:text> </xsl:text><b class="b1"><xsl:text> </xsl:text></b><b class="b2"><xsl:text> </xsl:text></b><b class="b3"><xsl:text> </xsl:text></b><b class="b4"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
<div class="cornercontent">
  	<h1><xsl:value-of select="/registrations/competitioninfos/@name"/></h1>
    <p>
      <xsl:text>Ort: </xsl:text>
      <xsl:value-of select="/registrations/competitioninfos/@location"/>
    </p>
    <p>
      <xsl:text>Datum: </xsl:text>
      <xsl:value-of select="/registrations/competitioninfos/@date"/>
    </p>
    <p>
      <xsl:text>Stand: </xsl:text>
      <xsl:value-of select="/registrations/competitioninfos/@dateoflastchange"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="/registrations/competitioninfos/@timeoflastchange"/>
    </p>

    <h2><xsl:value-of select="registrations/@name"/></h2>
    <table>
      <xsl:attribute name="width">99%</xsl:attribute>
      <xsl:attribute name="summary"><xsl:value-of select="registrations/@name"/></xsl:attribute>      
      <tr>
        <th><xsl:value-of select="/registrations/titles/@number"/></th>
        <th><xsl:value-of select="/registrations/titles/@name"/></th>
        <th><xsl:value-of select="/registrations/titles/@organisation"/></th>
        <xsl:choose>
          <xsl:when test='/registrations/competitioninfos/@single="true"'>
            <th><xsl:value-of select="/registrations/titles/@year"/></th>
          </xsl:when>
        </xsl:choose>
        <th><xsl:value-of select="/registrations/titles/@agegroup"/></th>
        <th><xsl:value-of select="/registrations/titles/@comment"/></th>
        <th><xsl:value-of select="/registrations/titles/@regpoints"/></th>
      </tr>
    <xsl:for-each select="/registrations/competition/registration">
      <tr>
        <td><xsl:value-of select="@number"/></td>
        <td><xsl:value-of select="@name"/>
          <xsl:choose>
            <xsl:when test='@ak="true"'>
              <xsl:text> (a.K.)</xsl:text>
            </xsl:when>
        </xsl:choose>        
        </td>
        <td><xsl:value-of select="@organisation"/></td>
        <xsl:choose>
          <xsl:when test='/registrations/competitioninfos/@single="true"'>
            <td><xsl:value-of select="@year"/></td>
          </xsl:when>
        </xsl:choose>
        <td><xsl:value-of select="@agegroup"/><xsl:text> </xsl:text><xsl:value-of select="@sex"/></td>
        <td><xsl:value-of select="@comment"/></td>
        <td><xsl:value-of select="@regpoints"/></td>
      </tr>
    </xsl:for-each>
  </table>
  <p>
    <xsl:attribute name="align">right</xsl:attribute>
    <a>
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:attribute name="href">http://<xsl:value-of select="/registrations/infos/@homepage"/></xsl:attribute>
      <xsl:value-of select="/registrations/infos/@name"/>
    </a>  
    <xsl:text> </xsl:text>
    <xsl:value-of select="/registrations/infos/@copyright"/>
  </p>
</div>
<b class="cornerbottom"><xsl:text> </xsl:text><b class="b4b"><xsl:text> </xsl:text></b><b class="b3b"><xsl:text> </xsl:text></b><b class="b2b"><xsl:text> </xsl:text></b><b class="b1b"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
</div>
</div>  
</body>
</html>
</xsl:template>
</xsl:stylesheet>