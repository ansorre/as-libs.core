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

package me.as.lib.core.context;


import java.security.*;
import java.util.*;


public final class Environment extends AbstractContext implements JVMContext
{

 public static Environment get()
 {
  try
  {
   Properties sp=System.getProperties();

   synchronized (sp)
   {
    if (env==null)
    {
     env=(Environment)sp.get(_Environment_key_);
     if (env==null)
     {
      env=new Environment();
      sp.put(_Environment_key_, env);
     }
    }
   }
  }
  catch (AccessControlException ace)
  {
   // maybe we are executing in an Applet!
   //ace.printStackTrace();

   if (env==null) env=new Environment();
  }

  return env;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // internals
 private static final String _Environment_key_="me.as.lib.core.context.Environment _instance_";
 private static Environment env=null;

 private Environment()
 {
  put(LINE_SEPARATOR, System.lineSeparator());
 }

}







