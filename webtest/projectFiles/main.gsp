<html>

<head>
	<title><g:layoutTitle default="Grails" /></title>
	<p:css name='main' />
	<p:favicon src='images/favicon.ico' />
	<g:layoutHead />
</head>

<body>
	<div id="spinner" class="spinner" style="display:none;">
		<p:image src='spinner.gif' alt='Loading...' />
	</div>
	<div id="grailsLogo">
		<a href="http://grails.org"><p:image src='grails_logo.png' alt="Grails" /></a>
	</div>
	<p:javascript src="application" />
	<g:layoutBody />
</body>

</html>
