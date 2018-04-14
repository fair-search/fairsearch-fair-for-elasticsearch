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

  def setup(name, p, a, k)
    uri = URI("http://localhost:9200/_fs/_mtable/#{name}/#{p}/#{a}/#{k}")
    do_request(uri).body
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

  def do_request(uri, payload={})
    http = Net::HTTP.new(uri.host, uri.port)
    request = Net::HTTP::Post.new(uri.request_uri)
    request.body = payload.to_json unless payload.empty?
    request["Content-Type"] = "application/json"
    http.request(request)
  end

end


class MTableLoader


  def self.run(iterations=100)
    client = ESClient.new
    name   = "default_mtables"
    iterations.times do |k|
      10.times do |p_i|
        p = p_i/10.0
        10.times do |a_i|
          a = a_i / 10.0
          puts client.setup(name, p, a, k+1)
        end
      end
    end
    client.close
  end
end

if __FILE__ == $0

  if ARGV.size < 1
    puts "Usage: setup [iterations]"
    return -1
  end

  iterations = ARGV[0].to_i #num of iterations
  MTableLoader.run(iterations)

end
