[#ftl]
[#setting url_escaping_charset='UTF-8']
<html>
<body>

<h1>Locked resources</h1>

<h3><a href="lock_admin.shtml">refresh</a></h3>

[#list locked_resources as locked_resource]
	<div>${locked_resource} [<a href="lock_admin.shtml?uri=${locked_resource?url}">remove lock</a>]</div>
[/#list]

</body>
</html>
