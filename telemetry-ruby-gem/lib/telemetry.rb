require 'uuidtools'

module Telemetry
  class << self
    attr_accessor :span_sinks
  end
  Telemetry.span_sinks = []

  def self.now_in_nanos
    (Time.now.to_f * 1000000000).to_i
  end

  class SpanContext
    @@spans = []

    def current_trace_id
      if @@spans.empty?
        nil
      else
        @@spans.slice(-1).trace_id
      end
    end

    def current_span_id()
      if @@spans.empty?
        nil
      else
        @@spans.slice(-1).id
      end
    end

    def start_span(span)
      @@spans.push(span)
    end

    def end_span(span)
      popped_span = @@spans.pop()

      while !popped_span.id.eql?(span.id)
        popped_span = @@spans.pop()
      end
    end

    def inspect
      "SpanContext(current = #{@@spans.slice(-1).inspect}, length = #{@@spans.length})"
    end
  end

  class Span
    attr_reader :trace_id
    attr_reader :id
    attr_reader :parent_id
    attr_reader :name
    attr_reader :start_time_nanos
    attr_reader :duration
    @@context = SpanContext.new

    def self.start_trace(name)
      start(name, nil, nil, nil, true)
    end

    def self.start_span(name)
      start(name, nil, nil, nil, true)
    end

    def self.attach_span(trace_id, span_id)
      start(nil, trace_id, span_id, nil, false)
    end

    def self.start(name, trace_id, span_id, parent_span_id, log_span)
      trace_id ||= @@context.current_trace_id() || UUIDTools::UUID.random_create
      span_id ||= UUIDTools::UUID.random_create
      parent_span_id ||= @@context.current_span_id()

      span = Span.new(trace_id, span_id, parent_span_id, name, Telemetry.now_in_nanos, log_span)
      @@context.start_span(span)
      span
    end

    def initialize(trace_id, id, parent_id, name, start_time_nanos, log_span)
      @trace_id = trace_id
      @id = id
      @parent_id = parent_id
      @name = name
      @start_time_nanos = start_time_nanos
      @duration = -1
      @log_span = log_span
      @annotations = []
    end

    def add_annotation(name, message = nil)
      @annotations.push(AnnotationData.new(name, message))
    end

    def end
      @duration = Telemetry.now_in_nanos - @start_time_nanos
      @@context.end_span(self)

      Telemetry.span_sinks.each { |sink|
        if (@log_span)
          sink.record(self)
        end

        @annotations.each { |annotation|
          sink.record_annotation(@trace_id, @id, annotation)
        }
      }
    end

    def inspect
      "Span(name = #{name}, trace_id = #{trace_id}, id = #{id}, parent_id = #{parent_id})"
    end
  end

  class AnnotationData
    attr_reader :start_time_nanos
    attr_reader :name
    attr_reader :message

    def initialize(name, message = nil)
      @start_time_nanos = Telemetry.now_in_nanos
      @name = name
      @message = message
    end

    def inspect
      "AnnotationData(name = #{name}, message = #{message}"
    end
  end
end
