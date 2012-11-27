/* MIT License
 * Copyright (c) 2012 Semantic Computation Research Group
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of 
 * the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 *  
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Some parts of BGraph are taken from sgvizler project: http://code.google.com/p/sgvizler/
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

function BGraph() {
	this.opts = {};
	
	this.opts['namespace'] = {
	        'rdf' : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
	        'rdfs': "http://www.w3.org/2000/01/rdf-schema#",
	        'owl' : "http://www.w3.org/2002/07/owl#",
	        'xsd' : "http://www.w3.org/2001/XMLSchema#",
	        'foaf' : "http://xmlns.com/foaf/0.1/",
	        'dct' : "http://purl.org/dc/terms/",
	        'skos' : "http://www.w3.org/2004/02/skos/core#",
	        'schema' : "http://schema.org/",
	        'org' : "http://www.w3.org/ns/org#",
	        'geo' : "http://www.w3.org/2003/01/geo/wgs84_pos#",
			'afn' : "http://jena.hpl.hp.com/ARQ/function#"
	    };
	    
		this.opts['types'] = {};
		this.opts['properties'] = [];
		
		// Returns prefixes as string for sparql query
		this.getPrefixes = function () {
			var prefixString = "";

			for(ns in bgraph.opts.namespace) 
				prefixString+="prefix "+ns+": <"+bgraph.opts.namespace[ns]+"> ";
				
			return prefixString;
	    };
	    
	    // Returns list of prefixes as string with line breaks
		this.getPrefixList = function () {
			var prefixString = "";

			for(ns in bgraph.opts.namespace) 
				prefixString+="prefix "+ns+": "+bgraph.opts.namespace[ns]+"\n";
				
			return prefixString;
	    };
	    
	    // Adds new prefix to opts
	    this.addPrefix = function (prefix,namespace) {
			bgraph.opts.namespace[prefix] = namespace;
		}
	
	this.init = function() { 
		bgraph.opts['typeCount'] = 0;
		bgraph.opts['selectedTypes'] = {};
		
		$('#bgraph_submit_query').click( function() {
			var viztype = $('#bgraph_visualizations').children(":selected").attr("type");
			if (bgraph.valid_selection(viztype)) {
				bgraph.execute_query(viztype, bgraph.opts['xaxis'], bgraph.opts['yaxis']);
			}
		}); 
		
		$('#bgraph_add_type').click( function() {
			$("#bgraph_types").append( $("#bgraphTypeTemplate").render([bgraph.opts]) );
			
			$('#bgraph_type_'+bgraph.opts['typeCount']).change(function() {
				bgraph.opts['selectedTypes'][bgraph.opts['typeCount']] = ($(this).children(":selected").attr("uri"));
				bgraph.update_query();
			});
			
			bgraph.opts['typeCount'] += 1;
		}); 
		$('#bgraph_yaxis').hide();
	};
	
	
	this.remove_type = function(typeCount) {
		delete bgraph.opts["selectedTypes"][typeCount];
		$('#bgraph_typediv_'+typeCount).remove();
		console.log('#bgraph_typediv_'+typeCount);
		this.update_query();
	};
	
	this.update_query = function() {
		var viztype = $('#bgraph_visualizations').children(":selected").attr("type");
		if (viztype == "scatter") {
			$('#bgraph_yaxis').show();
		} else if (viztype == "bar"||viztype == "bar2") {
			$('#bgraph_yaxis').hide();
		}
	};	 
	
	this.update_type_selection = function() {
		$('#bgraph_type_select').empty();
		$.each(bgraph.opts["types"], function(key, element) {
			console.log("key" + key);
			$('#bgraph_type_select').append('<option id="bgraph_type_select_option_'+element.index+'" uri="'+element.uri+'">'+element.label+'</option>');
			$('#bgraph_type_select_option_'+element.index).click(function () {
				bgraph.opts["selectedTypes"][element.uri] = bgraph.opts["types"][element.uri];
				delete bgraph.opts["types"][element.uri];
				$('#bgraph_selected_types').append('<a class="btn" id="bgraph_type_selected_option_'+element.index+'" uri="'+element.uri+'"><i class="icon-remove"></i> '+element.label+'</a>');
				$('#bgraph_type_selected_option_'+element.index).click(function() {
					$('#bgraph_type_selected_option_'+element.index).remove();
					bgraph.opts["types"][element.uri] = bgraph.opts["selectedTypes"][element.uri]; //{uri:element.uri, index: element.index, label: element.label};
					delete bgraph.opts["selectedTypes"][element.uri];
					bgraph.update_type_selection();
				});
				bgraph.update_type_selection();
			});
		});
	};
	
	
	this.update_bgraph = function() {
		this.update_type_selection();
		this.update_query();
	};

	this.valid_selection = function(visualization_type) {
		console.log("type:" + visualization_type);
		
		/*if (visualization_type == "scatter") {
			if (bgraph.opts["xaxis"] && bgraph.opts["yaxis"]) {
				return true;
			} else {
				console.log("validation failure");
				console.log(bgraph.opts);
				return false;
			}
		} else if (visualization_type == "bar") {
			if (bgraph.opts["xaxis"]) {
				return true;
			} else {
				return false;
			}
		}*/
		
		return true;
	}; 
	
	
	this.execute_query = function(vizualization_type, xlabel, ylabel) {
		$('#bgraph_visualization_holder').empty();
		var queryHeader = "";
		var queryFooter = "";
		
		
		var queryXAxis = $('select#bgraph_xaxis_property_selector').children(":selected").attr("uri"); 
		var queryYAxis = $('select#bgraph_yaxis_property_selector').children(":selected").attr("uri");
		
		
		if (vizualization_type == "scatter") {
			queryHeader += 	"select DISTINCT ?x ?y where {\n" +
							"\tOPTIONAL {" +
							"{ ?s <http://www.w3.org/2004/02/skos/core#prefLabel> ?label .} UNION {" +
							"?xaxis <http://www.w3.org/2000/01/rdf-schema#label> ?label ." +
							"OPTIONAL { ?xaxis <http://www.w3.org/2004/02/skos/core#prefLabel> ?prefLabel .}" +
							"FILTER (!bound(?prefLabel)) }\n} {SELECT ?s ?x ?y WHERE {\n\t\t ?s \n";
			// Type ------
			if (queryXAxis) {
				queryFooter += "\t\t <"+queryXAxis+"> ?x ; \n";
			}
			if (queryYAxis) {
				queryFooter += "\t\t <"+queryYAxis+"> ?y \n";
			}
			queryFooter += "\t} \n}\n";
			queryFooter += "}";
			
			
			
			
		} else if (vizualization_type == "bar") {
			queryHeader += 	"SELECT ?label ?count where {"+
							"\tOPTIONAL {" +
							"{ ?xaxis <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } UNION { " +
							"?xaxis <http://www.w3.org/2000/01/rdf-schema#label> ?label ." +
							"OPTIONAL { ?xaxis <http://www.w3.org/2004/02/skos/core#prefLabel> ?prefLabel .}" +
							"FILTER (!bound(?prefLabel)) }\n" +
							"} {SELECT ?xaxis (count(?s) as ?count) WHERE {\n" +
							"\t\t ?s \n";
			if (queryXAxis) {
				queryFooter += "\t\t<"+queryXAxis+"> ?xaxis ;\n";
			}
			queryFooter += 	"\t} GROUP BY ?xaxis\n }\n } order by DESC(?count)\n";
			
			
			
		} else if (vizualization_type == "bar2") {
			queryHeader += "SELECT DISTINCT (count(?s) as ?count) ?xaxis  { ?s\n";
			if (queryXAxis) {
				queryFooter += "\t\t<"+queryXAxis+"> ?xaxis ;\n";
			}
			queryFooter += "\t} GROUP BY ?xaxis\n";
		}
		
		
		
		queryFooter += "LIMIT "+ $('#bgraph_result_limit').children(":selected").text(); 
		var queryBody = "";
		var requestCnt = 0;
		var requestLimit = $('#bgraph_selected_types').children().length;
		var jsonData = null;
		if ($('#bgraph_selected_types').children().length == 0) {
			$('#bgraph_selected_types').append("<div id=\"tmpPlaceHolder\"></div>");
			requestLimit = 1;
		}

		$('#bgraph_selected_types').children().each(function(idx, value) {
			console.log("idx"+idx+"value"+ $(this).attr("uri")); 
			if ($(this).attr("uri")) {
				queryBody = "a <"+$(this).attr("uri")+">;";
			} else {
				queryBody = "";
			}
			console.log(queryHeader + queryBody + queryFooter);
			jQuery.getJSON("../service/data/"+bgraph.opts["project"]+"/sparql", {query: queryHeader + queryBody + queryFooter, output: "json"}, function(response) {
				if (requestCnt == 0) {
					jsonData = bgraph.SparqlJSON2GoogleJSON(response);
					console.log(jsonData);
				} else {
					var tmpJSONData = bgraph.SparqlJSON2GoogleJSON(response);
					var originalRows = jsonData.rows.length;
					var originalCols = jsonData.cols.length;
					for (var c=1; c < tmpJSONData.cols.length; c++) {
						jsonData.cols.push(tmpJSONData.cols[c]);
						for (var i=0; i < originalRows; i++) {
							jsonData.rows[i].c.push(null);
						}
					}
					for (var i=0; i < tmpJSONData.rows.length; i++) {
						var tmpObj = {}; 
						tmpObj['c'] = [tmpJSONData.rows[i].c[0]];
						for (var j=1; j < originalCols; j++) {
							tmpObj['c'].push(null);
						}
						for (var j=1; j < tmpJSONData.rows[i].c.length; j++) {
							tmpObj['c'].push(tmpJSONData.rows[i].c[j]);
						}
						jsonData.rows.push(tmpObj);
					}
				}
				if (!jsonData.cols[0].type) {
					jsonData.cols[0]["type"] = "string";
				}
				
				requestCnt++;
				if (requestCnt == requestLimit) { // Final call response, show some graphs
					var foo = new google.visualization.DataTable(jsonData);
					var chart=null;
					if (vizualization_type == "scatter") {
						chart = new google.visualization.ScatterChart(document.getElementById('bgraph_visualization_holder'));
					} else if ((vizualization_type == "bar")||(vizualization_type == "bar2")) {
						chart = new google.visualization.BarChart(document.getElementById('bgraph_visualization_holder'));
					}
					chart.draw(foo);
				}
			});
			$('#tmpPlaceHolder').remove();
		});
		//});
		/*
			if (vizualization_type == "scatter") {
				$.each(response.results.bindings, function(index, element) {
					var tmpRow = [parseFloat(element.x.value)];
					for (var i=0; i < columns; i++) {
						if (i == idx) {
							tmpRow.push(parseFloat(element.y.value));
						} else {
							tmpRow.push(null);
						}
					}
					console.log(tmpRow);
					bgraph_data.addRow(tmpRow);
				});
			} else if (vizualization_type == "bar") {
				$.each(response.results.bindings, function(index, element) {
					var tmpRow = [element.label.value];
					for (var i=0; i < columns; i++) {
						if (i == idx) {
							tmpRow.push(parseFloat(element.count.value));
						} else {
							tmpRow.push(null);
						}
					}
					console.log(tmpRow);
					bgraph_data.addRow(tmpRow);
				});
			} else if (vizualization_type == "bar2") {
				$.each(response.results.bindings, function(index, element) {
					var tmpRow = [element.label.value];
					for (var i=0; i < columns; i++) {
						if (i == idx) {
							tmpRow.push(parseFloat(element.count.value));
						} else {
							tmpRow.push(null);
						}
					}
				});
			}
			ajaxCnt++;
			console.log(ajaxCnt);
			if (ajaxCnt == columns) {
				console.log(bgraph_rows);
				var g_chart = null;
				if (vizualization_type == "scatter") {
					g_chart = new google.visualization.ScatterChart(document.getElementById('bgraph_visualization_holder'));
				} else  if (vizualization_type == "bar") {
					g_chart = new google.visualization.BarChart(document.getElementById('bgraph_visualization_holder'));
				} else  if (vizualization_type == "bar2") {
					
				}
				g_chart.draw(bgraph_data);
			}*/
		
		
	};
		/*jQuery.getJSON("../service/data/"+this.opts["project"]+"/sparql", {query: $('#bgraph_query').text(), output: "json"}, function(response) {
			if (response.results) {
				var bgraph_rows = [[xlabel, ylabel]];
				if (vizualization_type == "bar") {
					$.each(response.results.bindings, function(index, element) {
						bgraph_rows.push([element.label.value,  parseFloat(element.count.value)]);
					});
					var g_data = google.visualization.arrayToDataTable(bgraph_rows, false);
					var g_chart = new google.visualization.BarChart(document.getElementById('bgraph_visualization_holder'));
					g_chart.draw(g_data);
				} else if (vizualization_type == "scatter") {
					$.each(response.results.bindings, function(index, element) {
						bgraph_rows.push([parseFloat(element.x.value),  parseFloat(element.y.value)]);
					});
					var g_data = google.visualization.arrayToDataTable(bgraph_rows, false);
					var g_chart = new google.visualization.ScatterChart(document.getElementById('bgraph_visualization_holder'));
					g_chart.draw(g_data);
				}
			}
		});*/
	
    // Main loop for transformer
	this.SparqlJSON2GoogleJSON = function (stable) {
        var c,
        r,
        srow,
        grow,
        gvalue,
        stype,
        sdatatype,
        gcols = [],
        grows = [],
        gdatatype = [], // for easy reference of datatypes
        scols = stable.head.vars,
        srows = stable.results.bindings;

	    for (c = 0; c < scols.length; c += 1) {
	        r = 0;
	        stype = null;
	        sdatatype = null;
	        // find a row where there is a value for this column
	        while (typeof srows[r][scols[c]] === 'undefined' && r + 1 < srows.length) 
				{ r += 1; }
	        if (typeof srows[r][scols[c]] !== 'undefined') {
	            stype = srows[r][scols[c]].type;
	            sdatatype = srows[r][scols[c]].datatype;
	        }
	        gdatatype[c] = this.getGoogleJsonDatatype(stype, sdatatype);
	        gcols[c] = {'id': scols[c], 'label': scols[c], 'type': gdatatype[c]};
	    }
	
	    // loop rows
	    for (r = 0; r < srows.length; r += 1) {
	        srow = srows[r];
	        grow = [];
	        // loop cells
	        for (c = 0; c < scols.length; c += 1) {
	            gvalue = null;
	            if (typeof srow[scols[c]] !== 'undefined' &&
	                    typeof srow[scols[c]].value !== 'undefined') {
	                gvalue = this.getGoogleJsonValue(srow[scols[c]].value, gdatatype[c], srow[scols[c]].type);
	            }
	            grow[c] = { 'v': gvalue };
	        }
	        grows[r] = {'c': grow};
	    }
	    return {'cols': gcols, 'rows': grows};
	};
	
	// Transforms SPARQL JSON values to Google JSON values
	this.getGoogleJsonValue = function (value, gdatatype, stype) {
        var newvalue;
        if (gdatatype === 'number') {
            newvalue = Number(value);
        } else if (gdatatype === 'date') {
            //assume format yyyy-MM-dd
            newvalue = new Date(value.substr(0, 4),
                            value.substr(5, 2),
                            value.substr(8, 2));
        } else if (gdatatype === 'datetime') {
            //assume format yyyy-MM-ddZHH:mm:ss
            newvalue = new Date(value.substr(0, 4),
                            value.substr(5, 2),
                            value.substr(8, 2),
                            value.substr(11, 2),
                            value.substr(14, 2),
                            value.substr(17, 2));
        } else if (gdatatype === 'timeofday') {
            //assume format HH:mm:ss
            newvalue = [value.substr(0, 2),
                    value.substr(3, 2),
                    value.substr(6, 2)];
        } else { // datatype === 'string' || datatype === 'boolean'
            if (stype === 'uri') { // replace namespace with prefix
                newvalue = this.prefixify(value);
            }
            else newvalue = value;
        }
        return newvalue;
    };
    
    // Transforms SPARQL JSON datatypes to Google JSON datatypes
    this.getGoogleJsonDatatype = function (stype, sdatatype) {
        var gdatatype = 'string',
            xsdns = bgraph.opts.namespace.xsd;
                        
        if (typeof stype !== 'undefined' && (stype === 'typed-literal' || stype === 'literal')) {
            if (sdatatype === xsdns + "float"   ||
                    sdatatype === xsdns + "double"  ||
                    sdatatype === xsdns + "decimal" ||
                    sdatatype === xsdns + "int"     ||
                    sdatatype === xsdns + "long"    ||
                    sdatatype === xsdns + "integer" ||
                    sdatatype === xsdns + "gYearMonth" ||
					sdatatype === xsdns + "gYear" ||
					sdatatype === xsdns + "gMonthDay" ||
					sdatatype === xsdns + "gDay" ||
					sdatatype === xsdns + "gMonth") {
                gdatatype =  'number';
            } else if (sdatatype === xsdns + "boolean") {
                gdatatype =  'boolean';
            } else if (sdatatype === xsdns + "date") {
                gdatatype =  'date';
            } else if (sdatatype === xsdns + "dateTime") {
                gdatatype =  'datetime';
            } else if (sdatatype === xsdns + "time") {
                gdatatype =  'timeofday';
            } 
        } 
        return gdatatype;
    };

	// Prexifies URI
    this.prefixify = function (url) {
        var ns = null;
        for (ns in bgraph.opts.namespace) {
            if (bgraph.opts.namespace.hasOwnProperty(ns) &&
                    url.lastIndexOf(bgraph.opts.namespace[ns], 0) === 0) {
                return url.replace(bgraph.opts.namespace[ns], ns + ":");
            }
   
        }
        return url;
    };
    
    // Unprexifies URI
    this.unprefixify = function (qname) {
        var ns = null;
        for (ns in bgraph.opts.namespace) {
            if (bgraph.opts.namespace.hasOwnProperty(ns) &&
                    qname.lastIndexOf(ns + ":", 0) === 0) {
                return qname.replace(ns + ":", bgraph.opts.namespace[ns]);
            }
        }
        return qname;
    };
};