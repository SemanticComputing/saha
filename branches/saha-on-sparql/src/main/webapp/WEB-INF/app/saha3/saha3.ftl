[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]

[#assign pageSize=500]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model}</title>
	[@header model/]
	<script>
		function create_new_instance(model,type) {
			document.body.style.cursor="progress";
			ResourceEditService.createInstance(model,type,null, {
				callback:function(dataFromServer) {
					document.body.style.cursor="";
					location.href='editor.shtml?model=${model}&uri=' + encodeURIComponent(dataFromServer);
				},
				errorHandler:function(errorString, exception) { }
			});
		}
		function show_create_new(element) {
			for (i in element.childNodes)
				if (element.childNodes[i].className == 'createNewInstance')
					element.childNodes[i].style.display = 'inline';
		}
		function hide_create_new(element) {
			for (i in element.childNodes)
				if (element.childNodes[i].className == 'createNewInstance')
					element.childNodes[i].style.display = 'none';
		}
	</script>
</head>

<body class="tundra" onmouseover="if (typeof(dijit)!='undefined' && typeof(dijit.showTooltip)!='undefined') dijit.showTooltip('',this)">

[@searchBar model lang/]

<ul id="type_hierarchy">
[#list rootClasses?sort as rootClass]
	[@renderClass rootClass type/]
[/#list]
</ul>

<div style="float:left;margin:5px;padding:5px;min-width:500px;">
[#if result?exists]
	<div style="font-size:18pt;margin-top:0.8em;">
		${typeResource.label} (${result.size})
	</div>
	<div style="margin-bottom:1.2em;">
		<a href="javascript:create_new_instance('${model}','${typeResource.uri}')" style="color:forestgreen;">[create a new instance]</a>
	</div>
	[#if result.size > pageSize]
		<div style="color:black">Page</div>
		<div style="max-width:500px;font-family:monospace;font-size:9pt;margin-left:3px;">
		[#list 0..(result.size/pageSize) as i]
			[#if from != i*pageSize]
				[#if i<9]&nbsp;[/#if]<a href="index.shtml?type=${type?url}&from=${(i*pageSize)?c}&to=${((i*pageSize)+pageSize)?c}&model=${model?url}" style="color:#333;">${i+1}</a>
		 	[#else]
		 		[#if i<9]&nbsp;[/#if]<strong style="color:crimson">${i+1}</strong>
		 	[/#if] 
			[#if i_has_next] | [/#if]
		[/#list]
		</div>
	[/#if]
	[#list result.iterator() as instance]
		<div id="instance_list">
			<a href="resource.shtml?uri=${instance.uri?url}&model=${model?url}" onMouseOver="showResourceTooltip(this,'${instance.uri}')" 
					 onMouseOut="hideResourceTooltip(this)">
				[#assign label=instance.label]
				[#if label?length > 70]
					${label?substring(0,67)}...
				[#else]
					${label}
				[/#if]
			</a>
		</div>
	[/#list]
[#else]
	<div style="margin-top:150px;">
		<span style="font-size:16pt;color:white">&larr;</span>
		<span style="font-size:72pt;font-weight:bold;color:grey;">SAHA3</span><br/>
		<span style="font-size:16pt;color:white">&larr;</span>
		<span style="font-size:72pt;font-weight:bold;color:grey;">${model?upper_case}</span>
	</div>
	<div style="font-size:16pt;">&larr; select type</div>
[/#if]
</div>

[#macro renderClass class type]
	<li>
		<div onMouseOver="javascript:show_create_new(this)" onMouseOut="javascript:hide_create_new(this)">
		[#if type == class.uri]
			<strong>${class.label}</strong>
		[#else]
			<a href="index.shtml?type=${class.uri?url}&model=${model?url}">${class.label}</a>
		[/#if]
		[#if class.instanceCount > 0](${class.instanceCount})[/#if]
		<a class="createNewInstance" href="javascript:create_new_instance('${model}','${class.uri}')" style="color:forestgreen;display:none;">
			[create new]
		</a>
		</div>
		[#if class.children?size > 0]
		<ul>
			[#list class.children as childClass][@renderClass childClass type/][/#list]
		</ul>
		[/#if]
	</li>
[/#macro]

</body>
</html>