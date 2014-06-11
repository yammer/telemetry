<#-- @ftlvariable name="" type="com.yammer.telemetry.service.views.TraceView" -->
<html>
<head>
    <title>Trace - <@traceName trace=trace/></title>
    <style>
        .trace {
            width: 100%;
        }

        .span {
            position: relative;
            border: 1px solid #ff1493;
            background: #ffc0cb;
        }

        .annotationMarker {
            position: relative;
            border: 1px solid #000000;
            background: #444444;
            width: 1px;
        }
    </style>
    <script src="/webjars/jquery/1.10.2/jquery.js"></script>
    <script src="/webjars/jquery-ui/1.10.2/ui/jquery-ui.js"></script>
    <script>
        $(function() {
            $(document).tooltip();
        })
    </script>
</head>
<body>
<h1>Trace - <@traceName trace=trace/></h1>
<div id="trace-${trace.traceId}" class="trace">
    (start = ${trace.startTime} ; duration = ${trace.duration})
    <#if trace.root??>
        <@renderSpan span=trace.root trace=trace/>
    <#else>
        <@renderAnnotations trace=trace/>
    </#if>
</div>
</body>
</html>

<#macro renderSpan span trace>
<#-- @ftlvariable name="span" type="com.yammer.telemetry.tracing.Span" -->
<#-- @ftlvariable name="trace" type="com.yammer.telemetry.tracing.Trace" -->
<div id="span-${span.spanId}" class="span" style="left: ${((span.startTime - trace.loggedAt) / trace.duration) * 100}%; width: ${(span.duration / trace.duration) * 100}%;">
    <#list trace.getAnnotations(span) as annotation>
    <div class="annotationMarker" style="left: ${((annotation.loggedAt - span.loggedAt) / span.duration) * 100}%;" title="${annotation.name} - ${annotation.message!"null"} @ ${annotation.loggedAt}">&nbsp;</div>
    </#list>
    ${span.name} (start = ${span.startTime}; duration = ${span.duration})
</div>
<#list trace.getChildren(span) as child>
    <@renderSpan span=child trace=trace/>
</#list>
</#macro>

<#macro renderAnnotations trace>
<#-- @ftlvariable name="trace" type="com.yammer.telemetry.tracing.Trace" -->
    <div>Unable to render trace with no spans.</div>
</#macro>

<#macro traceName trace>
<#-- @ftlvariable name="trace" type="com.yammer.telemetry.tracing.Trace" -->
<#if trace.root??>${trace.root.name}<#else>Unknown</#if>
</#macro>