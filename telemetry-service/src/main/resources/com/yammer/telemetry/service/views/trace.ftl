<#-- @ftlvariable name="" type="com.yammer.telemetry.service.views.TraceView" -->
<html>
<head>
    <title>Trace - ${trace.root.info.name}</title>
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
<h1>Trace - ${trace.root.info.name}</h1>
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
    ${span.info.name} (start = ${span.startTimeNanos}; duration = ${span.duration}}
<#if (span.info.annotations?size > 0)>
    <table>
        <tr>
            <th>Annotation</th>
            <th>Value</th>
        </tr>
        <#list span.info.annotations?keys as annotation>
        <tr>
            <td>${annotation}</td>
            <td>${span.info.annotations[annotation]}</td>
        </tr>
        </#list>
    </table>
</#if>
</div>
<#list trace.getChildren(span) as child>
    <@renderSpan span=child trace=trace/>
</#list>
</#macro>