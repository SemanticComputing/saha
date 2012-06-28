[#ftl]
[#setting url_escaping_charset='UTF-8']

<html>
<head>
<title>Vera - Error - ${veraVersion}</title>
[#include "veraCSS.ftl" /]
</head>
<body>

<div class="outset">
<h2>Error in validation, reason:</h2><br/>
<div align="right">
<a href="vera.shtml">New validation</a>
</div>
${reason}
</div>




</body>
</html>