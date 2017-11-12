package ch.inofix.referencemanager.web.internal.portlet.action;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.servlet.SessionErrors;

import ch.inofix.referencemanager.exception.NoSuchReferenceException;
import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.web.internal.constants.ReferenceWebKeys;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 19:07
 * @modified 2017-11-12 19:07
 * @version 1.0.0
 *
 */
public abstract class GetReferenceMVCRenderCommand implements MVCRenderCommand {

    @Override
    public String render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException {

        _log.info("render()");

        try {
            Reference taskRecord = ActionUtil.getReference(renderRequest);

            renderRequest.setAttribute(ReferenceWebKeys.REFERENCE, taskRecord);
        } catch (Exception e) {
            if (e instanceof NoSuchReferenceException || e instanceof PrincipalException) {

                SessionErrors.add(renderRequest, e.getClass());

                return "/error.jsp";

            } else {
                throw new PortletException(e);
            }
        }

        return getPath();
    }

    protected abstract String getPath();

    private static Log _log = LogFactoryUtil.getLog(GetReferenceMVCRenderCommand.class.getName());

}
