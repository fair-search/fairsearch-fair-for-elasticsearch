"use strict";

var merge = require('xtend');

var reduceObj = function (predicate, initial, obj, pastAgg) {
  return Object.keys(obj).reduce(function (acc, key) {
    return predicate(acc, obj[key], key, pastAgg);
  }, initial)
};

function createKeyValueObject(key, value) {
  var obj = {};
  obj[key] = value;
  return obj
}

function ensureDimensionIsComplete(agg) {
  if (!agg._dimensionName || !agg._dimensionValue && !(agg._dimensionValue === 0)) return agg;
  var newAgg = merge(createKeyValueObject(agg._dimensionName, agg._dimensionValue), agg);
  delete newAgg._dimensionName;
  delete newAgg._dimensionValue;
  return newAgg;
}

function Aggregation(state) {
  this._state = state || [{}];
}

Aggregation.prototype.map = function (iterator) {
  return new Aggregation(this._state.map(iterator));
};

Aggregation.prototype.setDimensionName = function (dimensionName) {
  return this.map(function (agg) {
    return ensureDimensionIsComplete(merge(createKeyValueObject("_dimensionName", dimensionName), agg));
  })
};

Aggregation.prototype.setDimensionValue = function (dimensionValue) {
  return this.map(function (agg) {
    return ensureDimensionIsComplete(merge(createKeyValueObject("_dimensionValue", dimensionValue), agg));
  })
};

Aggregation.prototype.addMetric = function (name, val) {
  return this.map(function (agg) {
    return merge(agg, createKeyValueObject(name, val));
  });
};

Aggregation.prototype.concat = function (agg) {
  return new Aggregation(this._state.concat(agg._state));
};

Aggregation.prototype.addSubAggregation = function (agg) {
  return new Aggregation(this._state.reduce(function (acc, left) {
    return acc.concat((agg._state || []).map(function (right) {
      return merge(left, right);
    }));
  }, []));
};

Aggregation.prototype.get = function () {
  return this._state;
};

function EmptyAggregation() {
}
EmptyAggregation.prototype.map = function () {
  return this;
};
EmptyAggregation.prototype.setDimensionName = function (dimensionName) {
  return new Aggregation().setDimensionName(dimensionName)
};
EmptyAggregation.prototype.setDimensionValue = function (dimensionValue) {
  return new Aggregation().setDimensionValue(dimensionValue)
};
EmptyAggregation.prototype.addMetric = function (name, val) {
  return new Aggregation().addMetric(name, val)
};
EmptyAggregation.prototype.concat = function (agg) {
  return agg
};
EmptyAggregation.prototype.addSubAggregation = function (agg) {
  return agg
};
EmptyAggregation.prototype.get = function () {
  return []
};

function isBucket(bucket) {
  return typeof bucket === 'object' && bucket.hasOwnProperty('key');
}

function isSubAgg(subAgg) {
  return typeof subAgg === 'object' && subAgg.hasOwnProperty("buckets");
}

function handleMetrics(next, first, agg, metric, metricName, pastAggregation) {
  if (!metric.hasOwnProperty("value")) return next(agg, metric, metricName, pastAggregation);
  return agg.addMetric(metricName, metric.value);
}

function handleOneBucket(next, first, agg, bucket, key, pastAggregation) {
  if (!isBucket(bucket)) return next(agg, bucket, key, pastAggregation);
  return reduceObj(first, agg.setDimensionValue(bucket.key), bucket, agg.setDimensionValue(bucket.key));
}

function handleBuckets(next, first, agg, buckets, key, pastAggregation) {
  if (!Array.isArray(buckets) || key != "buckets") return next(agg, buckets, key, pastAggregation);
  return buckets.map(/*first.bind(null, agg)*/function (bucket, idx) {
    return first(agg, bucket, idx, pastAggregation);
  }).reduce(function (acc, _agg) {
    return acc.concat(_agg);
  }, new EmptyAggregation())
}

function handleSubAggregation(next, first, agg, subAgg, subAggName, pastAggregation) {
  if (!isSubAgg(subAgg)) return next(agg, subAgg, subAggName, pastAggregation);
  return addSubOrMissingAgg(first, agg, subAgg, subAggName, pastAggregation);
}

function addSubOrMissingAgg(first, agg, subAgg, subAggName, pastAggregation) {
  pastAggregation = pastAggregation ? pastAggregation : new EmptyAggregation();
  if (agg._state && agg._state.length && Object.keys(agg._state[0]).length > 1) {
    return agg.concat(pastAggregation.addSubAggregation(reduceObj(first, new EmptyAggregation().setDimensionName(subAggName), subAgg, agg)))
  } else {
    return (agg._state && agg._state.length ? agg : pastAggregation).addSubAggregation(reduceObj(first, new EmptyAggregation().setDimensionName(subAggName), subAgg, agg))
  }
}

function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function isMissingAggregation(missingAggName, missingAggs) {
  return (missingAggs.doc_count && (missingAggName && !isNumeric(missingAggName)) && typeof missingAggs === 'object' && missingAggName.indexOf("missing_") >= 0);
}

function createSubAggWithMissingAgg(missingAggs) {
  return createKeyValueObject("buckets", [merge(createKeyValueObject("key", "null"), missingAggs)]);
}


function handleMissingAggregations(next, first, agg, missingAggs, missingAggName, pastAggregation) {
  if (!isMissingAggregation(missingAggName, missingAggs)) return next(agg, missingAggs, missingAggName, pastAggregation);
  return addSubOrMissingAgg(first, agg, createSubAggWithMissingAgg(missingAggs), missingAggName.slice(8), pastAggregation);
}

var defaultHandlers = [
  handleMetrics,
  handleOneBucket,
  handleBuckets,
  handleSubAggregation,
  handleMissingAggregations
];

function createCor(handlers, fallBack) {
  return handlers.reduce(function (nextHandler, handler) {
    return handler.bind(null, nextHandler, function first() {
      return createCor(handlers, fallBack).apply(null, arguments);
    });
  }, fallBack);
}

module.exports.parse = function parseEsResponse(response, options) {
  options = options || {};
  if (!response.aggregations) return [];
  return reduceObj(createCor(options.handlers || defaultHandlers, function (handler) {
    return handler;
  }), new EmptyAggregation(), response.aggregations).get()
};
