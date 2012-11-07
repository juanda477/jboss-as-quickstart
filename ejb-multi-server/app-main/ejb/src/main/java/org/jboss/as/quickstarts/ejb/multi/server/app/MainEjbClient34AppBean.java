package org.jboss.as.quickstarts.ejb.multi.server.app;

import java.security.Principal;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * <p>
 * An example how to use the new features introduced with EJBCLIENT-34 in AS7.2.0.
 * </p>
 * <p>
 * The sub applications, deployed in different servers are called direct by using the ejb-client scoped context properties.
 * </p>
 * 
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Stateless
public class MainEjbClient34AppBean implements MainApp {
  private static final Logger LOGGER = Logger.getLogger(MainEjbClient34AppBean.class);
  @Resource
  SessionContext context;

  @Override
  public String getJBossNodeName() {
    return System.getProperty("jboss.node.name");
  }

  @Override
  public String invokeAll(String text) {
    Principal caller = context.getCallerPrincipal();
    LOGGER.info("[" + caller.getName() + "] " + text);
    final StringBuilder result = new StringBuilder("MainEjbClient34App[" + caller.getName() + "]@" + getJBossNodeName());

    // Call AppOne with the direct ejb: naming
    try {
      result.append("  >  [ " + invokeAppOne(text));
    } catch (Exception e) {
      LOGGER.error("Could not invoke AppOne", e);
    }

    result.append(" > " + invokeAppTwo(text));

    result.append(" ]");

    return result.toString();
  }

  private String invokeAppOne(String text) {
    Context iCtx = null;

    final Properties ejbClientContextProps = new Properties();
    ejbClientContextProps.put("endpoint.name", "appMain->appOne_endpoint");
    // Property to enable scoped EJB client context which will be tied to the JNDI context
    ejbClientContextProps.put("org.jboss.ejb.client.scoped.context", true);
    // Property which will handle the ejb: namespace during JNDI lookup
    ejbClientContextProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

    final String connectionName = "appOneConnection";
    ejbClientContextProps.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
    // add a property which lists the connections that we are configuring. In
    // this example, we are just configuring a single connection named "foo-bar-connection"
    ejbClientContextProps.put("remote.connections", connectionName);
    // add the properties to connect the app-one host
    ejbClientContextProps.put("remote.connection." + connectionName + ".host", "localhost");
    ejbClientContextProps.put("remote.connection." + connectionName + ".port", "4547");
    ejbClientContextProps.put("remote.connection." + connectionName + ".username", "quickuser1");
    ejbClientContextProps.put("remote.connection." + connectionName + ".password", "quick123+");
    // since we are connecting to a dummy server, we use anonymous user
    // ejbClientContextProps.put("remote.connection." + connectionName +
    // ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

    ejbClientContextProps.put("remote.clusters", "ejb");
    ejbClientContextProps.put("remote.cluster.ejb.username", "quickuser2");
    ejbClientContextProps.put("remote.cluster.ejb.password", "quick+123");

    try {
      iCtx = new InitialContext(ejbClientContextProps);
      final AppOne bean = (AppOne) iCtx.lookup("ejb:appone/ejb//AppOneBean!" + AppOne.class.getName());

      StringBuffer result = new StringBuffer("{");
      for (int i = 0; i < 8; i++) {
        // invoke on the bean
        final String appOneResult = bean.invoke(text);
        if (i > 0) {
          result.append(", ");
        }
        result.append(appOneResult);
      }
      result.append("}");

      LOGGER.info("AppOne return : " + result);
      return result.toString();

    } catch (NamingException e) {
      LOGGER.error("Could not invoke appOne", e);
      return null;
    } finally {
      saveContextClose(iCtx);
    }

  }

  private void saveContextClose(Context iCtx) {
    if (iCtx != null) {
      try {
        LOGGER.info("close Context " + iCtx.getEnvironment().get("endpoint.name"));
        iCtx.close();

      } catch (NamingException e) {
        LOGGER.error("InitialContext can not be closed", e);
      }
    }
  }

  private String invokeAppTwo(String text) {
    AppTwo beanA = null;
    AppTwo beanB = null;

    final Properties ejbClientContextProps = new Properties();
    ejbClientContextProps.put("endpoint.name", "appMain->appTwoA_endpoint");
    // Property to enable scoped EJB client context which will be tied to the JNDI context
    ejbClientContextProps.put("org.jboss.ejb.client.scoped.context", true);
    // Property which will handle the ejb: namespace during JNDI lookup
    ejbClientContextProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

    final String connectionName = "appTwoConnection";
    ejbClientContextProps.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
    // add the properties to connect the app-one host
    ejbClientContextProps.put("remote.connections", connectionName);
    ejbClientContextProps.put("remote.connection." + connectionName + ".host", "localhost");
    ejbClientContextProps.put("remote.connection." + connectionName + ".port", "4647");
    ejbClientContextProps.put("remote.connection." + connectionName + ".username", "quickuser1");
    ejbClientContextProps.put("remote.connection." + connectionName + ".password", "quick123+");
    // since we are connecting to a dummy server, we use anonymous user
    // ejbClientContextProps.put("remote.connection." + connectionName +
    // ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

    try {
      Context iCtxA = new InitialContext(ejbClientContextProps);
      beanA = (AppTwo) iCtxA.lookup("ejb:apptwo/ejb//AppTwoBean!" + AppTwo.class.getName());
      iCtxA.close();
    } catch (NamingException e) {
      LOGGER.error("Could not create InitialContext('appTwoA')");
    }

    ejbClientContextProps.put("endpoint.name", "appMain->appTwoB_endpoint");
    ejbClientContextProps.put("remote.connection." + connectionName + ".port", "5247");
    ejbClientContextProps.put("remote.connection." + connectionName + ".username", "quickuser2");
    ejbClientContextProps.put("remote.connection." + connectionName + ".password", "quick+123");
    try {
      Context iCtxB = new InitialContext(ejbClientContextProps);
      beanB = (AppTwo) iCtxB.lookup("ejb:apptwo/ejb//AppTwoBean!" + AppTwo.class.getName());
      iCtxB.close();
    } catch (NamingException e) {
      LOGGER.error("Could not create InitialContext('appTwoB')");
    }
    
    StringBuffer result = new StringBuffer(" appTwo loop(7 time A-B expected){");
    for (int i = 0; i < 8; i++) {
      // invoke on the bean
      String appResult = beanA.invoke(text);
      if (i > 0) {
        result.append(", ");
      }
      result.append(appResult);
      appResult = beanB.invoke(text);
      result.append(", ");
      result.append(appResult);
    }
    result.append("}");

    LOGGER.info("AppTwo (loop) return : " + result);
    return result.toString();
  }
}
