[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]
[#include "support/config_common.ftl"]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model} - CONFIGURE</title>
	[@header model/]
	[@configScripts model instance/]
</head>

<body class="tundra">

	[@searchBar model lang/]
	
	<div style="margin:5px;">
		${uri}
		<br/>
		<div style="font-size:18pt;margin-top:1em;margin-left:5px;">
			<strong>Configuring instances of <em>[#list instance.types as type]${type.label}[#if type_has_next], [/#if][/#list]</em></strong>
		</div>
		<div style="margin-bottom:10px;margin-left:5px;">
			return links: 
			<a href="resource.shtml?uri=${uri?url}&model=${model?url}" style="color:black">[view <em>${instance.label}</em>]</a>
			<a href="editor.shtml?uri=${uri?url}&model=${model?url}" style="color:black">[edit <em>${instance.label}</em>]</a>
		</div>
		
		<div style="margin-bottom:2px;margin-left:5px;color:grey;">
			Sort table rows using drag-and-drop.
		</div>
		<table dojoType="dojo.dnd.Source" class="properties" id="propertiesTable" skipForm="true">
		[#list instance.editorPropertyMapEntrySet as entry]
			[#if entry.value?size > 0]
				[#assign property = entry.value?first]
				<tr class="dojoDndItem [#if entry_index % 2 == 0]even[#else]odd[/#if]" id="${entry.key.uri}">
					<td class="key">
						<a href="resource.shtml?uri=${entry.key.uri?url}&model=${model?url}" style="text-decoration:none">
							${entry.key.label}
						</a>
						<div style="color:crimson;font-weight:normal;margin:1px;">
							<input type="checkbox" dojoType="dijit.form.CheckBox" id="hide_${entry_index}" 
							onClick="javascript:ResourceConfigService.toggleHidden('${model}','${property.uri}')" 
							[#if property.config.hidden]checked[/#if]><label 
							for="hide_${entry_index}">hide property</label>
						</div>
					</td>
					<td class="value">
						[#if property.literal]
							[@literalPropertyConfigurator id=entry_index model=model 
								propertyUri=property.uri propertyConfig=property.config/]
						[#else]
							[@objectPropertyConfigurator id=entry_index model=model 
								propertyUri=property.uri propertyConfig=property.config/]
						[/#if]
						<div id="results_${entry_index}" style="margin-top:6px;margin-bottom:4px;">
						[#if !property.literal]
							[#list property.config.repositoryConfigs as repositoryConfig]
								[@repositoryConfigValue id="config_"+entry_index+"_"+repositoryConfig_index model=model 
									propertyUri=property.uri repositoryConfig=repositoryConfig/]
							[/#list]
						[/#if]
						</div>
					</td>
				</tr>
			[/#if]
		[/#list]
		</table>
		<br/>
		[#if instance.types?size > 0]
		[#assign instanceType = instance.types?first]
		<div style="padding:5px">
			<strong>
				Add a Property to the Domain of <em>${instanceType.label}</em>
			</strong>
			<span style="color:grey">(reloads page)</span>
			<br/>
			<div id="property_select"></div>
			<script>
				var rdfsDomain = 'http://www.w3.org/2000/01/rdf-schema#domain';
				function sumbit_domain_config(propertyUri) {
					dijit.byId("combo").attr('value',null);
					if (propertyUri.length > 0) {
						document.body.style.cursor="progress";
						ResourceEditService.setObjectProperty('${model}','',propertyUri,rdfsDomain,'${instanceType.uri?js_string}', {
							callback:function(dataFromServer) {
								document.body.style.cursor="";
								location.href='config.shtml?model=${model?url}&uri=' + encodeURIComponent('${uri}');
							}
						});
					}
				}
				var objectProperty = encodeURIComponent('http://www.w3.org/2002/07/owl#ObjectProperty');
				var datatypeProperty = encodeURIComponent('http://www.w3.org/2002/07/owl#DatatypeProperty');
				
				
				dojo.addOnLoad(function() {
				var combo = new dijit.form.FilteringSelect(
				{
					store:new dojox.data.QueryReadStore({
						url:"../service/instance_search/?model=${model}&type="+objectProperty+"&type="+datatypeProperty,
						requestMethod:"post"
					}),
					autoComplete:false,
					labelAttr:"name",
					onChange:sumbit_domain_config,
					style:"width:300px;",
					searchDelay:300,
					labelType:"html",
					id:"combo"
				},	
				dojo.byId("property_select"));
				});
			</script>
		</div>
		[/#if]
		<br/>
	</div>
</body>
</html>
