<!doctype html>
<html>
	<head>
		<meta name="layout" content="error">
		<title>Hook</title>
	</head>
	<body>
		<g:if test="${flash.message}">
		<div class="message" role="status">${flash.message}</div>
		</g:if>
	</body>
</html>
