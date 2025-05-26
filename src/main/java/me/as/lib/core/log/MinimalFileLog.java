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

package me.as.lib.core.log;


import java.io.*;

import static me.as.lib.core.system.FileSystemExtras.deleteFile;
import static me.as.lib.core.system.FileSystemExtras.exists;
import static me.as.lib.core.system.FileSystemExtras.loadTextFromFile;
import static me.as.lib.core.system.FileSystemExtras.saveInFile;


public class MinimalFileLog implements MinimalLogable
{
 protected String fileName;
 protected PrintStream ps=null;


 public MinimalFileLog(String fileName, boolean createIfNotExistent, boolean appendMode)
 {
  this.fileName=fileName;
  start(createIfNotExistent, appendMode);
 }


 protected synchronized void start(boolean createIfNotExistent, boolean appendMode)
 {
  if (isClosed())
  {
   boolean exist=exists(fileName);

   if (exist || createIfNotExistent)
   {
    if (exist && !appendMode) deleteFile(fileName);

    try
    {
     ps=new PrintStream(new FileOutputStream(fileName, appendMode), true);
    }
    catch (Throwable tr)
    {
     saveInFile(fileName, "tmp");
     deleteFile(fileName);

     try
     {
      ps=new PrintStream(new FileOutputStream(fileName, appendMode), true);
     }
     catch (Throwable tr2)
     {
      throw new me.as.lib.core.io.IOException("Could not open or create for writing the file '"+fileName+"'", tr2);
     }
    }
   }
   else
   {
    throw new me.as.lib.core.io.IOException("The file '"+fileName+"' does not exist and 'createIfNotExistent' was false");
   }
  }
 }

 public synchronized void print(String str)
 {
  if (!isClosed()) ps.print(str);
 }

 public synchronized void flush()
 {
  if (!isClosed()) ps.flush();
 }

 public synchronized void close()
 {
  if (!isClosed())
  {
   ps.close();
   ps=null;
  }
 }

 public boolean isClosed()
 {
  return (ps==null);
 }

 public synchronized String getLogContent()
 {
  close();
  String res=loadTextFromFile(fileName);
  start(false, true);
  return res;
 }

}