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
package com.dotnetscript.main;

import com.dotnetscript.general.NodeFile;
import com.dotnetscript.general.ProjectConstants;
import com.dotnetscript.managers.DotNetCommandLineManager;
import com.dotnetscript.managers.DotNetPackagesManager;
import com.dotnetscript.managers.DotNetProjectManager;
import com.dotnetscript.tools.FileTools;
import com.dotnetscript.tools.StringTools;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import javax.annotation.Nonnull;
import net.sf.json.JSONObject;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.plugins.envinject.EnvInjectBuilder;

/**
 *
 * @author Ariel.Lenis
 */
public class DotNetScriptPluginFacade {
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
    public DotNetScriptPluginFacade(PrintStream logger, @Nonnull AbstractBuild<?, ?> build, @Nonnull Launcher launcher, @Nonnull BuildListener listener) throws IOException, InterruptedException {
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
     */
    public String getResourceFileContent(String fileName) {
	ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource(fileName).getFile());
        return FileTools.getFileContent(file);
    } 
    
    /**
     * Gets the unique folder name based in the target dotnet code
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
    public void runAll(String targetCode, String targetPackagesJson) throws IOException, InterruptedException, NoSuchAlgorithmException, EnvInjectException {
        NodeFile dotScriptWorkspace = new NodeFile(this.workspaceFolder, ProjectConstants.CACHE_FOLDER_NAME);
        
        DotNetPackagesManager dotNetPackages = new DotNetPackagesManager(this.logger, targetPackagesJson);                
        
        String uniqueFolderName = this.getUniqueFolderName(targetCode);        
        NodeFile uniqueFolder = new NodeFile(dotScriptWorkspace, uniqueFolderName);

        EnvVars env = this.build.getEnvironment(this.listener);
        DotNetCommandLineManager dotNetCommandLine = new DotNetCommandLineManager(this.logger, this.launcher, env, this.listener, uniqueFolder, ProjectConstants.PROJECT_FOLDER_NAME);
        
        NodeFile currentProjectFolder = new NodeFile(uniqueFolder, ProjectConstants.PROJECT_FOLDER_NAME);        
        DotNetProjectManager projectManager = new DotNetProjectManager(this.logger, build.getNumber(), dotNetCommandLine, dotNetPackages, currentProjectFolder);
        
        projectManager.addFileForCreation("JenkinsExecutor.cs", this.getResourceFileContent("com/dotnetscript/resources/JenkinsExecutor.cs"));
        projectManager.addFileForCreation("JenkinsManager.cs", this.getResourceFileContent("com/dotnetscript/resources/JenkinsManager.cs"));
        projectManager.addFileForCreation("Program.cs", this.getResourceFileContent("com/dotnetscript/resources/Program.cs"));
        projectManager.addFileForCreation("TargetCode.cs", targetCode);
   
        if (!uniqueFolder.exists()) {
            uniqueFolder.mkdir();
        }
        
        projectManager.createProject();
        projectManager.buildProject();
        projectManager.runProject();
        
        NodeFile resultsFile = new NodeFile(currentProjectFolder, "jenkinsExecution.json");
        
        this.processResultFile(resultsFile, build, launcher, env, listener);                
    }
    
    /**
     * Process the result file generated by the dotnet application
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
        
        String environmentPath = null;
        String environmentTemplate = "%s=%s\r\n";
        String environmentVariables = "";        
        
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

                environmentVariables += String.format(environmentTemplate, key, scapedValue);                    
            }

            EnvInjectBuilder envInjectBuilder = new EnvInjectBuilder(environmentPath, environmentVariables);
            envInjectBuilder.perform(build, launcher, listener); 
        }
        else
        {            
           this.listener.getLogger().println("##### SavedEnvironment not found");    
        }
    }
}
