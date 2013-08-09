require 'trace'

class MemorySpanSink
  attr_accessor :traces

  def initialize
    @traces = Hash.new
    @annotations= Hash.new
  end

  def trace(trace_id)
    @traces[trace_id]
  end

  def record(span)
    (@traces[span.trace_id] ||= Trace.new(span.trace_id)).add_span(span)
  end

  def record_annotation(trace_id, id, annotation_data)
    (@traces[trace_id] ||= Trace.new(trace_id)).add_annotation(id, annotation_data)
  end
end
