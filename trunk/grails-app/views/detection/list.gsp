
<%@ page import="au.org.emii.aatams.detection.ValidDetection" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'detection.label', default: 'Detection')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <shiro:hasPermission permission="projectWriteAny">
              <span class="menuButton"><g:link class="create" action="create"><g:message code="default.upload.label" args="[entityName]" /></g:link></span>
            </shiro:hasPermission>
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
                        
                            <td/>
                            
                            <g:sortableColumn property="timestamp" title="${message(code: 'detection.timestamp.label', default: 'Timestamp')}" />
                        
                            <g:sortableColumn property="receiverName" title="${message(code: 'detection.receiverName.label', default: 'Receiver Name')}" />
                        
                            <g:sortableColumn property="receiverDeployment" title="${message(code: 'detection.receiverDeployment.label', default: 'Receiver Deployment')}" />
                        
                            <g:sortableColumn property="transmitterId" title="${message(code: 'detection.transmitterId.label', default: 'Transmitter ID')}" />

                            <g:sortableColumn property="transmitterName" title="${message(code: 'detection.transmitterName.label', default: 'Transmitter Name')}" />
                        
                            <g:sortableColumn property="transmitterSerialNumber" title="${message(code: 'detection.transmitterSerialNumber.label', default: 'Transmitter Serial Number')}" />
                        
                            <g:sortableColumn property="stationName" title="${message(code: 'detection.stationName.label', default: 'Station Name')}" />
                        
                            <g:sortableColumn property="receiverDownload?.requestingUser" title="${message(code: 'receiverDownloadFile.requestingUser.label', default: 'Uploader')}" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${detectionInstanceList}" status="i" var="detectionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td class="rowButton"><g:link class="show" action="show" id="${detectionInstance.id}">.</g:link></td>
                    
                            <td><g:formatDate date="${detectionInstance.timestamp}"
                                              format="yyyy-MM-dd'T'HH:mm:ssZ"
                                              timeZone='${TimeZone.getTimeZone("UTC")}'/></td>
                        
                            <td>${fieldValue(bean: detectionInstance, field: "receiverName")}</td>

                            <td><g:link controller="receiverDeployment" action="show" id="${detectionInstance?.receiverDeployment?.id}">${fieldValue(bean: detectionInstance, field: "receiverDeployment")}</g:link></td>
                        
                            <td>${fieldValue(bean: detectionInstance, field: "transmitterId")}</td>
                        
                            <td>${fieldValue(bean: detectionInstance, field: "transmitterName")}</td>
                        
                            <td>${fieldValue(bean: detectionInstance, field: "transmitterSerialNumber")}</td>
                        
                            <td>${fieldValue(bean: detectionInstance, field: "stationName")}</td>
                        
                            <td><g:link controller="person" action="show" id="${detectionInstance?.receiverDownload?.requestingUser?.id}">${fieldValue(bean: detectionInstance.receiverDownload, field: "requestingUser")}</g:link></td>

                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${detectionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>