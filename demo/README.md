# Data Transparency Lab Demo-Application
This is a step-by-step tutorial to set up and run a Demo-Application of Elasticsearch with the FA*IR-search plugin on your local machine.
We highly recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) before you go through this tutorial since we will not explain the theory behind the plugin in this tutorial.

The demo consists of a html frontend, a node.js server which handles the requests from the frontend and sends the processed queries to an elasticsearch node with the fairsearch plugin.
![](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/res/demoInfrastructure.png)

For a more detailed explanation of the Demo-Application, and how it works, please visit our [tutorial.](TUTORIAL.md)

# How to run the Demo (Quick Version)
1. Download and install [Elasticsearch 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
2. Download and install the the snapshot build of the plugin [fairsearch-1.0-es6.2.4-snapshot.zip](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/fairsearch-1.0-es6.2.4-snapshot.zip)
3. Download and Install Node.js [from here](https://nodejs.org/en/download/)
4. Download the demo folder [from here]()
5. Start elasticsearch with `path\to\es\elasticsearch-6.2.4\bin>elasticsearch`
6. Create and fill a text Index with `path\to\project\elasticDemo\server>node server-ini.js`
7. Start the server with `path\to\project\elasticDemo\server>node server.js`
8. Open your browser with `http://localhost:8080/` 
9. Enter a Query and see the results.

# How to Install the Plugin

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
