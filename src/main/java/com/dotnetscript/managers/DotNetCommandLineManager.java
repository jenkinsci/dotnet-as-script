/*
 * The MIT License
 *
 * Copyright 2017 NewType.
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
package com.dotnetscript.managers;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author NewType
 */
public class DotNetCommandLineManager extends ManagerBase {
    String projectName;
    Launcher launcher;
    EnvVars env;
    File targetWorkspace;
    TaskListener listener;
    
    /**
     * 
     * @param launcher
     * @param env
     * @param listener
     * @param targetWorkspace
     * @param projectName
     * @throws IOException
     * @throws InterruptedException 
     */
    public DotNetCommandLineManager(PrintStream logger, Launcher launcher, EnvVars env, TaskListener listener, File targetWorkspace, String projectName) throws IOException, InterruptedException {
        super(logger);

        this.launcher = launcher;
        this.projectName = projectName;
        this.env = env;
        this.targetWorkspace = targetWorkspace;
        this.listener = listener;
    }
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public boolean createProject() throws IOException, InterruptedException
    {
        List<String> argsCreate = Arrays.asList("dotnet", "new", "console", "-n", this.projectName);
        
        File projectFolder = this.getProjectFolder();
        
        if (projectFolder.exists())
        {
            if (projectFolder.isFile())
                projectFolder.delete();
            else
                FileUtils.deleteDirectory(projectFolder);
        }
        
        int result = this.executeArgs(argsCreate, this.targetWorkspace);        
        return result == 0;
    }
    
    public boolean restoreDependencies() throws IOException, InterruptedException
    {
        List<String> argsCreate = Arrays.asList("dotnet", "restore");
        
        File projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists())
            return false;
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        return result == 0;
    }
    
    public File getProjectFolder() {
        return new File(this.targetWorkspace, this.projectName);
    }

    public boolean addPackage(String packageName) throws IOException, InterruptedException {
        List<String> argsCreate = Arrays.asList("dotnet", "add", "package", packageName);
        
        File projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists())
            return false;
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        return result == 0;
    }
    
    public boolean addPackage(String packageName, String version) throws IOException, InterruptedException {
        List<String> argsCreate = Arrays.asList("dotnet", "add", "package", packageName, "-v", version);
        
        File projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists())
            return false;
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        return result == 0;
    }
    
    
    public boolean build() throws IOException, InterruptedException {
        List<String> argsCreate = Arrays.asList("dotnet", "build");
        
        File projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists())
            return false;
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        return result == 0;
    }
    
    public boolean run() throws IOException, InterruptedException
    {
        List<String> argsCreate = Arrays.asList("dotnet", "run");
        
        File projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists())
            return false;
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        return result == 0;
    }
    
    private int executeArgs(List<String> args, File targetDirectory) throws IOException, InterruptedException
    {
        return this.launcher
                .launch()
                .cmds(args)
                .envs(this.env)
                .stdout(this.listener)
                .pwd(new FilePath(targetDirectory))
                .join();        
    }
    
}
