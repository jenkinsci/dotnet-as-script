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
package com.dotnetscript.tools;

import com.dotnetscript.general.NodeFile;
import com.dotnetscript.general.ProjectConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 *
 * @author Ariel.Lenis
 */
public class FileTools { 

    /**
     * Writes the content string inside the target file.
     * @param file
     * @param content
     * @throws FileNotFoundException 
     * @throws java.io.UnsupportedEncodingException 
     */
    public static void writeFile(File file, String content) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(file, ProjectConstants.ENCODING)) {
            writer.write(content);
        }
    }    
    
    /**
     * Writes the content string inside the target file.
     * @param nodeFile
     * @param content
     * @throws FileNotFoundException 
     * @throws java.lang.InterruptedException 
     */
    public static void writeFile(NodeFile nodeFile, String content) throws FileNotFoundException, IOException, InterruptedException {
        nodeFile.getFilePath().write(content, ProjectConstants.ENCODING);
    }       
    
    /**
     * Get the file content as String
     * @param file
     * @return 
     * @throws java.io.IOException 
     */
    public static String getFileContent(File file) throws IOException {
        StringBuilder result = new StringBuilder("");
        
        try (Scanner scanner = new Scanner(file, ProjectConstants.ENCODING)) {

            while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line).append("\n");
            }

            scanner.close();

	} catch (IOException e) {
            throw e;
	}
		
	return result.toString();
    } 
    
    /**
     * Get the file content as String
     * @param nodeFile
     * @return 
     * @throws java.io.IOException 
     * @throws java.lang.InterruptedException 
     */
    public static String getFileContent(NodeFile nodeFile) throws IOException, InterruptedException {
        return nodeFile.getFilePath().readToString();
    } 
    
    public static void deleteDirectory(NodeFile nodeFile) throws IOException, InterruptedException {
        nodeFile.getFilePath().deleteRecursive();
    }
            
}
