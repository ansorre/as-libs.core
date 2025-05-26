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

package me.as.lib.core.system;


import me.as.lib.core.concurrent.SimpleSynchro;
import me.as.lib.core.lang.ArrayExtras;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static me.as.lib.core.concurrent.ThreadExtras.executeOnAnotherThread;
import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;
import static me.as.lib.core.lang.StringExtras.isNotBlank;


public class ExternalProcessRunner
{
 private String cmd[];
 private ExternalProcessHandler handler;
 private String autoflushStrings[];
 SimpleSynchro mainSs=new SimpleSynchro();


 class ErrOutReader implements Runnable
 {
  BufferedReader br;
  boolean isOutStream;
  StringBuilder buff=new StringBuilder();
  boolean streamEnded=false;


  ErrOutReader(BufferedReader br, boolean isOutStream)
  {
   this.br=br;
   this.isOutStream=isOutStream;
   executeOnAnotherThread(true, this);
  }


  private void send()
  {
//   System.out.println(buff.toString());

   if (isOutStream)
   {
    handler.onOutput(buff.toString());
   }
   else
   {
    handler.onErrorOutput(buff.toString());
   }

   buff.setLength(0);
  }


  private void checkIfShouldSend()
  {
   int len=buff.length();

   if (len>0)
   {
    char ch=buff.charAt(len-1);

    if (ch=='\n')
    {
     send();
    }
    else
    {
     int t;
     len=ArrayExtras.length(autoflushStrings);

     if (len>0)
     {
      String line=buff.toString();

      for (t=0;t<len;t++)
      {
       if (line.endsWith(autoflushStrings[t]))
       {
        send();
        break;
       }
      }
     }
    }
   }
  }


  public void run()
  {
   try
   {
    do
    {
     int ch=br.read();
     if (ch==-1) break;

     buff.append((char)ch);
     checkIfShouldSend();
    } while (true);

    checkIfShouldSend();
   } catch (Throwable ignore){}

   checkIfShouldSend();
   streamEnded=true;
   mainSs.signal();
  }

 }


 ExternalProcessRunner(String[] cmd, ExternalProcessHandler handler)
 {
  this.cmd=cmd;
  this.handler=handler;
 }



 public void sendMoreStringsToProcess()
 {
  mainSs.signal();
 }


 public boolean run()
 {
  boolean isHandlerEx=(handler instanceof ExternalProcessHandlerEx);

  if (isHandlerEx)
  {
   ((ExternalProcessHandlerEx)handler).setExternalProcessRunner(this);
  }

  File wdf=null;
  String wd=handler.getWorkingDirectory();

  if (isNotBlank(wd))
  {
   wdf=new File(wd);
  }

  String autoflushStrings[];
  Runtime rt=Runtime.getRuntime();
  Process pr;
  ErrOutReader outReader;
  ErrOutReader errReader;
  BufferedWriter processInput;
  boolean again=true;

  try
  {
   pr=rt.exec(cmd, null, wdf);

  }
  catch (Throwable tr)
  {
   systemErrDeepCauseStackTrace(tr);
   return false;
  }

  autoflushStrings=handler.getAutoflushStrings();
  outReader=new ErrOutReader(new BufferedReader(new InputStreamReader(pr.getInputStream())), true);
  errReader=new ErrOutReader(new BufferedReader(new InputStreamReader(pr.getErrorStream())), false);
  processInput=new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

  do
  {
   try
   {
    int ev=pr.exitValue();
    handler.onExit(ev);
    again=false;
   } catch (IllegalThreadStateException ignore){}

   if (again)
   {
    try
    {
     while (!handler.shouldStopPassingStrings())
     {
      String toPass[]=handler.getStringsToPassToProcess();
      int t, len=ArrayExtras.length(toPass);

      for (t=0;t<len;t++)
      {
       processInput.write(toPass[t]+"\n");
      }

      processInput.flush();

      if (!isHandlerEx)
      {
       if (handler.shouldStopPassingStrings())
       {
        processInput.close();
        processInput=null;
       }
      }
     }
    }
    catch (Throwable tr)
    {
     systemErrDeepCauseStackTrace(tr);
     return false;
    }

    mainSs.waitFor(2000);
   }

  } while (again);

  return true;
 }


}
