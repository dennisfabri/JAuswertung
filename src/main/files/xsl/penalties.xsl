<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" encoding="ISO-8859-1" />
<xsl:include href="css.xsl"/>
<xsl:template match="/">
<html>
  <head>
    <META NAME="GENERATOR" CONTENT="JAuswertung"/>
    <title><xsl:text>Strafenkatalog - JAuswertung</xsl:text></title>
    <xsl:call-template name="cascadingstylesheet"/>
  </head>
  <body>
<div style="width:99%;margin:auto;padding:10px 0px">
<div class="corner">
<b class="cornertop"><xsl:text> </xsl:text><b class="b1"><xsl:text> </xsl:text></b><b class="b2"><xsl:text> </xsl:text></b><b class="b3"><xsl:text> </xsl:text></b><b class="b4"><xsl:text> </xsl:text></b><xsl:text> </xsl:text></b>
<div class="cornercontent">
    <xsl:for-each select="penalties/list/chapter">
      <h1><xsl:value-of select="@name"/></h1>
      <xsl:for-each select="paragraph">
        <h2><xsl:value-of select="@name"/></h2>
        <table>
          <xsl:attribute name="style">width: 99%;</xsl:attribute>
          <xsl:attribute name="summary"><xsl:value-of select="@name"/></xsl:attribute>
          <tr>
            <th><xsl:text>Code</xsl:text></th>
            <th><xsl:text>Strafe</xsl:text></th>
          </tr>
          <xsl:for-each select="penalty">
            <tr>
              <td><xsl:value-of select="@code"/></td>
              <td>
                <xsl:value-of select="@text"/><br/>
                <xsl:value-of select="@penalty"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </xsl:for-each>
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