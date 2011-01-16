[#ftl]
[#setting url_escaping_charset='UTF-8']

<style type="text/css">
div.top {
	position: absolute;
	margin-bottom: 80%;
	margin-left: 10%;
	margin-right: 10%;
	height: 40px;
	width: 80%;
	align: center;
	text-align: center;
	border-bottom-width: 5px;
	border-bottom-style: solid;
}

div.main {
	position: absolute;
	padding-top: 10px;
	margin-left: 30%;
	margin-right: 35%;
	margin-top: 50px;
	width: 35%;
	align: center;
	text-align: right;
	width: 35%;
}

div.left {
	position: absolute;
	padding-top: 10px;
	margin-top: 50px;
	margin-right: 70%;
	margin-left: 5%;
	width: 25%;
	align: left;
	text-align: center;
}

div.right {
	position: absolute;
	padding-top: 10px;
	margin-top: 50px;
	margin-left: 70%;
	margin-right: 5%;
	width: 25%;
	align: right;
	text-align: center;
}

div.filelist {
	color: #000000;
	min-height: 80px;
	border: inset gray;
	background: #b5d5f1;
	font-size: 12pt;
}

div.item {
	margin-top: 5px;
	display: inline;
	vertical-align: middle;
}

ul.level {
	list-style-position: inside;
	margin-left: 1.5em;
	padding-left: 0;
}

ul.item {
	margin: 0;
	padding: 0;
}

li.item {
	font-size: 9pt;
	list-style-type: none;
}

.headline {
	font-size: 28px;
	display: table-cell;
	vertical-align: middle;
	text-align: center;
}

.outset {
	list-style-type: none;
	color: #000000;
	background-color: #b5d5f1;
	padding: 3px;
	border: medium outset;
	vertical-align: middle;
}

.loose {
	margin: 10px 0px 10px 0px;
}

.submit {
	font-family: arial, verdana, "lucida console", sans-serif;
	text-decoration: none;
	color: #000000;
	background-color: #e0e0e0;
	border: 2px outset #000;
	outline: black groove thin;
	padding: 2px 5px 2px 5px;
}

body {
	font-family: verdana, arial, "lucida console", sans-serif;
	color: #ffffff;
	background-color: #6890cb;
}

span.link {
	cursor: pointer;
}

img {
	vertical-align: middle;
}

/*----------------------------------------------------------------------------*/
	/* Anchors
/*----------------------------------------------------------------------------*/
a.link {
	text-decoration: none;
	color: #000000;
	cursor: pointer;
}

/* --- Hover tooltip styles --- */ 

[#include "veraCSS_tooltip.css" parse=false /]

/*----------------------------------------------------------------------------*/
	/* Loader-block
/*----------------------------------------------------------------------------*/
div.loader_block {
	position: absolute;
	border: 2px solid black;
	border-style: outset;
	width: 30%;
	height: 150px;
	background-color: #b5d5f1;
	margin-left: 40%;
	margin-top: 200px;
	visibility: hidden;
	padding: 5px;
	z-index: 5;
}

div.loader_block_header {
	position: relative;
	margin-top: 0px;
	margin-left: 0px;
	width: 100%;
	height: 15px;
	color: #0d3483;
}

div.loader_block_body {
	position: relative;
	margin-top: 0px;
	text-align: center;
	border-top: 1px solid gray;
	padding-top: 10px;
	width: 100%;
}

p.loader_block_text {
	font-size: 1.3em;
}
</style> 