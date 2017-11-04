<%--
    reference/view.jsp: Default view of the reference manager portlet.
    
    Created:    2016-01-10 22:51 by Christian Berndt
    Modified:   2017-11-04 19:38 by Christian Berndt
    Version:    1.2.1
--%>

<%@ include file="/init.jsp" %>

<%
    String backURL = ParamUtil.getString(request, "backURL");
    String displayStyle = ParamUtil.getString(request, "displayStyle");
    String keywords = ParamUtil.getString(request, "keywords");
    String tabs1 = ParamUtil.getString(request, "tabs1", "browse");
    
    String [] columns = new String[] {"author", "title", "year"}; 

    if (Validator.isNotNull(referenceManagerConfiguration)) {
        columns = portletPreferences.getValues("columns", referenceManagerConfiguration.columns());
    }

    SearchContainer<Reference> searchContainer = new ReferenceSearch(renderRequest, "cur", portletURL);
    
    boolean reverse = false; 
    
    if (searchContainer.getOrderByType().equals("desc")) {
        reverse = true;
    }
    
    Sort sort = new Sort(searchContainer.getOrderByCol(), reverse);
    
    ReferenceSearchTerms searchTerms = (ReferenceSearchTerms) searchContainer.getSearchTerms();

    Hits hits = ReferenceServiceUtil.search(themeDisplay.getUserId(), scopeGroupId, -1, keywords,
            searchContainer.getStart(), searchContainer.getEnd(), sort);
    
    List<Document> documents = ListUtil.toList(hits.getDocs());
    
    List<Reference> references = new ArrayList<Reference>();
    
    for (Document document : documents) {
        try {
            long referenceId = GetterUtil.getLong(document.get("entryClassPK"));
            Reference reference = ReferenceServiceUtil.getReference(referenceId);
            references.add(reference); 
        } catch (Exception e) {
            System.out.println(e); 
        }
    }

    searchContainer.setResults(references); 
    searchContainer.setTotal(hits.getLength());
    
    ReferenceEntriesChecker entriesChecker = new ReferenceEntriesChecker(liferayPortletRequest, liferayPortletResponse);

    searchContainer.setRowChecker(entriesChecker);
    
    AssetRendererFactory<Reference> referenceAssetRendererFactory = AssetRendererFactoryRegistryUtil
            .getAssetRendererFactoryByClass(Reference.class);
    
    request.setAttribute("view.jsp-columns", columns);

    request.setAttribute("view.jsp-displayStyle", displayStyle);

    request.setAttribute("view.jsp-searchContainer", searchContainer);

    request.setAttribute("view.jsp-total", hits.getLength());
%>

<liferay-ui:error exception="<%= PrincipalException.class %>"
	message="you-dont-have-the-required-permissions" />
 
<liferay-ui:tabs names="browse,import,manage" param="tabs1"
	url="<%=portletURL.toString()%>" />

<c:choose>

	<c:when test='<%= tabs1.equals("import") %>'>
        <liferay-util:include page="/reference/import_references.jsp" servletContext="<%= application %>" />
	</c:when>
	
    <c:when test='<%= tabs1.equals("manage") %>'>
        <liferay-util:include page="/reference/manage.jsp" servletContext="<%= application %>" />
    </c:when>		

	<c:otherwise>

        <%-- 
        <div class="search-results">
            <liferay-ui:search-speed hits="<%= hits %>" searchContainer="<%= searchContainer %>" />
        </div>
        --%>
        
        <liferay-util:include page="/reference/toolbar.jsp" servletContext="<%= application %>">
            <liferay-util:param name="searchContainerId" value="references" />
        </liferay-util:include>

        <liferay-ui:search-container
            cssClass="references-search-container"            
            id="references"
            searchContainer="<%= searchContainer %>"
            var="referenceSearchContainer">
            
			<liferay-ui:search-container-row
				className="ch.inofix.referencemanager.model.Reference"
				escapedModel="true" keyProperty="referenceId" modelVar="reference">
                
                <%@ include file="/reference/search_columns.jspf" %>
 
                <liferay-ui:search-container-column-jsp cssClass="entry-action"
                     path="/reference/reference_action.jsp" valign="top" />

			</liferay-ui:search-container-row>

            <liferay-ui:search-iterator markupView="<%= markupView %>"/>
            			
		</liferay-ui:search-container>
	</c:otherwise>
</c:choose>
