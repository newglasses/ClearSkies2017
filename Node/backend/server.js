'use strict';

var http = require('http');
var url = require('url');
var querystring = require('querystring');

http.createServer(function(request, response) {

	// http://localhost:8080/getUser?id=4
	// http://localhost:8080/api/demo?lat=54.640891&lng=-5.941169100000025&aurora=true&iss=true

	// TODO: If anything is undefined then redo??

	var urlValue = request.url;
	var pathname = url.parse(urlValue).pathname; 
	var query = url.parse(urlValue).query;

	var queryData = url.parse(request.url, true).query;

	var lat = queryData.lat;
	var lng = queryData.lng;
	var aurora = queryData.aurora;
	var iss = queryData.iss;

	// Check that params are received
	console.log("****************************" + "\n" +
				"Pathname: " + pathname + "\n" + 
				"Query: " + query + "\n" + 
				"lat: " + lat + "\n" + 
				"lng: " + lng + "\n" + 
				"aurora: " + aurora + "\n" + 
				"iss: " + iss + "\n" +
				"****************************")

	// Return demo data
	var results = {
      		"_results": [
        		{'type': 'demo', 'weatherTier': 0, 'onForecast': "1471558560"}
			] 
	};

	response.writeHead(200, {"Content-Type": "application/json"}); response.end(JSON.stringify(results));
  	

}).listen( 8080, function() {
	console.log('Server listening on port 8080');

} );