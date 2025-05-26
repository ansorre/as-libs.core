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


package me.as.lib.core.report;


import me.as.lib.core.log.Logable;
import me.as.lib.core.report.Problem.Type;

import java.util.ArrayList;

import static me.as.lib.core.log.LogEngine.logErr;


public class Problems extends ArrayList<Problem>
{
 private Boolean hasShowStoppers=null;


 public void clear()
 {
  super.clear();
  hasShowStoppers=null;
 }

 public void addShowStopperNoPrefix(String message)
 {
  add(Type.showstopperNoPrefix,  message);
  hasShowStoppers=true;
 }

 public void addShowStopper(String message)
 {
  add(Type.showstopper, message);
  hasShowStoppers=true;
 }

 public void addShowStopper(String message, boolean alsoThrowRuntimeException)
 {
  add(Type.showstopper, message);
  hasShowStoppers=true;
  if (alsoThrowRuntimeException)
   throw new RuntimeException(message);
 }


 public void addShowStopper(Throwable tr, boolean alsoThrowRuntimeException)
 {
  add(Type.showstopper, tr.getMessage());
  hasShowStoppers=true;
  if (alsoThrowRuntimeException)
   throw new RuntimeException(tr);
 }


 public void addWarning(String message)
 {
  add(Type.warning,  message);
 }

 public void addError(String message)
 {
  add(Type.error,  message);
 }

 public void add(Type type, String message)
 {
  add(new Problem(type, message));
 }


 public boolean areThereShowStoppers()
 {
  if (hasShowStoppers==null)
  {
   for (Problem p : this)
    if (p.type==Type.showstopper || p.type==Type.showstopperNoPrefix)
    {
     hasShowStoppers=true;
     break;
    }
  }

  return hasShowStoppers!=null;
 }


 public void printIfTheCase()
 {
  printIfTheCase(null);
 }


 public void printIfTheCase(Logable _log)
 {
  if (size()>0)
  {
   Logable log=_log==null ? logErr : _log;
   forEach(p -> log.println(p.toString()));
  }
 }


}
