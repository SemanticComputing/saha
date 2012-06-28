[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]
[#include "support/editor_common.ftl"]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model} - EDIT: ${instance.label}</title>
	[@header model/]
	[@editorScripts/]
</head>

<body class="tundra">

	[@searchBar model lang/]
	
	<div style="margin:5px;">
		${uri}
		
		<br/>
		<div style="font-size:18pt;margin-top:1em;margin-left:5px;">
			[#if instance.types?size > 0]
				[#list instance.types as type]${type.label}[#if type_has_next], [/#if][/#list]: 
			[/#if]
			${instance.label}
			[#if locked]
				<span style="color:red;">
					[another user is editing this resource]
				</span>
			[/#if]
		</div>
		<div style="margin-bottom:10px;margin-left:5px;">
			<a href="resource.shtml?uri=${uri?url}" style="color:black">[view]</a> |
			<a href="export.shtml?uri=${uri?url}" style="color:#777">[rdf]</a>
			<a href="config.shtml?uri=${uri?url}" style="color:#777">[config]</a> |
			<a href="javascript:remove_resource('${model}','${instance.uri?url}','${instance.label?html?js_string}')" style="color:crimson">[remove]</a>
		</div>
		
		[@renderMap instance.propertyMapEntrySet instance.uri true /]		
		[@editorProperties model instance 'r'/]
		
		<br/>
	</div>
	
</body>
</html>
