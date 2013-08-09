require 'trace'

# A simple in-memory span sink for testing.
class MemorySpanSink
  attr_accessor :traces

  def initialize
    @traces = Hash.new
    @annotations= Hash.new
  end

  # Get the trace.
  def trace(trace_id)
    @traces[trace_id]
  end

  # Record the span.
  def record(span)
    (@traces[span.trace_id] ||= Trace.new(span.trace_id)).add_span(span)
  end

  # Record the annotation.
  def record_annotation(trace_id, id, annotation_data)
    (@traces[trace_id] ||= Trace.new(trace_id)).add_annotation(id, annotation_data)
  end
end
