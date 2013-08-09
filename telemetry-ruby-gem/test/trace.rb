# Models a trace, which includes a series of spans and annotations.
class Trace
  attr_accessor :trace_id
  attr_accessor :root

  def initialize(trace_id)
    @trace_id = trace_id
    @child_spans = Hash.new
    @annotations = Hash.new
    @root = nil
  end

  # Get the child spans of the given span.
  def children(span_data)
    @child_spans[span_data.id] || []
  end

  # Get the annotations of the given span.
  def annotations(span_data)
    @annotations[span_data.id] || []
  end

  # Add a span to the trace.
  def add_span(span_data)
    if span_data.parent_id.nil?
      # The span doesn't have a parent ID, it must be the root of the trace.
      @root = span_data
    else
      # The span has a parent ID, stash it with its siblings.
      (@child_spans[span_data.parent_id] ||= []).push(span_data)
    end
  end

  # Add an annotation to the trace under the given span ID.
  def add_annotation(span_id, annotation_data)
    (@annotations[span_id] ||= []).push(annotation_data)
  end
end