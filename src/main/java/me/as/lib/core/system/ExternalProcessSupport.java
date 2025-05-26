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


import me.as.lib.core.collection.Fifo;
import me.as.lib.core.concurrent.SimpleSynchro;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;

import java.io.*;
import java.util.*;

import static me.as.lib.core.concurrent.ThreadExtras.executeOnAnotherThread;


/*

Example:

ThreadUtil.executeOnAnotherThread(true, () ->
  {
   String tsWorkingDirectory;
   ArrayList<String> command=new ArrayList<>();

   command.add(jsweetTscPath);
   command.add("--watch");
   command.add("--target");command.add("ES3");
   command.add("--skipLibCheck");command.add("true");
   command.add("--moduleResolution");command.add("classic");
   command.add("--removeComments");command.add("true");
   command.add("--isolatedModules");command.add("true");
   command.add("--strict");command.add("false");
   command.add("--outDir");command.add(jsout);
   command.add("--outFile");command.add("..\\js\\bundle.js");
   command.add("--project");command.add(tsout);


   ExternalProcessSupport.exec(command, new ExternalProcessHandler()
   {
    public void onOutput(String line)
    {
     if (line.contains(" Watching for file changes.") || line.contains(" - Compilation complete."))
     {
      typescriptSynchro.signal();
     }
    }

public void onErrorOutput(String line){}
public String getWorkingDirectory(){return tsout;}
public String[] getAutoflushStrings(){return null;}
public void onExit(int exitValue){}
public String[] getStringsToPassToProcess(){return null;}
public boolean shouldStopPassingStrings(){return true;}
 });

 });

 */

public class ExternalProcessSupport
{
 // singleton   todo cant singleton because of
 // org.as.app.jfileman.filesystem.internal.Win32ExternalProcessSupport extending ExternalProcessSupport
// public ExternalProcessSupport(){}


 public static final int OT_OUTPUT = 0;
 public static final int OT_ERROR  = 1;




 public static boolean exec(String cmd[])
 {
  return exec(cmd, new ExternalProcessResults(), true);
 }


 public static boolean exec(String cmd[], ExternalProcessResults results)
 {
  return exec(cmd, results, true);
 }


 public static BoxFor2<InputStream, InputStream> execReadingErrorsAndOutput(String cmd[])
 {
  InputStream is, es;

  try
  {
/*
   Runtime rt=Runtime.getRuntime();
   Process pr=rt.exec(cmd, null, null);

   is=pr.getInputStream();
   es=pr.getErrorStream();
*/
   ProcessBuilder pb=new ProcessBuilder(cmd);
   Process process=pb.start();

   is=process.getInputStream();
   es=process.getErrorStream();
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return new BoxFor2<>(es, is);
 }



 public static boolean exec(List<String> cmd, ExternalProcessHandler handler)
 {
  return exec(ArrayExtras.toStrings(cmd.iterator()), handler);
 }


 public static void read(final SimpleSynchro ss, final BufferedReader br, final Fifo<Integer> read)
 {
  executeOnAnotherThread(true, () ->
  {
   boolean goOn;

   do
   {
    try
    {
     int v=br.read();
     goOn=(v!=-1);

     if (goOn)
     {
      read.put(v);
//      System.out.print((char)v);
      ss.signal();
     }
    }
    catch (Throwable tr)
    {
     goOn=false;
    }

   } while (goOn);

   read.put(-1);
   ss.signal();
  });

 }


 public static boolean exec(String cmd[], ExternalProcessHandler handler)
 {
  if (handler==null) return exec(cmd, null, false);
  else
  {
   return new ExternalProcessRunner(cmd,handler).run();
  }
 }


 public static boolean exec(List<String> cmd, ExternalProcessResults results, boolean waitTheEndOfTheProcess)
 {
  return exec(ArrayExtras.toStrings(cmd.iterator()), results, waitTheEndOfTheProcess);
 }

 public static boolean exec(String cmd[], ExternalProcessResults results, boolean waitTheEndOfTheProcess)
 {
  return exec(cmd, results, waitTheEndOfTheProcess, null, null);
 }


 public static boolean exec(String cmd[], ExternalProcessResults results, boolean waitTheEndOfTheProcess, String[] envp, File dir)
 {
  boolean res;

  if (results==null) results=new ExternalProcessResults();

  try
  {
   ExternalProcessResults2 epr2=((results instanceof ExternalProcessResults2)?(ExternalProcessResults2)results:null);

   results.outputText=null;
   results.errorText=null;
   results.exitValue=-1;
   results.throwable=null;

   ArrayList outputText=new ArrayList();
   ArrayList errorText=new ArrayList();

//TimeCounter tc=TimeCounter.start("execute");

   boolean detach=false;
   Runtime rt=Runtime.getRuntime();
   Process pr=rt.exec(cmd, envp, dir);
   if (epr2!=null)
   {
    epr2.processStarted(pr);
    detach=epr2.mustDetach();
   }

   if (!detach && waitTheEndOfTheProcess)
   {
    BufferedReader is=new BufferedReader(new InputStreamReader(pr.getInputStream()));
    BufferedReader es=new BufferedReader(new InputStreamReader(pr.getErrorStream()));
    boolean again;

    do
    {
     again=false;
     String line;

     while (!detach && is.ready())
     {
      line=is.readLine();
      if (line!=null)
      {
       outputText.add(line);
       if (epr2!=null)
       {
        epr2.processOutput(line, OT_OUTPUT);
        detach=epr2.mustDetach();
       }
      }
     }

     while (!detach && es.ready())
     {
      line=es.readLine();
      if (line!=null)
      {
       errorText.add(line);
       if (epr2!=null)
       {
        epr2.processOutput(line, OT_ERROR);
        detach=epr2.mustDetach();
       }
      }
     }

     try
     {
      results.exitValue=pr.exitValue();
     }
     catch (IllegalThreadStateException itse)
     {
      //ThreadExtras.sleep(50);
      //System.out.println("again=true!");
      again=true;
     }
    } while (!detach && again);

    results.outputText=ArrayExtras.toArray(outputText, String.class);
    results.errorText=ArrayExtras.toArray(errorText, String.class);

    /*
    pr.waitFor();
    results.exitValue=pr.exitValue();

    String line;
    BufferedReader is=new BufferedReader(new InputStreamReader(pr.getInputStream()));
    while ((line=is.readLine())!=null) tmpText.add(line);
    results.outputText=(String[])ArrayExtras.toObjects(tmpText.iterator(), String.class);

    tmpText.clear();
    BufferedReader es=new BufferedReader(new InputStreamReader(pr.getErrorStream()));
    while ((line=es.readLine())!=null) tmpText.add(line);
    results.errorText=(String[])ArrayExtras.toObjects(tmpText.iterator(), String.class);
        */
   }
   else
   {
    results.outputText=null;
    results.errorText=null;
    results.exitValue=-1;
   }

   results.throwable=null;
   res=true;

//tc.stop();

  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
   results.throwable=tr;
   res=false;
  }

  return res;
 }


}