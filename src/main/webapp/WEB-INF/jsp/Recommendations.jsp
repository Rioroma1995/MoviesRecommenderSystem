<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta lang="uk-UA">
	<title>RS</title>
	<link rel="stylesheet" type="text/css" href="../../resources/css/styles.css">
</head>
<body>
	<div class="container">
		<c:forEach items ="${books}" var = "book" varStatus="loop">
			<div class="book book-${loop.index}">
				<div class="cover">
					<div class="desc">
						<h2>${book.title}</h2>
					</div>
					<img src="${book.img}">
				</div>
			</div>
		</c:forEach>
	</div>
</body>
</html>