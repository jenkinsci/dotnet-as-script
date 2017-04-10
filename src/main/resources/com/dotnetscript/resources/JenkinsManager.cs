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

using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;

namespace DotNetTools.Jenkins
{
    [Serializable]
    public class JenkinsManager
    {
        public Dictionary<string, string> SavedEnvironment;

        public JenkinsManager()
        {
            this.SavedEnvironment = new Dictionary<string, string>();
        }

        public void SetSessionEnv(string key, string value)
        {
            if (this.SavedEnvironment.ContainsKey(key))
                this.SavedEnvironment[key] = value;
            else
                this.SavedEnvironment.Add(key, value);
        }

        public string GetSessionEnv(string key)
        {
            var currentVariables = Environment.GetEnvironmentVariables();
            if (this.SavedEnvironment.ContainsKey(key))
                return this.SavedEnvironment[key];
            else if (currentVariables.Contains(key))
                return Environment.GetEnvironmentVariable(key);

            return null;
        }
            
        public void SaveForPublish()
        {
            string json = JsonConvert.SerializeObject(this, Formatting.Indented);
            File.WriteAllText("jenkinsExecution.json", json);
        }
    }
}
