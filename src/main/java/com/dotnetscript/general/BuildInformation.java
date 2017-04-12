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
package com.dotnetscript.general;

import net.sf.json.JSONObject;

/**
 *
 * @author Ariel.Lenis
 */
public class BuildInformation {
    
    private int buildNumber;
    private String packagesHash;
    private String dotNetVersion;
    
    /**
     * Load the build information from a JSON string
     * @param json
     * @return 
     */
    public static BuildInformation loadFromJson(String json) {
        BuildInformation newOne = new BuildInformation();
        
        JSONObject jsonObject = JSONObject.fromObject(json);

        Object objBuildNumber = jsonObject.get("buildNumber");
        Object objPackagesHash = jsonObject.get("packagesHash");
        Object objDotNetVersion = jsonObject.get("dotNetVersion");
        
        if (objBuildNumber != null && objBuildNumber instanceof Integer) {
            newOne.buildNumber = (int)objBuildNumber;
        }
        
        if (objPackagesHash != null && objPackagesHash instanceof String) {
            newOne.packagesHash = (String)objPackagesHash;
        }
        
        if (objDotNetVersion != null && objDotNetVersion instanceof String) {
            newOne.dotNetVersion = (String)objDotNetVersion;
        }        
        
        return newOne;
    }
    
    /**
     * Gets the current object as a JSON string
     * @return the JSON string that represent this object
     */
    public String getAsJson() {
        JSONObject jsonObject = new JSONObject();
        
        jsonObject.put("buildNumber", this.buildNumber);
        jsonObject.put("packagesHash", this.packagesHash);
        jsonObject.put("dotNetVersion", this.dotNetVersion);
        
        return jsonObject.toString();
    }

    /**
     * @return the buildNumber
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber the buildNumber to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the packagesHash
     */
    public String getPackagesHash() {
        return packagesHash;
    }

    /**
     * @param packagesHash the packagesHash to set
     */
    public void setPackagesHash(String packagesHash) {
        this.packagesHash = packagesHash;
    }

    /**
     * @return the dotNetVersion
     */
    public String getDotNetVersion() {
        return dotNetVersion;
    }

    /**
     * @param dotNetVersion the dotNetVersion to set
     */
    public void setDotNetVersion(String dotNetVersion) {
        this.dotNetVersion = dotNetVersion;
    }
    
}
