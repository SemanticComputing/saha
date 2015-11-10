[#ftl]
[#setting url_escaping_charset='UTF-8']
<html>
<head>
<title>SAHA3 Admin Screen</title>
<script type='text/javascript' src='../dwr/engine.js'></script>
<script type='text/javascript' src='../dwr/util.js'></script>
<script type='text/javascript' src='../dwr/interface/SahaProjectRegistry.js'></script>
<script type='text/javascript' src='../dwr/interface/SahaChat.js'></script>
<script>
function close_project(model) {
	document.body.style.cursor="progress";
	SahaProjectRegistry.closeSahaProject(model, {
		callback:function(dataFromServer) {
			document.body.style.cursor="";
			location.href='admin.shtml';
		}
	});
}
function gc() {
	SahaProjectRegistry.gc();
	location.href='admin.shtml';
}
function sendAdminMessage() {
	var text = dwr.util.getValue("admin_msg");
	SahaChat.broadcast("<strong style=\"color:crimson\">admin</strong>",text);
	dwr.util.setValue("admin_msg","");
}
</script>
</head>
<body>
<strong>SAHA3 Admin Screen</strong>

<hr/>

<h2>Projects</h2>
<div style="font-family:monospace;font-size:9pt">
	[#list allProjects as project]
		<div>
			<!--<a href="javascript:remove_project('${project}')" style="color:crimson">[remove]</a>-->
			<a href="../${project}/index.shtml">${project}</a>
			<a href="../${project}/export.shtml" style="color:grey;text-decoration:none;">[rdf]</a>
			[#if openedProjects?seq_contains(project)]
				<a href="javascript:close_project('${project}')" style="color:black;">[close]</a>
			[/#if]
		</div>
	[/#list]
</div>

<h2>Broadcast message</h2>
<div style="font-family:monospace;font-size:9pt">admin says</div>
<input id="admin_msg" style="width:180px;" onkeypress="dwr.util.onReturn(event,sendAdminMessage)" />

<h2>Statistics</h2>
<div style="font-family:monospace;font-size:9pt">memory usage: ${mem} MiB <a href="javascript:gc();" style="color:black;">[gc]</a></div>
<div style="font-family:monospace;font-size:9pt">uptime: ${uptime} </div>

<h2>Add Project</h2>
<div style="font-family:monospace;font-size:9pt">
	<form id="form" action="../service/import_project/" enctype="multipart/form-data" method="post">
	<input type="hidden" name="allow_rewrite" value="true" />
	name: <input name="modelName" id="modelName" type="text"><br/>
	file: <input name="file" id="file" type="file"><br/>
	<input type="radio" name="operation" value="import" checked="checked">  <strong>add</strong> - import and index model 
	(if project exists, appends the new triples to the old model and does a full re-indexing)<br/>
	<input type="radio" name="operation" value="merge"> <strong>merge</strong> - add model to existing project 
	(indexes only the new triples, use 'add' if the appended model is large)<br/>
	<input type="radio" name="operation" value="rewrite"> <strong style="color:red">rewrite</strong> - clear existing project and add new<br/>
	<input type="submit" value="add" style="margin-top:5px">
	
	</form>
</div>

<h2>Locked resources</h2>
<div style="font-family:monospace;font-size:9pt">
	[#list locks as lock]
		<div>${lock.key}</div>
		<ul>
			[#list lock.value as resource]
				<li>${resource}</li>
			[/#list]
		</ul>
	[/#list]
</div>

<h2>Log</h2>
<div style="font-family:monospace;font-size:9pt">
	<pre>[#list messages?reverse as message]${message}[/#list]</pre>
</div>

<h2>Backup Log</h2>
<div style="font-family:monospace;font-size:9pt">
	[#list backupMessages?reverse as message]
		<div>${message}</div>
	[/#list]
</div>
</body>
</html>
