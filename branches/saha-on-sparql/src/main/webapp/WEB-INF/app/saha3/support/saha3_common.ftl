[#ftl]
[#setting url_escaping_charset='UTF-8']

[#macro header model]
	<script type="text/javascript">
		djConfig = {
	        isDebug: false,
	        parseOnLoad: true,
	        usePlainJson: true
		};
	</script>


	<script type='text/javascript' src='../dwr/interface/ResourceEditService.js'></script>
	<script type='text/javascript' src='../dwr/interface/ResourceConfigService.js'></script>
	<script type='text/javascript' src='../dwr/interface/SahaChat.js'></script>
	<script type='text/javascript' src='../dwr/engine.js'></script> 
	<script type='text/javascript' src='../app/scripts/dwr_resources/util.js'></script> 
	<script type='text/javascript' src='../app/scripts/dojo.js'></script>


	<link rel="stylesheet" type="text/css" href="../app/css/dojo.css" />
	<link rel="stylesheet" type="text/css" href="../app/css/tundra.css" />
	
	<script type='text/javascript'>
	    dojo.require("dojo.parser");
	    dojo.require("dojo.date.locale");
		dojo.require("dijit.form.FilteringSelect");
		dojo.require("dijit.form.ComboBox");
		dojo.require("dijit.form.Textarea");
		dojo.require("dijit.form.CheckBox");
		dojo.require("dijit.form.RadioButton");
		dojo.require("dijit.form.DateTextBox");
		dojo.require("dijit.form.TextBox");
		dojo.require("dijit.form.Button");
		dojo.require("dijit.layout.ContentPane");
		dojo.require("dojo.dnd.Source");
		dojo.require("dojox.data.QueryReadStore");
	 </script>
	 
	 <script>	 
		function openInstance(uri) {
			if (uri.length > 0)
				location.href='resource.shtml?model=${model?url}&uri=' + encodeURIComponent(uri);
		}
		
		var timer;
		var chatIsRunning = false;
		var chatTimeout = 12000;
		var idleChatTimeout = 60000;
		
		function initChat() {
			var cookies = document.cookie.split(";");
			for (i in cookies) {
				var cookie = cookies[i];
				if (cookie.indexOf("sahaChatNick") == 0)
					dwr.util.setValue("name",cookie.substring(13));
			}
			if (!chatIsRunning) {
				chatIsRunning = true;
				getMessages();
			}
		}
		function sendMessage() {
			clearTimeout(timer);
			var name = dwr.util.getValue("name");
			document.cookie="sahaChatNick="+name+";";
			var text = dwr.util.getValue("text");
			SahaChat.addMessage("${model}",name,text);
			dwr.util.setValue("text","");
			getMessages();
		}
		function getMessages() {
			SahaChat.getMessages("${model}", {
				callback:function(messages) {
					var chatlog = "";
					for (i in messages) {
						message = messages[messages.length-i-1];
						chatlog += 
							"<div class=\"message\">" + 
								"<span class=\"time\">"+message.time+"</span> " + 
								"<span class=\"name\">"+message.name+"</span><br/>" + 
								"<span class=\"text\">"+message.message+"</span>" + 
							"</div>";
					}
					dwr.util.setValue("chatlog",chatlog,{escapeHtml:false});
					if (chatIsRunning) {
					  if (messages.length==0) timer = setTimeout("getMessages()",idleChatTimeout);					
					  else timer = setTimeout("getMessages()",chatTimeout);
					}
				},
				errorHandler:function(errorString, exception) { }	
			});
		}
		function showResourceTooltip(domNode,uri) {
			ResourceEditService.getPropertyTable('${model}',uri, {
				callback:function(dataFromServer) {
					if (typeof(dijit)!="undefined" && typeof(dijit.showTooltip)!='undefined') dijit.showTooltip(dataFromServer,domNode);
				},
				errorHandler:function(errorString, exception) { }
			});
		}
		function showExternalResourceTooltip(domNode,ontology,uri) {
			ResourceEditService.getExternalPropertyTable('${model}',ontology,uri, {
				callback:function(dataFromServer) {
					if (typeof(dijit)!="undefined" && typeof(dijit.showTooltip)!='undefined') dijit.showTooltip(dataFromServer,domNode);
				},
			errorHandler:function(errorString, exception) { }
			});
		}
		function hideResourceTooltip(domNode) {
			if (typeof(dijit)!="undefined" && typeof(dijit.showTooltip)!='undefined') dijit.hideTooltip(domNode);
		}
		
		dojo.addOnLoad(initChat);
	 </script>
	 
	 <style>
	 	/* index styles */
	 	body {
	 		font-size: 10pt;
	 	}
	 	ul#type_hierarchy {
			float:left;
			border:thin solid pink;
			margin:5px;
			padding:5px;
			padding-left:10px;
			min-width:260px;
			font-size:10pt;
			background-color:snow;
			list-style-type:none;
		}
		ul#type_hierarchy ul {
			margin-left:1em;
			padding-left:1em;
			font-size:10pt;
			background-color:snow;
			list-style-type:none;
		}
		ul#type_hierarchy li {
			background-color:snow;
		}
		ul#type_hierarchy a {
			color: crimson;
		}
		div#instance_list a {
			color: black;
		}
	 
	 	/* resource and editor styles */
	 	
	 	table.properties {
			margin:0;
			padding:0;
			border-collapse:collapse;
			border:thin solid pink;
			max-width:800px;
		}
		table.properties td.key {
			width: 130px;
			padding: 5px;
			font-weight: bold;
			padding-right: 10px;
			border-bottom:thin solid pink;
			border-right:thin solid pink;
			vertical-align:top;
		}
		table.properties td.value {
			min-width: 260px;
			padding: 5px;
			border-bottom:thin solid pink;
		}
		table.properties tr.even {
			background-color: snow;
		}
		table.properties tr.odd {
			background-color: white;
		}
		table.properties tr.even:hover, table.properties tr.odd:hover {
			background-color: #eee;
		}
		table.properties table.properties tr.even {
			background-color: snow;
		}
		table.properties table.properties tr.odd {
			background-color: white;
		}
		table.properties a {
			color: black;
		}
		
		#references {
			margin-left: 5px;
		}
		#references div {
			margin-bottom:2px;
		}
		#references a {
			color:black;
		}
		#references a.type {
			text-decoration:none;
		}
		#references .inv_prop {
			color: grey;
		}
		
		/* chat styles */
		
		#chatlog .message {
			background-color:white;
			border-top: thin solid #ddd;
			border-left: thin solid #ddd;
			border-right: thin solid #ddd;
			padding: 3px;
		}
		#chatlog .message {
			color: #444;
		}
		#chatlog .message .time {
			color: grey;
		}
		#chatlog .message .name {
			color: grey;
		}
	 </style>
[/#macro]

[#macro searchBar model lang]
	<div style="position:relative;margin:5px 5px 5px 5px;padding-bottom:20px;padding-top:5px;font-family:sans-serif;border-bottom:medium solid hotpink;font-size:10pt;">
		<strong>
			<a href="../saha3/main.shtml" style="color:black;">SAHA3</a> | 
		</strong><a href="index.shtml?model=${model?url}" style="color:black">${model}</a> - search 
		<div id="uber_search"></div>
		<div style="position:absolute;right:5px;top:5px;color:deeppink;">
			<script>
				function switchLang(lang) {
					if (window.location.href.indexOf('lang=')!=-1) window.location.href=window.location.href.replace(/lang=../,'lang='+lang);
					else if (window.location.href.indexOf('?')!=-1) window.location.href=window.location.href+('&lang='+lang);
					else window.location.href=window.location.href+('?lang='+lang);
				}
				
				function setAboutLink(element) {
					ResourceConfigService.setAboutLink('${model}', element.value);
					element.style.display = 'none';
					document.getElementById('about_link_id').href = element.value;
				}
			</script>
			[#if lang!='fi']<a href="javascript:switchLang('fi')" style="color:black;">fi</a>[#else]<strong>fi</strong>[/#if] | 
			[#if lang!='sv']<a href="javascript:switchLang('sv')" style="color:black;">sv</a>[#else]<strong>sv</strong>[/#if] |
			[#if lang!='en']<a href="javascript:switchLang('en')" style="color:black;">en</a>[#else]<strong>en</strong>[/#if] ||				 
		<!--	<a href="hako.shtml?model=${model?url}" style="color:deeppink">HAKO</a> || -->
			<div style="display: inline;">
		<!--		<a href="${aboutLink!'#'}" id="about_link_id" style="color:black"><img title="About project" alt="About project" src="../app/images/saha3/saha3_about_project.png" /></a>
				<span onclick="javascript: var element = document.getElementById('about_link_box'); element.style.display = 'inline'; element.focus()" style="font-size: 50%">[edit]
				</span>
				<input style="display: none;" type="text" id="about_link_box" value="${aboutLink!'#'}" 
					onBlur="javascript: setAboutLink(this);"
					onKeyPress="javascript: if (event.keyCode==13) { setAboutLink(this) }"/> -->
				<a style="color:black;" href="manage.shtml?model=${model?url}">Settings</a>
			</div>	
		<!--	<a href="export.shtml?model=${model?url}&l=ttl" style="color:deeppink"><img title="Export data+schema" alt="Export data+schema" src="../app/images/saha3/saha3_export_all.png" /></a>
			<a href="export.shtml?model=${model?url}&l=ttl&schema" style="color:deeppink"><img title="Export schema" alt="Export schema" src="../app/images/saha3/saha3_export_schema.png" /></a>
			<a href="export.shtml?model=${model?url}&l=ttl&config" style="color:deeppink"><img title="Export configuration" alt="Export configuration" src="../app/images/saha3/saha3_export_config.png" /></a> -->
			 
		</div>
		<div style="position:absolute;right:5px;top:48px;width:180px;font-size:8pt;">
			<div style="margin-bottom:6px">
				<div style="color:grey">name</div><input id="name" style="width:110px;border:thin solid #ccc;" />
				<div style="color:grey">message</div>
				<input id="text" style="width:180px;border:thin solid #ccc;" 
				 onkeypress="dwr.util.onReturn(event,sendMessage)" />
			</div>
			<div style="border-bottom:thin solid #ddd">
				<div id="chatlog"></div>
			</div>
		</div>
		<script>
			dojo.addOnLoad(function() {new dijit.form.FilteringSelect(
				{store:new dojox.data.QueryReadStore({url:"../service/instance_search/?model=${model}",requestMethod:"post"}),
				autoComplete:false,
				labelAttr:"name",
				onChange:openInstance,
				searchDelay:1000,
				labelType:"html",
				hasDownArrow:false
			},dojo.byId("uber_search"));});
		</script>
	</div>
[/#macro]

[#macro properties model propertyMapEntrySet]
	<table class="properties">
	[#list propertyMapEntrySet as entry]
		[#if entry.value?size > 0]
			[#assign property = entry.value?first]
			[#if !property.config.hidden]
			<tr class="[#if entry_index % 2 == 0]even[#else]odd[/#if]">
				<td class="key">
					<a href="resource.shtml?uri=${entry.key.uri?url}&model=${model?url}" style="text-decoration:none">
						${entry.key.label}
					</a>
				</td>
				<td class="value">
					[#list entry.value as property]
						[#if property.literal]
							[#assign label = property.valueLabel]
							[#if property.config.pictureProperty || label?ends_with(".jpg") || label?ends_with(".jpeg") || label?ends_with(".png") || label?ends_with(".gif")]
								<div style="margin:5px;">
								[#if label?starts_with("http://") && !label?starts_with("http://demo.seco.tkk.fi/")]
									<a href="${label}" style="color:darkblue">
									<img src="${label}" style="max-width:60px;max-height:60px;margin-right:5px;
										border:thin solid black;"/>${label}</a>
								[#else]
									<a href="../service/pics/?name=${label}&model=${model}" style="color:darkblue">
									<img src="../service/pics/?name=${label}&model=${model}" 
										style="max-width:60px;max-height:60px;margin-right:5px;border:thin solid black;"/>${label}</a>
								[/#if]
								</div>
							[#elseif label?starts_with("http://")]
								[#if property.config.localized && property.valueLang?length > 0]
									<span style="color:grey">(${property.valueLang})</span>
								[/#if]
								<span style="white-space:pre-wrap;"><a href="${label}" style="color:darkblue">${label}</a></span>[#if property_has_next], [/#if]
							[#else]
								[#if property.config.localized && property.valueLang?length > 0]
									<span style="color:grey">(${property.valueLang})</span>
								[/#if]
								<span style="white-space:pre-wrap;">${label}</span>[#if property_has_next], [/#if]
							[/#if]
							[#-- text to speech demo 
							<a target="_blank" href="http://demo.seco.tkk.fi/jokeri/speech/?data=${label}">[play]</a>
							--]
						[#else]
							<a href="resource.shtml?uri=${property.valueUri?url}&model=${model?url}" 
							 onMouseOver="showResourceTooltip(this,'${property.valueUri}')" onMouseOut="hideResourceTooltip(this)">
								${property.valueLabel}</a>[#if property_has_next], [/#if]
						[/#if]
					[/#list]
				</td>
			</tr>
			[/#if]
		[/#if]
	[/#list]
	</table>
[/#macro]

[#macro renderStaticMap propertyMapEntrySet]
	[#assign googleMapsKey = "ABQIAAAAOVuTVW1aChPiS8ukar-AChRkY9KrTsc54SNfat8hJ7dc0OtNkRRuDC5nOgkBT53gdVJJH7oIXf0z-g"]
	[#assign latPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#lat"]
	[#assign longPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#long"]
	[#assign latValue = ""]
	[#assign longValue = ""]
	[#list propertyMapEntrySet as entry]
		[#if entry.key.uri == latPropertyUri && entry.value?size > 0]
			[#assign latValue = entry.value?first.valueLabel]
		[#elseif entry.key.uri == longPropertyUri && entry.value?size > 0]
			[#assign longValue = entry.value?first.valueLabel]
		[/#if]
	[/#list]
	[#if latValue!="" && longValue!=""]
	<div id="map_id">
	  <img src="http://maps.google.com/maps/api/staticmap?markers=${latValue},${longValue}&zoom=11&size=420x300&sensor=false&key=${googleMapsKey}"/>
	</div>
	[/#if]
[/#macro]

[#macro renderMap propertyMapEntrySet resourceUri allowEdit]	
	[#-- key for wrk-4.seco.hut.fi --]
	[#-- [#assign googleMapsKey = "ABQIAAAAbGh0INmRoyUSEYiw64wuMBQtMqU-qUFb9rtyXClbMJ0ruKndGRRicrYrFWb4nC7pWExSj7_BO7spSQ"] --]
	[#-- key for demo.seco.tkk.fi --]
	[#assign googleMapsKey = "ABQIAAAAOVuTVW1aChPiS8ukar-AChRkY9KrTsc54SNfat8hJ7dc0OtNkRRuDC5nOgkBT53gdVJJH7oIXf0z-g"]
	[#assign latPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#lat"]
	[#assign longPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#long"]
	[#assign polygonUri = "http://www.yso.fi/onto/sapo/hasPolygon"]
	[#assign routeUri = "http://www.yso.fi/onto/sapo/hasRoute"]
	[#assign polygonValue = ""]
	[#assign routeValue = ""]	
	[#assign latValue = ""]
	[#assign longValue = ""]
	[#assign mapExists = false /]		
	
	<script type="text/javascript">
		function createPolylineEncoding(lat, lng, plat, plng) {
			if (lat == undefined || lng == undefined
				|| plat == undefined || plng == undefined) {
					return "";
				}
				
  			var i, dlat, dlng;
			plat = Math.floor(plat * 1e5);
			plng = Math.floor(plng * 1e5);
  			var late5 = Math.floor(lat * 1e5);
  			var lnge5 = Math.floor(lng * 1e5);
  			dlat = late5 - plat;
  			dlng = lnge5 - plng;
  			plat = late5;
  			plng = lnge5;
  			encoded_point = encodeSignedNumber(dlat) + 
  			encodeSignedNumber(dlng);


  			return encoded_point;
		}
		function encodeSignedNumber(num) {
  			var sgn_num = num << 1;
  			if (num < 0) {
    			sgn_num = ~(sgn_num);
  			}
  			return(this.encodeNumber(sgn_num));
		}
		function encodeNumber(num) {
  			var encodeString = "";
  			var nextValue, finalValue;
  			while (num >= 0x20) {
    			nextValue = (0x20 | (num & 0x1f)) + 63;
			//     if (nextValue == 92) {
			//       encodeString += (String.fromCharCode(nextValue));
			//     }
    			encodeString += (String.fromCharCode(nextValue));
    			num >>= 5;
  			}
  			finalValue = num + 63;
			//   if (finalValue == 92) {
			//     encodeString += (String.fromCharCode(finalValue));
			//   }
  			encodeString += (String.fromCharCode(finalValue));
  			return encodeString;
		}
	
		function setNewCoordinates(coordinates, fc) {
			ResourceEditService.setMapProperty('${model}', '${resourceUri}', fc, coordinates)
				
			if (fc == 'singlepoint') {
				var x, y;
				if (coordinates != null) {
					var pointarr = coordinates.split(',');
					x = pointarr[0];
					y = pointarr[1];
				
				} else {
					x = null;
					y = null;
				}
				drawPointMap(x, y, true);
			} else if (fc == 'polygon') {
				drawPolygonMap(coordinates, true);
			} else if (fc == 'route') {
				drawRouteMap(coordinates, true);
			} else {
				removeMap();
			}
		}
		
		function drawPointMap(x, y, allowEdit) {
			var html = '';
			
			if (x != null && y != null)
			{
				html = '<img src="http://maps.google.com/maps/api/staticmap?markers=' + x + ',' + y + '&zoom=11&size=420x300&sensor=false&key=${googleMapsKey}"/>'
				+ '<br/>'
				+ '<small style="color:grey">' + x + ', ' + y + '</small>'
				+ '<br/>';
				
				if (allowEdit) {
					html += '<span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=singlepoint\', \'map_popup\', \'\'); return false;">[edit point]</span>';
					html += ' <span style="cursor:pointer;" onclick="javascript: setNewCoordinates(null, \'singlepoint\');">[remove point]</span>';
					
				}
			} else if (allowEdit) {
				html += '<span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=singlepoint\', \'map_popup\', \'\'); return false;">[set place]</span>';
			}
			
			document.getElementById("map_id").innerHTML = html;			
		}
		
		function drawPolygonMap(coordinates, allowEdit) {			
			var html = '';
			
			if (coordinates != null && coordinates.length > 0)
			{
				html = '<img src="http://maps.google.com/maps/api/staticmap?path=fillcolor:0xAA000044|color:0xFFFFFF00|enc:';

				var coordinateArray = coordinates.split(' ');
			
				var llat = 0;
				var llng = 0;
				for (var i in coordinateArray) {
					var coordinate = coordinateArray[i].split(',');

					html += createPolylineEncoding(coordinate[0], coordinate[1], llat, llng);
					llat = coordinate[0];
					llng = coordinate[1];		
				} 			
				html += '&size=420x300&sensor=false&key=${googleMapsKey}"/>';
				
				if (allowEdit) {
					html += '<br/><span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=polygon\', \'map_popup\', \'\'); return false;">[edit area]</span>';
					html += '<span style="cursor:pointer;" onclick="javascript: setNewCoordinates(null, \'polygon\');">[remove area]</span>';
				}
			} else if (allowEdit) {
				html += '<span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=polygon\', \'map_popup\', \'\'); return false;">[set area]</span>';
			}
			
			document.getElementById("map_id").innerHTML = html;					
		}
		
		function drawRouteMap(coordinates, allowEdit) {
			var html = '';
			
			if (coordinates != null && coordinates.length > 0)
			{
				html = '<img src="http://maps.google.com/maps/api/staticmap?path=weight:2|color:0xFF0000FF|enc:';
	
				var coordinateArray = coordinates.split(' ');
			
				var llat = 0;
				var llng = 0;
				for (var i in coordinateArray) {
					var coordinate = coordinateArray[i].split(',');

					html += createPolylineEncoding(coordinate[0], coordinate[1], llat, llng);
					llat = coordinate[0];
					llng = coordinate[1];	
				
					
				} 					
				html += '&size=420x300&sensor=false&key=${googleMapsKey}"/>';
				
				if (allowEdit) {
					html += '<br/><span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=route\', \'map_popup\', \'\'); return false;">[edit route]</span>';
					html += ' <span style="cursor:pointer;" onclick="javascript: setNewCoordinates(null, \'route\');">[remove route]</span>';
				}
			} else if (allowEdit) {
				html += '<span style="cursor:pointer;" onclick="javascript:window.open(\'map.shtml?model=${model?url}&uri=\' + encodeURIComponent(\'${resourceUri}\') + \'&fc=route\', \'map_popup\', \'\'); return false;">[set route]</span>';
			}
			
			document.getElementById("map_id").innerHTML = html;					
		}
	</script>
	
	
	[#list propertyMapEntrySet as entry]
		[#if entry.key.uri == routeUri && entry.value?size > 0]
			[#assign routeValue = entry.value?first.valueLabel]
		[#elseif entry.key.uri == polygonUri && entry.value?size > 0]
			[#assign polygonValue = entry.value?first.valueLabel]
		[#elseif entry.key.uri == latPropertyUri && entry.value?size > 0]
			[#assign latValue = entry.value?first.valueLabel]
		[#elseif entry.key.uri == longPropertyUri && entry.value?size > 0]
			[#assign longValue = entry.value?first.valueLabel]
		[/#if]
	[/#list]
	<div id="map_id"></div>
	<div id="map_change"></div>
	[#if routeValue?length > 0]
		<script type="text/javascript">drawRouteMap('${routeValue}', ${allowEdit?string});</script>
		[#assign mapExists = true /]		
	[#elseif polygonValue?length > 0]
		<script type="text/javascript">drawPolygonMap('${polygonValue}', ${allowEdit?string});</script>
		[#assign mapExists = true /]					
	[#elseif latValue?length > 0 && longValue?length > 0]
		<script type="text/javascript">drawPointMap(${latValue}, ${longValue}, ${allowEdit?string});</script>
		[#assign mapExists = true /]				
	[/#if]
			
	[#if !mapExists && allowEdit] 
			[#list instance.properties as property]
				[#assign latPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#lat" /]
				[#assign longPropertyUri = "http://www.w3.org/2003/01/geo/wgs84_pos#long" /]
				[#assign polygonUri = "http://www.yso.fi/onto/sapo/hasPolygon" /]
				[#assign routeUri = "http://www.yso.fi/onto/sapo/hasRoute" /]
				
				[#if property.uri == latPropertyUri || property.uri == longPropertyUri]
					<script type="text/javascript">drawPointMap(null, null, ${allowEdit?string});</script>
					[#break /]
				[#elseif property.uri == polygonUri]	
					<script type="text/javascript">drawPolygonMap(null, ${allowEdit?string});</script>
					[#break /]			
				[#elseif property.uri == routeUri]			
					<script type="text/javascript">drawRouteMap(null, ${allowEdit?string});</script>
					[#break /]					
				[/#if]
				
			[/#list]
		[/#if]
	
[/#macro]
