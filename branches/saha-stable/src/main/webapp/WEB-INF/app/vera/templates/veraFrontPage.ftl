[#ftl]
[#setting url_escaping_charset='UTF-8']

<script type="text/javascript" src="app/scripts/vera/multiSelector.js">
</script>
<script type="text/javascript" src="app/scripts/vera/veraUtil.js">
</script>

<html>
<head>
<title>Vera - Front Page - ${veraVersion}</title>
[#include "veraCSS.ftl" /] 
</head>
<body>

<div id="loader_block" class="loader_block">
	<div class="loader_block_header">			
		Processing validation...
	</div>
	<div class="loader_block_body">
		<p class="loader_block_text">
		Wait a moment...
		</p>
		<img src="app/images/saha/loader.gif" border="0"/>
	</div>
</div>

<div id="whole">
<div class="top headline">
Vera - ${veraVersion}
</div>


<div class="left">
Load an existing project:
<form class="loose" id="existing" name="existing" action="vera_result.shtml" enctype="multipart/form-data" method="post">
  <select name="project">
      <option value="" selected="selected"></option>
    [#list projects as project]
      <option value="${project}">${project}</option>
    [/#list]
  </select>
  <a class="submit" href="javascript:submitForm('existing')">Open</a>
</form><br/>

Delete a project:
<form class="loose" id="deletion" name="deletion" action="vera.shtml" enctype="multipart/form-data" method="post">
  <select name="projectToDelete">
      <option value="" selected="selected"></option>
    [#list projects as project]
      <option value="${project}">${project}</option>
    [/#list]
  </select>
<a class="submit" href="javascript: submitForm('deletion')">Delete</a> 
</form><br/>
</div>

<form id="validation" name="input" action="vera_result.shtml" enctype="multipart/form-data" method="post">
 

<div class="main">
  Input schema file(s):<input type="file" id="schema_first" name="schema_file_1"><br/>
  
  <div class="filelist" id="schema_list">
  <strong class="underline">Selected schema files (max 4):</strong>
  </div>
  <br/>
  
  Input data file(s) or a saha project name: <input type="file" id ="data_first" name="data_file_1"><br/>
  
  <div class="filelist" id="data_list">
  <strong class="underline">Selected data files (max 4):</strong>
  </div>
  Saha projects:
  <select name="sahaProjectToLoad">
    <option value="" selected="selected"></option>
    [#list sahaProjects as project]
      <option value="${project}">${project}</option>
    [/#list]
  </select>
  <br/>
  If you want to save this validation as a new project in the database, insert the project name here:
  <input type="text" name="projectName"/><br/>
  Additional options:
  <br/>
  [#list options as option]
  	[#if option.showToUI?string == "true"]
  		${option.description}
  		<input type="checkbox" name="options" value="${option}" />
  		<br/>
  	[/#if]
  [/#list]
  
  <div class="loose">
  <a class="submit" href="javascript: submitForm('validation')">Validate</a>
  </div>
</div>

<div class="right">

Select existing schemas:
<select name="usedExistingSchemas" multiple="multiple">
	[#list existingSchemas as schema]	
		<option value="${schema}">${schema}</option> 
	[/#list]
</select>

<br/>
<br/>To validate models against schemas, input them in their respective input lists.
<br/>To validate models (or schemas) by themselves, leave either of the input lists empty.<br/><br/>

</div>

</form>

<script>  
<!--
  var schema_multi_selector = new MultiSelector( document.getElementById( 'schema_list' ), 'schema', 4 );
  schema_multi_selector.addElement( document.getElementById( 'schema_first' ) );

  var data_multi_selector = new MultiSelector( document.getElementById( 'data_list' ), 'data', 4 );
  data_multi_selector.addElement( document.getElementById( 'data_first' ) );
-->  
</script>

</div>
</body>
</html>