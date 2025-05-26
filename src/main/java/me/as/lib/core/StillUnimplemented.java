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

package me.as.lib.core;


import static me.as.lib.core.log.DefaultTraceLevels.WARNING;
import static me.as.lib.core.log.LogEngine.logOut;


public class StillUnimplemented extends RuntimeException
{

 public StillUnimplemented()
 {
  this("StillUnimplemented");
 }

 public StillUnimplemented(String msg)
 {
  super(msg);
 }

 public StillUnimplemented(String msg, Throwable tr)
 {
  super(msg, tr);
 }



 public static void hardThrowStillUnimplemented()
 {
  throw new StillUnimplemented();
 }

 public static void throwStillUnimplemented()
 {
  throwStillUnimplemented(null);
 }


 public static void throwStillUnimplemented(String str)
 {
  try
  {
   if (str!=null) throw new StillUnimplemented(str);
   else throw new StillUnimplemented();
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }
 }


 public static void warning(String msg)
 {
  logOut.println(WARNING, WARNING+": StillUnimplemented -> "+msg);
 }


}