package ch.inofix.referencemanager.web.internal.portlet.action;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;

import ch.inofix.referencemanager.constants.PortletKeys;

/**
 * 
 * @author Christian Berndt
 * @created 2017-11-12 19:04
 * @modified 2017-11-12 19:04
 * @version 1.0.0
 *
 */
@Component(
    immediate = true,
    property = {
        "javax.portlet.name=" + PortletKeys.REFERENCE_MANAGER,
        "mvc.command.name=editReference"
    },
    service = MVCRenderCommand.class
)
public class EditReferenceMVCRenderCommand extends GetReferenceMVCRenderCommand {

    @Override
    protected String getPath() {

        return "/reference/edit_reference.jsp";
    }
}
