require 'uuidtools'

# The Telemetry module models a dapper(http://research.google.com/pubs/pub36356.html)-like
# system for distributed execution tracing.
module Telemetry
  class << self
    attr_accessor :span_sinks
  end
  Telemetry.span_sinks = []

  # Ruby 1.8.7 only does seconds as a float. Newer versions have explicit nanosecond
  # support. Until then...
  def self.now_in_nanos
    (Time.now.to_f * 1000000000).to_i
  end

  # Stores the span context so information can be carried across function call boundaries
  # without resorting to ugly parameter passing. The Java analog uses a ThreadLocal variable.
  class SpanContext
    @@spans = []

    # Gets the current trace ID.
    def current_trace_id
      if @@spans.empty?
        nil
      else
        @@spans.slice(-1).trace_id
      end
    end

    # Gets the current span ID.
    def current_span_id()
      if @@spans.empty?
        nil
      else
        @@spans.slice(-1).id
      end
    end

    # Start the given span. Switches the current context to run in the context of this span
    # and all of it's associated IDs.
    def start_span(span)
      @@spans.push(span)
    end

    # Ends the given span. Restores the current context to run in the context of the span
    # started immediately before this span.
    def end_span(span)
      popped_span = @@spans.pop()

      # It's possible that someone may have started a span without ending it. Shame on them,
      # but shame on us (too) if we don't guard against it.
      while !popped_span.id.eql?(span.id)
        popped_span = @@spans.pop()
      end
    end

    def inspect
      "SpanContext(current = #{@@spans.slice(-1).inspect}, length = #{@@spans.length})"
    end
  end

  # Models a span of execution within a trace.
  class Span
    attr_reader :trace_id
    attr_reader :id
    attr_reader :parent_id
    attr_reader :name
    attr_reader :start_time_nanos
    attr_reader :duration
    @@context = SpanContext.new

    # Start a brand new trace.
    def self.start_trace(name)
      start(name, nil, nil, nil, true)
    end

    # Start a new span within a trace.
    def self.start_span(name)
      start(name, nil, nil, nil, true)
    end

    # Attach to an existing span. This is useful when a span has been created elsewhere
    # (probably on another host) and you'd like to log annotations against that span locally.
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

    # Adds an annotation to the current span.
    def add_annotation(name, message = nil)
      @annotations.push(AnnotationData.new(name, message))
    end

    # Ends a span.
    def end
      # Stop the span timer.
      @duration = Telemetry.now_in_nanos - @start_time_nanos

      # Pop the span context.
      @@context.end_span(self)

      # Log the span (if enabled) and any annotations to any configured span sink(s).
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

  # Point in time annotation within a span.
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
