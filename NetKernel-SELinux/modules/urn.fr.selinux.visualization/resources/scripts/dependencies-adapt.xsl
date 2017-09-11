<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:nk="http://netkernel.org"
>
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

  <xsl:param name="modulename" nk:class="java.lang.String" />

  <xsl:template match="/">

    <xsl:element name="output">
      <xsl:for-each select="/output/content/m">
        <xsl:element name="link">
          <xsl:attribute name="source">
            <xsl:value-of select="$modulename" />
          </xsl:attribute>
          <xsl:attribute name="target">
            <xsl:value-of select="." />
          </xsl:attribute>
          <xsl:if test="@o='true'">
            <xsl:attribute name="type">optional</xsl:attribute>
          </xsl:if>
          <xsl:if test="not(@o)">
            <xsl:attribute name="type">mandatory</xsl:attribute>
          </xsl:if>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>

  </xsl:template>

</xsl:stylesheet>
