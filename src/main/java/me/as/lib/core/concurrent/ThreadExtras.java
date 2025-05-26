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

package me.as.lib.core.concurrent;


import me.as.lib.core.collection.Fifo;
import me.as.lib.core.extra.Continuable;
import me.as.lib.core.lang.ArrayExtras;

import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.*;

import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;
import static me.as.lib.core.lang.StringExtras.isBlank;
import static me.as.lib.core.system.FileSystemExtras.adjustPath;
import static me.as.lib.core.system.FileSystemExtras.deleteFile;
import static me.as.lib.core.system.FileSystemExtras.isDirectory;


public class ThreadExtras
{

/*
 public static void executeLater(final long howManyMillisLater, final Runnable runnable)
 {
  Thread tt=new Thread(new Runnable()
  {
   public void run()
   {
    sleep(howManyMillisLater);
    runnable.run();
   }
  }, "executeLater temporary thread for "+howManyMillisLater+" milliseconds");
  tt.setDaemon(true);
  tt.start();
 }
*/


 private static AtomicInteger sleeperThreadCount=new AtomicInteger(-1);
 private static final int maxSleeperThreadsInPool=20;

 private static class SleeperThread extends Thread
 {
  SimpleSynchro ss=new SimpleSynchro();
  long howManyMillisLater;
  Runnable runnable;

  SleeperThread()
  {
   this(true);
  }

  SleeperThread(boolean daemon)
  {
   super("SleeperThread_"+sleeperThreadCount.incrementAndGet());
//   System.out.println("{9rdvaaa} new SleeperThread: "+getName());
   setDaemon(daemon);
   start();
  }


  void setRunnable(long howManyMillisLater, Runnable runnable)
  {
   this.runnable=runnable;
   this.howManyMillisLater=howManyMillisLater;
   ss.signalAll();
  }


  public void run()
  {
   while (true)
   {
    ss.waitFor();
    if (howManyMillisLater>0) ThreadExtras.sleep(howManyMillisLater);

    try
    {
     runnable.run();
    }
    catch (Throwable tr)
    {
     throw new RuntimeException(tr);
    }

    if (isDaemon() &&
        threadPool.size()<maxSleeperThreadsInPool)
    {
     this.runnable=null;
     ss.resetSignaled();
     threadPool.put(this);
    }
    else
     break;
   }
  }
 }

 private static final Fifo<SleeperThread> threadPool=new Fifo<>();


 public static void executeOnAnotherThread(Runnable runnable)
 {
  executeLater(0, runnable);
 }

 public static void executeOnAnotherThread(boolean daemon, Runnable runnable)
 {
  executeLater(0, daemon, runnable);
 }


 public static void executeLater(long howManyMillisLater, Runnable runnable)
 {
  executeLater(howManyMillisLater, true, runnable);
 }

 public static void executeLater(long howManyMillisLater, boolean daemon, Runnable runnable)
 {
  SleeperThread st=threadPool.get();
  if (st==null) st=new SleeperThread(daemon);
  st.setRunnable(howManyMillisLater, runnable);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static void join(Thread th)
 {
  try
  {
   th.join();
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


 public static void sleep(long millis)
 {
  try {Thread.sleep(millis);} catch (Throwable ignore){}
 }


 public static boolean longSleep(long sleepAmount, long millisBetweenTests, Continuable owner)
 {
  boolean res;
  long done=0;

  do
  {
   res=owner.canContinue();

   if (res)
   {
    if (sleepAmount<millisBetweenTests) sleep(sleepAmount);
    else sleep(millisBetweenTests);
    done+=millisBetweenTests;
   }

  } while (res && done<sleepAmount);

  return res;
 }



 public static void traceStack()
 {
  traceStack("traceStack fake exception!");
 }



 public static void traceStack(String msg)
 {
  try
  {
   System.out.println("currentThread = "+Thread.currentThread());
   throw new Throwable(msg);
  }
  catch (Throwable tr)
  {
   tr.printStackTrace(System.out);
  }
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Thread-aware log handling
 private static String logsDir=null;
 private static HashMap<Thread, RandomAccessFile> logFiles=null;


 private static RandomAccessFile getRAF()
 {
  RandomAccessFile res=null;

  if (logFiles!=null && isDirectory(logsDir))
  {
   Thread key=Thread.currentThread();

   res=logFiles.get(key);

   if (res==null)
   {
    try
    {
     String fname=adjustPath(logsDir+File.separator+key.getName()+".log");
     deleteFile(fname);
     res=new RandomAccessFile(fname, "rws");
     logFiles.put(key, res);
    }
    catch (Throwable tr)
    {
     systemErrDeepCauseStackTrace(tr);
    }
   }
  }

  return res;
 }





 public static void setPathForLogs(String debLogsdir)
 {
  if (isBlank(debLogsdir))
   throw new RuntimeException("setPathForLogs with blank 'debLogsdir'");

  logsDir=debLogsdir;
  if (logFiles==null && isDirectory(logsDir))
  {
   logFiles=new HashMap<>();
  }
 }


 public static void log(String first, String... other)
 {
  RandomAccessFile raf=getRAF();

  try
  {
   if (raf!=null) raf.writeBytes(first);
   System.out.print(first);

   int t, len=ArrayExtras.length(other);

   for (t=0;t<len;t++)
   {
    if (raf!=null) raf.writeBytes(other[t]);
    System.out.print(other[t]);
   }
  }
  catch (Throwable tr)
  {
   systemErrDeepCauseStackTrace(tr);
  }
 }



 public static void logln(String... strs)
 {
  int t, len=ArrayExtras.length(strs);

  for (t=0;t<len;t++)
  {
   log(strs[t]);
  }

  log("\r\n");
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static Iterable<Thread> getAllRunningThreads()
 {
  ThreadGroup tg=Thread.currentThread().getThreadGroup();
  ThreadGroup rootTg=null;

  do
  {
   rootTg=tg;
   tg=tg.getParent();
  } while (tg!=null);

  return getAllThreadsInGroup(rootTg);
 }


 public static Iterable<Thread> getAllThreadsInGroup()
 {
  return getAllThreadsInGroup(null);
 }


 public static Iterable<Thread> getAllThreadsInGroup(ThreadGroup tg)
 {
  int def_tooManyTries=10;

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  if (tg==null) tg=Thread.currentThread().getThreadGroup();

  int tryCount=0;
  int gc, returned, t, len;
  Thread thList[]=null;

  gc=0;
  returned=Integer.MAX_VALUE;

  while (returned>gc && tryCount<def_tooManyTries)
  {
   gc=tg.activeCount()*3;
   thList=new Thread[gc];
   returned=tg.enumerate(thList, true);
   tryCount++;
  }

  ArrayList<Thread> list=new ArrayList<Thread>();
  len=Math.min(returned, gc);
  for (t=0;t<len;t++)
  {
   list.add(thList[t]);
  }

  return list;
 }






 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static void debugThread(Thread th, String prefix)
 {
  StringBuilder sb=new StringBuilder(prefix);
  sb.append("Thread ID: ").append(th.getId()).
   append(", name: ").append("\"").append(th.getName()).append("\"").
   append(", priority: ").append(th.getPriority()).
   append(", daemon: ").append(th.isDaemon()).
   append(", state: ").append(th.getState().toString());

  System.out.println(sb.toString());
 }




 public static void debugThreadGroup(ThreadGroup tg, String prefix)
 {
  StringBuilder sb=new StringBuilder(prefix);
  sb.append("ThreadGroup name: ").append("\"").append(tg.getName()).append("\"");
  System.out.println(sb.toString());
  prefix+="  ";

  int gc, returned, t, len;
  ThreadGroup tgList[]=null;
  Thread thList[]=null;

  gc=0;
  returned=Integer.MAX_VALUE;

  while (returned>gc)
  {
   gc=tg.activeCount()*3;
   thList=new Thread[gc];
   returned=tg.enumerate(thList, false);
  }

  len=returned;
  for (t=0;t<len;t++)
  {
   debugThread(thList[t], prefix);
  }


  gc=0;
  returned=Integer.MAX_VALUE;

  while (returned>gc)
  {
   gc=tg.activeGroupCount()*3;
   tgList=new ThreadGroup[gc];
   returned=tg.enumerate(tgList, false);
  }

  len=returned;
  for (t=0;t<len;t++)
  {
   debugThreadGroup(tgList[t], prefix);
  }

 }


 public static void debugAllThreads()
 {
  ThreadGroup root=null;
  ThreadGroup cursor=Thread.currentThread().getThreadGroup();

  while (cursor!=null)
  {
   root=cursor.getParent();
   if (root==null)
   {
    root=cursor;
    cursor=null;
   } else cursor=root;
  }

  debugThreadGroup(root, "");
 }


/* // test: SUCCESS
 public static void main(String args[])
 {
  int t, len=3;
  Thread tt;

  for (t=0;t<len;t++)
  {
   tt=new Thread(new Runnable(){
    public void run()
    {
     while (true) sleep(1000);
    }
   });

   tt.setDaemon(true);
   tt.start();
  }

  debugAllThreads();
 }
*/






}


