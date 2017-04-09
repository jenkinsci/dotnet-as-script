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

import com.dotnetscript.general.BuildInformation;
import com.dotnetscript.tools.FileTools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 *
 * @author NewType
 */
public class BuildInformationManager extends ManagerBase {
    private final File buildInformationFile;
    private boolean isInformationLoaded;
    private BuildInformation buildInformation;
    
    public BuildInformationManager(PrintStream logger, File buildInformationFile) {
        super(logger);
        
        this.isInformationLoaded = false;
        this.buildInformationFile = buildInformationFile;
    }
    
    private boolean buildInformationExists() {
        return this.buildInformationFile.exists();
    }
    
    private boolean isInformationLoaded() {
        return this.isInformationLoaded;
    }
    
    public void reloadInformation() {
        String json = FileTools.getFileContent(this.buildInformationFile);
        this.buildInformation = BuildInformation.LoadFromJson(json);
        this.isInformationLoaded = true;        
    }    
    
    public boolean needsRecreation(String packagesHash) {
        if (!this.buildInformationExists()) {
            this.prettyLog("The build information file doesn't exists.");
            return true;
        }
        
        if (!this.isInformationLoaded()) {
            this.reloadInformation();
        }
        
        if (this.buildInformation.getPackagesHash() == null) {
            this.prettyLog("The packages hash is null.");
            return true;
        }
        
        if (!this.buildInformation.getPackagesHash().equals(packagesHash)) {
            this.prettyLog("The hash " + this.buildInformation.getPackagesHash() + " differs from " + packagesHash + ".");
            return true;
        }
        
        return false;
    }

    /**
     * @return the buildInformation
     */
    public BuildInformation getBuildInformation() {     
        return buildInformation;
    }

    /**
     * @param buildInformation the buildInformation to set
     */
    public void setBuildInformation(BuildInformation buildInformation) {
        this.buildInformation = buildInformation;
    }
    
    public void saveBuildInformation() throws FileNotFoundException {
        String json = this.buildInformation.getAsJson();
        FileTools.writeFile(buildInformationFile, json);
    }
} 
