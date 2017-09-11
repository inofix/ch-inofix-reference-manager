package ch.inofix.referencemanager.web.internal.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import ch.inofix.referencemanager.constants.PortletKeys;
import ch.inofix.referencemanager.service.BibliographyService;

/**
 * View Controller of Inofix' your-bibliographies-portlet
 * 
 * @author Christian Berndt
 * @created 2016-12-01 02:02
 * @modified 2017-09-11 18:44
 * @version 1.0.3
 */
@Component(immediate = true, property = { "com.liferay.portlet.add-default-resource=true",
        "com.liferay.portlet.css-class-wrapper=portlet-your-bibliographies",
        "com.liferay.portlet.display-category=category.inofix", "com.liferay.portlet.header-portlet-css=/css/main.css",
        "com.liferay.portlet.instanceable=false", "javax.portlet.init-param.template-path=/",
        "javax.portlet.init-param.view-template=/your_bibliographies.jsp",
        "javax.portlet.name=" + PortletKeys.YOUR_BIBLIOGRAPHIES, "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.security-role-ref=power-user,user" }, service = Portlet.class)

public class YourBibliographiesPortlet extends BibliographiesPortlet {

    /**
     * 
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.0
     * @throws Exception
     */
    public void deleteBibliography(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        _log.info("deleteBibliography()");

        long bibliographyId = ParamUtil.getLong(actionRequest, "bibliographyId");

        _log.info("bibliographyId = " + bibliographyId);

        _bibliographyService.deleteBibliography(bibliographyId);
    }

    @org.osgi.service.component.annotations.Reference
    protected void setBibliographyService(BibliographyService bibliographyService) {
        this._bibliographyService = bibliographyService;
    }

    private BibliographyService _bibliographyService;

    private static Log _log = LogFactoryUtil.getLog(YourBibliographiesPortlet.class.getName());

}
