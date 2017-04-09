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

import com.dotnetscript.general.FileForCreation;
import com.dotnetscript.general.ProjectConstants;
import com.dotnetscript.general.BuildInformation;
import com.dotnetscript.tools.FileTools;
import java.io.File;
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
 * @author NewType
 */
public class DotNetProjectManager extends ManagerBase {
    private final DotNetCommandLineManager commandLine;
    private final List<FileForCreation> filesToCreate;
    private final DotNetPackagesManager packages;
    private final File projectFolder;
    private final File buildInformationFile;
    private final String[] additionalPackages = new String[]{"Newtonsoft.Json"};  
    private final BuildInformationManager buildInformationManager;
    private final int buildNumber;
    
    /**
     * 
     * @param commandLine 
     */
    public DotNetProjectManager(PrintStream logger, int buildNumber, DotNetCommandLineManager commandLine, DotNetPackagesManager targetPackages, File projectFolder) {
        super(logger);
        
        this.buildNumber = buildNumber;
        this.commandLine = commandLine;
        this.projectFolder = projectFolder;
        this.filesToCreate = new ArrayList<FileForCreation>();
        this.packages = targetPackages;
        this.buildInformationFile = new File(projectFolder, ProjectConstants.BUILD_INFORMATION_FILE);
        this.buildInformationManager = new BuildInformationManager(this.logger, this.buildInformationFile);
    }
    
    private void preparePackages() {  
        for(String requiredPackage : this.additionalPackages) {
            if (!this.packages.contains(requiredPackage)) {
                this.packages.putPackage(requiredPackage, null);
            }          
        }
    }
    
    public boolean needsRecreation() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (!this.projectFolder.exists() || !this.buildInformationFile.exists()) {
            logger.println("#### The project folder and/or the build information file doesn't exists.");
            return true;
        }
        
        if (this.buildInformationManager.needsRecreation(this.packages.getPackagesHash())) {
            logger.println("#### The packages list changed.");
            return true;
        }
        
        return false;
    }
    
    public void createProject() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InterruptedException {
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
    
    public void buildProject() throws IOException, InterruptedException, NoSuchAlgorithmException {
        this.commandLine.build();
        this.updateBuildInformation();
    }
    
    public void runProject() throws IOException, InterruptedException, NoSuchAlgorithmException {
        this.commandLine.run();
    }
    
    private void writeFiles() throws FileNotFoundException {
        for(FileForCreation fileForCreation : this.filesToCreate) {
            File targetPath = new File(this.projectFolder, fileForCreation.getPath());
            String content = fileForCreation.getContent();
            
            FileTools.writeFile(targetPath, content);
        }
    }
    
    private void addPackages() throws IOException, InterruptedException {
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
    
    private void restorePackages() throws IOException, InterruptedException, InterruptedException {
        this.commandLine.restoreDependencies();
    }
    
    private void createDefaultProject() throws IOException, InterruptedException {
        this.commandLine.createProject();
    }
    
    public void recreateProjectFolder() {
        this.deleteProjectFolder();
        this.createProjectFolder();
    }
    
    private void deleteProjectFolder() {
        if (this.projectFolder.exists()) {
            this.projectFolder.delete();
        }            
    }
    
    private void createProjectFolder() {
        this.projectFolder.mkdir();
    }
    
    public void addFileForCreation(String relativePath, String content) {
        FileForCreation newOne = new FileForCreation(relativePath, content);
        this.filesToCreate.add(newOne);
    }

    private void updateBuildInformation() throws NoSuchAlgorithmException, UnsupportedEncodingException, FileNotFoundException {
        BuildInformation buildInformation = this.buildInformationManager.getBuildInformation();
        
        if (buildInformation == null) {
            buildInformation = new BuildInformation();
        }
        
        buildInformation.setBuildNumber(this.buildNumber);
        buildInformation.setPackagesHash(this.packages.getPackagesHash());
        
        this.buildInformationManager.setBuildInformation(buildInformation);
        this.buildInformationManager.saveBuildInformation();
    }

}
