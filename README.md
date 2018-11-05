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
First intall [Elasticsearch Version 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4)

See the full list of [prebuilt versions](https://fair-search.github.io/). If you don't see a version available, see the link below for building.

To install, you'd run a command such as:

`./bin/elasticsearch-plugin install https://fair-search.github.io/fair-reranker/fairsearch-1.0-es6.1.2-SNAPSHOT.zip`

(It's expected you'll confirm some security exceptions, you can pass `-b` to `elasticsearch-plugin` to automatically install)

If you already are running Elasticsearch, don't forget to restart!

# Development

Notes if you want to dig into the code or build for a version there's no build for.

### 1. Build with Gradle Wrapper

```
./gradlew clean check
```

This runs the tasks in the `esplugin` gradle plugin that builds, tests, generates a Elasticsearch plugin zip file.

### 2. Install with `./bin/elasticsearch-plugin`

```
./bin/elasticsearch-plugin install file:///path/to/project/build/distributions/fairsearch-1.0.0-es6.2.2.zip
```

# Who built this?

Initially developed by:
- Pere Urbón Bayes
- Tom Sühr

Special thanks to Meike Zehlike and Carlos Castillo, the minds behind the science in this plugin.

## Other Acknowledgments & Stuff To Read

- [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368)
