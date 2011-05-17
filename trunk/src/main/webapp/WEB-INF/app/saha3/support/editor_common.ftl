[#ftl]
[#setting url_escaping_charset='UTF-8']

[#macro editorScripts]
	<script>
		function submit_date(id,model,resourceUri,propertyUri) {
			var input = dijit.byId("input_" + id);
			var value = input.attr('value');
			var dateValue = dojo.date.locale.format(value,{selector:'date',datePattern:'yyyy-MM-dd'});
			var timeValue = dojo.date.locale.format(value,{selector:'time',timePattern:'HH:mm:ss'});
			var dateTime = dateValue + "T" + timeValue;
			submit_literal_property(id,model,resourceUri,propertyUri,dateTime,null);
			input.attr('value','');
			input.attr('displayedValue','');
		}
		function submit_text(id,model,resourceUri,propertyUri) {
			var inputValue = dijit.byId("textarea_" + id);
			var value = inputValue.attr('value');
			var inputLang = dijit.byId("lang_" + id);
			var lang = null;
			if (inputLang != null) lang = inputLang.attr('value');
			submit_literal_property(id,model,resourceUri,propertyUri,value,lang);
			inputValue.attr('value','');
		}
		function submit_literal_property(id,model,resourceUri,propertyUri,value,lang) {
			if (value.length > 0) {
				document.body.style.cursor="progress";
				newChildId = dojo.byId("results_" + id).childNodes.length + 1;
				ResourceEditService.setLiteralProperty(model,id + "_" + newChildId,resourceUri,propertyUri,value,lang, {
					callback:function(dataFromServer) {
						var newElement = document.createElement('div');
						newElement.innerHTML = dataFromServer;
						document.body.style.cursor="";
						dojo.byId("results_" + id).appendChild(newElement);
					}
				});
			}
		}
		function get_literal_editor(id,model,resourceUri,propertyUri,valueShaHex) {
			document.body.style.cursor="progress";

			ResourceEditService.getLiteralPropertyEditor(model,id,resourceUri,propertyUri,valueShaHex, {
				callback:function(dataFromServer) {
					var editor = dojo.byId(id);					
					editor.innerHTML = "";
					
					var newElement = document.createElement('div');
					editor.appendChild(newElement);
					
					newElement.innerHTML = dataFromServer;
					
					// Execute new scripts
					
					var newScripts = newElement.getElementsByTagName("script");   
   					for(var i=0 ; i<newScripts.length ; i++)  
   					{  
       					eval(newScripts[i].text);  
   					}  
   					
					document.body.style.cursor="";
				}
			});
		}
		function update_text(id,model,resourceUri,propertyUri,oldValueShaHex) {
			var inputValue = dijit.byId("textarea_" + id);
			var value = inputValue.attr('value');
			var inputLang = dijit.byId("lang_" + id);
			var lang = null;
			if (inputLang != null) lang = inputLang.attr('value');
			update_literal_property(id,model,resourceUri,propertyUri,value,lang,oldValueShaHex);
			inputValue.attr('value','');
		}
		function update_literal_property(id,model,resourceUri,propertyUri,value,lang,oldValueShaHex) {
			if (value.length > 0) {
				document.body.style.cursor="progress";
				ResourceEditService.updateLiteralProperty(model,id,resourceUri,propertyUri,value,lang,oldValueShaHex, {
					callback:function(dataFromServer) {
						dojo.byId(id).innerHTML = dataFromServer;
						document.body.style.cursor="";
					}
				});
			}
		}
		function submit_object_property(id,model,resourceUri,propertyUri,valueUri) {
			if (valueUri.length > 0) {
				document.body.style.cursor="progress";
				newChildId = dojo.byId("results_" + id).childNodes.length + 1;
				ResourceEditService.setObjectProperty(model,id + "_" + newChildId,resourceUri,propertyUri,valueUri, {
					callback:function(dataFromServer) {
						var newElement = document.createElement('div');
						newElement.innerHTML = dataFromServer;
						document.body.style.cursor="";
						dojo.byId("results_" + id).appendChild(newElement);
					}
				});
			}
		}
		function create_inline_instance(id,model,resourceUri,propertyUri,type,label,showInlineEditor) {
			document.body.style.cursor="progress";
			ResourceEditService.createInstance(model,type,label, {
				callback:function(dataFromServer) {
					document.body.style.cursor="";
					submit_object_property(id,model,resourceUri,propertyUri,dataFromServer);
					if (showInlineEditor) render_inline_editor(id,model,dataFromServer);
				}
			});
		}
		function render_inline_editor(id,model,resourceUri) {
			document.body.style.cursor="progress";
			ResourceEditService.getEditorPropertyTable(model,resourceUri,'inline_' + 
				(dojo.byId("results_" + id).childNodes.length+1) + '_' +id, 
			{
				callback:function(dataFromServer) {
					var editorContainer = dijit.byId('container_'+id);
					if (editorContainer == null)
						editorContainer = new dijit.layout.ContentPane({id:"container_"+id},"editor_"+id);
						
						
					editorContainer.innerHTML = "";
					
					var newElement = document.createElement('div');
					newElement.style.border = "thin solid black";
					newElement.style.marginTop = "5px";
					editorContainer.domNode.appendChild(newElement);
					
					newElement.innerHTML = build_close_link(id)+dataFromServer;
					
					// Execute new scripts
					
					/*
					var newScripts = newElement.getElementsByTagName("script");   
   					for(var i=0 ; i<newScripts.length ; i++)  
   					{  
       					window.alert(eval(newScripts[i].text));
       					window.alert(newScripts[i].text);
   					} */ 
   					
					document.body.style.cursor="";
					
					//editorContainer.attr('content',
					//	'<div style="">'+build_close_link(id)+dataFromServer+'</div>');
					load_inline_editor_scripts(editorContainer);					
					document.body.style.cursor="";
				}
			});
		}
		function build_close_link(id) {
			return '<div style="background-color:white;padding:5px;">' + 
			'<a href="javascript:close_inline_editor(\''+id+'\')">[close]</a></div>';
		}
		function close_inline_editor(id) {
			var editorContainer = dijit.byId('container_'+id);
			editorContainer.destroyDescendants();
		}
		function load_inline_editor_scripts(editor) {
			/*
			var scripts = editor.domNode.getElementsByTagName("script");
						   
   			for(var i=0 ; i < scripts.length ; i++)  
   			{  
       			eval(scripts[i].text);  
   			}  
   			*/
   			
			var scripts = editor.domNode.getElementsByTagName("script");
						   
   			for(var i=0 ; i < scripts.length ; i++)  
   			{ 
			var head = document.getElementsByTagName('head')[0];
			for (i=0;i<scripts.length;i++) {
				var script = document.createElement('script');
				script.type = 'text/javascript';
				script.innerHTML = scripts[i].innerHTML;
				head.appendChild(script);
			}
			}
		}
		function remove_literal_property(id,model,resourceUri,propertyUri,valueShaHex) {
			document.body.style.cursor="progress";
			ResourceEditService.removeLiteralProperty(model,resourceUri,propertyUri,valueShaHex, {
				callback:function(dataFromServer) {
					if (dataFromServer == true)
						dojo.byId(id).style.textDecoration = 'line-through';
					document.body.style.cursor="";
				}
			});
		}
		function remove_object_property(id,model,resourceUri,propertyUri,valueUri) {
			document.body.style.cursor="progress";
			ResourceEditService.removeObjectProperty(model,resourceUri,propertyUri,valueUri, {
				callback:function(dataFromServer) {
					if (dataFromServer == true)
						dojo.byId(id).style.textDecoration = 'line-through';
					document.body.style.cursor="";
				}
			});
		}
		function remove_resource(model,uri,label) {
			if (confirm('Remove \"' + label + '\"?')) {
				document.body.style.cursor="progress";
				ResourceEditService.removeResource(model,uri, {
					callback:function(dataFromServer) {
						document.body.style.cursor="";
						location.href='index.shtml';
					}
				});
			} else {
				console.log('wussy!');
			}
		}
	</script>
[/#macro]

[#macro editorProperties model instance editorId]
	<table class="properties" id="editor_properties_${editorId}">
	[#list instance.editorPropertyMapEntrySet as entry]
		[#if entry.value?size > 0]
		[#assign property = entry.value?first]
		[#if !property.config.hidden]
			<tr class="[#if entry_index % 2 == 0]even[#else]odd[/#if]">
				<td class="key">
					<a href="resource.shtml?uri=${entry.key.uri?url}" style="text-decoration:none">
						${entry.key.label}
					</a>
					<div style="color:#666;font-weight:normal;font-size:90%;">${property.comment}</div>
				</td>
				<td class="value">
					[#if property.literal]
						[@literalPropertyEditor id=editorId+"_"+entry_index model=model 
							resourceUri=instance.uri property=property inline=false/]
					[#else]
						[@objectPropertyEditor id=editorId+"_"+entry_index model=model 
							resourceUri=instance.uri property=property/]
					[/#if]
					<div id="results_${editorId}_${entry_index}" style="margin-top:6px;margin-bottom:4px;">
					[#list entry.value as property]
						[#if property.literal]
							[@literalPropertyValue id=editorId+"_"+entry_index+"_"+property_index model=model 
								resourceUri=instance.uri 
								propertyUri=property.uri 
								propertyValueLang=property.valueLang
								propertyValueLabel=property.valueLabel 
								propertyValueShaHex=property.valueShaHex/]
						[#else]
							[@objectPropertyValue id=editorId+"_"+entry_index+"_"+property_index model=model 
								resourceUri=instance.uri 
								propertyUri=property.uri 
								propertyValueLabel=property.valueLabel
								propertyValueUri=property.valueUri/]
						[/#if]
					[/#list]
					</div>
				</td>
			</tr>
		[/#if]
		[/#if]
	[/#list]
	</table>
[/#macro]

[#macro literalPropertyEditor id model resourceUri property inline]
	[#assign isPicture = property.config.pictureProperty /]
	[#if !inline && !isPicture]<div style="color:#888;">add new literal</div>[/#if]
	[#if !inline && isPicture]
		<form action="http://media.onki.fi/upload" enctype="multipart/form-data" method="post">
			<div style="color:#888;margin-bottom:5px;">upload file</div>
			<input name="image" type="file">
			<input name="target" type="hidden" value="${resourceUri}"/>
			<input name="context" type="hidden" value="saha/${model}"/>
			<input name="property" type="hidden" value="${property.uri}"/>
			<input type="submit" value="Upload"/>
		</form>
	[#elseif !inline && isDate(property)]
		<input id="input_${id}">
		<button id="button_${id}"></button>
		<script type="text/javascript" class="inline_script">
			function submit_date_${id}() { submit_date('${id}','${model}','${resourceUri}','${property.uri}','${inline?string}'); }
			dojo.addOnLoad(function() { 
			new dijit.form.DateTextBox({},"input_${id}");
			new dijit.form.Button({
					label: "add",
					onClick: submit_date_${id},
				},"button_${id}");});
		</script>
	[#else]
		<table><tr>
			[#if property.config.localized || property.valueLang?length > 0]
			<td><textarea id="lang_${id}"></textarea></td>
			[/#if]
			<td><textarea id="textarea_${id}"></textarea></td>
			<td><button id="button_${id}"></button></td>
		</tr></table>
		<script type="text/javascript" class="inline_script">
			[#if !inline]
			function submit_text_${id}() { submit_text('${id}','${model}','${resourceUri}','${property.uri}'); }
			[#else]
			function submit_text_${id}() { update_text('${id}','${model}','${resourceUri}','${property.uri}','${property.valueShaHex}'); }
			[/#if]
			[#if property.config.localized || property.valueLang?length > 0]
			dojo.addOnLoad(function() { new dijit.form.TextBox({
				name: "lang_${id}",
				[#if !inline && lang?exists]value:"${lang}",[/#if]
				[#if inline]value:"${property.valueLang}",[/#if]
				style: "width:20px;color:grey;"
			},"lang_${id}");});
			[/#if]
			
			dojo.addOnLoad( function() { new dijit.form.Textarea({
					name: "textarea_${id}",
					style: "width:262px;",
					[#if inline]value: "${property.valueLabel?js_string}",[/#if]
					wrap: "true"
				},"textarea_${id}");
				new dijit.form.Button({
					label: "add",
					onClick: submit_text_${id},
					style: "margin:4px;"
				},"button_${id}");});
		</script>
	[/#if]
[/#macro]

[#function isDate property]
	[#assign datatype = property.valueDatatypeUri]
	[#if datatype?length == 0 && property.range?size > 0]
		[#assign datatype = property.range?first]
	[/#if]
	[#if datatype == "http://www.w3.org/2001/XMLSchema#dateTime"][#return true]
	[#elseif datatype == "http://www.w3.org/2001/XMLSchema#date"][#return true]
	[#elseif datatype == "http://purl.org/dc/terms/ISO8601"][#return true]
	[#elseif datatype == "http://purl.org/dc/terms/W3CDTF"][#return true][/#if]
	[#return false]
[/#function]

[#macro objectPropertyEditor id model resourceUri property]
	<div style="color:#888;">select reference 
		[#if property.range?size == 0]<span style="color:forestgreen">(range unknown)</span>[/#if]
	</div>
	<div id="select_${id}"></div>
	<div id="editor_${id}"></div>
	<script type="text/javascript" class="inline_script">
		function submit_${id}(valueUri) {
			submit_object_property("${id}","${model}","${resourceUri}","${property.uri}",valueUri);
			dijit.byId("combo_${id}").attr('value',null);
		}
		var displayedValue_${id} = '';
		function create_${id}(showInlineEditor,type) {
			create_inline_instance("${id}","${model}","${resourceUri}","${property.uri}",
				type,displayedValue_${id},showInlineEditor);
		}
		function askForNewInstance_${id}(msg) {
			if (msg.length > 0) showNewInstanceTooltip_${id}();
		}
		function showNewInstanceTooltip_${id}() {
			displayedValue_${id} = dijit.byId('combo_${id}').attr('displayedValue');
			[#if property.range?size > 1]
				dijit.showTooltip('create a new instance<br/>' + 
				[#list property.rangeTree as rangeNode]
					[@renderRangeNode id=id rangeNode=rangeNode indent=''/]
					[#if rangeNode_has_next]+[/#if]
				[/#list]
				,dijit.byId("combo_${id}").domNode);
			[#else]
				[#assign type='']
				[#if property.range?size==1][#assign type=property.range?first][/#if]
				dijit.showTooltip('create a new instance ' + 
					'<a href="javascript:create_${id}(false,\'${type}\')" style="color:green">quick</a> | ' + 
					'<a href="javascript:create_${id}(true,\'${type}\')" style="color:green">inline</a>',
					dijit.byId("combo_${id}").domNode);
			[/#if]
		}
		function hideNewInstanceTooltip_${id}() {
			dijit.hideTooltip(dijit.byId("combo_${id}").domNode);
		}
		dojo.addOnLoad(function() { var combo_${id} = new dijit.form.FilteringSelect(
			{
				store:new dojox.data.QueryReadStore({
					url:"../service/instance_search/?model=${model}&resource=${resourceUri?url}&property=${property.uri?url}[#list property.range as r]&r=${r?url}[/#list]",
					requestMethod:"post"
				}),				
				autoComplete:false,
				labelAttr:"name",
				onChange:submit_${id},
				[#if !property.config.denyInstantiation]
				displayMessage:askForNewInstance_${id},
				onKeyUp:showNewInstanceTooltip_${id},
				onFocus:showNewInstanceTooltip_${id},
				onBlur:hideNewInstanceTooltip_${id},
				[/#if]
				style:"width:300px;",
				searchDelay:300,
				labelType:"html",
				hasDownArrow:true,
				id:"combo_${id}"
			},
			dojo.byId("select_${id}"));});
	</script>
[/#macro]

[#macro renderRangeNode id rangeNode indent]
	'${indent}' + '<strong>${rangeNode.label}</strong>: ' + 
	'<a href="javascript:create_${id}(false,\'${rangeNode.uri}\')" style="color:green">quick</a> | ' + 
	'<a href="javascript:create_${id}(true,\'${rangeNode.uri}\')" style="color:green">inline</a><br/>'
	[#assign children=rangeNode.children]
	[#if children?size > 0]
		+
		[#list rangeNode.children as child]
			[@renderRangeNode id=id rangeNode=child indent=indent+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'/]
			[#if child_has_next]+[/#if]
		[/#list]
	[/#if]
[/#macro]

[#macro literalPropertyValue id model resourceUri propertyUri propertyValueLang propertyValueLabel propertyValueShaHex]
	[#if propertyValueLabel?length > 0]
		<div id="value_${id}">
			[#assign label=propertyValueLabel]
			[#if label?ends_with(".jpg") || label?ends_with(".jpeg") || label?ends_with(".png") || label?ends_with(".gif")]
				<div style="margin:5px;">
				[#if label?starts_with("http://") && !label?starts_with("http://demo.seco.tkk.fi/")]
					<a href="${label}" style="color:darkblue">
					<img src="${label}" style="max-width:60px;max-height:60px;margin-right:5px;
						border:thin solid black;"/></a>
				[#else]
					<a href="../service/pics/?name=${label}&model=${model}" style="color:darkblue">
					<img src="../service/pics/?name=${label}&model=${model}" 
						style="max-width:60px;max-height:60px;margin-right:5px;border:thin solid black;"/></a>
				[/#if]
				</div>						
			[/#if]
			<a href="javascript:remove_literal_property('value_${id}','${model}','${resourceUri?url}',
			'${propertyUri}','${propertyValueShaHex}');" style="color:crimson">
			[remove]</a>
			[#if propertyValueLang?length > 0]<span style="color:grey">(${propertyValueLang})</span>[/#if]
			<span style="white-space:pre-wrap;"><a href="javascript:get_literal_editor('value_${id}','${model}','${resourceUri?url}',
			'${propertyUri}','${propertyValueShaHex}');" style="color:black;text-decoration:none;">${propertyValueLabel?html}</a></span>
		</div>
	[/#if]
[/#macro]

[#macro objectPropertyValue id model resourceUri propertyUri propertyValueLabel propertyValueUri]
	[#if propertyValueLabel?length > 0]
		<div id="value_${id}">
			<a href="javascript:remove_object_property('value_${id}','${model}','${resourceUri?url}',
			'${propertyUri}','${propertyValueUri?url}');" style="color:crimson">
			[remove]</a> 
			<a href="resource.shtml?uri=${propertyValueUri?url}" 
			 onMouseOver="showResourceTooltip(this,'${propertyValueUri}')" 
			 onMouseOut="hideResourceTooltip(this)">${propertyValueLabel}</a>
			<a href="javascript:render_inline_editor('${id}','${model}','${propertyValueUri?url}')" style="color:grey">[edit]</a>
		</div>
		<div id="editor_${id}"></div>
		<div id="results_${id}"></div>
	[/#if]
[/#macro]