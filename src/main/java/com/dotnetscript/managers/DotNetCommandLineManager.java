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
package com.dotnetscript.managers;

import com.dotnetscript.exceptions.DotNetCommandLineException;
import com.dotnetscript.general.NodeFile;
import com.dotnetscript.tools.FileTools;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Ariel.Lenis
 */
public class DotNetCommandLineManager extends ManagerBase {
    String projectName;
    Launcher launcher;
    EnvVars env;
    NodeFile targetWorkspace;
    TaskListener listener;
    
    /**
     * 
     * @param logger
     * @param launcher
     * @param env
     * @param listener
     * @param targetWorkspace
     * @param projectName
     * @throws IOException
     * @throws InterruptedException 
     */
    public DotNetCommandLineManager(PrintStream logger, Launcher launcher, EnvVars env, TaskListener listener, NodeFile targetWorkspace, String projectName) throws IOException, InterruptedException {
        super(logger);

        this.launcher = launcher;
        this.projectName = projectName;
        this.env = env;
        this.targetWorkspace = targetWorkspace;
        this.listener = listener;
    }
    
    private String getDotNetExecutable() {
        return "dotnet";
    }
    
    /**
     * Creates the DOTNET project
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void createProject() throws IOException, InterruptedException, DotNetCommandLineException
    {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "new", "console", "-n", this.projectName);
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (projectFolder.exists())
        {
            if (projectFolder.isDirectory())
                FileTools.deleteDirectory(projectFolder);
            else
                projectFolder.delete();                
        }
        
        int result = this.executeArgs(argsCreate, this.targetWorkspace);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at create step.", result);
        }
    }
    
    /**
     * Restores the DOTNET packages
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void restoreDependencies() throws IOException, InterruptedException, DotNetCommandLineException
    {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "restore");
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists()) {
            throw new DotNetCommandLineException("The project folder does not exists.");
        } 
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at restore dependencies step.", result);
        }
    }
    
    /**
     * Gets the current project folder
     * @return 
     */
    public NodeFile getProjectFolder() {
        return new NodeFile(this.targetWorkspace, this.projectName);
    }

    /**
     * Adds a package to the project with the last version
     * @param packageName
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void addPackage(String packageName) throws IOException, InterruptedException, DotNetCommandLineException {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "add", "package", packageName);
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists()) {
            throw new DotNetCommandLineException("The project folder does not exists.");
        } 
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at add package step.", result);
        }
    }
    
    /**
     * Adds a package to the project with the specific version
     * @param packageName
     * @param version
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void addPackage(String packageName, String version) throws IOException, InterruptedException, DotNetCommandLineException {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "add", "package", packageName, "-v", version);
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists()) {
            throw new DotNetCommandLineException("The project folder does not exists.");
        } 
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at add package step.", result);
        }
    }
    
    /**
     * Builds the current DOTNET project
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void build() throws IOException, InterruptedException, DotNetCommandLineException {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "build");
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists()) {
            throw new DotNetCommandLineException("The project folder does not exists.");
        } 
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at build step.", result);
        }
    }
    
    /**
     * Runs the current DOTNET project
     * @throws IOException
     * @throws InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public void run() throws IOException, InterruptedException, DotNetCommandLineException
    {
        List<String> argsCreate = Arrays.asList(this.getDotNetExecutable(), "run");
        
        NodeFile projectFolder = this.getProjectFolder();
        
        if (!projectFolder.exists()) {
            throw new DotNetCommandLineException("The project folder does not exists.");
        }            
        
        int result = this.executeArgs(argsCreate, projectFolder);        
        if (result != 0) {
            throw new DotNetCommandLineException("Error at run step.", result);
        }
    }
    
    /**
     * Execute the commands
     * @param args
     * @param targetDirectory
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    private int executeArgs(List<String> args, NodeFile targetDirectory) throws IOException, InterruptedException
    {
        return this.launcher
                .launch()
                .cmds(args)
                .envs(this.env)
                .stdout(this.listener)
                .pwd(targetDirectory.getFilePath())
                .join();        
    }
    
}
