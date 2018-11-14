# Elasticsearch aggregation response parser

[![Build Status](https://travis-ci.org/Isabek/es-response-parser.svg)](https://travis-ci.org/Isabek/es-response-parser)

Elasticsearch response parser for Node.JS

## how to use

```sh
$ npm install es-response-parser --save
```

## example


```javascript

var esResponseParser = require("es-response-parser");

var esResponse = {
      "aggregations": {
        "offerId": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [{
            "key": "F1A2LqSYD3u",
            "doc_count": 6,
            "os": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "Desktop",
                "doc_count": 6,
                "campaignClick": {"value": 6.0},
                "offerClick": {"value": 6.0},
                "revenue": {"value": 0.0}
              }]
            }
          }, {
            "key": "F1MGDprRRJP",
            "doc_count": 6,
            "os": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "Desktop",
                "doc_count": 6,
                "campaignClick": {"value": 6.0},
                "offerClick": {"value": 6.0},
                "revenue": {"value": 0.0}
              }]
            }
          }, {
            "key": "F1MGDprnv7y",
            "doc_count": 5,
            "os": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "Desktop",
                "doc_count": 5,
                "campaignClick": {"value": 5.0},
                "offerClick": {"value": 5.0},
                "revenue": {"value": 0.0}
              }]
            }
          }]
        }
      }
    };
    
  var result = esResponseParser.parse(esResponse);
  
  console.log(result);

```
###Result will be

```javascript

  [
      {
        "campaignClick": 6,
        "offerClick": 6,
        "offerId": "F1A2LqSYD3u",
        "os": "Desktop",
        "revenue": 0
      },
      {
        "campaignClick": 6,
        "offerClick": 6,
        "offerId": "F1MGDprRRJP",
        "os": "Desktop",
        "revenue": 0
      },
      {
        "campaignClick": 5,
        "offerClick": 5,
        "offerId": "F1MGDprnv7y",
        "os": "Desktop",
        "revenue": 0
      }
    ]
```

## Tests

  npm test
