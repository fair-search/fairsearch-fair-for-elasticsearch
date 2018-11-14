var elasticsearch = require('elasticsearch');
var Promise = require('bluebird');
var esParser = require('es-response-parser');
var log = console.log.bind(console);
var fs=require('fs');

var client = new elasticsearch.Client({
  host: 'localhost:9200',
  log: 'trace'
});

function dropIndex() {
  return client.indices.delete({
    index: 'test',
  });
}

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
}

function addAllToIndex(){
var data = JSON.parse(fs.readFileSync('../data/resume_dataset.json', 'utf8'));
for(var i=0; i<data.length; i++){
	if(data[i].id >= 1000){
		client.index({
		index: 'test',
		type: 'test',
		id: ""+data[i].id,
		body: {
			body: ""+data[i].body.body,
			gender: 'm'
		}
		});
	}else{
		client.index({
		index: 'test',
		type: 'test',
		id: ""+data[i].id,
		body: {
			body: ""+data[i].body.body,
			gender: 'f'
		}
		});
	}
	}
	
}

function dropIndex() {
  return client.indices.delete({
    index: 'test',
  });
}

Promise.resolve()
  //.then(dropIndex)
  .then(createIndex)
  .then(addAllToIndex);