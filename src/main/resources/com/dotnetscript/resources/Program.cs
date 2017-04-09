using System;
using System.Reflection;
using System.Linq;
using System.Collections.Generic;
using DotNetTools.Jenkins;

namespace JenkinsPluginTests
{
    class Program
    {
        static int Main(string[] args)
        {
            return JenkinsExecutor.Execute(args);
        }
    }
}