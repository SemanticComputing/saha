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
			
			<h2>Change password:</h2><br/>
			<form method="POST">
				New password:<br/>			
				<input type="password" name="newPass" /><br/>
				New password again:<br/>			
				<input type="password" name="newPass2" /><br/>
				<input type="hidden" name="passhash" value="${passhash}" />
				<input type="submit" value="Change password" />
			</form>		
			
			<h2>Change SPARQL configuration</h2>
			<form method="POST">
				<input type="hidden" name="operation" value="changeSettings" />
				<input type="hidden" name="passhash" value="${passhash}" />
				SPARQL endpoint URL:<br />
				<input type="text" name="sparqlURL" value="${sparqlConfiguration.sparqlURL!''}" /><br />
				
				SPARUL endpoint URL:<br />
				<input type="text" name="sparulURL" value="${sparqlConfiguration.sparulURL!''}" /><br />

				Graph URI:<br />
				<input type="text" name="graphURI" value="${sparqlConfiguration.graphURI!''}" /><br />

				Label property URI:<br />
				<input type="text" name="labelURI" value="${sparqlConfiguration.labelURI}" /><br />
				
				SPARQL query for getting whole model:<br />
				<textarea name="wholeModelQuery" rows="2" cols="90">${sparqlConfiguration.wholeModelQuery}</textarea><br />
				
				SPARQL query for getting string matches:<br />
				<textarea name="stringMatchesQuery" rows="18" cols="90">${sparqlConfiguration.stringMatchesQuery}</textarea><br />
				
				SPARQL query for getting instances matching a specified type:<br />
				<textarea name="instanceQuery" rows="8" cols="90">${sparqlConfiguration.instanceQuery}</textarea><br />
				
				SPARQL query for getting the label of a resource:<br />
				<textarea name="labelQuery" rows="8" cols="90">${sparqlConfiguration.labelQuery}</textarea><br />
				
				SPARQL query for getting the types of a resource:<br />
				<textarea name="typesQuery" rows="8" cols="90">${sparqlConfiguration.typesQuery}</textarea><br />

				SPARQL query for getting the properties of a resource:<br />
				<textarea name="propertiesQuery" rows="10" cols="90">${sparqlConfiguration.propertiesQuery}</textarea><br />

				SPARQL query for getting the inverse properties of a resource:<br />
				<textarea name="inversePropertiesQuery" rows="18" cols="90">${sparqlConfiguration.inversePropertiesQuery}</textarea><br />
		
				SPARQL query for getting the properties of a resource for editing:<br />
				<textarea name="editorPropertiesQuery" rows="28" cols="90">${sparqlConfiguration.editorPropertiesQuery}</textarea><br />
		
				SPARQL query for getting the subsumption tree for the range of a property:<br />
				<textarea name="propertyTreeQuery" rows="18" cols="90">${sparqlConfiguration.propertyTreeQuery}</textarea><br />

				SPARQL query for getting the subsumption tree of the project:<br />
				<textarea name="classTreeQuery" rows="24" cols="90">${sparqlConfiguration.classTreeQuery}</textarea><br />
				
				<input type="submit" value="Change" />
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