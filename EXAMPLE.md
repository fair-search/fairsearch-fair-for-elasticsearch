# Example Application
This is a step-by-step tutorial to set up an Elasticsearch Application with the fairsearch plug-in on your local machine.
We highly recommend to read [FA*IR: A Fair Top-k Ranking Algorithm](https://arxiv.org/abs/1706.06368) before you go through this tutorial since we will not explain the theory behind the plugin in this tutorial.

### Install Elasticsearch
The first thing we need is Elasticsearch. Download [Elasticsearch 6.2.4](https://www.elastic.co/de/downloads/past-releases/elasticsearch-6-2-4).
Once you have downloaded it, unzip it to a location of your choice. In this Tutorial, we will unzip it to "C:\Users\Demo\".
You have now installed Elasticsearch! You can run it with `C:\Users\Demo\elasticsearch-6.2.4\bin>elasticsearch`.

### Install the Plugin
Download the the snapshot build of the plugin [fairsearch-1.0-es6.2.4-snapshot.zip](https://github.com/fair-search/fairsearch-elasticsearch-plugin). This is just one way to get the current build of the plugin. See [README.md](README.md) for other options.
