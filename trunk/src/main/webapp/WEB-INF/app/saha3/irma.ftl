[#ftl]
[#setting url_escaping_charset='UTF-8']

<html>
<head>

	<link rel="stylesheet" type="text/css" href="scripts/dijit/themes/tundra/tundra_rtl.css" />
	<link rel="stylesheet" type="text/css" href="scripts/dijit/themes/tundra/tundra.css" />
	<link rel="stylesheet" type="text/css" href="scripts/dojo/resources/dojo.css" />
	<link rel="dojo/dijit/themes/dijit.css" />
	
	<link rel="stylesheet" type="text/css" href="http://wrk-1.seco.hut.fi:8080/irma/app/css/haresponse.css" /> 
	<link rel="stylesheet" type="text/css" href="http://wrk-1.seco.hut.fi:8080/irma/app/css/sapo.css" />
	
	<script type="text/javascript">
		djConfig = {
	        isDebug: true,
	        parseOnLoad: true,
	        useXDomain: true,
	        modulePaths: {
	            "semweb": "http://wrk-1.seco.hut.fi:8080/irma/scripts/semweb"
	        },
	        xdWaitSeconds: 3
		};
	</script>

	<script type="text/javascript" src="http://o.aolcdn.com/dojo/1.0.2/dojo/dojo.xd.js"></script>

	<script type="text/javascript">
		dojo.require("dojo.parser");
		dojo.require("dijit.Menu");
		dojo.require("dijit.layout.ContentPane");
		dojo.require("dijit.TitlePane");
		dojo.require("dijit._Widget");
		dojo.require("dijit._Templated");
		
		dojo.require("semweb.widget.Autocompletion");
		dojo.require("semweb.widget.HAResponse");
		dojo.require("semweb.widget.RelatedContext");
		dojo.require("semweb.widget.ContextCreator");
		dojo.require("semweb.widget.Hierarchy");
	</script>
        
	<!-- load dwr engine -->
	<script type='text/javascript' src='/smetana/dwr/engine.js'></script>
                
	<!-- load dwr interface definition for hierarchical autocompletion service -->
	<script type="text/javascript" src="http://wrk-1.seco.hut.fi:8080/irma/dwr/interface/YKLAutocompletionService.js"></script>
	<script type="text/javascript" src="http://wrk-1.seco.hut.fi:8080/irma/dwr/interface/YKLKeskuspalvelu.js"></script> 
	   
	<script>
		YKLKeskuspalvelu._path = 'http://wrk-1.seco.hut.fi:8080/irma/dwr'
		YKLAutocompletionService._path = 'http://wrk-1.seco.hut.fi:8080/irma/dwr'
		
		dwr.engine.isCrossDomain=true;
		
		function test(uri,label) {
			console.log("test: " + uri + " " + label);
		}
	</script>
	
</head>
<body class="tundra">
	
	<br/>
	
	<div class="result"><ul id="lista"></ul></div>
	<div dojoType="semweb.widget.Autocompletion" class="auto">
		<div dojoType="semweb.widget.HAResponse" callback="test" acservice="YKLAutocompletionService" cservice="YKLKeskuspalvelu"></div>
	</div>

</body>
</html>