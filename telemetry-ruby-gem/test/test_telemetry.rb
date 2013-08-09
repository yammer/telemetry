require 'test/unit'
require 'telemetry'
require 'memory_span_sink'

class SpanTest < Test::Unit::TestCase
  attr_accessor :memory_sink

  def setup
    @memory_sink = MemorySpanSink.new
    Telemetry.span_sinks.push(@memory_sink)
  end

  def test_start_trace
    span = Telemetry::Span.start_trace('span 1')
    span.end

    trace = @memory_sink.trace(span.trace_id)
    assert_equal(span, trace.root)
  end

  def test_sub_trace
    span = Telemetry::Span.start_trace('span 1')
    span2 = Telemetry::Span.start_span('span 1.1')

    assert_equal(span.trace_id, span2.trace_id)

    span2.end
    span.end

    trace = @memory_sink.trace(span.trace_id)
    assert_equal(span, trace.root)
    assert_equal([span2], trace.children(span))
  end

  def test_sub_sub_trace_with_siblings
    span = Telemetry::Span.start_trace('span 1')
    span2 = Telemetry::Span.start_span('span 1.1')
    span3 = Telemetry::Span.start_span('span 1.1.1')
    span3.end
    span4 = Telemetry::Span.start_span('span 1.1.2')
    span4.end
    span2.end
    span.end

    trace = @memory_sink.trace(span.trace_id)
    assert_equal(span, trace.root)
    assert_equal([span2], trace.children(span), trace.inspect)
    assert_equal([span3, span4], trace.children(span2))
  end

  def test_annotation
    span = Telemetry::Span.start_trace('span')
    span.add_annotation('ServiceStart')
    span.add_annotation('ServiceName', 'unittests')
    span.end

    trace = @memory_sink.trace(span.trace_id)
    assert_equal([['ServiceStart', nil], ['ServiceName', 'unittests']], trace.annotations(span).map { |s| [s.name, s.message]})
  end

  def test_attach_span
    trace_id = UUIDTools::UUID.random_create
    span_id = UUIDTools::UUID.random_create
    span = Telemetry::Span.attach_span(trace_id, span_id)
    span.add_annotation('Attached', 'Yay')

    span2 = Telemetry::Span.start_trace('subspan')
    span2.add_annotation('SubSpan', 'w00t')
    span2.end

    span.end

    trace = @memory_sink.trace(span.trace_id)

    assert_equal([['Attached', 'Yay']], trace.annotations(span).map { |s| [s.name, s.message]})
    assert_equal([span2], trace.children(span))
    assert_equal([['SubSpan', 'w00t']], trace.annotations(span2).map { |s| [s.name, s.message]})
  end
end