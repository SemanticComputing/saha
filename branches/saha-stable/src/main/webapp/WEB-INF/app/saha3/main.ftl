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
			<li><a href="../${project}/index.shtml" style="color:black">${project}</a></li> 
			[/#list]
		</ul>
		</div>
		<div style="border:thin solid #ccc;float:left;width:420px;padding-left:10px;padding-right:10px;min-height:300px;">
		<h2 style="color:grey;">Create a new project</h2>
		<form id="form" action="../service/import_project/" enctype="multipart/form-data" method="post">
			<input name="operation" id="operation" type="hidden" value="import">
			<div style="margin-top:10px">
				<small>Project name <span style="color:grey">(will be part of URL, i.e don't use whitespace etc.)</span></small><br/>
				<input name="modelName" id="modelName" type="text">
			</div>
			<div style="margin-top:10px">
				<small>RDF file <span style="color:grey">(xml, turtle and n-triple serializations are OK.)</span></small><br/>
				<input name="file" id="file" type="file">
			</div>
			<div style="margin-top:10px">
				<input type="submit" value="Do it!">
			</div>
		</form>
		</div>
	</div>
</body>
</html>

