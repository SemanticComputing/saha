[#ftl]
[#setting url_escaping_charset='UTF-8']
<!DOCTYPE html>
<head>
	<title>HAKO - ${model}</title>
    <script type='text/javascript' src='../dwr/interface/ResourceConfigService.js'></script>
    <script type='text/javascript' src='../dwr/interface/ResourceEditService.js'></script>
    <script type='text/javascript' src='../dwr/engine.js'></script>

    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

    <script type="text/javascript" src="../app/scripts/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="../app/scripts/jquery-ui-1.8.21.custom.min.js"></script>
    <script type="text/javascript" src="../app/scripts/jsrender.js"></script>
    <script type="text/javascript" src="../app/scripts/waypoints.min.js"></script>
    <script type="text/javascript" src="../app/scripts/mxn/mxn.js?(googlev3)"></script>
    <script type="text/javascript" src="../app/scripts/timeline-2.3.0.js"></script>
    <script src="../app/scripts/timemap.js" type="text/javascript"></script>
	<link rel="stylesheet" type="text/css" href="../app/css/hako/jquery-ui-1.8.21.custom.css" />
	
	
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
		span.toggle {
			cursor: pointer;
		}
		ul.category span.toggle {
			position: relative;			
		}		
	</style>

    <script id="resultTemplate" type="text/x-jsrender">
        <div id="c_{{:#index+1}}" class="cell" style="border-bottom:thin solid #e0e0e0;">
            <a href="javascript:show_instance('{{:uri}}','c_{{:#index+1}}');">
                {{:label}}
            </a>
        </div>
    </script>
    
    <script id="selectionTemplate" type="text/x-jsrender">
        <div>
            <a onclick="javascript:addSelection('{{:backQuery}}')" style="cursor: pointer; color:grey">[remove]</a>
            <strong><a onclick="javascript:addSingleSelection('{{:propertyUri}}', '{{:uri}}')" style="cursor: pointer; color:orangered">
            <em>{{:label}}</em></a></strong>
        </div>
    </script>
    
    <script id="toggleCategoryTemplate" type="text/x-jsrender">
        <span id="span{{:uri}}" class="toggle" style="cursor: pointer" onclick="javascript:toggle('list_{{:rootCategory}}_{{:uri}}', this)">[+]</span>
    </script>
    
	<script>
		var tm, markers, condition = null, numberOfResults = 0;

		function onLoad() {

            // Set up tab controls
            $('#tab1').click(function() {
                $('#tab_target').children().hide();
                $('#result_list').show();  
            });
            $('#tab2').click(function() {
                $('#tab_target').children().hide();
                $('#map_view').show();
            });

		    // Make a new TimeMap object, passing the ids of the map and timeline DOM elements.
		    // This will initialize the map.
	    	tm = new TimeMap(
	        	document.getElementById("tl"), 
	        	document.getElementById("map"),
				{
		  			openInfoWindow: function() {		  			
		  				this.map.setCenter(this.getInfoPoint(), 16); 
                        var item = this;
						ResourceEditService.getHakoPropertyTable('${model}',this.opts.uri, {
							callback:function(dataFromServer) {
                                item.opts.infoHtml = dataFromServer;
                                TimeMapItem.openInfoWindowBasic.call(item);
							}
						}); 		  							
		  			},
		  			centerOnItems: false,
		  			showMapCtrl: true,
		  			//mapType: 'hybrid',
		  			eventIconPath: '/smetana/app/images/saha3/timeline/',
		  			mapCenter: new mxn.LatLonPoint(64.15436, 38.40440100),
		  			mapZoom: 4	  			
				}
	        );
			// Make a new dataset with the id "markers" and green markers.
	   	 	// You can load multiple datasets with different visual themes.
	    	markers = tm.createDataset("markers", {
	        	title:  "Marker",
	        	theme:  "green"
	    	});

		$(function() {
			$( "#tab_target" ).tabs();
		});	
		$('#tab_target').bind('tabsshow', function(event, ui) {
    		if (ui.panel.id == "map_view") {
    			google.maps.event.trigger(map, "resize");
    			tm.map.setCenter(new mxn.LatLonPoint(64.15436, 38.40440100));
    			tm.timeline.layout();
   			}
		});
	     var theme = Timeline.ClassicTheme.create(); // create the themes
	     //theme.ether.marker = "Right";
	     theme.mouseWheel = 'zoom';
	     
	     theme.autoWidth = false;
	     //theme.autoWidthAnimationTime = '5ms';
	    // Create band information for the timeline as you would for any SIMILE timeline,
	    // but using the eventSource from the dataset object.
	    // See http://simile.mit.edu/timeline/docs/ for more info.
	    // In this case we're making two equally-sized bands with two different datasets.
	    var bands = [
	        Timeline.createBandInfo({
	            eventSource:    markers.eventSource,
	            width:          "100%",
	            intervalPixels: 50,
	            intervalUnit:   Timeline.DateTime.YEAR, 

				layout:         'original',
		    	theme: theme,
		    	eventPainter:   Timeline.OriginalEventPainter,
	            zoomIndex:      10,
            zoomSteps:      new Array(
              {pixelsPerInterval: 280,  unit: Timeline.DateTime.HOUR},
              {pixelsPerInterval: 140,  unit: Timeline.DateTime.HOUR},
              {pixelsPerInterval:  70,  unit: Timeline.DateTime.HOUR},
              {pixelsPerInterval:  35,  unit: Timeline.DateTime.HOUR},
              {pixelsPerInterval: 400,  unit: Timeline.DateTime.DAY},
              {pixelsPerInterval: 200,  unit: Timeline.DateTime.DAY},
              {pixelsPerInterval: 100,  unit: Timeline.DateTime.DAY},
              {pixelsPerInterval:  50,  unit: Timeline.DateTime.DAY},
              {pixelsPerInterval: 400,  unit: Timeline.DateTime.MONTH},
              {pixelsPerInterval: 200,  unit: Timeline.DateTime.MONTH},
              {pixelsPerInterval: 100,  unit: Timeline.DateTime.MONTH} // DEFAULT zoomIndex
            )

        		}) ];

    		// Initialize the timeline with the band info
    		tm.initTimeline(bands);
    		tm.timeline.autoWidth = false;
 

    		// you usually need to call this to get items to display on the timeline
   			//tm.timeline.layout();
    		// you might also want to scroll the timeline somewhere
    		//tm.timeline.getBand(0).setCenterVisibleDate(new Date("2007-01-01"));
			//tm.timeline.layout();
        
            loadData(); 	        
			
			//tm.timeline.getBand(0).setCenterVisibleDate(new Date("0000-01-01"));
			//tm.timeline.getBand(0).setCenterVisibleDate(new Date("1007-01-01"));
		} // End of onLoad


        function renderCategory(obj, depth, selectedCategoryURIs, rootCategory) {
            // Move rendering conditions to jsrender template
            // i.e. this function is basicly:
            // renderCategory(obj,depth,selectedCategoryURIs) { return $('#template').render(obj, ...) }
            var res = "<li id=\"item_"+rootCategory+"_"+obj.uri+"\">";
            if (obj['children'].length > 0) {
            	obj.rootCategory = rootCategory;
                res += $("#toggleCategoryTemplate").render(obj);
            }
            var query = null;
            var label = null;
            if (selectedCategoryURIs.indexOf(obj['uri']) == -1) {
                query = obj["selectQuery"]
                label = obj.label
            } else {
                query = obj["backQuery"]
                label = '<strong style="color:orangered"><em>'+obj.label+'</em></strong>'
            }
            res += '<a style=\"cursor: pointer\" onclick="javascript:addSelection(\''+query+'\')">'+label+'</a><span>'+obj["itemCount"]+"</span></li><ul class=\"category\" id=\"list_"+rootCategory+"_"+obj['uri']+"\" style=\"display: none;\">" ;
            for (var i in obj['children']) {
                res += renderCategory(obj['children'][i], depth +1, selectedCategoryURIs, rootCategory);
            }
            res += "</ul>";
            return res;    
        }		

        function addSelection(cond) {
            condition = cond;
            unloadData();
            tm.datasets.markers.items = new Array();
            tm.map.removeAllMarkers();
            tm.map.removeAllPolylines();
            markers.eventSource.clear();
            loadData();
        }
        function addSingleSelection(property, uri) {
        	addSelection(escape(property)+"="+escape(uri));
        }
        
        function getSearchCondition() {
            if (condition) {
                return condition;    
            } else {
                return "";
            }
        }

        function unloadData() {
            $('#resultSelectedCategoriesList').children().fadeOut('fast').empty();
            $('#resultList').children().fadeOut('fast').empty();
            $('#categoryContainer').children().fadeOut('fast').empty();
            $('#resultList').append("Searching...");
            $('#categoryContainer').append("Searching...");
            numberOfResults = 0;
        }

        function filterSelectedCategoryURIs(l) {
            var array = new Array();
            for (var i in l) {
                array.push(l[i]["uri"]);
            }
            return array;
        }
		
		function moreData () {
			jQuery.getJSON("./hako/instances?from=" + numberOfResults +  "&to=" + (numberOfResults + 100) + "&" +getSearchCondition(), function(data) {
				$('#resultList').append($("#resultTemplate").render( data["results"] ));

				numberOfResults = numberOfResults + data["results"].length;
				
				if ( data["results"].length > 0 ) {
					var $resultsCount = $('<div class="resultsCount" style="position:absolute;padding:3px;right:0;color:#999;font-size:small;">'+numberOfResults+' results</div>');				
	                $('#resultList').append($resultsCount);                
					$('#resultList').waypoint( function() { moreData(); }, { offset: 'bottom-in-view', onlyOnScroll: true, triggerOnce: true } );
				}
				
                for (var i in data["results"]) {
                    var obj = data["results"][i];
		            markers.loadItem( obj.tmdata );
                }
                tm.timeline.layout();
			});
		}
		
        function loadData() {
        	jQuery.getJSON("./hako/ui_categories?"+getSearchCondition(), function(data) {
        		$('#categoryContainer').empty();
        		$('#resultSelectedCategoriesList').append($("#selectionTemplate").render( data["selectedCategories"] ));
        		var selectedCategoryURIs = filterSelectedCategoryURIs(data["selectedCategories"]);
        		for (var i in data["facets"]) {
                    var facetObj = data["facets"][i];
                    $('#categoryContainer').append("<div class=\"list_title\"><span class=\"toggle\" onclick=\"javascript:toggle('hako_category_"+i+"', this)\">[-]</span> "+facetObj["label"]+"</div>");
                    $('#categoryContainer').append("<div id=\"hako_category_"+i+"\">");
                    for (var cls in facetObj['facetClasses']) {
                        $('#hako_category_'+i).append("<ul class=\"category\" style=\"margin:0;padding:0\">" + renderCategory(facetObj['facetClasses'][cls], 0, selectedCategoryURIs, i) + "</ul>");
                    }
                }
        	});
            jQuery.getJSON("./hako/instances?from=" + numberOfResults +  "&to=" + (numberOfResults + 100) + "&" +getSearchCondition(), function(data) {
               	$('#resultList').empty();
                $('#resultList').append($("#resultTemplate").render( data["results"] ));
				numberOfResults = numberOfResults + data["results"].length;
				
				if ( data["results"].length > 0 ) {
					var $resultsCount = $('<div class="resultsCount" style="position:absolute;padding:3px;right:0;color:#999;font-size:small;">'+numberOfResults+' results</div>');				
	                $('#resultList').append($resultsCount);                
					$('#resultList').waypoint( function() { moreData(); }, { offset: 'bottom-in-view', onlyOnScroll: true, triggerOnce: true } )
				}

                for (var i in data["results"]) {
                    var obj = data["results"][i];
		            markers.loadItem( obj.tmdata );
                }
                tm.timeline.layout();
            });
        }

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

<body onload="onLoad();">
	<div id="header">
		<a href="hako.shtml">Hako - Faceted Search Engine</a>		 
		<script>
				function switchLang(lang) {
					if (window.location.href.indexOf('lang=')!=-1) window.location.href=window.location.href.replace(/lang=../,'lang='+lang);
					else if (window.location.href.indexOf('?')!=-1) window.location.href=window.location.href+('&lang='+lang);
					else window.location.href=window.location.href+('?lang='+lang);
				}
				function toggle(id, obj) {
					var el = document.getElementById(id);
					if ( el.style.display != 'none' ) {
						el.style.display = 'none';
						obj.innerHTML = "[+]";
					}
					else {
						el.style.display = '';
						obj.innerHTML = "[-]";
					}
					
				}
		</script>
		<div style="font-size:small;font-weight:normal;position:absolute;right:10px;bottom:3px;">		
		[#if lang!='fi']<a href="javascript:switchLang('fi')" style="color:white;">fi</a>[#else]<strong style="color:white">fi</strong>[/#if] | 
		[#if lang!='sv']<a href="javascript:switchLang('sv')" style="color:white;">sv</a>[#else]<strong style="color:white">sv</strong>[/#if] |
		[#if lang!='en']<a href="javascript:switchLang('en')" style="color:white;">en</a>[#else]<strong style="color:white">en</strong>[/#if] ||		
		<a href="javascript:ResourceConfigService.destroyHako('${model}', function() {location.href='hako.shtml'})">[reset HAKO]</a>
		<a href="index.shtml" style="margin-left: 30px">SAHA</a>
		</div>
	</div>
	
	<div id="main_container">
		<div style="margin-left:10px;margin-top:25px;">
			<div style="font-size:x-large;margin-bottom:4px;">
				<a href="hako.shtml" style="text-decoration:none;">${model?cap_first}</a>
			</div>
			<form id="searchParameters" method="get" style="margin:0;padding:0">
				<input type="text" name="term" style="width:320px;" value="[#list terms as term]${term}[/#list]"/>
				<input type="submit" value="Search"/>
			</form>
		</div>
		
		<table style="width:100%;border-collapse:collapse;border:thin solid #ccc;margin-top:1em;">
		<tr>
			<td style="vertical-align:top;border-right:thin dashed #ccc;width:260px;background-color:#FFFFEF">
			<div id="categoryContainer" style="margin:10px;font-size:10pt;">Searching</div>
			</td>
			<td style="vertical-align:top;padding:0;">
			
			
			
			<!--<div id="tab_bar" style="margin-top: 10px;border-bottom: 1px solid #DDD;">
				<button id="tab1" class="tab">Listaus</button>
				<button id="tab2" class="tab">Kartta</button>
			</div>-->
			<div id="tab_target" style="margin-top: 5px;">
				<ul>
					<li><a href="#result_list">Listaus</a></li>
					<li><a href="#map_view">Kartta</a></li>
				</ul>
				<div id="map_view">
					 <div id="mapContainer" class="mapContainer" style="width: 100%; height: 900px"><div id="map" class="mapDiv" style="width: 100%; height: 900px"></div></div>				
					 <div id="tl" class="timeline" style="width: 100%; height: 200px"></div>
				</div>

				<div class="result_list" id="result_list">
					<div id="resultSelectedCategoriesList"> </div>
					<div id="resultList"></div>
				</div>
			</div>
			</td>
		</tr>
		</table>
	</div>
</body>
</html>
