"use strict";

var esResponseParser = require("./../index");
var assert = require("assert");

describe("testing Elasticsearch Response Parser", function () {
  it("should have method parse", function (done) {
    (esResponseParser.hasOwnProperty('parse')) ? done() : done("Method does not exists.");
  });

  it("should return parsed aggregation in object with 1 group by and 1 metric", function (done) {

    var esResponse = {
      "aggregations": {
        "offerId": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [
            {
              "key": "F1A2LqSYD3u",
              "doc_count": 6,
              "offerClick": {
                "value": 6.0
              }
            },
            {
              "key": "F1MGDprRRJP",
              "doc_count": 6,
              "offerClick": {"value": 6.0}
            },
            {
              "key": "F1MGDprnv7y",
              "doc_count": 5,
              "offerClick": {
                "value": 5.0
              }
            }
          ]
        }
      }
    };

    assert.deepEqual(esResponseParser.parse(esResponse), [
      {
        "offerClick": 6,
        "offerId": "F1A2LqSYD3u"
      },
      {
        "offerClick": 6,
        "offerId": "F1MGDprRRJP"
      },
      {
        "offerClick": 5,
        "offerId": "F1MGDprnv7y"
      }
    ]);

    done();
  });

  it("should return parsed aggregation in object with 2 group by and 3 metric", function (done) {
    var response = {
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
    }
    assert.deepEqual(esResponseParser.parse(response), [
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
    ]);
    done();
  });

  it("should return parsed aggregation in object with 2 group by and 3 metric and 1 missing field", function (done) {
    var response = {
      "aggregations": {
        "os": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [{
            "key": "Desktop",
            "doc_count": 17,
            "offerId": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [
                {
                  "key": "F1A2LqSYD3u",
                  "doc_count": 6,
                  "missing_token_1": {
                    "doc_count": 6,
                    "campaignClick": {"value": 6.0},
                    "offerClick": {"value": 6.0},
                    "revenue": {"value": 0.0}
                  },
                  "token_1": {
                    "doc_count_error_upper_bound": 0,
                    "sum_other_doc_count": 0,
                    "buckets": []
                  }
                },
                {
                  "key": "F1MGDprRRJP",
                  "doc_count": 6,
                  "missing_token_1": {
                    "doc_count": 6,
                    "campaignClick": {"value": 6.0},
                    "offerClick": {"value": 6.0},
                    "revenue": {"value": 0.0}
                  },
                  "token_1": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
                },
                {
                  "key": "F1MGDprnv7y",
                  "doc_count": 5,
                  "missing_token_1": {
                    "doc_count": 4,
                    "campaignClick": {"value": 4.0},
                    "offerClick": {"value": 4.0},
                    "revenue": {"value": 0.0}
                  },
                  "token_1": {
                    "doc_count_error_upper_bound": 0,
                    "sum_other_doc_count": 0,
                    "buckets": [{
                      "key": "123",
                      "doc_count": 1,
                      "campaignClick": {"value": 1.0},
                      "offerClick": {"value": 1.0},
                      "revenue": {"value": 0.0}
                    }]
                  }
                }]
            }
          }]
        }
      }
    };
    assert.deepEqual(esResponseParser.parse(response), [
      {
        "campaignClick": 6,
        "offerClick": 6,
        "offerId": "F1A2LqSYD3u",
        "os": "Desktop",
        "revenue": 0,
        "token_1": "null"
      },
      {
        "campaignClick": 6,
        "offerClick": 6,
        "offerId": "F1MGDprRRJP",
        "os": "Desktop",
        "revenue": 0,
        "token_1": "null"
      },
      {
        "campaignClick": 4,
        "offerClick": 4,
        "offerId": "F1MGDprnv7y",
        "os": "Desktop",
        "revenue": 0,
        "token_1": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "offerId": "F1MGDprnv7y",
        "os": "Desktop",
        "revenue": 0,
        "token_1": "123"
      }
    ]);
    done();
  });

  it("should return parsed aggregation in object with 1 group by and 3 metric and started with 1 missing field", function (done) {
    var response = {
      took: 5,
      timed_out: false, _shards: {total: 5, successful: 3, failed: 0},
      hits: {total: 4, max_score: 0, hits: []},
      aggregations: {
        missing_token_1: {
          doc_count: 3,
          campaignClick: {value: 3},
          offerClick: {value: 3},
          revenue: {value: 0},
          spend: {value: 0.03}
        },
        token_1: {
          doc_count_error_upper_bound: 0,
          sum_other_doc_count: 0,
          buckets: [{
            key: '123',
            doc_count: 1,
            campaignClick: {value: 1},
            offerClick: {value: 1},
            revenue: {value: 0},
            spend: {value: 0.01}
          }]
        }
      }
    };
    assert.deepEqual(esResponseParser.parse(response), [
      {"offerClick": 3, "campaignClick": 3, "revenue": 0, "spend": 0.03, "token_1": "null"},
      {"offerClick": 1, "campaignClick": 1, "revenue": 0, "spend": 0.01, "token_1": "123"}
    ]);
    done();
  });

  it("should return parsed aggregation in object with 2 group by and 3 metric and start with 2 missing field", function (done) {
    var response = {
      "aggregations": {
        "missing_token_1": {
          "doc_count": 3,
          "os": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [{
              "key": "Desktop",
              "doc_count": 3,
              "missing_token_2": {
                "doc_count": 2,
                "campaignClick": {"value": 2.0},
                "offerClick": {"value": 2.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.02}
              },
              "token_2": {
                "doc_count_error_upper_bound": 0,
                "sum_other_doc_count": 0,
                "buckets": [{
                  "key": "asdf",
                  "doc_count": 1,
                  "campaignClick": {"value": 1.0},
                  "offerClick": {"value": 1.0},
                  "revenue": {"value": 0.0},
                  "spend": {"value": 0.01}
                }]
              }
            }]
          }
        },
        "token_1": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [{
            "key": "asdf",
            "doc_count": 2,
            "os": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "Desktop",
                "doc_count": 2,
                "missing_token_2": {
                  "doc_count": 2,
                  "campaignClick": {"value": 2.0},
                  "offerClick": {"value": 2.0},
                  "revenue": {"value": 0.0},
                  "spend": {"value": 0.02}
                },
                "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
              }]
            }
          }]
        }
      }
    };
    assert.deepEqual(esResponseParser.parse(response), [
      {
        "campaignClick": 2,
        "offerClick": 2,
        "os": "Desktop",
        "revenue": 0,
        "spend": 0.02,
        "token_1": "null",
        "token_2": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "os": "Desktop",
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "asdf"
      },
      {
        "campaignClick": 2,
        "offerClick": 2,
        "os": "Desktop",
        "revenue": 0,
        "spend": 0.02,
        "token_1": "asdf",
        "token_2": "null"
      }
    ]);
    done();
  });

  it("should return parsed aggregation in object 3 missing aggregations and 3 metrics", function (done) {
    var response = {
      "aggregations": {
        "missing_token_1": {
          "doc_count": 5,
          "missing_token_2": {
            "doc_count": 3,
            "token_wpoken": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "333",
                "doc_count": 1,
                "campaignClick": {"value": 1.0},
                "offerClick": {"value": 1.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.01}
              }]
            },
            "missing_token_wpoken": {
              "doc_count": 2,
              "campaignClick": {"value": 2.0},
              "offerClick": {"value": 2.0},
              "revenue": {"value": 0.0},
              "spend": {"value": 0.02}
            }
          },
          "token_2": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [{
              "key": "asdf",
              "doc_count": 1,
              "token_wpoken": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []},
              "missing_token_wpoken": {
                "doc_count": 1,
                "campaignClick": {"value": 1.0},
                "offerClick": {"value": 1.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.01}
              }
            }, {
              "key": "ttt",
              "doc_count": 1,
              "token_wpoken": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []},
              "missing_token_wpoken": {
                "doc_count": 1,
                "campaignClick": {"value": 1.0},
                "offerClick": {"value": 1.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.01}
              }
            }]
          }
        },
        "token_1": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [{
            "key": "asdf",
            "doc_count": 2,
            "missing_token_2": {
              "doc_count": 2,
              "token_wpoken": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []},
              "missing_token_wpoken": {
                "doc_count": 2,
                "campaignClick": {"value": 2.0},
                "offerClick": {"value": 2.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.02}
              }
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }, {
            "key": "123",
            "doc_count": 1,
            "missing_token_2": {
              "doc_count": 1,
              "token_wpoken": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []},
              "missing_token_wpoken": {
                "doc_count": 1,
                "campaignClick": {"value": 1.0},
                "offerClick": {"value": 1.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.01}
              }
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }, {
            "key": "rrr",
            "doc_count": 1,
            "missing_token_2": {
              "doc_count": 1,
              "token_wpoken": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []},
              "missing_token_wpoken": {
                "doc_count": 1,
                "campaignClick": {"value": 1.0},
                "offerClick": {"value": 1.0},
                "revenue": {"value": 0.0},
                "spend": {"value": 0.01}
              }
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }]
        }
      }
    };
    assert.deepEqual(esResponseParser.parse(response), [
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "null",
        "token_wpoken": "333"
      },
      {
        "campaignClick": 2,
        "offerClick": 2,
        "revenue": 0,
        "spend": 0.02,
        "token_1": "null",
        "token_2": "null",
        "token_wpoken": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "asdf",
        "token_wpoken": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "ttt",
        "token_wpoken": "null"
      },
      {
        "campaignClick": 2,
        "offerClick": 2,
        "revenue": 0,
        "spend": 0.02,
        "token_1": "asdf",
        "token_2": "null",
        "token_wpoken": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "123",
        "token_2": "null",
        "token_wpoken": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "rrr",
        "token_2": "null",
        "token_wpoken": "null"
      }
    ]);
    done();
  });

  it("should return parsed aggregation in object 2 missing aggregations, 2 group by and 3 metrics", function (done) {
    var response = {
      "aggregations": {
        "missing_token_1": {
          "doc_count": 5,
          "missing_token_2": {
            "doc_count": 3,
            "campaignClick": {"value": 3},
            "offerClick": {"value": 3},
            "revenue": {"value": 0},
            "spend": {"value": 0.03}
          },
          "token_2": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [{
              "key": "asdf",
              "doc_count": 1,
              "campaignClick": {"value": 1},
              "offerClick": {"value": 1},
              "revenue": {"value": 0},
              "spend": {"value": 0.01}
            }, {
              "key": "ttt",
              "doc_count": 1,
              "campaignClick": {"value": 1},
              "offerClick": {"value": 1},
              "revenue": {"value": 0},
              "spend": {"value": 0.01}
            }]
          }
        },
        "token_1": {
          "doc_count_error_upper_bound": 0,
          "sum_other_doc_count": 0,
          "buckets": [{
            "key": "asdf",
            "doc_count": 2,
            "missing_token_2": {
              "doc_count": 2,
              "campaignClick": {"value": 2},
              "offerClick": {"value": 2},
              "revenue": {"value": 0},
              "spend": {"value": 0.02}
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }, {
            "key": "123",
            "doc_count": 1,
            "missing_token_2": {
              "doc_count": 1,
              "campaignClick": {"value": 1},
              "offerClick": {"value": 1},
              "revenue": {"value": 0},
              "spend": {"value": 0.01}
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }, {
            "key": "rrr",
            "doc_count": 1,
            "missing_token_2": {
              "doc_count": 1,
              "campaignClick": {"value": 1},
              "offerClick": {"value": 1},
              "revenue": {"value": 0},
              "spend": {"value": 0.01}
            },
            "token_2": {"doc_count_error_upper_bound": 0, "sum_other_doc_count": 0, "buckets": []}
          }, {
            "key": "t1",
            "doc_count": 1,
            "missing_token_2": {
              "doc_count": 0,
              "campaignClick": {"value": 0},
              "offerClick": {"value": 0},
              "revenue": {"value": 0},
              "spend": {"value": 0}
            },
            "token_2": {
              "doc_count_error_upper_bound": 0,
              "sum_other_doc_count": 0,
              "buckets": [{
                "key": "t2",
                "doc_count": 1,
                "campaignClick": {"value": 1},
                "offerClick": {"value": 1},
                "revenue": {"value": 0},
                "spend": {"value": 0.01}
              }]
            }
          }]
        }
      }
    };
    assert.deepEqual(esResponseParser.parse(response), [
      {
        "campaignClick": 3,
        "offerClick": 3,
        "revenue": 0,
        "spend": 0.03,
        "token_1": "null",
        "token_2": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "asdf"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "null",
        "token_2": "ttt"
      },
      {
        "campaignClick": 2,
        "offerClick": 2,
        "revenue": 0,
        "spend": 0.02,
        "token_1": "asdf",
        "token_2": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "123",
        "token_2": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "rrr",
        "token_2": "null"
      },
      {
        "campaignClick": 1,
        "offerClick": 1,
        "revenue": 0,
        "spend": 0.01,
        "token_1": "t1",
        "token_2": "t2"
      }
    ]);
    done();
  });
});