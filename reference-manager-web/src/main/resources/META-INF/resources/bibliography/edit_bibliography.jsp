<%--
    edit_bibliography.jsp: default view of the bibliography manager portlet.
    
    Created:    2016-11-30 00:18 by Christian Berndt
    Modified:   2017-11-12 22:11 by Christian Berndt
    Version:    1.1.1
--%>

<%@ include file="/init.jsp"%>

<%
    String redirect = ParamUtil.getString(request, "redirect");
    String tabNames = "settings"; 
    String tabs1 = ParamUtil.getString(request, "tabs1", "settings");
    
    Bibliography bibliography = (Bibliography) request.getAttribute(BibliographyWebKeys.BIBLIOGRAPHY);
    
    Group group = null;  
    
    String userLink = null;

    portletURL.setParameter("mvcPath", "/bibliography/edit_bibliography.jsp");
    portletURL.setParameter("mvcRenderCommandName", "editBibliography");

    boolean hasUpdatePermission = true;

    if (bibliography != null) {
        
        group = GroupLocalServiceUtil.getGroup(bibliography.getGroupId());  
        userLink = "<a href=\"" + group.getDisplayURL(themeDisplay) + "\">" + bibliography.getUserName() + "</a>";
        
        hasUpdatePermission = BibliographyPermission.contains(permissionChecker, bibliography, BibliographyActionKeys.UPDATE);
        tabNames = "browse,import,settings";
        portletURL.setParameter("bibliographyId", String.valueOf(bibliography.getBibliographyId()));
        tabs1 = ParamUtil.getString(request, "tabs1", "browse");
        AssetEntryServiceUtil.incrementViewCounter(Bibliography.class.getName(), bibliography.getBibliographyId());
    }
%>

<c:choose>
    <c:when test="<%=Validator.isNull(bibliography)%>">
        <div class="bibliography-head">
            <h2>
                <liferay-ui:message key="create-a-new-bibliography" />
            </h2>
            
            <c:choose>
                <c:when test="<%= themeDisplay.isSignedIn() %>">
                    <p>
                        <liferay-ui:message key="you-can-import-your-references-from-a-file-or-pick-references-already-available-on-bibshare" />
                    </p>                    
                </c:when>
                <c:otherwise>
                    <div class="alert alert-info">
                        <liferay-ui:message key="you-must-sign-in-order-to-create-a-new-bibliography"/>
                        
                        <strong>
                            <aui:a href="<%= themeDisplay.getURLSignIn() %>" label="sign-in">
                                <liferay-ui:icon iconCssClass="icon-signin"/>
                            </aui:a>
                        </strong> 
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:when>
    <c:otherwise>
        <div class="bibliography-head">
            <h2><%=bibliography.getTitle()%></h2>
            <div class="clearfix">
                <div class="compiled-by pull-left">

                    <liferay-ui:message key="compiled-by-x"
                        arguments="<%=new String[] { userLink }%>" />

                </div>

                <%
                    ResourceURL downloadURL = liferayPortletResponse.createResourceURL();

                    downloadURL.setResourceID("exportBibliography");

                    // Copy render parameters to resourceRequest
                    downloadURL.setParameters(renderRequest.getParameterMap());

                    downloadURL.setParameter("bibliographyId", String.valueOf(bibliography.getBibliographyId()));
                    downloadURL.setParameter("cmd", "download");
                %>


                <%--                 <portlet:resourceURL id="exportBibliography" var="exportBibliographyURL"> --%>
<%--                     <portlet:param name="bibliographyId" value="<%=String.valueOf(bibliography.getBibliographyId())%>" /> --%>
<%--                     <portlet:param name="mvcRenderCommandName" value="exportBibliography" /> --%>
<%--                 </portlet:resourceURL> --%>

                <aui:button cssClass="btn-sm pull-right"
                    icon="icon-download"
                    href="<%= downloadURL.toString() %>"
                    value="download" />

            </div>

        </div>
    </c:otherwise>
</c:choose>

<liferay-ui:tabs names="<%= tabNames %>" param="tabs1"
    url="<%=portletURL.toString()%>" />
    
<c:choose>

    <c:when test='<%= tabs1.equals("import") %>'>
        <liferay-util:include page="/bibliography/import_bibliography.jsp" servletContext="<%= application %>" />
    </c:when>
    
   <c:when test='<%= tabs1.equals("settings") %>'>
       <liferay-util:include page="/bibliography/bibliography_settings.jsp" servletContext="<%= application %>" />
   </c:when>        

    <c:otherwise> 
       <liferay-util:include page="/bibliography/bibliography_entries.jsp" servletContext="<%= application %>" />     
    </c:otherwise>
</c:choose>
