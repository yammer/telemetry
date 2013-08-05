<#-- @ftlvariable name="" type="com.yammer.telemetry.service.views.TraceView" -->
<html>
<head>
    <title>Trace - ${trace.root.name}</title>
    <style>
        .trace {
            width: 100%;
        }

        .span {
            position: relative;
            border: 1px solid #ff1493;
            background: #ffc0cb;
        }
    </style>
</head>
<body>
<h1>Trace - ${trace.root.name}</h1>
<div id="trace-${trace.id}" class="trace">
    (start = ${trace.startTimeNanos} ; duration = ${trace.duration})
    <@renderSpan span=trace.root trace=trace/>
</div>
</body>
</html>

<#macro renderSpan span trace>
<#-- @ftlvariable name="span" type="com.yammer.telemetry.tracing.Span" -->
<#-- @ftlvariable name="trace" type="com.yammer.telemetry.tracing.Trace" -->
<div id="span-${span.id}" class="span" style="left: ${((span.startTimeNanos - trace.startTimeNanos) / trace.duration) * 100}%; width: ${(span.duration / trace.duration) * 100}%;">
    ${span.name} (start = ${span.startTimeNanos}; duration = ${span.duration})
</div>
<#list trace.getChildren(span) as child>
    <@renderSpan span=child trace=trace/>
</#list>
</#macro>