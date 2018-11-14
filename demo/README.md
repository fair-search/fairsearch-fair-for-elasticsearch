# Data Transparency Lab Demo-Application
This is a step-by-step tutorial to set up and run a Demo-Application of Elasticsearch with the FA*IR-search plugin on your local machine.
We highly recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) before you go through this tutorial since we will not explain the theory behind the plugin in this tutorial.

# How to run the Demo (Quick Version)
1. Download and install [Elasticsearch 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
2. Download and install the the snapshot build of the plugin [fairsearch-1.0-es6.2.4-snapshot.zip](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/fairsearch-1.0-es6.2.4-snapshot.zip)
3. Download and Install Node.js [from here](https://nodejs.org/en/download/)
4. Download the demo folder [from here](demo)
5. Start elasticsearch with `path\to\es\elasticsearch-6.2.4\bin>elasticsearch`
6. Create and fill a text Index with `path\to\project\elasticDemo\server>node server-ini.js`
7. Start the server with `path\to\project\elasticDemo\server>node server.js`
8. Open your browser with `http://localhost:8080/` 
9. Enter a Query and see the results.

# Setup the Project

### Install Elasticsearch
The first thing we need is Elasticsearch. Download [Elasticsearch 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
Once you have downloaded it, unzip it to a location of your choice. In this Tutorial, we will unzip it to "C:\Users\Demo\".
You have now installed Elasticsearch! You can run it with `C:\Users\Demo\elasticsearch-6.2.4\bin>elasticsearch`.

Elasticsearch will run on the port 9200 per default.

### Install the Plugin
Download the the snapshot build of the plugin [fairsearch-1.0-es6.2.4-snapshot.zip](https://github.com/fair-search/fairsearch-elasticsearch-plugin). This is just one way to get the current build of the plugin. See [README.md](README.md) for other options.
We will assume that we downloaded the plugin snapshot to the directory `C:\Users\Downloads\`. 
Navigate to the bin folder of your Elasticsearch folder:
```
C:\Users\Demo\elasticsearch-6.2.4\
cd bin
C:\Users\Demo\elasticsearch-6.2.4\bin>
```
and then enter the following command:
```
C:\Users\Demo\elasticsearch-6.2.4\bin>
elasticsearch-plugin install file:///C:\Users\Downloads\fairsearch-1.0-es6.2.4-snapshot.zip
```
You have now installed the fairsearch plugin!

### Install Node.js
Download Node.js [from here](https://nodejs.org/en/download/) and install it on your local machine.

### Create the Project Directory
Our Project directory will be located in `C:\Users\Demo\App\`. We will use [npm](https://www.npmjs.com/) to install all important dependencies for our node server.
Run the following command:
```
C:\Users\Demo\App> npm init
```
and follow the instructions. This will create a local npm module folder in our project directory.
We will need some dependencies for the server to work with Elasticsearch and everything we need. Execute the following commands to install everything in your local module:
```
C:\Users\Demo\App> npm install express
C:\Users\Demo\App> npm install elasticsearch
C:\Users\Demo\App> npm install xhr2
C:\Users\Demo\App> npm install es-response-parser
C:\Users\Demo\App> npm install bluebird
C:\Users\Demo\App> npm install JSON
C:\Users\Demo\App> npm install fs
```
### Create the Server
Create the a file called `server.js` in the directory `C:\Users\Demo\App\`.
Add the following lines to the file:
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
