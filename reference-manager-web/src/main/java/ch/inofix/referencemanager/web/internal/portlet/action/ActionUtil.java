package ch.inofix.referencemanager.web.internal.portlet.action;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import ch.inofix.referencemanager.model.Bibliography;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.BibliographyServiceUtil;
import ch.inofix.referencemanager.service.ReferenceServiceUtil;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 19:20
 * @modified 2017-11-12 19:20
 * @version 1.0.0
 *
 */
public class ActionUtil {

    public static Bibliography getBibliography(HttpServletRequest request) throws Exception {

        _log.info("getBibliography(request)");

        long bibliographyId = ParamUtil.getLong(request, "bibliographyId");

        Bibliography bibliography = null;

        if (bibliographyId > 0) {
            bibliography = BibliographyServiceUtil.getBibliography(bibliographyId);

            // TODO: Add TrashBin support
            // if (bibliography.isInTrash()) {
            // throw new NoSuchBibliographyException("{bibliographyId=" +
            // bibliographyId + "}");
            // }
        }

        return bibliography;
    }

    public static Bibliography getBibliography(PortletRequest portletRequest) throws Exception {

        _log.info("getBibliography(portletRequest)");

        HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);

        return getBibliography(request);
    }

    public static Reference getReference(HttpServletRequest request) throws Exception {

        _log.info("getReference(request)");

        long referenceId = ParamUtil.getLong(request, "referenceId");

        Reference reference = null;

        if (referenceId > 0) {
            reference = ReferenceServiceUtil.getReference(referenceId);

            // TODO: Add TrashBin support
            // if (reference.isInTrash()) {
            // throw new NoSuchReferenceException("{referenceId=" +
            // referenceId + "}");
            // }
        }

        return reference;
    }

    public static Reference getReference(PortletRequest portletRequest) throws Exception {

        _log.info("getReference(portletRequest)");

        HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);

        return getReference(request);
    }

    private static Log _log = LogFactoryUtil.getLog(ActionUtil.class.getName());

}
