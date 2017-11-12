package ch.inofix.referencemanager.web.internal.portlet.action;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;

import ch.inofix.referencemanager.constants.PortletKeys;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 21:06
 * @modified 2017-11-12 21:06
 * @version 1.0.0
 *
 */
@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + PortletKeys.BIBLIOGRAPHY_MANAGER,
        "mvc.command.name=editBibliography"
    },
    service = MVCRenderCommand.class
)
public class EditBibliographyMVCRenderCommand extends GetBibliographyMVCRenderCommand {

    @Override
    protected String getPath() {
        
        _log.info("getPath");

        return "/bibliography/edit_bibliography.jsp";
    }
    
    private static Log _log = LogFactoryUtil.getLog(EditBibliographyMVCActionCommand.class.getName()); 
}
