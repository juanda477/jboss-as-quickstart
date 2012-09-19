package org.jboss.as.quickstarts.ejb.multi.server.app;

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

@Model
public class JsfController {
    private static final Logger LOOGER = Logger.getLogger(JsfController.class);
    private EjbInvocation invocation;
    
    @EJB
    MainApp mainApp;

    /**
     * Injection is not possible for foreign application in a different server.
     * The reference will be set during PostConstruct
     */
    AppOne oneApp;
    /**
     * Injection is not possible for foreign application in a different server.
     * The reference will be set during PostConstruct
     */
    AppTwo twoApp;
    
    /**
     * Initialize an store the context for the EJB invocation.
     */
    @PostConstruct
    public void initController() {
        try {
            final Hashtable<String, String> p = new Hashtable<String, String>();
            p.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            // create the InitialContext
            final InitialContext iCtx = new InitialContext(p);
            oneApp = (AppOne)iCtx.lookup("ejb:appone/ejb//AppOneBean!org.jboss.as.quickstarts.ejb.multi.server.app.AppOne");
            twoApp = (AppTwo)iCtx.lookup("ejb:apptwo/ejb//AppTwoBean!org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo");
        } catch (NamingException e) {
            throw new RuntimeException("Could not initialize context", e);
        }
        initForm();
    }

    public void initForm() {
        this.invocation = new EjbInvocation();
    }
    
    @Produces
    @Named
    public EjbInvocation getInvocation() {
        return this.invocation;
    }
    
    public void callEJBMainLocal() {
        LOOGER.info("Try to invoke the local MainApp to log the given text and get the invocation results. Proxy="+mainApp);
        this.invocation.setResult(mainApp.invokeAll(this.invocation.getText()));
    }
    public void callEJBAppOneRemote() {
        LOOGER.info("Try to invoke the remote AppOne to log the given text and get the invocation results. Proxy="+oneApp);
        this.invocation.setResult(oneApp.invoke(this.invocation.getText()));
    }
    public void callEJBAppTwoRemote() {
        LOOGER.info("Try to invoke the remote AppTwo to log the given text and get the invocation results. Proxy="+twoApp);
        this.invocation.setResult(twoApp.invoke(this.invocation.getText()));
    }
}
