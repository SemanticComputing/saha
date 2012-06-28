[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model} - CONFIGURE</title>
	[@header model/]
	
</head>

<body class="tundra">

	[@searchBar model lang/]
	<div style="margin-left: 30px; margin-top: 30px;">
	[#if message??]<h3 style="color:red">${message}</h3><br/>[/#if]
	[#if authorized]
	
			<h2>Delete project:</h2><br/>
			<form method="POST">
				<input type="hidden" name="passhash" value="${passhash}" />
				<input type="hidden" name="operation" value="delete" />
				Yes, I'm sure: <input type="checkbox" name="confirm" value="true" /><br/>
				<input type="submit" value="Delete" />
			</form>		
			
			<h2>Modify project with a model file:</h2>
			<form id="form" action="../service/import_project/" enctype="multipart/form-data" method="post">
				<input type="hidden" name="allow_rewrite" value="true" />
				<input name="modelName" id="modelName" type="hidden" value="${model}" /><br/>
				<input type="hidden" name="passhash" value="${passhash}" />
				<input name="file" id="file" type="file"><br/>
				<input type="radio" name="operation" value="import" checked="checked">  <strong>add</strong> - import and index model 
				(appends the new triples to the old model and does a full re-indexing)<br/>
				<input type="radio" name="operation" value="merge"> <strong>merge</strong> - add model to existing project 
				(indexes only the new triples, use 'add' if the appended model is large)<br/>
				<input type="radio" name="operation" value="rewrite"> <strong style="color:red">rewrite</strong> - clear existing project and add new<br/>
								
				<input type="submit" value="Submit" style="margin-top:5px">
			</form>
			
			<h2>Change password:</h2><br/>
			<form method="POST">
				New password:<br/>			
				<input type="password" name="newPass" /><br/>
				New password again:<br/>			
				<input type="password" name="newPass2" /><br/>
				<input type="hidden" name="passhash" value="${passhash}" />
				<input type="submit" value="Change password" />
			</form>		
	[#else]			
			Insert password:<br/>
			<form method="POST">		
				<input type="password" name="password" /><br/>
				<input type="submit" value="Submit" />
			</form>
		
	[/#if]
	</div>
</body>