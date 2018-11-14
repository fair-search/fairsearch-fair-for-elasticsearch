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

### The Data
Every document has two fields:
"body" which contains the resume-text and "gender" which contains the character `f` for female and `m` for male.
Like this:
```
{
"index": "test",
"type": "test", 
"id": 1000,
"body": {
	"body": "Eddy Example - Computer Scientist - CV ...",
	"gender": "m"
	}
}
```
To learn more about our sample data, please visit our [data description](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/demo/data/README.md).

### Search unaware
The demo-app is able to perform "a normal" full-text search query and show the results. The server provides the following endpoint to perform this unaware query.
```
app.get('/searchunfair/:k/:q', function(req, res){
	var q = "'"+req.params.q+"'";	//get the query
	var k = req.params.k;	//get the desired length of the ranking
	var xhr = new XMLHttpRequest();
	var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": q}}}); //JSON query for ES
	xhr.addEventListener("readystatechange", function () {
	if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var answer = [];
		if(response.hits.hits.length === 0){
			res.status(404);
			res.send();
		}else{
		for(var i=0; i<response.hits.hits.length; i++){	//built a simple response array for the frontend
		  var person = [response.hits.hits[i]._source.body,response.hits.hits[i]._source.gender];
		  answer.push(person);
		}		
		res.status(200);
		res.send(answer);	// send the response array to the frontend
		}
	}
	});
	xhr.open("POST", "http://localhost:9200/test/_search");		//The ES node runs usually on port 9200
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.send(data);
});
```
### FA*IR Search
Before we can make a fairsearch request to our es node, we have to create the mtable for the desired type of ranking.
What a mtable is and why we need one is answered in the [FA*IR Ranking paper](https://arxiv.org/abs/1706.06368).
Once a mtable is created, it is stored within elasticsearch. If you want to know which mtables already exist, you can adress the following endpoint:

```
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
```

The unique identifier for a mtable is the tuple (k,p,alpha) where k is the length of the ranking, p the desired proportion of protected candidates and alpha the significance level.

The Demo is built such that every time we perform a fair query, we will create the corresponding mtable. The following endpoint will initiate the process:

```
app.get('/searchfair/:k/:p/:alpha/:q', function(req, res){
	var p = req.params.p;
	var alpha = req.params.alpha;
	var q = '"'+req.params.q+'"';
	var k = req.params.k;
	createMtableAndExecuteQuery(k,p,alpha,q, req, res);
	console.log(q);
	
});
```
The first route leads us to

```
function createMtableAndExecuteQuery(k ,p, alpha, query, req, res){
		var xhr = new XMLHttpRequest();
		var data = JSON.stringify({"from" : 0, "size" : k,"query": {"match": {"body": query}}});
		xhr.addEventListener("readystatechange", function() {
		if (this.readyState === 4) {
		var response = JSON.parse(this.responseText);
		var realK = response.hits.hits.length;
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
```
The following taks are done in this method:
1. Perform an unaware request to see how many results are there.
	We have to do this, because if our desired ranking length is 20 but there are only 10 matches in our index, we have to 		built a mtable for k=10 instead of k=20. The variable `realK` holds this result size <=k.
2. Built the mtable with parameters (realK, p ,alpha)
3. Call `executeFairQueryWithAdjustedParameters`

Now we can be sure, that the mtable exists and we can perorm the FA*IR query with `executeFairQueryWithAdjustedParameters`.

```
function executeFairQueryWithAdjustedParameters(k,p,alpha,q, req, res){
	var data = JSON.stringify({"from" : 0, "size" : k, "query":
		{"match": {"body": q}}, "rescore":
		{"window_size": k, "fair_rescorer":
		{"protected_key": "gender","protected_value": "f","significance_level": alpha,"min_proportion_protected": p}}});
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
```
The first part of this methods simply builts the JSON query with our desired parameters and sends it to our elasticsearch node.
The second part looks exactly like the unaware query and will also built a simplified response for our frontend.

# server-ini.js
At the first start of your demo-application, you have to perform `>node server-ini.js` in order to create an index in elasticsearch and insert the testdata in this index.
The `server-ini.js` uses an elasticsearch package installed via npm.
```
var client = new elasticsearch.Client({
  host: 'localhost:9200',
  log: 'trace'
});
```
creates a connection to your local elasticsearch node. If your es node runs on a different port, you have to change the port here.

Another important part of this file are the last four lines:

```
Promise.resolve()
  //.then(dropIndex)
  .then(createIndex)
.then(addAllToIndex);
```
If you have already indexed your elasticsearch node, remove the comment from `.then(dropIndex)`. If you do `>node server-ini.js` now, the script will remove the `test` index and then create it with the example data.
