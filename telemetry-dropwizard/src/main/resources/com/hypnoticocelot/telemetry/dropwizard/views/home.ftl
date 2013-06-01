<#-- @ftlvariable name="" type="com.hypnoticocelot.telemetry.dropwizard.views.TracingHomeView" -->
<html>
<head>
    <title>Traces</title>
</head>
<body>
<table>
    <tr>
        <th>ID</th>
        <th>Name</th>
    </tr>
<#list traces as trace>
    <tr>
        <td><a href="/tracing/${trace.id.toString()?url('utf-8')}">${trace.id}</a></td>
        <td>${trace.root.data.name}</td>
    </tr>
</#list>
</table>
</body>
</html>