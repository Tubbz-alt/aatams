
<%@ page import="au.org.emii.aatams.SensorDetection" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sensorDetection.label', default: 'SensorDetection')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'sensorDetection.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="timestamp" title="${message(code: 'sensorDetection.timestamp.label', default: 'Timestamp')}" />
                        
                            <th><g:message code="sensorDetection.receiver.label" default="Receiver" /></th>
                        
                            <g:sortableColumn property="transmitterName" title="${message(code: 'sensorDetection.transmitterName.label', default: 'Transmitter Name')}" />
                        
                            <g:sortableColumn property="stationName" title="${message(code: 'sensorDetection.stationName.label', default: 'Station Name')}" />
                        
                            <g:sortableColumn property="location" title="${message(code: 'sensorDetection.location.label', default: 'Location')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${sensorDetectionInstanceList}" status="i" var="sensorDetectionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${sensorDetectionInstance.id}">${fieldValue(bean: sensorDetectionInstance, field: "id")}</g:link></td>
                        
                            <td><g:formatDate date="${sensorDetectionInstance.timestamp}" /></td>
                        
                            <td>${fieldValue(bean: sensorDetectionInstance, field: "receiver")}</td>
                        
                            <td>${fieldValue(bean: sensorDetectionInstance, field: "transmitterName")}</td>
                        
                            <td>${fieldValue(bean: sensorDetectionInstance, field: "stationName")}</td>
                        
                            <td>${fieldValue(bean: sensorDetectionInstance, field: "location")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${sensorDetectionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>