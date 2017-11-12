package ch.inofix.referencemanager.web.internal.portlet.action;

import java.io.File;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import ch.inofix.referencemanager.constants.PortletKeys;
import ch.inofix.referencemanager.exception.NoSuchBibliographyException;
import ch.inofix.referencemanager.model.Bibliography;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.BibliographyService;
import ch.inofix.referencemanager.service.ReferenceService;
import ch.inofix.referencemanager.web.internal.portlet.util.PortletUtil;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-10 19:09
 * @modified 2017-11-11 13:28
 * @version 1.0.1
 *
 */
@Component(
    property = {
        "javax.portlet.name=" + PortletKeys.BIBLIOGRAPHY_MANAGER,
        "mvc.command.name=editBibliography"
    },
    service = MVCActionCommand.class
)
public class EditBibliographyMVCActionCommand extends BaseMVCActionCommand {
    
    protected void deleteGroupBibliographies(ActionRequest actionRequest) throws Exception {

        _log.info("deleteGroupBibliographies()");

        ServiceContext serviceContext = ServiceContextFactory.getInstance(Bibliography.class.getName(), actionRequest);

        _bibliographyService.deleteGroupBibliographies(serviceContext.getScopeGroupId());

    }

    protected void deleteBibliographys(ActionRequest actionRequest) throws Exception {

        long bibliographyId = ParamUtil.getLong(actionRequest, "bibliographyId");

        long[] bibliographyIds = ParamUtil.getLongValues(actionRequest, "deleteBibliographyIds");

        if (bibliographyId > 0) {
            bibliographyIds = new long[] { bibliographyId };
        }

        for (long id : bibliographyIds) {
            _bibliographyService.deleteBibliography(id);
        }

    }

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

        _log.info("doProcessAction");
        _log.info("cmd = " + cmd);

        ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

        Bibliography bibliography = null;
        try {

            if (cmd.equals(Constants.ADD) || cmd.equals(Constants.UPDATE)) {
                bibliography = updateBibliography(actionRequest);
            } else if (cmd.equals(Constants.DELETE)) {
                deleteBibliographys(actionRequest);
            } else if (cmd.equals("deleteGroupBibliographies")) {
                deleteGroupBibliographies(actionRequest);
            } else if (cmd.equals("importBibliography")) {
                importBibliography(actionRequest);
            }

            if (Validator.isNotNull(cmd)) {
                String redirect = ParamUtil.getString(actionRequest, "redirect");
                if (bibliography != null) {

                    redirect = getSaveAndContinueRedirect(actionRequest, bibliography, themeDisplay.getLayout(),
                            redirect);

                    sendRedirect(actionRequest, actionResponse, redirect);
                }
            }

        } catch (NoSuchBibliographyException | PrincipalException e) {

            SessionErrors.add(actionRequest, e.getClass());

            actionResponse.setRenderParameter("mvcPath", "/error.jsp");

            // TODO: Define set of exceptions reported back to user. For an
            // example, see EditCategoryMVCActionCommand.java.

        } catch (Exception e) {

            SessionErrors.add(actionRequest, e.getClass());
        }
    }
    
    protected String getSaveAndContinueRedirect(ActionRequest actionRequest, Bibliography bibliography, Layout layout,
            String redirect) throws Exception {

        PortletConfig portletConfig = (PortletConfig) actionRequest.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);

        String tabs1 = ParamUtil.getString(actionRequest, "tabs1");

        LiferayPortletURL portletURL = PortletURLFactoryUtil.create(actionRequest, portletConfig.getPortletName(),
                layout, PortletRequest.RENDER_PHASE);

        portletURL.setParameter("bibliographyId", String.valueOf(bibliography.getBibliographyId()), false);
        portletURL.setParameter(Constants.CMD, Constants.UPDATE, false);
        portletURL.setParameter("groupId", String.valueOf(bibliography.getGroupId()), false);
        portletURL.setParameter("mvcRenderCommandName", "editBibliography");
        portletURL.setParameter("redirect", redirect, false);
        portletURL.setParameter("tabs1", tabs1);
        portletURL.setWindowState(actionRequest.getWindowState());

        return portletURL.toString();
    }
    
    // TODO: move import to separate class. 
    protected void importBibliography(ActionRequest actionRequest) throws Exception {

        HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);

        File file = uploadPortletRequest.getFile("file");
        String fileName = file.getName();

        long userId = themeDisplay.getUserId();
        long groupId = themeDisplay.getScopeGroupId();
        boolean privateLayout = themeDisplay.getLayout().isPrivateLayout();

        Map<String, String[]> parameterMap = request.getParameterMap();

        if (Validator.isNotNull(file)) {

            String message = PortletUtil.translate("upload-successfull-import-will-finish-in-a-separate-thread");
            ServiceContext serviceContext = ServiceContextFactory.getInstance(Reference.class.getName(),
                    uploadPortletRequest);

            _referenceService.importReferencesInBackground(userId, fileName, groupId, privateLayout, parameterMap, file,
                    serviceContext);

            SessionMessages.add(actionRequest, "request_processed", message);

        } else {

            SessionErrors.add(actionRequest, "file-not-found");

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

    protected Bibliography updateBibliography(ActionRequest actionRequest) throws Exception {

        _log.info("updateBibliography");

        long bibliographyId = ParamUtil.getLong(actionRequest, "bibliographyId");

        String title = ParamUtil.getString(actionRequest, "title");
        String description = ParamUtil.getString(actionRequest, "description");
        String urlTitle = ParamUtil.getString(actionRequest, "urlTitle");

        ServiceContext serviceContext = ServiceContextFactory.getInstance(Bibliography.class.getName(), actionRequest);

        Bibliography bibliography = null;

        // Only available in imported database, see
        // ch.inofix.referencemanager.service.util.ReferenceImporter
        String comments = null;
        String preamble = null;
        String strings = null;

        if (bibliographyId <= 0) {
            bibliography = _bibliographyService.addBibliography(title, description, urlTitle, comments, preamble,
                    strings, serviceContext);
        } else {
            bibliography = _bibliographyService.updateBibliography(bibliographyId, title, description, urlTitle,
                    comments, preamble, strings, serviceContext);
        }

        return bibliography;
    }
    
    @org.osgi.service.component.annotations.Reference
    private Http _http;

    @org.osgi.service.component.annotations.Reference
    private Portal _portal;
    
    private BibliographyService _bibliographyService;
    private ReferenceService _referenceService;

    private static Log _log = LogFactoryUtil.getLog(EditBibliographyMVCActionCommand.class.getName()); 

}
