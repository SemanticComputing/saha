[#ftl]
[#setting url_escaping_charset='UTF-8']
<html>
<head>
<title>Poka Web Service - Info Page</title>
<style type="text/css">
 div.top {
	position: static;
	margin-left: 10%;
	margin-right: 10%;
	padding-bottom: 20px;	
	width: 80%;
	align: center;
	text-align: center;
	border-bottom-width: 5px;
	border-bottom-style: solid;
 }
 
 div.left {
	position: absolute;
	margin-top: 20px;	
	margin-left: 0%;
	margin-right: 50%;
	width: 45%;
	align: left;
	text-align: center;
 }

div.right {
	position: absolute;
	margin-top: 20px;
	margin-left: 50%;
	margin-right: 0%;
	width: 45%;
	align: right;
	text-align: center;
 } 

 .headline {
	font-size: 28px;
	text-align: center;
 }

 .outset {
	color: #000000;
	background-color: #b5d5f1;
	padding: 3px;
	border: medium outset;
 }
 
 body {
	font-family: verdana, arial, "lucida console", sans-serif;
	color: #ffffff;
	background-color: #6890cb;
 }
 
 a {
    text-decoration: none;
    color: #000000;    
 }

</style>
</head>
<body>
<div class="top">
<div class="outset headline">
Poka web service
</div>
Usage example:<br/>
<a href="poka.shtml?url=http://www.seco.tkk.fi&termset=trieYSO&termset=trieMeshEnglish&language=en&output=html">
poka.shtml?url=http://www.seco.tkk.fi&termset=trieYSO&termset=trieMeshEnglish&language=en&output=html
</a><br/><br/>
Parameters:<br/>
<b>url</b> : URL of the document to be parsed<br/>
<b>termset</b> : term vocabulary to search for; multiple termsets supported<br/>
<b>output</b> : "html" for tagged html, "termxml" for an XML document about the found concepts<br/>
<b>language</b> : preferred stemming language to be used in parsing<br/>
<b>overwrite</b> : set to "true" to bypass result cache; defaults to false<br/>

</div>
<div class="left">
[#if termsets??]
<b>Available termsets</b>:<br/>
[#list termsets as term]
<br/>${term}
[/#list]
[#else]
<b>Error in database connection</b>
[/#if]
</div>
[#if languages??]
<div class="right">
<b>Supported languages</b><br/>(other languages parsed without stemming):<br/>
[#list languages as language]
<br/>${language}
[/#list]
</div>
[/#if]
</body>
</html>
