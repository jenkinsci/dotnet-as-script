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
            string targetMethodName = "ScriptMain";
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
