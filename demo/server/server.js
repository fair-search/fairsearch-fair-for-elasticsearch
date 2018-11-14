var express = require('express');
var app = express();
var log = console.log.bind(console);
var XMLHttpRequest = require('xhr2');
var fs=require('fs');

app.get('/', function(req, res) {
  res.sendFile(__dirname + "/public/" + "demo.html");
});
app.get('/stylesheet.css', function(req, res) {
  res.sendFile(__dirname + "/public/" + "stylesheet.css");
});
app.get('/eslogo.png', function(req, res) {
  res.sendFile(__dirname + "/public/" + "eslogo.png");
});
app.get('/dtl1.png', function(req, res) {
  res.sendFile(__dirname + "/public/" + "dtl1.png");
});
app.get('/mtables.html', function(req, res) {
  res.sendFile(__dirname + "/public/" + "mtables.html");
});
app.get('/male.png', function(req, res) {
  res.sendFile(__dirname + "/public/" + "male.png");
});
app.get('/female.png', function(req, res) {
  res.sendFile(__dirname + "/public/" + "female.png");
});

app.get('/mtables', function(req, res) {
	var xhr = new XMLHttpRequest();
	xhr.addEventListener("readystatechange", function () {
	if (this.readyState === 4) {
		var mtables = JSON.parse(this.responseText).hits.hits;
		console.log(mtables);
		var answer = [];
		for(var i=0; i<mtables.length; i++){
			var table ="";
			for(var j=0; j<mtables[i]._source.mtable.length; j++){
					table += mtables[i]._source.mtable[j];
					if(j<mtables[i]._source.mtable.length-1){
						table+=", ";
						}
				}
			var tableArr = [mtables[i]._id, table];
				answer.push(tableArr);
			}
			res.status(200);
			res.send(answer);
	}
	});
	xhr.open("GET", "http://localhost:9200/_fs/_mtable");
	xhr.send();
});

app.get('/searchunfair/:k/:q', function(req, res){
	var q = "'"+req.params.q+"'";
	var k = req.params.k;
	var xhr = new XMLHttpRequest();
	var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": q}}});
	xhr.addEventListener("readystatechange", function () {
	if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var answer = [];
		if(response.hits.hits.length === 0){
			res.status(404);
			res.send();
		}else{
		for(var i=0; i<response.hits.hits.length; i++){
			var person = [response.hits.hits[i]._source.body, response.hits.hits[i]._source.gender];
				answer.push(person);
			}
			
			res.status(200);
			res.send(answer);
		}
	}
	});
	xhr.open("POST", "http://localhost:9200/test/_search");
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.send(data);
});

app.get('/searchfair/:k/:p/:alpha/:q', function(req, res){
	var p = req.params.p;
	var alpha = req.params.alpha;
	var q = '"'+req.params.q+'"';
	var k = req.params.k;
	createMtableAndExecuteQuery(k,p,alpha,q, req, res);
	console.log(q);
	
});

app.listen(8080, function(){
console.log('Demo server up and running.');
});

function createMtableAndExecuteQuery(k ,p, alpha, query, req, res){
		var xhr = new XMLHttpRequest();
		var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": query}}});
		xhr.addEventListener("readystatechange", function() {
		if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var realK = response.hits.hits.length;
		if(realK === 0){
		res.status(404);
		res.send();
		return;
		}
		console.log(realK);
		var xhrTable = new XMLHttpRequest();
		xhrTable.addEventListener("readystatechange", function() {
			if(this.readyState === 4) {
					executeFairQueryWithAdjustedParameters(k,p,alpha,query, req, res);
			}
		
		});
		xhrTable.open("POST", "http://localhost:9200/_fs/_mtable/"+p+"/"+alpha+"/"+realK);
		xhrTable.send();
		return response.hits.hits.length;
	}
	});
	xhr.open("POST", "http://localhost:9200/test/_search");
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.send(data);
}

function executeFairQueryWithAdjustedParameters(k,p,alpha,q, req, res){
	var data = JSON.stringify({"from" : 0, "size" : k, "query": {"match": {"body": q}}, "rescore": {"window_size": k, "fair_rescorer": {"protected_key": "gender","protected_value": "f","significance_level": alpha,"min_proportion_protected": p}}});

	var xhr = new XMLHttpRequest();
	xhr.withCredentials = true;
	xhr.addEventListener("readystatechange", function () {
	if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var answer = [];
		if(response.status === 500){
			res.status(404);
			res.send();
		}else{
			for(var i=0; i<response.hits.hits.length; i++){
				var person = [response.hits.hits[i]._source.body, response.hits.hits[i]._source.gender];
				answer.push(person);
			}
			res.status(200);
			res.send(answer);
		}
	}
	});
	xhr.open("POST", "http://localhost:9200/test/_search");
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.send(data);
}