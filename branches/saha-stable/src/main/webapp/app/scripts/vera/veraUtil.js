function submitForm(formName)
{
	showLoader();
	document.getElementById(formName).submit();
}

function showLoader()
{	
		document.body.style.backgroundColor = '#000000'; 
		document.getElementById("whole").style.opacity = '0.5';
		document.getElementById("loader_block").style.visibility = 'visible';	
}