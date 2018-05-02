#!/usr/bin/env ruby

require "json"
require "elasticsearch"

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
        "xing": {
          "properties": {
            "sex": {
              "type": "text",
              "store": true
            }
          }
        }
      }
    }

    uri = URI("http://localhost:9200/xing")
    puts do_request(uri, payload).body
  end

  def append(doc)
    buffer.push({ index: { _index: "xing", _type: 'xing', data: doc } })
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


class XingLoader


  def self.run(pattern)
    client = ESClient.new
    client.setup

    Dir.glob(pattern).each do |file|

      puts "processing #{file}"

      payload = File.read(file)
      content = JSON.parse(payload)

      category = content["category"]
      profiles = content["profiles"]

      profiles.each do |profile|
        person_record = profile["profile"].first

        profile.each_pair do |k,v|
          next if k == "profile"
          person_record[k] = v unless v.empty? || ( v.is_a?(Array) and v.first.empty?)
        end

        person_record[:category] = category

        client.append(person_record)
      end
    end

    client.close
  end
end

if __FILE__ == $0

  if ARGV.size < 1
    puts "Usage: load-xing-dataset [path_pattern]"
    return -1
  end

  pattern = ARGV[0] # path to the json files of this dataset
  XingLoader.run(pattern)

end
