[#ftl]
[#setting url_escaping_charset='UTF-8']

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3</title>
</head>

<body style="font-family:sans-serif;">
	<div style="margin:50px;">
		<h1 style="color:grey;margin-left:10px;">SAHA3 
		<div style="color:hotpink;font-size:large;">
			annotation editor for RDF-data
		</div></h1>
		<div style="border:thin solid #ccc;float:left;width:320px;padding-left:10px;padding-right:10px;min-height:300px;">
		<h2 style="color:grey;">Projects</h2>
		<ul>
			[#list projectList as project]
			<li><a href="../project/index.shtml?model=${project?url}" style="color:black">${project}</a></li> 
			[/#list]
		</ul>
		</div>
		</form>
		</div>
	</div>
</body>
</html>

