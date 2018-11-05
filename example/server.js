var express = require('express');
var app = express();
var elasticsearch = require('elasticsearch');
var Promise = require('bluebird');
var esParser = require('es-response-parser');
var log = console.log.bind(console);
var XMLHttpRequest = require('xhr2');

var client = new elasticsearch.Client({
  host: 'localhost:9200',
  log: 'trace'
});

app.get('/', function(req, res) {
  res.sendFile(__dirname + "/public/" + "index.html");
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



app.listen(8080, function(){
console.log('Demo server up and running.');
});

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
/*
Promise.resolve()
  .then(dropIndex)
  .then(createIndex)
  .then(addAllToIndex);
 */