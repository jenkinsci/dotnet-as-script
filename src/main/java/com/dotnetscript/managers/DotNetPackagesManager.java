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

import com.dotnetscript.tools.JsonTools;
import com.dotnetscript.tools.StringTools;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 *
 * @author NewType
 */
public class DotNetPackagesManager extends ManagerBase {
    private final Map<String, String> packagesMap;
    
    public DotNetPackagesManager(PrintStream logger, String jsonPackages) {
        super(logger);
        
        this.packagesMap = JsonTools.JsonToStringMap(jsonPackages);
    }
    
    public boolean contains(String packageName) {
        return this.packagesMap.containsKey(packageName);
    }
    
    public Map<String, String> getPackagesMap() {
        return this.packagesMap;
    }
    
    public void putPackage(String packageName, String version) {
        this.packagesMap.put(packageName, version);
    }
    
    public String normalizedJson() {
        return JsonTools.StringMapToJson(this.packagesMap);
    }
    
    public String getPackagesHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return StringTools.getStringIdentificator(JsonTools.StringMapToJson(this.packagesMap));
    }
}
