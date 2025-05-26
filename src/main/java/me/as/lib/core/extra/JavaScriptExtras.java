/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package me.as.lib.core.extra;


import me.as.lib.core.lang.ClassExtras;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;

import static me.as.lib.core.log.LogEngine.logOut;


/*

https://github.com/openjdk/nashorn
Nashorn used to be part of the JDK until Java 14. This project provides a standalone version of Nashorn suitable for use with Java 11 and later.
(NB: they say you need to configure as a Java module to use it, but in reality just put everything in the classpath as usual)

*/

public final class JavaScriptExtras
{
 // singleton
 private JavaScriptExtras(){}

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static Boolean canDisableDeprecation=null;


 @SuppressWarnings("removal")
 public static ScriptEngine newJavaScriptEngine()
 {
  // all this is because Nashorn has been deprecated!
  synchronized (JavaScriptExtras.class)
  {
   if (canDisableDeprecation==null)
   {
    try
    {
     canDisableDeprecation=true;
     return newJavaScriptEngine();
    }
    catch (Throwable tr)
    {
     canDisableDeprecation=false;
    }
   }

   if (canDisableDeprecation)
   {
    try
    {
     Class<ScriptEngineFactory> sefc=ClassExtras.classFromNameNE("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
     ScriptEngineFactory sef=sefc.getConstructor().newInstance();
     Method gse=sefc.getMethod("getScriptEngine", String[].class);
     return (ScriptEngine)gse.invoke(sef, (Object)new String[]{"--no-deprecation-warning"});
    }
    catch (Throwable tr)
    {
     throw new RuntimeException(tr);
    }
   }
   else
   {
    ScriptEngine res;

    try
    {
     res=new ScriptEngineManager().getEngineByName("nashorn");
    }
    catch (Throwable tr)
    {
     res=null;
    }

    if (res==null)
     throw new RuntimeException("Nashorn JavaScript engine cannot be found. If you are using a JDK version 15 or up please add to the classpath the standalone version of the Nashorn JavaScript engine that you can find here: https://github.com/openjdk/nashorn");

    return res;
   }
  }
 }


/*
 public static void main(String args[])
 {
  try
  {
   newJavaScriptEngine().eval("print('Hei!')");
   newJavaScriptEngine().eval("print('Hello, World!')");
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }
*/


}
