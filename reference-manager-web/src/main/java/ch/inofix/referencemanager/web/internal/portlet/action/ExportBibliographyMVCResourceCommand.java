package ch.inofix.referencemanager.web.internal.portlet.action;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import ch.inofix.referencemanager.constants.PortletKeys;

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
        // TODO Auto-generated method stub
        
    }

}
