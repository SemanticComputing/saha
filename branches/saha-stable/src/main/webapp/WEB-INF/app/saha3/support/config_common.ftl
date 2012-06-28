[#ftl]
[#setting url_escaping_charset='UTF-8']

[#macro configScripts model instance]
	<script>
		function init() {
			dojo.subscribe("/dnd/drop", function(source,nodes,copy,target) {
				recolorPropertiesTable();
				savePropertyOrder();
			});
		}
		function recolorPropertiesTable() {
			var tableRows = dojo.byId("propertiesTable").rows;
			for (var i=0; i<tableRows.length; i++)
				if (i % 2 == 0)
					tableRows[i].className = "even dojoDndItem";
				else
					tableRows[i].className = "odd dojoDndItem";
		}
		function savePropertyOrder() {
			var tableRows = dojo.byId("propertiesTable").rows;
			var propertyUris = new Array();
			for (var i=0; i<tableRows.length; i++)
				propertyUris[i] = tableRows[i].id;
			ResourceConfigService.setPropertyOrder(
				'${model}',
				'[#if instance.types?size > 0]${instance.types?first.uri}[/#if]',
				propertyUris);
		}
		function submit_repository_config(id,model,propertyUri) {
			var ontologyName = dijit.byId("ontology_" + id).attr('value');
			var parentRestrictions = dijit.byId("parent_" + id).attr('value').split(';');
			var typeRestrictions = dijit.byId("type_" + id).attr('value').split(';');
			if (ontologyName.length > 0) {
				ResourceConfigService.addRepositoryConfig(model,id,propertyUri,ontologyName,parentRestrictions,typeRestrictions, {
					callback:function(dataFromServer) {
						var newElement = document.createElement('div');
						newElement.innerHTML = dataFromServer;
						dijit.byId("ontology_" + id).attr('value','');
						dijit.byId("parent_" + id).attr('value','');
						dijit.byId("type_" + id).attr('value','');
						document.body.style.cursor="";
						dojo.byId("results_" + id).appendChild(newElement);
					}
				});
			}
		}
		function remove_repository_config(id,model,propertyUri,sourceName) {
			document.body.style.cursor="progress";
			ResourceConfigService.removeRepositoryConfig(model,propertyUri,sourceName, {
				callback:function(dataFromServer) {
					if (dataFromServer == true)
						dojo.byId(id).style.textDecoration = 'line-through';
					document.body.style.cursor="";
				}
			});
		}
		dojo.addOnLoad(init);
	</script>
[/#macro]

[#macro literalPropertyConfigurator id model propertyUri propertyConfig]
	<div style="margin-top:5px">
		<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_${id}"
		onClick="javascript:ResourceConfigService.toggleLocalized('${model}','${propertyUri}')" 
			[#if propertyConfig.localized]checked[/#if]>
		<label for="checkbox_${id}">localized <span style="color:grey">(display lang)</span></label>
		<input type="checkbox" dojoType="dijit.form.CheckBox" id="checkbox_pic_${id}"
		onClick="javascript:ResourceConfigService.togglePictureProperty('${model}','${propertyUri}')" 
			[#if propertyConfig.pictureProperty]checked[/#if]>
		<label for="checkbox_pic_${id}">picture property</label>
	</div>
[/#macro]

[#macro objectPropertyConfigurator id model propertyUri propertyConfig]
	<div style="margin-top:5px;margin-bottom:5px;">
		<input type="checkbox" dojoType="dijit.form.CheckBox" id="i_checkbox_${id}" 
			onClick="javascript:ResourceConfigService.toggleDenyInstantiation('${model}','${propertyUri}')" 
			[#if propertyConfig.denyInstantiation]checked[/#if]>
		<label for="i_checkbox_${id}">deny instantiation</label>
	</div>
	<div style="margin-top:5px;margin-bottom:10px;">
		<input type="checkbox" dojoType="dijit.form.CheckBox" id="l_checkbox_${id}" 
			onClick="javascript:ResourceConfigService.toggleDenyLocalReferences('${model}','${propertyUri}')" 
			[#if propertyConfig.denyLocalReferences]checked[/#if]>
		<label for="l_checkbox_${id}">deny local references</label>
	</div>
	<div>connect to external ontology</div>
	<table style="width:300px;border:thin solid #ccc;background-color:#ddd;border-collapse:collapse;"><tr>
		<td style="padding:3px;">
			ontology name<textarea id="ontology_${id}"></textarea>
			parent restrictions (URIs separated by ';')<textarea id="parent_${id}"></textarea>
			type restrictions (URIs separated by ';')<textarea id="type_${id}"></textarea>
		</td>
		<td style="border-left:thin dotted grey;">
			<button id="button_${id}"></button>
		</td>
	</tr></table>
	<script type="text/javascript" class="inline_script">
		function submit_repository_config${id}() { 
			submit_repository_config('${id}','${model}','${propertyUri}');
		}
		dojo.addOnLoad(function() {
			new dijit.form.TextBox({
				name: "ontology_${id}",
				style: "width:262px;"
			},"ontology_${id}");
			new dijit.form.TextBox({
				name: "parent_${id}",
				style: "width:262px;"
			},"parent_${id}");
			new dijit.form.TextBox({
				name: "type_${id}",
				style: "width:262px;"
			},"type_${id}");
			new dijit.form.Button({
				label: "add",
				onClick: submit_repository_config${id},
				style: "margin:4px;"
			},"button_${id}");
		});
	</script>
[/#macro]

[#macro repositoryConfigValue id model propertyUri repositoryConfig]
	<div id="value_${id}">
		<a href="javascript:remove_repository_config('value_${id}','${model}',
		'${propertyUri}','${repositoryConfig.sourceName}');" style="color:crimson">
		[remove]</a> 
		source: <strong>${repositoryConfig.sourceName}</strong>
		<div>
			[#list repositoryConfig.parentRestrictions as parent]
				${parent}[#if parent_has_next], [/#if]
			[/#list]
		</div><div>
			[#list repositoryConfig.typeRestrictions as type]
				${type}[#if type_has_next], [/#if]
			[/#list]
		</div>
	</div>
[/#macro]
