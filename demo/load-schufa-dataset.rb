#!/usr/bin/env ruby

require "elasticsearch"
require "csv"
require "json"

class ESClient

  attr_reader :buffer

  def initialize
    @client = Elasticsearch::Client.new(log: false, hosts:["localhost:9200"])
    @buffer = []
    @index
  end

  def setup
    payload =  {
      "settings": {
        "number_of_replicas": 1,
        "number_of_shards": 1
      },
      "mappings": {
        "schufa": {
          "properties": {
            "age25": {
              "type": "keyword",
              "store": true
            },
            "age35": {
              "type": "keyword",
              "store": true
            },
            "sex": {
              "type": "keyword",
              "store": true
            }
          }
        }
      }
    }

    uri = URI("http://localhost:9200/schufa")
    puts do_request(uri, payload).body
  end

  def append(doc)
    buffer.push({ index: { _index: "schufa", _type: 'schufa', data: doc } })
    if buffer.count > 1000
      flush
    end
  end

  def flush
    puts "buffer.count #{@buffer.count}"
    @client.bulk(body: buffer) unless buffer.empty?
    @buffer.clear
    puts "buffer.count #{@buffer.count}"
  end

  def close
    flush
  end

  private

  def do_request(uri, payload)
    http = Net::HTTP.new(uri.host, uri.port)
    request = Net::HTTP::Put.new(uri.request_uri)
    request.body = payload.to_json
    request["Content-Type"] = "application/json"
    http.request(request)
  end

end


class SchufaLoader


  def self.run(pattern)
    client = ESClient.new
    client.setup

    Dir.glob(pattern).each do |file|
      puts "processing #{file}"
      headers = nil
      ::CSV.foreach(file, headers: true, return_headers: true) do |row|
        if headers.nil?
          headers = row
          next
        end
        doc = row.to_hash
        client.append(doc)
      end
    end

    client.close
  end
end

if __FILE__ == $0

  if ARGV.size < 1
    puts "Usage: load-schufa-dataset [path_pattern]"
    return -1
  end

  pattern = ARGV[0] # path to the json files of this dataset
  SchufaLoader.run(pattern)

end
