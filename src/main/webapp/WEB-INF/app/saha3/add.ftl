[#ftl]
[#setting url_escaping_charset='UTF-8']
<html>
<head>
<title>Create ${model}!</title>
</head>
<body>

<div style="margin:5em;">
	<h2 style="color:grey;font-family:sans-serif">Create project: <span style="color:hotpink">${model}</span></h2>
	<form id="form" action="../service/import_project/" enctype="multipart/form-data" method="post">
		<input name="modelName" id="modelName" type="hidden" value="${model}">
		<input type="submit" value="Do it!" style="margin-top:5px">
	</form>
</div>

</body>
</html>