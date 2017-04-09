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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sf.json.JSONObject;

/**
 *
 * @author Ariel.Lenis
 */
public class JsonTools {
    
    /**
     * Static method that converts a JSON string to a map of String values
     * @param json
     * @return 
     */
    public static Map<String, String> jsonToStringMap(String json) {
        Map<String, String> result = new HashMap<>();
        JSONObject jsonObject = JSONObject.fromObject(json);
        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            Object objValue = jsonObject.get(key);
            String strValue = null;
            if (objValue != null) {
                strValue = objValue.toString();
            }
            result.put(key, strValue);
        }
        return result;
    }
    
    /**
     * Static method that converts a String map to a JSON string representation.
     * @param map
     * @return 
     */
    public static String stringMapToJson(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<String, String> mapEntry : map.entrySet()) {
            jsonObject.put(mapEntry.getKey(), mapEntry.getValue());
        }
        return jsonObject.toString();
    }
}
