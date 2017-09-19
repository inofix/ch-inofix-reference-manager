<%--
    bibliography/configuration.jsp: configuration of the bibliography manager portlet.
    
    Created:    2017-02-09 00:04 by Christian Berndt
    Modified:   2017-02-13 22:44 by Christian Berndt
    Version:    1.0.2
--%>

<%@ include file="/init.jsp"%>

<%
    String[] columns = new String[0];

    if (Validator.isNotNull(bibliographyManagerConfiguration)) {
        columns = portletPreferences.getValues("columns", bibliographyManagerConfiguration.columns());
    }

    ReferenceSearch searchContainer = new ReferenceSearch(liferayPortletRequest, portletURL);
    List<String> headerNames = searchContainer.getHeaderNames();
%>

<liferay-portlet:actionURL portletConfiguration="<%= true %>"
    var="configurationActionURL" />

<liferay-portlet:renderURL portletConfiguration="<%= true %>"
    var="configurationRenderURL" />

<aui:form action="<%= configurationActionURL %>" method="post" name="fm"
    onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "saveConfiguration();" %>'>

    <aui:input name="<%=Constants.CMD%>" type="hidden"
        value="<%=Constants.UPDATE%>" />

    <aui:input name="redirect" type="hidden"
        value="<%=configurationRenderURL%>" />
        
        <aui:input name="columns" type="hidden"/>

        <aui:fieldset collapsible="<%=true%>" label="show-columns">
            <%
                Set<String> availableColumns = SetUtil.fromList(headerNames);

                // Left list

                List leftList = new ArrayList();

                for (String column : columns) {
                    leftList.add(new KeyValuePair(column, LanguageUtil.get(request, column)));
                }

                // Right list

                List rightList = new ArrayList();

                Arrays.sort(columns);

                for (String column : availableColumns) {
                    if (Arrays.binarySearch(columns, column) < 0) {
                        rightList.add(new KeyValuePair(column, LanguageUtil.get(request, column)));
                    }
                }

                rightList = ListUtil.sort(rightList, new KeyValuePairComparator(false, true));
            %>

            <liferay-ui:input-move-boxes
                leftBoxName="currentColumns"
                leftList="<%=leftList%>"
                leftReorder="<%=Boolean.TRUE.toString()%>"
                leftTitle="current"
                rightBoxName="availableColumns"
                rightList="<%=rightList%>" rightTitle="available" />
        </aui:fieldset>
                
    <aui:button-row>
        <aui:button type="submit"></aui:button>
    </aui:button-row>
</aui:form>

<aui:script>
    function <portlet:namespace />saveConfiguration() {
        var Util = Liferay.Util;

        var form = AUI.$(document.<portlet:namespace />fm);

        form.fm('columns').val(Util.listSelect(form.fm('currentColumns')));

        submitForm(form);
    }
</aui:script>
