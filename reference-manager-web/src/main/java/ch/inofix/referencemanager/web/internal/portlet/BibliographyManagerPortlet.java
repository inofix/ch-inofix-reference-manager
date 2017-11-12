package ch.inofix.referencemanager.web.internal.portlet;

import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import ch.inofix.referencemanager.constants.PortletKeys;

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
    
}
