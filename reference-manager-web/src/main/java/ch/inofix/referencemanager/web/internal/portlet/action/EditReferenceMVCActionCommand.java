package ch.inofix.referencemanager.web.internal.portlet.action;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;

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
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import ch.inofix.referencemanager.constants.PortletKeys;
import ch.inofix.referencemanager.exception.NoSuchReferenceException;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.ReferenceService;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 19:13
 * @modified 2017-11-12 19:13
 * @version 1.0.0
 *
 */
@Component(
    property = {
        "javax.portlet.name=" + PortletKeys.REFERENCE_MANAGER,
        "mvc.command.name=editReference"
    },
    service = MVCActionCommand.class
)
public class EditReferenceMVCActionCommand extends BaseMVCActionCommand {
    
    protected void deleteGroupReferences(ActionRequest actionRequest) throws Exception {

        _log.info("deleteGroupReferences()");

        ServiceContext serviceContext = ServiceContextFactory.getInstance(Reference.class.getName(), actionRequest);

        _referenceService.deleteGroupReferences(serviceContext.getScopeGroupId());

    }

    protected void deleteReferences(ActionRequest actionRequest) throws Exception {

        long referenceId = ParamUtil.getLong(actionRequest, "referenceId");

        long[] referenceIds = ParamUtil.getLongValues(actionRequest, "deleteReferenceIds");

        if (referenceId > 0) {
            referenceIds = new long[] { referenceId };
        }

        for (long id : referenceIds) {
            _referenceService.deleteReference(id);
        }

    }

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

        _log.info("doProcessAction");
        _log.info("cmd = " + cmd);

        try {

            if (cmd.equals(Constants.DELETE)) {
                deleteReferences(actionRequest);
            } else if (cmd.equals("deleteGroupReferences")) {
                deleteGroupReferences(actionRequest);
            }

            String redirect = ParamUtil.getString(actionRequest, "redirect");

            sendRedirect(actionRequest, actionResponse, redirect);

        } catch (NoSuchReferenceException | PrincipalException e) {

            SessionErrors.add(actionRequest, e.getClass());

            actionResponse.setRenderParameter("mvcPath", "/error.jsp");

            // TODO: Define set of exceptions reported back to user. For an
            // example, see EditCategoryMVCActionCommand.java.

        } catch (Exception e) {

            SessionErrors.add(actionRequest, e.getClass());
        }
    }
    
    protected String getSaveAndContinueRedirect(
            ActionRequest actionRequest, Reference reference, Layout layout, String redirect)
        throws Exception {

        PortletConfig portletConfig = (PortletConfig)actionRequest.getAttribute(
            JavaConstants.JAVAX_PORTLET_CONFIG);
        
        LiferayPortletURL portletURL = PortletURLFactoryUtil.create(actionRequest, portletConfig.getPortletName(), layout, PortletRequest.RENDER_PHASE);

        portletURL.setParameter("mvcRenderCommandName", "editReference");

        portletURL.setParameter(Constants.CMD, Constants.UPDATE, false);
        portletURL.setParameter("redirect", redirect, false);
        portletURL.setParameter(
            "groupId", String.valueOf(reference.getGroupId()), false);
        portletURL.setParameter(
            "referenceId", String.valueOf(reference.getReferenceId()), false);
        portletURL.setWindowState(actionRequest.getWindowState());

        return portletURL.toString();
    }

    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setReferenceService(ReferenceService referenceService) {
        this._referenceService = referenceService;
    }

    
    @org.osgi.service.component.annotations.Reference
    private Http _http;

    @org.osgi.service.component.annotations.Reference
    private Portal _portal;
    
    private ReferenceService _referenceService;

    private static Log _log = LogFactoryUtil.getLog(EditReferenceMVCActionCommand.class.getName()); 

}
