package ch.inofix.referencemanager.web.internal.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import ch.inofix.referencemanager.model.Reference;

/**
 * 
 * @author Christian Berndt
 * @created 2016-11-18 00:58
 * @modified 2017-11-04 20:44
 * @version 1.0.3
 *
 */
public class ReferenceSearch extends SearchContainer<Reference> {

    public static final String EMPTY_RESULTS_MESSAGE = "no-references-were-found";

    public static List<String> headerNames = new ArrayList<>();
    public static Map<String, String> orderableHeaders = new HashMap<>();

    static {
        headerNames.add("author");
        headerNames.add("create-date");
        headerNames.add("modified-date");
        headerNames.add("reference-id");
        headerNames.add("title");
        headerNames.add("type");
        headerNames.add("year");

        orderableHeaders.put("author", "author_sortable");
        orderableHeaders.put("create-date", "createDate_Number_sortable");
        orderableHeaders.put("modified-date", "modifiedDate_Number_sortable");
        orderableHeaders.put("reference-id", "referenceId");
        orderableHeaders.put("title", "title_sortable");
        orderableHeaders.put("type", "type_sortable");
        orderableHeaders.put("year", "year_sortable");

    }

    public ReferenceSearch(PortletRequest portletRequest, PortletURL iteratorURL) {
        this(portletRequest, DEFAULT_CUR_PARAM, iteratorURL);
    }

    public ReferenceSearch(PortletRequest portletRequest, String curParam, PortletURL iteratorURL) {

        super(portletRequest, new ReferenceDisplayTerms(portletRequest), new ReferenceSearchTerms(portletRequest),
                curParam, DEFAULT_DELTA, iteratorURL, headerNames, EMPTY_RESULTS_MESSAGE);

        PortletConfig portletConfig = (PortletConfig) portletRequest.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);

        ReferenceDisplayTerms displayTerms = (ReferenceDisplayTerms) getDisplayTerms();
        ReferenceSearchTerms searchTerms = (ReferenceSearchTerms) getSearchTerms();

        String portletId = PortletProviderUtil.getPortletId(User.class.getName(), PortletProvider.Action.VIEW);
        String portletName = portletConfig.getPortletName();

        if (!portletId.equals(portletName)) {
            displayTerms.setStatus(WorkflowConstants.STATUS_APPROVED);
            searchTerms.setStatus(WorkflowConstants.STATUS_APPROVED);
        }

        iteratorURL.setParameter(ReferenceDisplayTerms.AUTHOR, displayTerms.getAuthor());
        iteratorURL.setParameter(ReferenceDisplayTerms.STATUS, String.valueOf(displayTerms.getStatus()));
        iteratorURL.setParameter(ReferenceDisplayTerms.TITLE, displayTerms.getTitle());
        iteratorURL.setParameter(ReferenceDisplayTerms.YEAR, displayTerms.getYear());

        try {
            PortalPreferences preferences = PortletPreferencesFactoryUtil.getPortalPreferences(portletRequest);

            String orderByCol = ParamUtil.getString(portletRequest, "orderByCol");
            String orderByType = ParamUtil.getString(portletRequest, "orderByType");

            if (Validator.isNotNull(orderByCol) && Validator.isNotNull(orderByType)) {

                preferences.setValue(portletId, "references-order-by-col", orderByCol);
                preferences.setValue(portletId, "references-order-by-type", orderByType);
            } else {
                orderByCol = preferences.getValue(portletId, "references-order-by-col", "last-name");
                orderByType = preferences.getValue(portletId, "references-order-by-type", "asc");
            }

            setOrderableHeaders(orderableHeaders);

            if (Validator.isNotNull(orderableHeaders.get(orderByCol))) {
                setOrderByCol(orderableHeaders.get(orderByCol));
            } else {
                _log.error(orderByCol + " is not an orderable header.");
                setOrderByCol(orderByCol);
            }

            setOrderByType(orderByType);
            
        } catch (Exception e) {
            _log.error(e);
        }
    }

    private static final Log _log = LogFactoryUtil.getLog(ReferenceSearch.class);

}
