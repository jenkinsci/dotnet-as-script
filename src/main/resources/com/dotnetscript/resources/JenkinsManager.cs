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
