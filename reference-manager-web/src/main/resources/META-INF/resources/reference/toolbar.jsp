<%--
    toolbar.jsp: The toolbar of the reference-manager portlet
    
    Created:    2017-11-04 19:19 by Christian Berndt
    Modified:   2017-11-04 19:19 by Christian Berndt
    Version:    1.0.0
--%>

<%@ include file="/init.jsp"%>

<%
    // TODO: read orderable columns from configuration
    String [] columns = new String[] {"author", "title", "year", "modified-date"}; 

    String orderByCol = ParamUtil.getString(request, "orderByCol", "modified-date");

    String orderByType = ParamUtil.getString(request, "orderByType", "desc");

    String searchContainerId = ParamUtil.getString(request, "searchContainerId");
    
    int total = GetterUtil.getInteger(request.getAttribute("view.jsp-total"));
    
    portletURL.setParameters(renderRequest.getParameterMap());
%>

<liferay-frontend:management-bar disabled="<%=total == 0%>"
    includeCheckBox="<%=true%>"
    searchContainerId="<%=searchContainerId%>">

    <liferay-frontend:management-bar-filters>
        <liferay-frontend:management-bar-sort
            orderByCol="<%=orderByCol%>"
            orderByType="<%=orderByType%>"
            orderColumns='<%=columns%>' portletURL="<%=portletURL%>" />
    </liferay-frontend:management-bar-filters>



    <liferay-frontend:management-bar-buttons>

        <liferay-util:include
            page="/reference/display_style_buttons.jsp"
            servletContext="<%=application%>" />
            
    </liferay-frontend:management-bar-buttons>

    <liferay-frontend:management-bar-action-buttons>

        <liferay-ui:icon-menu cssClass="pull-left">
            <%
                // TODO
            %>
            <%--             <liferay-ui:icon iconCssClass="icon-ok" message="approve" url="<%= portletURL.toString() %>" /> --%>
            <%--             <liferay-ui:icon iconCssClass="icon-download" message="download" url="<%= portletURL.toString() %>" /> --%>
            <%--             <liferay-ui:icon iconCssClass="icon-ban-circle" message="reject" url="<%= portletURL.toString() %>" /> --%>
            <liferay-ui:icon iconCssClass="icon-remove" message="delete"
                url="<%="javascript:" + renderResponse.getNamespace() + "deleteEntries();"%>" />
        </liferay-ui:icon-menu>

    </liferay-frontend:management-bar-action-buttons>
</liferay-frontend:management-bar>

<aui:script>
    function <portlet:namespace />deleteEntries() {
        if (confirm('<%= UnicodeLanguageUtil.get(request, "are-you-sure-you-want-to-delete-the-selected-entries") %>')) {
            var form = AUI.$(document.<portlet:namespace />fm);

            form.attr('method', 'post');
            form.fm('<%= Constants.CMD %>').val('<%= Constants.DELETE %>');
            form.fm('deleteTaskRecordIds').val(Liferay.Util.listCheckedExcept(form, '<portlet:namespace />allRowIds'));

            submitForm(form);
        }
    }
</aui:script>
