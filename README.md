# Fair search algorithms for Elasticsearch


[![Build Status](https://travis-ci.org/fair-search/fairsearch-elasticsearch-plugin.svg?branch=master)](https://travis-ci.org/fair-search/fairsearch-elasticsearch-plugin)
[![Maintainability](https://api.codeclimate.com/v1/badges/d1782dfbff41827f33f9/maintainability)](https://codeclimate.com/github/fair-search/fairsearch-elasticsearch-plugin/maintainability)

The Fair Search Elasticsearch plugin uses machine learning to provide a fair search result with relevant protected 
and non protected classes. 

:warning: This is a work in progress, will be launched officially end of November 2018.

# What this plugin does...

This plugin:

- Store fairness distribution tables to use during rescoring.
- Allows you to rescore fairly any query in elasticsearch.

## Where's the docs?

We recommend taking time to [read the docs](http://fairsearch-elasticsearch.readthedocs.io). 

## How to contribute?

This plugin is an open source project and we love to receive contributions from the community — you! All contributions are welcome: ideas, patches, documentation, bug reports, complaints, and even something you drew up on a napkin.

Programming is not a required skill. Whatever you've seen about open source and maintainers or community members saying "send patches or die" - you will not see that here.

It is more important to me that you are able to contribute.

Extra bits at [CONTRIBUTING.md](CONTRIBUTTING.md)


# Installing

### Install Elasticsearch
First intall [Elasticsearch Version 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
To install the fairsearch plugin you can run 

`./bin/elasticsearch-plugin install https://fair-search.github.io/fair-reranker/fairsearch-1.0-es6.2.4-SNAPSHOT.zip`
(It's expected you'll confirm some security exceptions, you can pass `-b` to `elasticsearch-plugin` to automatically install)

or if you have made changes to the plugin you can run 

```
./gradlew clean check
```
and then install your build with the following command

```
./bin/elasticsearch-plugin install file:///path/to/project/build/distributions/fairsearch-1.0.0-es6.2.2.zip
```

See the full list of [prebuilt versions](https://fair-search.github.io/). If you don't see a version available, see the link below for building.

# Development

Notes if you want to dig into the code or build for a version there's no build for.

# How to use the Plugin
Once you have a running Elasticsearch node with the fairsearch plugin installed you can perform search queries and get the results in a fair ordering according to the [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368).

### Create a MTable

A MTable is a representation for a fair ranking. Before we can perform a fair query we have to create and store one mtable in our Elasticsearch node. Before we create the mtable we have to think about some parameters:
```
k=25 "The length of the Ranking"
p=0.5 "The desired proportion of candidates with an protected attribute"
alpha = 0.1 "The significance level"
```
We recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) in order to understand why we need these parameters.

For this description of a fair ranking we can now create the mtable with the following query:

```
POST http://yourESNodeAdress/_fs/_mtable/0.5/0.1/25
```
The Mtable is now stored in your Elasticsearch node.
To get a list of all mtables in your node you can make the following request:

```
GET http://yourESNodeAdress/_fs/_mtable
```

### Get a Fair Ranking
After you have created the mtable for your Ranking we can make a fair search query as follows:

```
POST http://yourESNodeAdress/indexName/_search

{
	"from" : 0, "size" : 25,
	"query" : {
		"match" : {
			"body" : "hello"
			}
		},
	"rescore" : {
		"window_size" : 25,
		"fair_rescorer" : {
			"protected_key" : "gender",
			"protected_value" : "f",
			"significance_level" : 0.1,
			"min_proportion_protected" : 0.5
			}
		}
}
	
```

# Example Application

See [EXAMPLE.md](EXAMPLE.md) for a detailed example of an Elasticsearch Application on your local machine.

### 1. Build with Gradle Wrapper

```
./gradlew clean check
```

This runs the tasks in the `esplugin` gradle plugin that builds, tests, generates a Elasticsearch plugin zip file.

# Who built this?

Initially developed by:
- Pere Urbón Bayes
- Tom Sühr

Special thanks to Meike Zehlike and Carlos Castillo, the minds behind the science in this plugin.

## Other Acknowledgments & Stuff To Read

- [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368)
