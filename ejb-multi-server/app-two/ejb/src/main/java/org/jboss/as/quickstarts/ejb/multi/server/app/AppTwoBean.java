package org.jboss.as.quickstarts.ejb.multi.server.app;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

/**
 * Simple bean with methods to get the node name of the server and log messages.
 * One method is annotated with a security role. The security-domain is declared by the JBoss specific annotation SecurityDomain.
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@SecurityDomain(value = "other")
public @Stateless class AppTwoBean implements AppTwo {
    private static final Logger LOGGER = Logger.getLogger(AppTwoBean.class);
    
    @Resource SessionContext context;
    
    @Override
    public String getJBossNodeName() {
        return System.getProperty("jboss.node.name");
    }

    @Override
    public String invoke(String text) {
        Principal caller = context.getCallerPrincipal();
        LOGGER.info("["+caller.getName()+"] "+text);
        return "app2["+caller.getName()+"]@"+getJBossNodeName();
    }
    
    @Override
    @RolesAllowed({"AppTwo","Intern"})
    public String invokeSecured(String text) {
        Principal caller = context.getCallerPrincipal();
        LOGGER.info("Secured invocation ["+caller.getName()+"] "+text);
        LOGGER.info("Is in Role AppTwo="+context.isCallerInRole("AppTwo")+" Intern="+context.isCallerInRole("Intern"));
        return "app2["+caller.getName()+"]@"+getJBossNodeName();
    }

}
