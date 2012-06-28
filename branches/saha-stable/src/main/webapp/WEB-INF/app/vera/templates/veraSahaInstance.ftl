[#ftl]
[#setting url_escaping_charset='UTF-8' /]
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Vera - Saha instance edit</title>
<link rel="stylesheet" type="text/css" href="app/css/saha/style_${versionId}.css" />
<script type="text/javascript" src="app/scripts/saha/sahaUtil_${versionId}.js"></script>
<script type="text/javascript" src="http://www.yso.fi/onki.js"></script>
</head>
<body onLoad="initSahaUtils()">
<div style="font-size: 24px; text-align: center; font-family: verdana, arial, sans-serif;">
Note: changes made here do not apply to the validation report until revalidation!<br/>
<a href="javascript: self.close()">[Close this window]</a>
</div>
<hr/>

${sahaInstanceHTML}

</body>
</html>