<%--
    display_styles_buttons.jsp: Select the display style of the reference manager.
    
    Created:    2017-11-04 19:24 by Christian Berndt
    Modified:   2017-11-04 19:24 by Christian Berndt
    Version:    1.0.0
--%>

<%@ include file="/init.jsp" %>

<%
    String navigation = ParamUtil.getString(request, "navigation", "all");

    String displayStyle = ParamUtil.getString(request, "displayStyle");

    // TODO: implement displayStyle descriptive
    
//     if (Validator.isNull(displayStyle)) {
//         displayStyle = portalPreferences.getValue(PortletKeys.REFERENCE_MANAGER, "display-style", "list");
//         displayStyle = portalPreferences.getValue(PortletKeys.REFERENCE_MANAGER, "display-style", "descriptive");
//     }
    
    PortletURL displayStyleURL = renderResponse.createRenderURL();
%>

<liferay-frontend:management-bar-display-buttons
    displayViews='<%= new String[] {"list"} %>'
    portletURL="<%= displayStyleURL %>"
    selectedDisplayStyle="<%= displayStyle %>"
/>
