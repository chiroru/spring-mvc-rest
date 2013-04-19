<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Home</title>
<link rel="stylesheet" href="<c:url value="/resources/css/blueprint/screen.css" />" type="text/css" media="screen, projection" />
<link rel="stylesheet" href="<c:url value="/resources/css/blueprint/print.css" />" type="text/css" media="print" />
<!--[if lt IE 8]><link rel="stylesheet" href="<c:url value="/resources/blueprint/ie.css" />" type="text/css" media="screen, projection"><![endif]-->
<script type="text/javascript" src="<c:url value='/resources/js/jquery-1.7.2.min.js' />" /></script>
</head>
<body>
  <div class="container">
    <h1 class="alt">Spring MVC 3.2 Rest Sample - Ajax</h1>
    <div>
      <a href="${pageContext.request.contextPath}">back</a>
    </div>
    <hr>
    <div>
      <a onclick="script:profilesJson()">profiles(json)</a>
      <div id="profilesJson"></div>
    </div>
    <div>
      <a onclick="script:profilesXml()">profiles(xml)</a>
      <div id="profilesXml"></div>
    </div>
  </div>
</body>
<script type="text/javascript">
jQuery.extend({
    stringify : function stringify(obj) {
        var t = typeof (obj);
        if (t != "object" || obj === null) {
            // simple data type
            if (t == "string") obj = '"' + obj + '"';
            return String(obj);
        } else {
            // recurse array or object
            var n, v, json = [], arr = (obj && obj.constructor == Array);
 
            for (n in obj) {
                v = obj[n];
                t = typeof(v);
                if (obj.hasOwnProperty(n)) {
                    if (t == "string") v = '"' + v + '"'; else if (t == "object" && v !== null) v = jQuery.stringify(v);
                    json.push((arr ? "" : '"' + n + '":') + String(v));
                }
            }
            return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
        }
    }
})
  function profilesJson() {
    jQuery.ajax({url : '${pageContext.request.contextPath}/rest/profiles', type : 'GET', dataType : 'json', cache: false, 
      success : function(messages) {
        jQuery('#profilesJson').text(jQuery.stringify(messages));
/*         jQuery.each( messages, function( key, value ) {
            jQuery( '#profilesJson' ).append( '<p>' + key + ': ' + value + '</p>' );
        } ); */
      },
      error : function(xhr) {
        alert('error');
      }
    })
  };
  
  function profilesXml() {
	    jQuery.ajax({url : '${pageContext.request.contextPath}/rest/profiles', type : 'GET', dataType : 'xml', cache: false, 
	      success : function(messages) {
	        jQuery('#profilesXml').text(jQuery(messages).text());
	/*         jQuery.each( messages, function( key, value ) {
	            jQuery( '#profilesJson' ).append( '<p>' + key + ': ' + value + '</p>' );
	        } ); */
	      },
	      error : function(xhr) {
	        alert('error');
	      }
	    })
	  };
</script>
</html>
