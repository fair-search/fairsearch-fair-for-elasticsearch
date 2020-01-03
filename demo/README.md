# Demo Application

This is a step-by-step tutorial to set up and run a Demo-Application of Elasticsearch with the FA*IR-search plugin on your local machine.
We highly recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) before you go through this tutorial since we will not explain the theory behind the plugin in this tutorial.

The demo consists of a html frontend, a node.js server which handles the requests from the frontend and sends the processed queries to an elasticsearch node with the fairsearch plugin.

![](https://github.com/fair-search/fairsearch-elasticsearch-plugin/blob/master/res/demoInfrastructure.png)

# How to Run the Demo (Quick Start)

1. Download and install [Elasticsearch 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
2. Download and install the the snapshot build of the plugin [fairsearch-1.0-es6.2.4-snapshot.zip](https://fair-search.github.io/fair-reranker/fairsearch-1.0-es6.2.4-snapshot.zip) (see [this readme](https://github.com/fair-search/fairsearch-fair-for-elasticsearch/blob/master/README.md) for installation instructions)
3. Download and Install [Node.js](https://nodejs.org/en/download/)
4. Download the the data/ and server/ sub-directories of the [demo/ folder](.)
5. Start elasticsearch: `path/to/elasticsearch-6.2.4/bin> elasticsearch`
6. Initialize your index with some test data: `path/to/demo/server> nodejs server-ini.js`
7. Start the demo: `path/to/demo/server> nodejs server.js`
8. Open your browser with `http://localhost:8080/` 
9. Enter a query and see the results.

# Understanding the Demo

For a detailed explanation of this demo application, and to learn how to write your own application, visit our [tutorial.](TUTORIAL.md)

# Credits

This demo was prepared by [Tom Sühr](https://github.com/tsuehr) and was first demonstrated at the DTL workshop in November 2018.
