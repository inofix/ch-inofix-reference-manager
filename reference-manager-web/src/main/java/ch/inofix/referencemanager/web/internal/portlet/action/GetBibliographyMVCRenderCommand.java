package ch.inofix.referencemanager.web.internal.portlet.action;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.servlet.SessionErrors;

import ch.inofix.referencemanager.exception.NoSuchBibliographyException;
import ch.inofix.referencemanager.model.Bibliography;
import ch.inofix.referencemanager.web.internal.constants.BibliographyWebKeys;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 21:06
 * @modified 2017-11-12 21:06
 * @version 1.0.0
 *
 */
public abstract class GetBibliographyMVCRenderCommand implements MVCRenderCommand {

    @Override
    public String render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException {

        _log.info("render()");

        try {
            Bibliography bibliography = ActionUtil.getBibliography(renderRequest);

            renderRequest.setAttribute(BibliographyWebKeys.BIBLIOGRAPHY, bibliography);
        } catch (Exception e) {
            if (e instanceof NoSuchBibliographyException || e instanceof PrincipalException) {

                SessionErrors.add(renderRequest, e.getClass());

                return "/error.jsp";

            } else {
                throw new PortletException(e);
            }
        }

        return getPath();
    }

    protected abstract String getPath();

    private static Log _log = LogFactoryUtil.getLog(GetBibliographyMVCRenderCommand.class.getName());

}
