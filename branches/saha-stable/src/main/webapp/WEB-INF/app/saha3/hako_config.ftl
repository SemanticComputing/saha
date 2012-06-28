[#ftl]
[#setting url_escaping_charset='UTF-8']

<html>
<head>
	<title>HAKO - ${model}</title>
	<script type='text/javascript' src='../dwr/interface/ResourceConfigService.js'></script>
	<script type='text/javascript' src='../dwr/engine.js'></script> 
	
	<style>
		body {
			margin: 0px;
			padding: 0px;
		}
		div#main_container {
			margin: 10px;
		}
		div.label {
			font-size: x-large;
			margin-bottom: 0px;
		}
		div.uri {
			font-size: x-small;
			font-family: sans-serif;
			color: gray;
		}
		div#header {
			width: 100%;
			padding: 5px;
			background-color: #979735;
			border-bottom: thick solid #cc9;
			font-family: sans-serif;
			font-weight: bold;
			margin-bottom: 1.3em;
		}
		div#header a {
			text-decoration: none;
			color: #ffffef;
		}
		.list_title {
			list-style-type: none;
			font-size: 120%;
			color: #555;
			font-weight: bold;
			padding-bottom: 5px;
			padding-top: 10px;
		}
		.result_list div {
			padding: 5px;
			padding-left: 15px;
		}
		.result_list h1 {
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
		.result_list div.cell:hover {
			background-color: #ffc !important;
		}
		.category {
			padding-left: 0.8em;
			margin-left: 0.8em;
			margin-bottom: 10px;
		}
	</style>
</head>
<body>
	<div id="header"><a href="hako.shtml">HAKO</a></div>
	
	<div id="main_container" style="margin-left:13px;">
		<h1>Configure project</h1>
		<a href="javascript:ResourceConfigService.destroyHako('${model}');location.href='hako.shtml'" style="color:grey">reset hako</a> | 
		<a href="javascript:location.href='hako.shtml'" style="color:black">start hako</a>
		<div style="float:left;padding:10px;border:thin solid #ccc;">
			<h2>Instances</h2>
			[#list types.iterator() as type]
				[#assign label=type.label]
				[#if label?length > 70]
				<div>
					<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_type_${type_index}" 
					onClick="javascript:ResourceConfigService.addTypeToHakoConfig('${model}','${type.uri}')">
					${label?substring(0,67)}...
				</div>
				[#else]
				<div>
					<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_type_${type_index}" 
					onClick="javascript:ResourceConfigService.addTypeToHakoConfig('${model}','${type.uri}')">
					${label}
				</div>
				[/#if]
			[/#list]
		</div>
		<div style="float:left;padding:10px;border:thin solid #ccc;">
			<h2>Facets</h2>
			[#list properties.iterator() as property]
				[#assign label=property.label]
				[#if label?length > 70]
				<div>
					<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_property_${property_index}" 
					onClick="javascript:ResourceConfigService.addPropertyToHakoConfig('${model}','${property.uri}')">
					${label?substring(0,67)}...
				</div>
				[#else]
				<div>
					<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_property_${property_index}" 
					onClick="javascript:ResourceConfigService.addPropertyToHakoConfig('${model}','${property.uri}')">
					${label}
				</div>
				[/#if]
			[/#list]
		</div>
	</div>
</body>
</html>

