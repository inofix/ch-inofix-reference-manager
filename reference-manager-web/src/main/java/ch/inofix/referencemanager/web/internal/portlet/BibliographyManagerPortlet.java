package ch.inofix.referencemanager.web.internal.portlet;

import java.io.IOException;
import java.util.List;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.theme.ThemeDisplay;
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
 * View Controller of Inofix' bibliography-manager.
 * 
 * @author Christian Berndt
 * @created 2016-11-29 22:33
 * @modified 2017-11-12 21:10
 * @version 1.3.3
 */
@Component(
    configurationPid = "ch.inofix.referencemanager.web.configuration.BibliographyManagerConfiguration",
    immediate = true, 
    property = { 
        "com.liferay.portlet.add-default-resource=true",
        "com.liferay.portlet.css-class-wrapper=portlet-bibliography-manager",
        "com.liferay.portlet.display-category=category.inofix", 
        "com.liferay.portlet.header-portlet-css=/css/main.css",
        "com.liferay.portlet.instanceable=false", 
        "javax.portlet.init-param.template-path=/",
        "javax.portlet.init-param.view-template=/bibliography/view.jsp",
        "javax.portlet.name=" + PortletKeys.BIBLIOGRAPHY_MANAGER, 
        "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.security-role-ref=power-user,user" 
    }, 
    service = Portlet.class
)
public class BibliographyManagerPortlet extends MVCPortlet { 
    
    // TODO: move export handling to ExportBibliographyMVCResourceCommand.
    
    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException, PortletException {

        exportBibliography(resourceRequest, resourceResponse);

    }
    
    protected void exportBibliography(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws PortletException {

        ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);

        long bibliographyId = ParamUtil.getLong(resourceRequest, "bibliographyId");

        try {

            Bibliography bibliography = _bibliographyService.getBibliography(bibliographyId);

            String fileName = bibliography.getTitle() + ".bib";

            Hits hits = _referenceService.search(themeDisplay.getUserId(), bibliography.getGroupId(),
                    bibliography.getBibliographyId(), null, 0, Integer.MAX_VALUE, null);

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
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    
    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setBibliographyService(BibliographyService bibliographyService) {
        this._bibliographyService = bibliographyService;
    }
    



    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setReferenceService(ReferenceService referenceService) {
        this._referenceService = referenceService;
    }

    
    private BibliographyService _bibliographyService;

    private ReferenceService _referenceService;

}
