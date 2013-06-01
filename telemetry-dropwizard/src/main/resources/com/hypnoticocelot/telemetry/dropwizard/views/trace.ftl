<#-- @ftlvariable name="" type="com.hypnoticocelot.telemetry.dropwizard.views.TraceView" -->
<html>
<head>
    <title>Trace - ${trace.root.data.name}</title>
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
<h1>Trace - ${trace.root.data.name}</h1>
<div id="trace-${trace.id}" class="trace">
    <@renderSpan span=trace.root trace=trace/>
</div>
</body>
</html>

<#macro renderSpan span trace>
<#-- @ftlvariable name="span" type="com.hypnoticocelot.telemetry.tracing.Span" -->
<#-- @ftlvariable name="trace" type="com.hypnoticocelot.telemetry.tracing.Trace" -->
<div id="span-${span.id}" class="span" style="left: ${((span.startTime - trace.startTime) / trace.duration) * 100}%; width: ${(span.duration / trace.duration) * 100}%;">
    ${span.data.name}
</div>
<#list trace.getChildren(span) as child>
    <@renderSpan span=child trace=trace/>
</#list>
</#macro>