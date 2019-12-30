<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" encoding="ISO-8859-1" />
<xsl:include href="css.xsl"/>
<xsl:template match="/">
<html>
  <head>
    <META NAME="GENERATOR" CONTENT="JAuswertung"/>
    <title><xsl:value-of select="/results/competitioninfos/@name"/><xsl:text> - </xsl:text><xsl:value-of select="/results/@name"/><xsl:text> - JAuswertung</xsl:text></title>
    <xsl:call-template name="cascadingstylesheet"/>
  </head>
  <body>
<div style="width:99%;margin:auto;padding:10px 0px">
<div class="corner">
<b class="cornertop"><xsl:text> </xsl:text><b class="b1"><xsl:text> </xsl:text></b><b class="b2"><xsl:text> </xsl:text></b><b class="b3"><xsl:text> </xsl:text></b><b class="b4"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
<div class="cornercontent">
  	<h1><xsl:value-of select="/results/competitioninfos/@name"/></h1>
    <p><xsl:text>Ort: </xsl:text><xsl:value-of select="/results/competitioninfos/@location"/></p>
    <p><xsl:text>Datum: </xsl:text><xsl:value-of select="/results/competitioninfos/@date"/></p>
    <p>
      <xsl:text>Stand: </xsl:text>
      <xsl:value-of select="/results/competitioninfos/@dateoflastchange"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="/results/competitioninfos/@timeoflastchange"/>
    </p>

    <h2><xsl:value-of select="/results/@name"/></h2>
    <xsl:for-each select="results/competition/agegroup">
      <h3><xsl:value-of select="@name"/><xsl:text> </xsl:text><xsl:value-of select="@sex"/></h3>
      <table>
        <xsl:attribute name="width">99%</xsl:attribute>
        <xsl:attribute name="summary">Ergebnisse <xsl:value-of select="@name"/><xsl:text> </xsl:text><xsl:value-of select="@sex"/></xsl:attribute>
        <tr>
	      <th rowspan="2"><xsl:value-of select="/results/titles/@rankshort"/></th>
	      <th rowspan="2"><xsl:value-of select="/results/titles/@name"/></th>
	      <th rowspan="2"><xsl:value-of select="/results/titles/@organisation"/></th>
          <xsl:choose>
            <xsl:when test='/results/competitioninfos/@single="true"'>
              <th rowspan="2"><xsl:value-of select="/results/titles/@yearshort"/></th>
            </xsl:when>
          </xsl:choose>
	      <th rowspan="2" class="center"><xsl:value-of select="/results/titles/@points"/></th>
		  <xsl:for-each select="discipline">
	        <th colspan="4" class="center"><xsl:value-of select="@name"/></th>
		  </xsl:for-each>
		  <xsl:for-each select="hlw">
	        <th rowspan="2" class="center"><xsl:value-of select="/results/titles/@hlwname"/></th>
		  </xsl:for-each>
		</tr>
        <tr>
		  <xsl:for-each select="discipline">
	        <th><xsl:value-of select="/results/titles/@rankshort"/></th>
	        <th><xsl:value-of select="/results/titles/@time"/></th>
	        <th><xsl:value-of select="/results/titles/@points"/></th>
	        <th><xsl:value-of select="/results/titles/@penaltyshort"/></th>
		  </xsl:for-each>
	    </tr>
      <xsl:for-each select="registration">
        <tr>
          <td class="right"><xsl:value-of select="@rank"/></td>
          <td class="left borderleft"><xsl:value-of select="@name"/></td>
          <td class="left"><xsl:value-of select="@organisation"/></td>
          <xsl:choose>
            <xsl:when test='/results/competitioninfos/@single="true"'>
              <td class="right"><xsl:value-of select="@yearshort"/></td>
            </xsl:when>
          </xsl:choose>
          <td class="right"><xsl:value-of select="@points"/></td>
          <xsl:for-each select="result">
            <xsl:choose>
              <xsl:when test='@rank="-1"'>
                <td class="right borderleft"> </td>
                <td class="right">-:--,--</td>
                <td class="right">-</td>
                <td class="center">-</td>
              </xsl:when>
              <xsl:otherwise>
                <td class="right borderleft"><xsl:value-of select="@rank"/></td>
                <td class="right"><xsl:value-of select="@time"/></td>
                <td class="right"><xsl:value-of select="@pointsshort"/></td>
                <td class="center"><xsl:value-of select="@penalty"/></td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
          <xsl:for-each select="hlwresult">
            <td class="right borderleft"><xsl:value-of select="@points"/></td>
          </xsl:for-each>
        </tr>
      </xsl:for-each>
    </table>
   </xsl:for-each>
    <xsl:for-each select="results/competition/groupevaluation">
      <h2><xsl:value-of select="/results/titles/@groupevaluation"/></h2>
      <table>
        <xsl:attribute name="width">99%</xsl:attribute>
        <xsl:attribute name="summary"><xsl:value-of select="/results/@name"/></xsl:attribute>
        <tr>
	      <th><xsl:value-of select="/results/titles/@rank"/></th>
	      <th><xsl:value-of select="/results/titles/@organisation"/></th>
	      <th><xsl:value-of select="/results/titles/@points"/></th>
		</tr>
      <xsl:for-each select="registration">
        <tr>
          <td><xsl:value-of select="@rank"/></td>
          <td><xsl:value-of select="@organisation"/></td>
          <td><xsl:value-of select="@points"/></td>
        </tr>
      </xsl:for-each>
    </table>
   </xsl:for-each>
  <p>
    <xsl:attribute name="align">right</xsl:attribute>
    <a>
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:attribute name="href">http://<xsl:value-of select="/results/infos/@homepage"/></xsl:attribute>
      <xsl:value-of select="/results/infos/@name"/>
    </a>  
    <xsl:text> </xsl:text>
    <xsl:value-of select="/results/infos/@copyright"/>
  </p>
</div>
<b class="cornerbottom"><xsl:text> </xsl:text><b class="b4b"><xsl:text> </xsl:text></b><b class="b3b"><xsl:text> </xsl:text></b><b class="b2b"><xsl:text> </xsl:text></b><b class="b1b"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
</div>
</div>  
  </body>
</html>
</xsl:template>
</xsl:stylesheet>