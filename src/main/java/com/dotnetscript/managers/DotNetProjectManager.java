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
import com.dotnetscript.exceptions.DotNetProjectManagerException;
import com.dotnetscript.general.FileForCreation;
import com.dotnetscript.general.ProjectConstants;
import com.dotnetscript.general.BuildInformation;
import com.dotnetscript.general.NodeFile;
import com.dotnetscript.tools.FileTools;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ariel.Lenis
 */
public class DotNetProjectManager extends ManagerBase {
    private final DotNetCommandLineManager commandLine;
    private final List<FileForCreation> filesToCreate;
    private final DotNetPackagesManager packages;
    private final NodeFile projectFolder;
    private final NodeFile buildInformationFile;
    private final String[] additionalPackages = new String[]{"Newtonsoft.Json"};  
    private final BuildInformationManager buildInformationManager;
    private final int buildNumber;
    
    /**
     * The project manager constructor
     * @param logger
     * @param buildNumber
     * @param commandLine
     * @param targetPackages
     * @param projectFolder 
     * @throws java.io.IOException 
     * @throws java.lang.InterruptedException 
     * @throws com.dotnetscript.exceptions.DotNetProjectManagerException 
     * @throws com.dotnetscript.exceptions.DotNetCommandLineException 
     */
    public DotNetProjectManager(PrintStream logger, int buildNumber, DotNetCommandLineManager commandLine, DotNetPackagesManager targetPackages, NodeFile projectFolder) throws IOException, InterruptedException, InterruptedException, DotNetCommandLineException, DotNetProjectManagerException {
        super(logger);
        
        this.buildNumber = buildNumber;
        this.commandLine = commandLine;
        this.projectFolder = projectFolder;
        this.filesToCreate = new ArrayList<>();
        this.packages = targetPackages;
        this.buildInformationFile = new NodeFile(projectFolder, ProjectConstants.BUILD_INFORMATION_FILE);
        this.buildInformationManager = new BuildInformationManager(this.logger, this.buildInformationFile);
        
        this.validateVersion();
    }
    
    /**
     * Prepares the packages
     */
    private void preparePackages() {  
        for(String requiredPackage : this.additionalPackages) {
            if (!this.packages.contains(requiredPackage)) {
                this.packages.putPackage(requiredPackage, null);
            }          
        }
    }
    
    /**
     * Determine if this project needs be recreated
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     * @throws java.lang.InterruptedException 
     */
    public boolean needsRecreation() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InterruptedException, DotNetCommandLineException {
        if (!this.projectFolder.exists() || !this.buildInformationFile.exists()) {
            logger.println("#### The project folder and/or the build information file doesn't exists.");
            return true;
        }
        
        if (this.buildInformationManager.needsRecreation(this.packages.getPackagesHash(), this.commandLine.getDotNetVersion())) {
            logger.println("#### The packages list changed.");
            return true;
        }
        
        return false;
    }
    
    public void validateVersion() throws IOException, InterruptedException, DotNetCommandLineException, DotNetProjectManagerException {
        String currentVersion = this.commandLine.getDotNetVersion();
        boolean validation =  this.commandLine.validateDotNetVersion();
        if (!validation) {
            String message = String.format("Invalid dotnet version [%s].", currentVersion);
            String step = "Validating version";
            throw new DotNetProjectManagerException(message, step);
        }
    }
    
    /**
     * Create the project
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws InterruptedException 
     */
    public void createProject() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InterruptedException, DotNetCommandLineException {
        if (this.needsRecreation()) {
            logger.println("#### The project needs recreation.");
            this.recreateProjectFolder();
            this.preparePackages();
            this.createDefaultProject();
            this.addPackages();
            this.restorePackages();
            this.writeFiles();
        } else {
            logger.println("#### The project doesnt needs recreation.");
        }
    }
    
    /**
     * Build the project
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException 
     */
    public void buildProject() throws IOException, InterruptedException, NoSuchAlgorithmException, DotNetCommandLineException {
        this.commandLine.build();
        this.updateBuildInformation();
    }
    
    /**
     * Run the project
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException 
     */
    public void runProject() throws IOException, InterruptedException, NoSuchAlgorithmException, DotNetCommandLineException {
        this.commandLine.run();
    }
    
    /**
     * Write the target files to the project
     * @throws FileNotFoundException 
     */
    private void writeFiles() throws FileNotFoundException, IOException, InterruptedException {
        for(FileForCreation fileForCreation : this.filesToCreate) {
            NodeFile targetPath = new NodeFile(this.projectFolder, fileForCreation.getPath());
            String content = fileForCreation.getContent();
            
            FileTools.writeFile(targetPath, content);
        }
    }
    
    /**
     * Add the packages to the project
     * @throws IOException
     * @throws InterruptedException 
     */
    private void addPackages() throws IOException, InterruptedException, DotNetCommandLineException {
        for(Map.Entry<String, String> thePackage : this.packages.getPackagesMap().entrySet()) {
            String packageName = thePackage.getKey();
            String packageVersion = thePackage.getValue();
            
            if (packageVersion == null) {
                this.commandLine.addPackage(packageName);
            } else {
                this.commandLine.addPackage(packageName, packageVersion);
            }
        }        
    }
    
    /**
     * Restore the packages to the project
     * @throws IOException
     * @throws InterruptedException
     * @throws InterruptedException 
     */
    private void restorePackages() throws IOException, InterruptedException, InterruptedException, DotNetCommandLineException {
        this.commandLine.restoreDependencies();
    }
    
    /**
     * Create a default project
     * @throws IOException
     * @throws InterruptedException 
     */
    private void createDefaultProject() throws IOException, InterruptedException, DotNetCommandLineException {
        this.commandLine.createProject();
    }
    
    /**
     * Recreate the project folder
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public void recreateProjectFolder() throws IOException, InterruptedException {
        this.deleteProjectFolder();
        this.createProjectFolder();
    }
    
    /**
     * Delete the project folder
     */
    private void deleteProjectFolder() throws IOException, InterruptedException {
        if (this.projectFolder.exists()) {
            FileTools.deleteDirectory(this.projectFolder);
        }            
    }
    
    /**
     * Create the project folder
     */
    private void createProjectFolder() throws IOException, InterruptedException {
        this.projectFolder.mkdir();
    }
    
    /**
     * Register a file for creation inside the project
     * @param relativePath
     * @param content 
     */
    public void addFileForCreation(String relativePath, String content) {
        FileForCreation newOne = new FileForCreation(relativePath, content);
        this.filesToCreate.add(newOne);
    }

    /**
     * Update the build information for future runs
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException 
     */
    private void updateBuildInformation() throws NoSuchAlgorithmException, UnsupportedEncodingException, FileNotFoundException, IOException, InterruptedException, DotNetCommandLineException {
        BuildInformation buildInformation = this.buildInformationManager.getBuildInformation();
        
        if (buildInformation == null) {
            buildInformation = new BuildInformation();
        }
        
        buildInformation.setBuildNumber(this.buildNumber);
        buildInformation.setPackagesHash(this.packages.getPackagesHash());
        buildInformation.setDotNetVersion(this.commandLine.getDotNetVersion());
        
        this.buildInformationManager.setBuildInformation(buildInformation);
        this.buildInformationManager.saveBuildInformation();
    }

}
