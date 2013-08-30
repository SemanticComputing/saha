[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model} - CONFIGURE</title>
	[@header model/]
	
</head>

<body class="tundra">

	[@searchBar model lang/]
	<div style="margin-left: 30px; margin-top: 30px;">
	[#if message??]<h3 style="color:red">${message}</h3><br/>[/#if]
	<h2>Download project</h2>
		<a href="export.shtml?model=${model?url}&l=ttl" style="color:deeppink">Download all triples from endpoint as text/turle</a></br>
					<a href="export.shtml?model=${model?url}&l=ttl&schema" style="color:deeppink">Dowload schema as text/turtle</a></br>
					<a href="export.shtml?model=${model?url}&l=ttl&config" style="color:deeppink">Download SAHA configuration as text/turtle</a> 
        [#if authorized]				
			<h2>Change SPARQL configuration</h2>
			<form method="POST">
				<input type="hidden" name="operation" value="changeSettings" />
				<input type="hidden" name="passhash" value="${passhash}" />
				SPARQL endpoint URL:<br />
				<input type="text" size="80" name="sparqlURL" value="${sparqlConfiguration.sparqlURL!''}" /><br />
				
				SPARUL endpoint URL:<br />
				<input type="text" size="80" name="sparulURL" value="${sparqlConfiguration.sparulURL!''}" /><br />

				Graph URI:<br />
				<input type="text" size="80" name="graphURI" value="${sparqlConfiguration.graphURI!''}" /><br />

				Label property URI:<br />
				<input type="text" size="80" name="labelURI" value="${sparqlConfiguration.labelURI!'http://www.w3.org/2000/01/rdf-schema#label'}" /><br />
				
				SPARQL query for getting whole model:<br />
				<textarea name="wholeModelQuery" rows="2" cols="90">[#if sparqlConfiguration.wholeModelQuery??]${sparqlConfiguration.wholeModelQuery}[#else]
CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }
[/#if]</textarea><br />
				
				SPARQL query for getting string matches for the top query bar:<br />
				<textarea name="topStringMatchesQuery" rows="18" cols="90">[#if sparqlConfiguration.topStringMatchesQuery??]${sparqlConfiguration.topStringMatchesQuery}[#else]
PREFIX text: <http://jena.apache.org/text#>
CONSTRUCT {
  ?item saha:itemLabel ?itemLabel .
  ?item saha:typeLabel ?itemTypeLabel .
  ?item ?property ?object .
  ?property saha:propertyLabel ?
}
SELECT ?item ?itemLabel ?itemType ?itemTypeLabel ?property ?propertyLabel ?object WHERE {
  {
    SELECT ?item ?property ?object {
      ?object text:query ?query .
      ?item ?property ?object .
    }
    LIMIT ?limit
  }
  OPTIONAL {
    ?item rdfs:label|skos:prefLabel ?itemLabel .
  }
  OPTIONAL {
    ?item a ?itemType .
    ?itemType rdfs:label|skos:prefLabel ?itemTypeLabel .
  }
  OPTIONAL {
    ?property rdfs:label|skos:prefLabel ?propertyLabel .
  }
}				
[/#if]</textarea><br />

				SPARQL query for getting string matches for the inline editor:<br />
				<textarea name="inlineStringMatchesQuery" rows="18" cols="90">[#if sparqlConfiguration.inlineStringMatchesQuery??]${sparqlConfiguration.inlineStringMatchesQuery}[#else]
PREFIX text: <http://jena.apache.org/text#>
SELECT ?item ?itemLabel ?itemType ?itemTypeLabel ?property ?propertyLabel ?object WHERE {
  {
    SELECT ?item ?property ?object {
      ?object text:query ?query .
      ?item ?property ?object .
      <typelimit>
      ?item a/rdfs:subClassOf* ?typeURI .
      </typelimit>
    }
    LIMIT ?limit
  }
  OPTIONAL {
    ?item rdfs:label|skos:prefLabel ?itemLabel .
  }
  OPTIONAL {
    ?item a ?itemType .
    ?itemType rdfs:label|skos:prefLabel ?itemTypeLabel .
  }
  OPTIONAL {
    ?property rdfs:label|skos:prefLabel ?propertyLabel .
  }
}				
[/#if]</textarea><br />
				
				SPARQL query for getting instances matching a specified type:<br />
				<textarea name="instanceQuery" rows="8" cols="90">[#if sparqlConfiguration.instanceQuery??]${sparqlConfiguration.instanceQuery}[#else]
SELECT ?item ?label WHERE {
  ?item rdf:type ?type .
  OPTIONAL {
    ?item rdfs:label|skos:prefLabel ?label
  }
}
ORDER BY ?label
[/#if]</textarea><br />
				
				SPARQL query for getting the label of a resource:<br />
				<textarea name="labelQuery" rows="8" cols="90">[#if sparqlConfiguration.labelQuery??]${sparqlConfiguration.labelQuery}[#else]
SELECT ?label WHERE {
  ?uri rdfs:label|skos:prefLabel ?label .
}				
[/#if]</textarea><br />
				
				SPARQL query for getting the types of a resource:<br />
				<textarea name="typesQuery" rows="8" cols="90">[#if sparqlConfiguration.typesQuery??]${sparqlConfiguration.typesQuery}[#else]
SELECT ?type ?label WHERE {
  ?uri rdf:type ?type .
  OPTIONAL {
    ?type rdfs:label|skos:prefLabel ?label .
  }
}
[/#if]</textarea><br />

				SPARQL query for getting the properties of a resource:<br />
				<textarea name="propertiesQuery" rows="10" cols="90">[#if sparqlConfiguration.propertiesQuery??]${sparqlConfiguration.propertiesQuery}[#else]
SELECT ?propertyURI ?propertyLabel ?object ?objectLabel WHERE {
  ?uri ?propertyURI ?object .
  OPTIONAL {
    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . 
  }
  OPTIONAL {
    ?object rdfs:label|skos:prefLabel ?objectLabel .
  }
}
[/#if]</textarea><br />

				SPARQL query for getting the inverse properties of a resource:<br />
				<textarea name="inversePropertiesQuery" rows="18" cols="90">[#if sparqlConfiguration.inversePropertiesQuery??]${sparqlConfiguration.inversePropertiesQuery}[#else]
SELECT ?propertyURI ?propertyLabel ?object ?objectLabel ?objectType ?objectTypeLabel WHERE {
  ?object ?propertyURI ?uri .
  OPTIONAL {
    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . 
  }
  OPTIONAL {
    ?object rdfs:label|skos:prefLabel ?objectLabel .
  }
   OPTIONAL {
    ?object rdf:type ?objectType .
    OPTIONAL {
      ?objectType rdfs:label|skos:prefLabel ?objectTypeLabel .
    }
  }
}
[/#if]</textarea><br />
		
				SPARQL query for getting the properties of a resource for editing:<br />
				<textarea name="editorPropertiesQuery" rows="28" cols="90">[#if sparqlConfiguration.editorPropertiesQuery??]${sparqlConfiguration.editorPropertiesQuery}[#else]
SELECT ?propertyURI ?propertyComment ?propertyLabel ?propertyType ?propertyRangeURI ?propertyRangeLabel ?object ?objectLabel WHERE {
  { 
    ?uri ?propertyURI ?object 
    OPTIONAL { 
      ?object rdfs:label|skos:prefLabel ?objectLabel . 
    } 
  } UNION { 
    {
      ?uri a/rdfs:subClassOf* ?typeURI .  
      ?propertyURI rdfs:domain ?typeURI .
    } UNION {
      BIND(rdf:type AS ?propertyURI)
    }    
    OPTIONAL { 
      ?propertyURI rdfs:range ?propertyRangeURI 
      OPTIONAL { 
       ?propertyRangeURI rdfs:label|skos:prefLabel ?propertyRangeLabel .
      }
    }
  }
  OPTIONAL { 
    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel .
  }
  OPTIONAL {
    ?propertyURI a ?propertyType .
  }
  OPTIONAL { 
    ?propertyURI rdfs:comment ?propertyComment . 
  }
}
[/#if]</textarea><br />
		
				SPARQL query for getting the subsumption tree for the range of a property:<br />
				<textarea name="propertyTreeQuery" rows="18" cols="90">[#if sparqlConfiguration.propertyTreeQuery??]${sparqlConfiguration.propertyTreeQuery}[#else]
CONSTRUCT { 
  ?propertyURI rdfs:range ?sp . 
  ?s rdfs:subClassOf ?oc . 
  ?sc rdfs:subClassOf ?s . 
  ?s rdfs:label ?label . 
  ?sp rdfs:label ?label . 
} WHERE { 
  { 
    ?propertyURI rdfs:range ?sp 
    OPTIONAL { 
      ?sp rdfs:label|skos:prefLabel ?label .
    }
  } UNION {
    { 
      ?s rdfs:subClassOf ?oc
    } UNION { 
      ?sc rdfs:subClassOf ?s 
    } OPTIONAL { 
      ?s rdfs:label|skos:prefLabel ?label .
    }
  }
}
[/#if]</textarea><br />

				SPARQL query for getting the subsumption tree of the project:<br />
				<textarea name="classTreeQuery" rows="24" cols="90">[#if sparqlConfiguration.classTreeQuery??]${sparqlConfiguration.classTreeQuery}[#else]
CONSTRUCT { 
  ?s rdfs:subClassOf ?oc . 
  ?sc rdfs:subClassOf ?s . 
  ?s rdfs:label ?label . 
  ?s rdf:count ?count . 
} WHERE { 
  { 
    SELECT (count(?foo) as ?count) ?s WHERE { 
      ?foo rdf:type ?s 
    } 
    GROUP BY ?s 
  } UNION { 
    ?s rdf:type owl:Class 
  } UNION { 
    ?s rdf:type rdfs:Class 
  } UNION { 
    ?s rdfs:subClassOf ?oc
  } UNION { 
    ?sc rdfs:subClassOf ?s
  } OPTIONAL { 
    ?s rdfs:label|skos:prefLabel ?label . 
  } 
}
[/#if]</textarea><br />

                       <h2>Delete project:</h2><br/>
                        <form method="POST">
                                <input type="hidden" name="passhash" value="${passhash}" />
                                <input type="hidden" name="operation" value="delete" />
                                Yes, I'm sure: <input type="checkbox" name="confirm" value="true" /><br/>
                                <input type="submit" value="Delete" />
                        </form>

                        <h2>Change password:</h2><br/>
                        <form method="POST">
                                New password:<br/>
                                <input type="password" name="newPass" /><br/>
                                New password again:<br/>
                                <input type="password" name="newPass2" /><br/>
                                <input type="hidden" name="passhash" value="${passhash}" />
                                <input type="submit" value="Change password" />
                        </form>

				
				<input type="submit" value="Change" />
			</form>
	[#else]			
			Insert password:<br/>
			<form method="POST">		
				<input type="password" name="password" /><br/>
				<input type="submit" value="Submit" />
			</form>
		
	[/#if]
	</div>
</body>
</html>
