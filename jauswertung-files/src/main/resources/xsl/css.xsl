<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" encoding="ISO-8859-1" />

<xsl:template name="cascadingstylesheet">
<xsl:text>
    <style type="text/css">
		html           {
						 font-size:100.01%; 
					   }
		body           { color:            #000000;
        		         background-color: #FFFFFF;
		                 font-family:      Verdana, Tahoma, Helvetica;
        		         font-size:        12;
		               }
		a:link         { text-decoration:  none;
        		         color:            #000060;
        		       }
		a:visited      { text-decoration:  none;
        		         color:            #000000; 
					   }
		a:active       { text-decoration:  none;
        		         color:            #000000;
		               }
		a:hover        { text-decoration:  underline;
        		         color:            #000000;
					   }
		table          { empty-cells:      show;
                		 border-collapse:  collapse;
                		 border:           1px solid #FCD71A;
                		 font-size:        1em;
		               }
		tr	           {
		                 font-size:        1em;
                		 border:           1px solid #FCD71A;
					   }
		td             {
		                 margin:           2px;
        		         padding:          3px;
		                 border:           1px solid #FCD71A;
		                 border-left:      0px;
		                 border-right:     0px;
		               }
		th             {
						 background-color: #FCD71A;
						 color:            #5C5C5C;
		                 border:           1px solid #FCD71A;
		                 border-left:      0px;
		                 border-right:     0px;
						 text-align:       center;
						 vertical-align:   middle;
		               }
		.center        {
						 text-align:       center;
		               }
		.left          {
						 text-align:       left;
		               }
		.right         {
						 text-align:       right;
		               }
	    .borderright   {
                		 border-right:     #FCD71A solid 1px;
	    }
	    .borderleft    {
                		 border-left:      #FCD71A solid 1px;
	    }
		h1             { font-size:        2em;
		                 font-weight:      bold;
		                 width:            100%;
		                 background-color: #FFFFFF;
		                 color:            #5C5C5C;
					   }
		h2             { font-size:        1.6em;
		                 font-weight:      bold;
		                 width:            100%;
		                 background-color: #FFFFFF;
		                 color:            #5C5C5C;
					   }
		h3             { font-size:        1.2em;
		                 font-weight:      bold;
		                 width:            100%;
		                 background-color: #FFFFFF;
		                 color:            #5C5C5C;
		               }
    </style>
</xsl:text>
</xsl:template>
</xsl:stylesheet>