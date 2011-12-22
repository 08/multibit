package org.multibit.protocolhandler.builders;

import org.multibit.protocolhandler.GenericApplication;
import org.multibit.protocolhandler.GenericApplicationSpecification;
import org.multibit.protocolhandler.handlers.DefaultOpenURIHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>[Pattern] to provide the following to {@link Object}:</p>
 * <ul>
 * <li></li>
 * </ul>
 * <p>Example:</p>
 * <pre>
 * </pre>
 *
 * @since 1.0.0
 *         
 */
public class MacApplicationBuilder {

    private static final Logger log = LoggerFactory.getLogger(MacApplicationBuilder.class);

    /**
     * <p>Builds either a {@link GenericApplication} if the platform does not provide EAWT support, or a {@link MacApplication} if it does.</p>
     * <p>Requires reflective code to avoid introducing Apple-specific code. The direct equivalent operation is as follows:</p>
     * <pre>
     *
     *  Application application = Application.getApplication();
     *  application.setAboutHandler(new AboutHandler() {
     *      void handleAbout(AppEvent.AboutEvent event) {
     *          // Fire the internal about handler event
     *      }
     *  });
     *  
     *  application.setOpenURIHandler(new OpenURIHandler() {
     *      void openURI(AppEvent.OpenURIEvent event) {
     *          // Fire the internal open file event
     *      }
     *  });
     *  
     *  application.setOpenFileHandler(new OpenFilesHandler() {
     *      void openFiles(AppEvent.OpenFilesEvent event) {
     *          // Fire the internal open file event
     *      }
     *  });
     * </pre>
     * @return A {@link GenericApplication}
     * @param specification The specification containing the listeners
     */
    public GenericApplication build(GenericApplicationSpecification specification) {

        Object nativeApplication;
        Class nativeApplicationListenerClass;
        Class nativeOpenURIHandlerClass;

        try {
            // Attempt to locate the Apple Java JDK
            final File file = new File("/System/Library/Java");
            if (file.exists()) {
                // Create a suitable class loader
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                Class systemClassLoaderClass = systemClassLoader.getClass();
                // Determine if class loading by URL is supported
                if (URLClassLoader.class.isAssignableFrom(systemClassLoaderClass)) {
                    // Get the addURL method from the class loader
                    Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                    addUrl.setAccessible(true);
                    // Load the Apple JDK classes
                    addUrl.invoke(systemClassLoader, file.toURI().toURL());
                }
            }

            // Must have successfully loaded the Apple JDK or it's not there

            // Try to load the EAWT support classes
            Class nativeApplicationClass = Class.forName("com.apple.eawt.Application");
            // Instantiate through getApplication()
            nativeApplication = nativeApplicationClass.getMethod("getApplication", new Class[0]).invoke(null);

            //nativeApplicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");

            nativeOpenURIHandlerClass = Class.forName("com.apple.eawt.OpenURIHandler");

        } catch (ClassNotFoundException e) {
            return new UnknownApplicationBuilder().build(specification);
        } catch (IllegalAccessException e) {
            return new UnknownApplicationBuilder().build(specification);
        } catch (NoSuchMethodException e) {
            return new UnknownApplicationBuilder().build(specification);
        } catch (InvocationTargetException e) {
            return new UnknownApplicationBuilder().build(specification);
        } catch (MalformedURLException e) {
            return new UnknownApplicationBuilder().build(specification);
        }
        
        // Must have successfully configured the EAWT library to be here

        log.debug("Found EAWT libraries so building native bridge");
        MacApplication macApplication = new MacApplication();
        macApplication.setApplication(nativeApplication);
        macApplication.setOpenURIHandlerClass(nativeOpenURIHandlerClass);

        // Create and configure the default handlers that simply broadcast
        // the events back to their listeners

        // Open URI handler
        log.debug("Adding the DefaultOpenURIHandler");
        DefaultOpenURIHandler openURIHandler = new DefaultOpenURIHandler();
        openURIHandler.addListeners(specification.getOpenURIEventListeners());
        macApplication.addOpenURIHandler(openURIHandler);

        // TODO Add the rest later

        return macApplication;

    }


}

