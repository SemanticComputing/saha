[#ftl]
[#setting url_escaping_charset='UTF-8']

<html>
<head>
	<title>HAKO - ${model}</title>
	<script type='text/javascript' src='../dwr/interface/ResourceConfigService.js'></script>
	<script type='text/javascript' src='../dwr/interface/ResourceEditService.js'></script>
	<script type='text/javascript' src='../dwr/engine.js'></script> 
	<style>
		body {
			margin: 0px;
			padding: 0px;
			font-family: Georgia, serif;
		}
		div#main_container {
			margin: 10px;
		}
		a {
			color:black;
		}
		div#header {
			padding-top:5px;
			padding-left: 10px;
			padding-bottom: 2px;
			background-color: #333;
			border-bottom: medium solid #ccc;
			margin-bottom: 1.3em;
			font-size: 10pt;
			position: relative;
		}
		div#header a {
			text-decoration: none;
			color: #eee;
		}
		.list_title {
			font-size: 14pt;
			color: #555;
			margin-top: 10px;
			margin-bottom: 5px;
		}
		.result_list div {
			padding: 5px;
			padding-left: 15px;
		}
		.result_list a {
			color: black;
			text-decoration: none;
		}
		.result_list a:hover {
			color: black;
		}
		.result_list .alt_label {
			color: grey;
		}
		.result_list a:hover .alt_label {
			color: #333;
		}
		ul.category {
			padding-left: 0.5em;
			margin-left: 0.5em;
			margin-bottom: 10px;
			list-style-type: none;
			position: relative;
		}
		ul.category li {
			border-bottom: thin dotted #ccc;
		}
		ul.category li a {
			color: black;
			text-decoration:none;
		}
		ul.category li span {
			position: absolute;
			right:0;
			color:#333;
		}
	</style>
	<script>
		function show_instance(uri, id) {
			var element = document.getElementById(id);
			var hasChild = false;
			for (i in element.childNodes) {
				var child = element.childNodes[i];
				if (child.nodeName == 'SPAN') {
					if (child.style.display == 'none')
						child.style.display = 'block';
					else 
						child.style.display = 'none';
					hasChild = true;
					break;
				}
			}
			if (!hasChild) {
				document.body.style.cursor="progress";
				ResourceEditService.getHakoPropertyTable('${model}',uri, {
					callback:function(dataFromServer) {
						var newElement = document.createElement('span');
						newElement.innerHTML = dataFromServer;
						element.appendChild(newElement);
						document.body.style.cursor="";
					}
				});
			}
		}
	</script>
</head>
<body>
	<div id="header">
		<a href="hako.shtml">Hako - Faceted Search Engine</a> 
		<script>
				function switchLang(lang) {
					if (window.location.href.indexOf('lang=')!=-1) window.location.href=window.location.href.replace(/lang=../,'lang='+lang);
					else if (window.location.href.indexOf('?')!=-1) window.location.href=window.location.href+('&lang='+lang);
					else window.location.href=window.location.href+('?lang='+lang);
				}
		</script>
		<div style="font-size:small;font-weight:normal;position:absolute;right:10px;bottom:3px;">		
		[#if lang!='fi']<a href="javascript:switchLang('fi')" style="color:white;">fi</a>[#else]<strong style="color:white">fi</strong>[/#if] | 
		[#if lang!='sv']<a href="javascript:switchLang('sv')" style="color:white;">sv</a>[#else]<strong style="color:white">sv</strong>[/#if] |
		[#if lang!='en']<a href="javascript:switchLang('en')" style="color:white;">en</a>[#else]<strong style="color:white">en</strong>[/#if] ||		
		<a href="javascript:ResourceConfigService.destroyHako('${model}');location.href='hako.shtml'">[reset HAKO]</a>
		<a href="index.shtml" style="margin-left: 30px">SAHA</a>
		</div>
	</div>
	
	<div id="main_container">
		<div style="margin-left:10px;margin-top:25px;">
			<div style="font-size:x-large;margin-bottom:4px;">
				<a href="hako.shtml" style="text-decoration:none;">${model?cap_first}</a>
			</div>
			<form method="get" style="margin:0;padding:0">
				[#list parameterMap?keys as parameterName]
					[#list parameterMap[parameterName] as parameterValue]
						<input type="hidden" name="${parameterName}" value="${parameterValue}"/>
					[/#list]
				[/#list]
				<input type="text" name="term" style="width:320px;" value="[#list terms as term]${term}[/#list]"/>
				<input type="submit" value="Search"/>
			</form>
		</div>
		
		<table style="width:100%;border-collapse:collapse;border:thin solid #ccc;margin-top:1em;">
		<tr>
			<td style="vertical-align:top;border-right:thin dashed #ccc;width:260px;background-color:#FFFFEF">
			<div style="margin:10px;font-size:10pt;">
				[#list categories?keys as categoryName]
					[#assign category=categories[categoryName]]
					[#if category?size > 0]
						<div class="list_title">${categoryName?cap_first}</div>
						<div>
							<ul class="category" style="margin:0;padding:0">
								[@renderCategory categories[categoryName]/]
							</ul>
						</div>
					[/#if]
				[/#list]
			</div>
			</td>
			<td style="vertical-align:top;padding:0;">
			<div class="result_list" style="position:relative;padding-top:5px;">
				<div style="position:absolute;padding:3px;right:0;top:0;color:#999;font-size:small;">Results ${result.size}</div>
				[#if selected?size > 0]
					[#list selected?values?sort as selected]
						<div>
							<a href="hako.shtml?${selected.backQuery}" style="color:grey">[remove]</a>
							<strong><a href="hako.shtml?${selected.propertyUri?url}=${selected.uri?url}" style="color:orangered">
							<em>${selected.label?cap_first}</em></a></strong>
						</div>
					[/#list]
					<div style="border-bottom:medium double #e0e0e0;padding-top:0px;"></div>
				[/#if]
				[#list result.iterator() as instance]
					<div id="c_${instance_index}" class="cell" style="border-bottom:thin solid #e0e0e0;">
						<a href="javascript:show_instance('${instance.uri}','c_${instance_index}');">
							[#assign label=instance.label]
							[#if label?length > 70]
								${label?substring(0,67)}...
								[#list instance.altLabels as altLabel]<br/><span class="alt_label">&rarr; ${altLabel}</span>[/#list]
							[#else]
								${label}
								[#list instance.altLabels as altLabel]<br/><span class="alt_label">&rarr; ${altLabel}</span>[/#list]
							[/#if]
						</a>
					</div>
				[/#list]
			</div>
			</td>
		</tr>
		</table>
	</div>
</body>
</html>

[#macro renderCategory categoryValues]
	[#list categoryValues as category]
		[#if category.itemCount > 0]
			<li>
				[#if !selected[category.uri]?exists]
					<a href="hako.shtml?${category.selectQuery}">${category.label?cap_first}</a>
					<span>${category.itemCount}</span>
				[#else]
					<a href="hako.shtml?${category.backQuery}"><strong style="color:orangered"><em>${category.label?cap_first}</em></strong></a>
					<span>${category.itemCount}</span>
				[/#if]
			</li>
		[/#if]
		[#if category.children?size > 0]
			<ul class="category">
				[@renderCategory category.children/]
			</ul>
		[/#if]
	[/#list]
[/#macro]
