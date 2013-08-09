class Trace
  attr_accessor :trace_id
  attr_accessor :root

  def initialize(trace_id)
    @trace_id = trace_id
    @child_spans = Hash.new
    @annotations = Hash.new
    @root = nil
  end

  def children(span_data)
    @child_spans[span_data.id] || []
  end

  def annotations(span_data)
    @annotations[span_data.id] || []
  end

  def add_span(span_data)
    if span_data.parent_id.nil?
      @root = span_data
    else
      (@child_spans[span_data.parent_id] ||= []).push(span_data)
    end
  end

  def add_annotation(span_id, annotation_data)
    (@annotations[span_id] ||= []).push(annotation_data)
  end
end