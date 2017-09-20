/**
 * Copyright 2016-present Inofix GmbH, Luzern.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.inofix.referencemanager.web.internal.portlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import aQute.bnd.annotation.metatype.Configurable;
import ch.inofix.referencemanager.constants.PortletKeys;
import ch.inofix.referencemanager.exception.NoSuchReferenceException;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.ReferenceService;
import ch.inofix.referencemanager.web.configuration.ReferenceManagerConfiguration;
import ch.inofix.referencemanager.web.internal.portlet.util.PortletUtil;

/**
 * View Controller of Inofix' reference-manager.
 * 
 * @author Christian Berndt
 * @created 2016-04-10 22:32
 * @modified 2017-09-19 22:31
 * @version 1.1.5
 */
@Component(configurationPid = "ch.inofix.referencemanager.web.configuration.ReferenceManagerConfiguration", 
    immediate = true, 
    property = {
        "com.liferay.portlet.add-default-resource=true",
        "com.liferay.portlet.css-class-wrapper=portlet-reference-manager",
        "com.liferay.portlet.display-category=category.inofix", 
        "com.liferay.portlet.header-portlet-css=/css/main.css",
        "com.liferay.portlet.instanceable=false", 
        "javax.portlet.init-param.template-path=/",
        "javax.portlet.init-param.view-template=/view.jsp", 
        "javax.portlet.name=" + PortletKeys.REFERENCE_MANAGER,
        "javax.portlet.resource-bundle=content.Language",
        "javax.portlet.security-role-ref=power-user,user" 
    }, 
    service = Portlet.class
)
public class ReferenceManagerPortlet extends MVCPortlet {

    @Override
    public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        renderRequest.setAttribute(ReferenceManagerConfiguration.class.getName(), _referenceManagerConfiguration);

        super.doView(renderRequest, renderResponse);
    }
    
    /**
     * 
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.0
     * @throws Exception
     */
    public void importBibTeXFile(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        HttpServletRequest request = PortalUtil.getHttpServletRequest(actionRequest);

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);

        File file = uploadPortletRequest.getFile("file");
        String fileName = file.getName();

        long userId = themeDisplay.getUserId();
        long groupId = themeDisplay.getScopeGroupId();
        boolean privateLayout = themeDisplay.getLayout().isPrivateLayout();

        String servletContextName = request.getSession().getServletContext().getServletContextName();

        String[] servletContextNames = new String[] { servletContextName };

        Map<String, String[]> parameterMap = new HashMap<String, String[]>(actionRequest.getParameterMap());
        parameterMap.put("servletContextNames", servletContextNames);

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

    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) {

        String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

        _log.info("processAction()");
        _log.info("cmd = " + cmd);
        
        try {
            if (cmd.equals(Constants.DELETE)) {
                
                deleteReference(actionRequest, actionResponse);

            } else if (cmd.equals("deleteGroupReferences")) {
                
                deleteGroupReferences(actionRequest, actionResponse);

            }
        } catch (Exception e) {

            // TODO: improve error handling
            _log.error(e);
            
//            throw new Exception(e);

        }
    }

    @Activate
    @Modified
    protected void activate(Map<Object, Object> properties) {
        _referenceManagerConfiguration = Configurable.createConfigurable(ReferenceManagerConfiguration.class,
                properties);
    }

    /**
     * 
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.4
     * @throws Exception
     */
    protected void deleteAllReferences(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        String tabs1 = ParamUtil.getString(actionRequest, "tabs1");

        List<Reference> references = _referenceService.deleteReferences();

        SessionMessages.add(actionRequest, REQUEST_PROCESSED,
                PortletUtil.translate("successfully-deleted-x-references", references.size()));

        actionResponse.setRenderParameter("tabs1", tabs1);
    }

    /**
     * 
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.8
     * @throws Exception
     */
    protected void deleteGroupReferences(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        String tabs1 = ParamUtil.getString(actionRequest, "tabs1");

        ServiceContext serviceContext = ServiceContextFactory.getInstance(Reference.class.getName(), actionRequest);

        List<Reference> references = _referenceService.deleteGroupReferences(serviceContext.getScopeGroupId());

        SessionMessages.add(actionRequest, REQUEST_PROCESSED,
                PortletUtil.translate("successfully-deleted-x-references", references.size()));

        actionResponse.setRenderParameter("tabs1", tabs1);
    }

    /**
     * 
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.0
     * @throws Exception
     */
    protected void deleteReference(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        
        _log.info("deleteReference()");

        long referenceId = ParamUtil.getLong(actionRequest, "referenceId");

        _referenceService.deleteReference(referenceId);

    }

    /**
     * 
     */
    @Override
    protected void doDispatch(RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        if (SessionErrors.contains(renderRequest, PrincipalException.getNestedClasses())
                || SessionErrors.contains(renderRequest, NoSuchReferenceException.class)) {
            include("/error.jsp", renderRequest, renderResponse);
        } else {
            super.doDispatch(renderRequest, renderResponse);
        }
    }



    @org.osgi.service.component.annotations.Reference
    protected void setReferenceService(ReferenceService referenceService) {
        this._referenceService = referenceService;
    }

    private ReferenceService _referenceService;

    private volatile ReferenceManagerConfiguration _referenceManagerConfiguration;

    private static final String REQUEST_PROCESSED = "request_processed";

    private static Log _log = LogFactoryUtil.getLog(ReferenceManagerPortlet.class.getName());

}
