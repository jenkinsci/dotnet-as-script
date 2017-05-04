/*
 * The MIT License
 *
 * Copyright 2017 Ariel.Lenis.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.dotnetasscript.main;

import hudson.plugins.dotnetasscript.exceptions.DotNetCommandLineException;
import hudson.plugins.dotnetasscript.exceptions.DotNetPluginException;
import hudson.plugins.dotnetasscript.exceptions.DotNetProjectManagerException;
import hudson.plugins.dotnetasscript.general.NodeFile;
import hudson.plugins.dotnetasscript.general.ProjectConstants;
import hudson.plugins.dotnetasscript.managers.DotNetCommandLineManager;
import hudson.plugins.dotnetasscript.managers.DotNetPackagesManager;
import hudson.plugins.dotnetasscript.managers.DotNetProjectManager;
import hudson.plugins.dotnetasscript.tools.FileTools;
import hudson.plugins.dotnetasscript.tools.StringTools;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import javax.annotation.Nonnull;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.plugins.envinject.EnvInjectBuilder;

/**
 *
 * @author Ariel.Lenis
 */
public class DotNetAsScriptPluginFacade {
    private final AbstractBuild<?, ?> build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final NodeFile workspaceFolder;
    private final PrintStream logger;
    
    /**
     * The facade to encapsulate the engine
     * @param logger
     * @param build
     * @param launcher
     * @param listener
     * @throws IOException
     * @throws InterruptedException 
     */
    public DotNetAsScriptPluginFacade(PrintStream logger, @Nonnull AbstractBuild<?, ?> build, @Nonnull Launcher launcher, @Nonnull BuildListener listener) throws IOException, InterruptedException {
        this.logger = logger;
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.workspaceFolder = new NodeFile(build.getWorkspace());
    }
    
    /**
     * Gets the target resource file as string
     * @param fileName
     * @return 
     * @throws java.io.IOException 
     */
    public String getResourceFileContent(String fileName) throws IOException {
	ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        String content = IOUtils.toString(inputStream, ProjectConstants.ENCODING);
        return content;
    } 
    
    /**
     * Gets the unique folder name based in the target DOTNET code
     * @param targetCode
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    private String getUniqueFolderName(String targetCode) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return StringTools.getStringIdentificator(targetCode);
    }
    
    /**
     * Runs the plugin configuration
     * @param targetCode
     * @param targetPackagesJson
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * @throws EnvInjectException 
     */
    public void runAll(String targetCode, String targetPackagesJson) throws IOException, InterruptedException, NoSuchAlgorithmException, EnvInjectException, UnsupportedEncodingException, DotNetPluginException, DotNetCommandLineException {
        NodeFile dotScriptWorkspace = new NodeFile(this.workspaceFolder, ProjectConstants.CACHE_FOLDER_NAME);
        
        DotNetPackagesManager dotNetPackages = new DotNetPackagesManager(this.logger, targetPackagesJson);                
        
        String uniqueFolderName = this.getUniqueFolderName(targetCode);        
        NodeFile uniqueFolder = new NodeFile(dotScriptWorkspace, uniqueFolderName);

        EnvVars env = this.build.getEnvironment(this.listener);
        DotNetCommandLineManager dotNetCommandLine = new DotNetCommandLineManager(this.logger, this.launcher, env, this.listener, uniqueFolder, ProjectConstants.PROJECT_FOLDER_NAME);
        
        NodeFile currentProjectFolder = new NodeFile(uniqueFolder, ProjectConstants.PROJECT_FOLDER_NAME);        
        DotNetProjectManager projectManager = null;
        
        try {
            projectManager = new DotNetProjectManager(this.logger, build.getNumber(), dotNetCommandLine, dotNetPackages, currentProjectFolder);
        } catch (DotNetProjectManagerException error) {
            throw new DotNetPluginException("Error initalizing the dotnet project manager class", error);
        }        
        
        projectManager.addFileForCreation("JenkinsExecutor.cs", this.getResourceFileContent("hudson/plugins/dotnetasscript/resources/JenkinsExecutor.cs"));
        projectManager.addFileForCreation("JenkinsManager.cs", this.getResourceFileContent("hudson/plugins/dotnetasscript/resources/JenkinsManager.cs"));
        projectManager.addFileForCreation("Program.cs", this.getResourceFileContent("hudson/plugins/dotnetasscript/resources/Program.cs"));
        projectManager.addFileForCreation("TargetCode.cs", targetCode);
   
        if (!uniqueFolder.exists()) {
            uniqueFolder.mkdir();
        }
        
        try {
            projectManager.createProject();
            projectManager.buildProject();
            projectManager.runProject();
        } catch (DotNetCommandLineException error) {
            throw new DotNetPluginException("Error running the project.", error);
        }
        
        NodeFile resultsFile = new NodeFile(currentProjectFolder, "jenkinsExecution.json");
        
        this.processResultFile(resultsFile, build, launcher, env, listener);                
    }
    
    /**
     * Process the result file generated by the DOTNET application
     * @param resultsFile
     * @param build
     * @param launcher
     * @param env
     * @param listener
     * @throws IOException
     * @throws EnvInjectException
     * @throws InterruptedException 
     */
    private void processResultFile(NodeFile resultsFile, AbstractBuild<?,?> build, Launcher launcher, EnvVars env, BuildListener listener) throws IOException, EnvInjectException, InterruptedException { 
        
        //String theJson = FileUtils.readFileToString(resultsFile, "utf-8");
        String theJson = FileTools.getFileContent(resultsFile);
        JSONObject jsonObject = JSONObject.fromObject(theJson);
        
        String environmentTemplate = "%s=%s%n";
        String environmentVariables;        
        StringBuilder environmentVariablesBuffer = new StringBuilder();
        
        if (jsonObject.containsKey("SavedEnvironment"))
        {
            JSONObject environment = jsonObject.getJSONObject("SavedEnvironment");
               
            Iterator iterator = environment.keys();
            while (iterator.hasNext())
            {               
                String key = iterator.next().toString();
                String value = environment.getString(key);
                
                if (value == null) {
                    value = "null";
                }
                    
                String scapedValue = value.replace("\\", "\\\\");

                environmentVariablesBuffer.append(String.format(environmentTemplate, key, scapedValue));
            }

            environmentVariables = environmentVariablesBuffer.toString();
            EnvInjectBuilder envInjectBuilder = new EnvInjectBuilder(null, environmentVariables);
            envInjectBuilder.perform(build, launcher, listener); 
        }
        else
        {            
           this.listener.getLogger().println("##### SavedEnvironment not found");    
        }
    }
}
