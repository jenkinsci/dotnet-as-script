package com.dotnetscript.main;
import com.dotnetscript.tools.FileTools;
import com.dotnetscript.tools.JsonTools;
import com.dotnetscript.tools.StringTools;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.plugins.envinject.EnvInjectBuilder;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.PrintStream;
import java.io.Serializable;
import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link DotNetCoreRunner} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #targetCode})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class DotNetCoreRunner extends Builder implements Serializable {

    private BuildListener currentListener;
    private final String targetCode;
    private final String additionalPackages;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public DotNetCoreRunner(String targetCode, String additionalPackages) {
        this.targetCode = targetCode;
        this.additionalPackages = additionalPackages;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     * @return 
     */
    public String getTargetCode() {
        return this.targetCode;
    }
    
    public String getAdditionalPackages() {
        if (this.additionalPackages == null) {
            return "";
        }
        return this.additionalPackages;
    }
    
    private void println(String message) {
        this.currentListener.getLogger().println(message);        
    }
            

    /**
     *
     * @param build
     * @param launcher
     * @param listener
     * @return
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     * @throws java.io.UnsupportedEncodingException
     */
    @Override
    public boolean perform(@Nonnull AbstractBuild<?, ?> build, @Nonnull Launcher launcher, @Nonnull BuildListener listener) throws IOException, InterruptedException {
        PrintStream targetLogger = listener.getLogger();
        
        DotNetScriptPluginFacade dotNetScriptFacade = new DotNetScriptPluginFacade(targetLogger, build, launcher, listener);
        
        try {
            dotNetScriptFacade.RunAll(this.getTargetCode(), this.getAdditionalPackages());  
        } catch (Exception error) {
            error.printStackTrace(targetLogger);
            build.setResult(Result.FAILURE);            
        }
        
        return true;
    }

    public String getResourceFileContent(String fileName) {
	ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource(fileName).getFile());
        return FileTools.getFileContent(file);
    }    
    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    /**
     * Descriptor for {@link DotNetCoreRunner}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        
        private static final String pluginName = "DotNet as Script";
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return DescriptorImpl.pluginName;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }
    }
}

