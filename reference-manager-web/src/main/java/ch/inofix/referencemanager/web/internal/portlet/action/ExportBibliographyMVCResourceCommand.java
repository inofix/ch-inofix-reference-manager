package ch.inofix.referencemanager.web.internal.portlet.action;

import java.io.IOException;
import java.util.List;

import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;

import ch.inofix.referencemanager.constants.PortletKeys;
import ch.inofix.referencemanager.model.Bibliography;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.BibliographyService;
import ch.inofix.referencemanager.service.ReferenceService;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 23:38
 * @modified 2017-11-12 23:38
 * @version 1.0.0
 *
 */
@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + PortletKeys.BIBLIOGRAPHY_MANAGER,
        "mvc.command.name=exportBibliography"
    },
    service = MVCResourceCommand.class
)
public class ExportBibliographyMVCResourceCommand extends BaseMVCResourceCommand {

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {

        _log.info("doServeResource()");

        String cmd = ParamUtil.getString(resourceRequest, Constants.CMD);

        _log.info("cmd = " + cmd);

        PortletRequestDispatcher portletRequestDispatcher = null;

        if (cmd.equals("download")) {

            download(resourceRequest, resourceResponse);


        } else {
            
            portletRequestDispatcher = getPortletRequestDispatcher(resourceRequest, "/view.jsp");

            portletRequestDispatcher.include(resourceRequest, resourceResponse);

        }

    }

    protected void download(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws PortalException, IOException {

        ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);

        long bibliographyId = ParamUtil.getLong(resourceRequest, "bibliographyId");

        Bibliography bibliography = _bibliographyService.getBibliography(bibliographyId);

        String fileName = bibliography.getTitle() + ".bib";

        Hits hits = _referenceService.search(themeDisplay.getUserId(), 0, bibliography.getBibliographyId(), null, 0,
                Integer.MAX_VALUE, null);

        StringBuilder sb = new StringBuilder();

        sb.append(StringPool.PERCENT);
        sb.append(" Encoding: UTF-8");
        sb.append(StringPool.NEW_LINE);
        sb.append(StringPool.NEW_LINE);

        sb.append(bibliography.getPreamble());

        sb.append(bibliography.getStrings());

        List<Document> documents = hits.toList();

        for (Document document : documents) {

            long referenceId = GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK));

            Reference reference = _referenceService.getReference(referenceId);
            String bibTeX = reference.getBibTeX();

            sb.append(bibTeX);
            sb.append(StringPool.NEW_LINE);

        }

        sb.append(bibliography.getComments());

        String export = sb.toString();

        PortletResponseUtil.sendFile(resourceRequest, resourceResponse, fileName, export.getBytes(),
                ContentTypes.TEXT_PLAIN_UTF8);

    }

    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setBibliographyService(BibliographyService bibliographyService) {
        this._bibliographyService = bibliographyService;
    }

    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setReferenceService(ReferenceService referenceService) {
        this._referenceService = referenceService;
    }

    private static Log _log = LogFactoryUtil.getLog(ExportBibliographyMVCResourceCommand.class.getName());

    private BibliographyService _bibliographyService;

    private ReferenceService _referenceService;

}
