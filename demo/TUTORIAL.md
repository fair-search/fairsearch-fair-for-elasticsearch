# Demo-Application Tutorial
This is a step-by-step tutorial about how the demo application works. If you just want to try the demo, we recommend installing the demo like described [here](README.md).
We highly recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) before you go through this tutorial since we will not explain the theory behind the plugin in this tutorial.

:warning: The plugin is a work in progress until end of November 2018. This Tutorial only describes how you can test the current snapshot-version of the plugin.

# Goal of this Tutorial
Our goal with this tutorial is to set up a Node.js Web search application which uses Elasticsearch with the fairsearch plug-in.
However there are uncountable ways to use Elasticsearch and many different possible infrastructures, we want to provide an easy tutorial to get a feeling how to work with the fairsearch plug-in and Elasticsearch.
Our target infrastructure will look like this:

![](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/res/demoInfrastructure.png)

# Installation
Before you go through this tutorial, please install the demo-application as described [here](README.md).

# The Server
In this parrt we will describe the [server.js](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/demo/server/server.js) file, which contains the node.js server. The server will receive search requests, process them and then send them to your elasticsearch node. After receiving the response from elasticsearch, the server will again simplyfie the es-response and then send the results to the fronted. 
```
var express = require('express');
var app = express();
var elasticsearch = require('elasticsearch');
var Promise = require('bluebird');
var esParser = require('es-response-parser');
var log = console.log.bind(console);
var XMLHttpRequest = require('xhr2');
var fs=require('fs');

var client = new elasticsearch.Client({
  host: 'localhost:9200',
  log: 'trace'
});

app.get('/', function(req, res) {
  res.sendFile(__dirname + "/public/" + "index.html");
});

app.listen(8080, function(){
console.log('Demo server up and running.');
});
```
As we said above, Elasticsearch will run by default on port 9200. The elasticsearch package will create a connection to elasticsearch and helps us later.

#### Create an Index with documents
For this example we will create an Index in Elasticsearch called `test` and fill it with simple test data. The following function will create the Index:
```
function createIndex() {
  return client.indices.create({
    index: 'test',
	body:{
		settings:{
			number_of_shards: '1',
			number_of_replicas: '1'
		},
		mappings:{
			test:{
				properties:{
					gender:{
						type: 'text',
						store: 'true'
					}
				}
			}
		}
	}
	
  });
```
Our test documents will look like this:
```
{"index": "test", "type": "test", "id": 1, "body": {"body": "hello hello hello hello hello hello hello hello hello hello", "gender": "m"}}
```
We stored the test documents in `example_Data.json` we will insert every document through the following function:

```
function addAllToIndex(){
var data = JSON.parse(fs.readFileSync('example_data.json', 'utf8'));
for(var i=0; i<data.length; i++){
	client.index({
		index: 'test',
		type: 'test',
		id: ""+data[i].id,
		body: {
			body: ""+data[i].body.body,
			gender: ""+data[i].body.gender
		}
		});
	}
}
```
Add the following lines to the bottom of the `server.js` file:
```
Promise.resolve()
  .then(createIndex)
  .then(addAllToIndex);
```
this will create the index and add all files with the server start.

#### Handle Client Requests
We will implement two types of requests:
1. A fair query
2. An unfair query
To keep everything easy, we will model both as a get request to the server. Add the following lines to the `server.js` file:

```
app.get('/searchunfair/:k/:q', function(req, res){
	var q = "'"+req.params.q+"'";
	var k = req.params.k;
	var xhr = new XMLHttpRequest();
	var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": q}}});
	xhr.addEventListener("readystatechange", function () {
	if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var answer = [];
		for(var i=0; i<response.hits.hits.length; i++){
			var person = [response.hits.hits[i]._source.body, response.hits.hits[i]._source.gender];
				answer.push(person);
			}
			res.status(200);
			res.send(answer);
	}
	});
	xhr.open("POST", "http://localhost:9200/test/_search");
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.send(data);
});
```
This will handle requests like `GET localhost:8080/searchunfair/10/hello` a completly default query to elasticsearch.
However we assume here, that the documents indexed in your Elasticsearch node have a field called `gender`. We will come to that later.

For a fair query we need the parameters k,p and alpha. And we have to create the mtable before we send the fair request. Add the following lines to `server.js`:

```
app.get('/searchfair/:k/:p/:alpha/:q', function(req, res){
	var k = req.params.k;
	var p = req.params.p;
	var alpha = req.params.alpha;
	var q = '"'+req.params.q+'"';
	console.log(q);
	
	var xhrTable = new XMLHttpRequest();
	xhrTable.addEventListener("readystatechange", function() {
		if(this.readyState == 4){
			var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": q}}, "rescore": {"window_size" : k, "fair_rescorer": {"protected_key": "gender","protected_value": "f","significance_level": alpha,"min_proportion_protected": p}}});

			var xhr = new XMLHttpRequest();
			xhr.withCredentials = true;
			xhr.addEventListener("readystatechange", function () {
			if (this.readyState === 4) {
				var response = JSON.parse(this.responseText);
				var answer = [];
				for(var i=0; i<response.hits.hits.length; i++){
					var person = [response.hits.hits[i]._source.body, response.hits.hits[i]._source.gender];
					answer.push(person);
				}
			res.status(200);
			res.send(answer);
		}
	});
		xhr.open("POST", "http://localhost:9200/test/_search");
		xhr.setRequestHeader("Content-Type", "application/json");
		xhr.send(data);
		}
	
	});
	xhrTable.open("POST", "http://localhost:9200/_fs/_mtable/"+p+"/"+alpha+"/"+k);
	xhrTable.send();
});

```
This will get the request for a fair query and then creates the corresponding mtable. After that the fair query will be sent to elasticsearch.
### Create a Frontend with HTML
For this tutorial, the following frontend will be sufficient:
```
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en"> 
	<head> 
		<meta http-equiv="content-type" content="text/html; charset=utf-8">
		<title>FA*IR Example</title> 
	</head>
	<body>
		<button onclick="fairQuery()" style="width: 100px; height: 50px;">FA*IR Search</button>
		<p/>
		<button onclick="unfairQuery()" style="width: 100px; height: 50px;">Unfair Search</button>
		<div id="ranking"> Rankings
		</div>
	</body>
	<script>
	function unfairQuery(){
	var http = new XMLHttpRequest();
	var url = "/searchunfair/10/hello";
	http.open("GET", url, true);
	http.onreadystatechange =  function(){
	if(http.readyState === 4 && http.status === 200) {
	var arr = JSON.parse(http.responseText);
	var ranking = document.getElementById("ranking");
	var html = "";
	for(var i = 0; i<arr.length; i++){
		html +="<li>"+arr[i][0]+"</li>";
		html+="<p></p>";
	}
	ranking.innerHTML=html;
	}
	};
	
	http.send();
}
	function fairQuery(){
	var http = new XMLHttpRequest();
	var url = "/searchfair/10/0.8/0.1/hello";
	http.open("GET", url, true)
	http.onreadystatechange =  function(){
	if(http.readyState === 4 && http.status === 200) {
	var arr = JSON.parse(http.responseText);
	var ranking = document.getElementById("ranking");
	var html = "Rankings<p/>";
	for(var i = 0; i<arr.length; i++){
		html +="<li>"+arr[i][0]+"</li>";
		html+="<p></p>";
	}
	ranking.innerHTML=html;
	}
	};
	
	http.send();
}
	</script>
</html>
```
This will perform a Top10 query depending on which button you click. After receiving the response from the server, both methods will insert the response as a list into the html document.
### Run everything
First we have to start Elasticsearch. Open the bin directory in your elasticsearch folder and type
`C:\Users\Demo\elasticsearch-6.2.4\bin>elasticsearch`
Wait until Elasticsearch has started.

After that we have to start the server. Open the Project directory and type:
`C:\Users\Demo\App>node server.js`

Now open your browser on `localhost:8080`.
Clicking on one of the buttons will show you the results.
