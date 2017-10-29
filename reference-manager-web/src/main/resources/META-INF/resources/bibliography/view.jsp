<%--
    bibliography/view.jsp: display the list of bibliographies.
    
    Created:    2016-12-16 00:12 by Christian Berndt
    Modified:   2017-10-29 19:17 by Christian Berndt
    Version:    1.1.2
--%>

<%@ include file="/init.jsp"%>

<%
    boolean hasAddPermission = false;

    long userGroupId = 0;
    Group userGroup = user.getGroup();
    String userName = themeDisplay.getScopeGroupName();

    if (userGroup != null) {
        userGroupId = userGroup.getGroupId();
        hasAddPermission = BibliographyManagerPortletPermission.contains(permissionChecker, userGroupId,
                BibliographyActionKeys.ADD_BIBLIOGRAPHY);
    }
    boolean isUserGroup = themeDisplay.getScopeGroupId() == userGroupId;

    String keywords = null;

    SearchContainer<Bibliography> bibliographySearch = new BibliographySearch(renderRequest, "cur", portletURL);

    boolean reverse = false;
    if (bibliographySearch.getOrderByType().equals("desc")) {
        reverse = true;
    }

    String tabNames = "settings";
    String tabs1 = ParamUtil.getString(request, "tabs1", "settings");

    Sort sort = new Sort(bibliographySearch.getOrderByCol(), reverse);

    BibliographySearchTerms searchTerms = (BibliographySearchTerms) bibliographySearch.getSearchTerms();

    Hits hits = null;

    if (isUserGroup) {
        hits = BibliographyServiceUtil.search(themeDisplay.getUserId(), 0, themeDisplay.getUserId(), keywords,
                bibliographySearch.getStart(), bibliographySearch.getEnd(), sort);
    } else {

        hits = BibliographyServiceUtil.search(themeDisplay.getUserId(), 0, -1,
                keywords, bibliographySearch.getStart(), bibliographySearch.getEnd(), sort);
    }

    List<Document> documents = ListUtil.toList(hits.getDocs());

    List<Bibliography> bibliographies = new ArrayList<Bibliography>();

    for (Document document : documents) {
        try {
            long bibliographyId = GetterUtil.getLong(document.get("entryClassPK"));
            Bibliography bibliography = BibliographyServiceUtil.getBibliography(bibliographyId);
            bibliographies.add(bibliography);
        } catch (Exception e) {
            // TODO: use logging
            System.out.println(e);
        }
    }

    bibliographySearch.setResults(bibliographies);
    bibliographySearch.setTotal(hits.getLength());

    AssetRendererFactory<Bibliography> assetRendererFactory = AssetRendererFactoryRegistryUtil
            .getAssetRendererFactoryByClass(Bibliography.class);
%>

<div class="bibliography-head">
    <c:choose>
        <c:when test="<%= isUserGroup %>">
            <h2><liferay-ui:message key="your-bibliographies"/></h2>
        </c:when>
        <c:otherwise>
            <h2><liferay-ui:message key="bibliographies"/></h2>
<%--             <h2><liferay-ui:message key="bibliographies-of-x" arguments="<%= new String[] {userName} %>"/></h2> --%>
        </c:otherwise>
    </c:choose>
</div>

<liferay-ui:search-container
    cssClass="bibliographies-search-container"  
    emptyResultsMessage="the-user-hasnt-created-any-bibliographies-yet"          
    id="references"    
    searchContainer="<%= bibliographySearch %>"
    var="bibliographySearchContainer">
    
    <liferay-ui:search-container-row
        className="ch.inofix.referencemanager.model.Bibliography"
        escapedModel="true" modelVar="bibliography">
        
        <portlet:renderURL var="viewURL">
            <portlet:param name="mvcPath" value="/bibliography/edit_bibliography.jsp" />
            <portlet:param name="redirect" value="<%=currentURL%>" />
            <portlet:param name="bibliographyId"
                value="<%=String.valueOf(bibliography.getBibliographyId())%>" />
        </portlet:renderURL>
        
        <%
                    Group group = null;  
                    
                    String userLink = null;
                        
                    portletURL.setParameter("mvcPath", "/bibliography/edit_bibliography.jsp");
                        
                    boolean hasUpdatePermission = true;
                    
                    int numReferences = 0; 
                        
                    if (bibliography != null) {

                        group = GroupLocalServiceUtil.getGroup(bibliography.getGroupId());
                        userLink = "<a href=\"" + group.getDisplayURL(themeDisplay) + "\">" + bibliography.getUserName()
                                + "</a>";

                        hasUpdatePermission = BibliographyPermission.contains(permissionChecker, bibliography,
                                BibliographyActionKeys.UPDATE);
                        tabNames = "browse,import,settings";
                        portletURL.setParameter("bibliographyId", String.valueOf(bibliography.getBibliographyId()));
                        tabs1 = ParamUtil.getString(request, "tabs1", "browse");
                        AssetEntryServiceUtil.incrementViewCounter(Bibliography.class.getName(),
                                bibliography.getBibliographyId());

                        Hits referenceHits = ReferenceServiceUtil.search(themeDisplay.getUserId(), bibliography.getGroupId(),
                                bibliography.getBibliographyId(), keywords, 0, 0, sort);
                        
                        numReferences = referenceHits.getLength();
                    }
                %>

        <liferay-ui:search-container-column-text orderable="true"
            orderableProperty="title_sortable" valign="middle">

            <aui:a cssClass="bibliography-title" href="<%=viewURL%>"
                label="<%=bibliography.getTitle() + " (" + numReferences + ")" %>" />
                
            <div class="description"><%=bibliography.getDescription()%></div>
            
            <div class="compiled-by">
                <liferay-ui:message key="compiled-by-x"
                        arguments="<%=new String[] { userLink }%>" />
            </div>

            <liferay-ui:asset-tags-summary
                className="<%=Bibliography.class.getName()%>"
                classPK="<%=bibliography.getBibliographyId()%>" />
                
            <liferay-ui:ratings 
                className="<%=Bibliography.class.getName()%>"
                classPK="<%=bibliography.getBibliographyId()%>" 
                type="stars" />

        </liferay-ui:search-container-column-text>

        <liferay-ui:search-container-column-jsp align="right" cssClass="entry-action"
              path="/bibliography/bibliography_action.jsp" valign="top"/>

    </liferay-ui:search-container-row>

    <liferay-ui:search-iterator markupView="<%= markupView  %>"/>
                
</liferay-ui:search-container>

<%
    liferayPortletRequest.setAttribute("redirect", currentURL);
    String editBibliographyURL = ""; 
 
    PortletURL addURL = assetRendererFactory
            .getURLAdd(liferayPortletRequest, liferayPortletResponse);
    
    if (addURL != null) {
        editBibliographyURL = addURL.toString(); 
    } else {
        // GUEST user
        hasAddPermission = false; 
    }
%>

<aui:button href="<%=editBibliographyURL%>"
    cssClass="btn-primary btn-success" value="new-bibliography"
    disabled="<%=!hasAddPermission%>" />
