/*
 * The MIT License
 *
 * Copyright 2017 Ariel Lenis.
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

import hudson.FilePath;
import java.io.IOException;

/**
 *
 * @author Ariel Lenis
 */
public class NodeFile {
    private FilePath filePath;
    
    /**
     * Node file abstraction constructor
     * @param filePath 
     */
    public NodeFile(FilePath filePath) {
        this.filePath = filePath;
    }    
    
    /**
     * Node file abstraction constructor
     * @param nodeFile
     * @param relative 
     */
    public NodeFile(NodeFile nodeFile, String relative) {
        this.filePath = new FilePath(nodeFile.getFilePath(), relative);
    }
    
    /**
     * Determines if the target node file exists
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public boolean exists() throws IOException, InterruptedException {
        return this.filePath.exists();
    }
    
    /**
     * Delete the current node file
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public boolean delete() throws IOException, InterruptedException {
        return this.filePath.delete();
    }
    
    /**
     * Create the folder
     * @throws IOException
     * @throws InterruptedException 
     */
    public void mkdir() throws IOException, InterruptedException { 
        this.filePath.mkdirs();
    }
    
    /**
     * Determines if this node file is a directory
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public boolean isDirectory() throws IOException, InterruptedException {
        return this.filePath.isDirectory();
    }

    /**
     * @return the filePath
     */
    public FilePath getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(FilePath filePath) {
        this.filePath = filePath;
    }
    
    
}
