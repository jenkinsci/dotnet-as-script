using System;
using System.Collections.Generic;
using System.Reflection;
using System.Linq;

namespace DotNetTools.Jenkins
{
    class JenkinsExecutor
    {
        private static MethodInfo GetTargetMethod()
        {
            string targetMethodName = "DotScriptMain";
            var currentAssembly = Assembly.GetEntryAssembly();

            List<Type> validReturnTypes = new List<Type>() { typeof(int), typeof(void) };

            foreach (var type in currentAssembly.DefinedTypes)
            {
                var method = type.GetDeclaredMethods(targetMethodName).Where(x => x.IsStatic && validReturnTypes.Contains(x.ReturnType)).FirstOrDefault();
                if (method != null)
                    return method;
            }
            return null;
        }

        public static int Execute(string[] args)
        {
            JenkinsManager manager = new JenkinsManager();

            var targetMethod = GetTargetMethod();

            if (targetMethod == null)
            {
                throw new Exception("Error, cannot found any main method.");
            }


            var methodParameters = targetMethod.GetParameters().ToList();

            List<object> values = new List<object>();

            foreach (var parameter in methodParameters)
            {
                if (parameter.ParameterType == typeof(string[]))
                    values.Add(args);
                else if (parameter.ParameterType == typeof(JenkinsManager))
                    values.Add(manager);
                else
                    throw new Exception("Error, the main method contains invalid parameters.");
            }

            int methodResult = 0;

            try
            {
                object objResult = targetMethod.Invoke(null, values.ToArray());

                if (targetMethod.ReturnType == typeof(int))
                    methodResult = (int)objResult;
            }
            catch (Exception error)
            {
                throw new Exception("An error happens executing the script.", error);
            }
            finally
            {
                manager.SaveForPublish();
            }

            return methodResult;
        }
    }
}
