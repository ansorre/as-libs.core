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


import me.as.lib.core.collection.Fifo;
import me.as.lib.core.concurrent.SimpleSynchro;

import java.io.*;
import java.util.concurrent.atomic.*;


public class AsynchLog implements Logable, Runnable
{
 protected static final int CMDID_close                              =  0;
 protected static final int CMDID_flush                              =  1;
 protected static final int CMDID_print_v                            =  2;
 protected static final int CMDID_println_v                          =  3;
 protected static final int CMDID_println_av                         =  4;
 protected static final int CMDID_println_ov                         =  5;
 protected static final int CMDID_printStackTrace_v                  =  6;
 protected static final int CMDID_print_tv                           =  7;
 protected static final int CMDID_println_tv                         =  8;
 protected static final int CMDID_println_tav                        =  9;
 protected static final int CMDID_println_tov                        = 10;
 protected static final int CMDID_printStackTrace_tv                 = 11;
 protected static final int CMDID_setEnabled                         = 12;
 protected static final int CMDID_setTraceLevels                     = 13;
 protected static final int CMDID_attachLog                          = 14;
 protected static final int CMDID_detachLog                          = 15;
 protected static final int CMDID_setRedirectedExclusivelyToAttached = 16;

 protected static final Integer CMDIDs[]=new Integer[]
 {
  CMDID_close,
  CMDID_flush,
  CMDID_print_v,
  CMDID_println_v,
  CMDID_println_av,
  CMDID_println_ov,
  CMDID_printStackTrace_v,
  CMDID_print_tv,
  CMDID_println_tv,
  CMDID_println_tav,
  CMDID_println_tov,
  CMDID_printStackTrace_tv,
  CMDID_setEnabled,
  CMDID_setTraceLevels,
  CMDID_attachLog,
  CMDID_detachLog,
  CMDID_setRedirectedExclusivelyToAttached
 };

 protected static final Object NULL_OBJECT = new Object();


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected LogableHandler synchLog;
 protected Fifo commands=new Fifo(50);
 protected SimpleSynchro flushSynchro=new SimpleSynchro();
 protected boolean closed=false;


 public AsynchLog()
 {
  synchLog=new LogableHandler();
  initialize();
 }

 public AsynchLog(MinimalLogable ml)
 {
  synchLog=new LogableHandler(ml);
  initialize();
 }

 public AsynchLog(MinimalLogable ml, boolean redirectedExclusivelyToAttached)
 {
  synchLog=new LogableHandler(ml, redirectedExclusivelyToAttached);
  initialize();
 }

 public AsynchLog(PrintStream ps)
 {
  synchLog=new LogableHandler(ps);
  initialize();
 }

 protected synchronized void initialize()
 {
  Thread t=new Thread(this);
  t.setDaemon(true);
  t.start();
 }


 private static final AtomicInteger count=new AtomicInteger();

 public void run()
 {
  Thread.currentThread().setName("AsynchLog thread - "+count.incrementAndGet());
  boolean again=true;

  while (again)
  {
   Integer cmd=(Integer)commands.getWaiting();

   switch (cmd)
   {
    case CMDID_close:
         {
          synchLog.close();
          again=false;
         } break;

    case CMDID_flush:
         {
          synchLog.flush();
          flushSynchro.signal();
         } break;

    case CMDID_print_v:
         {
          String p=(String)commands.getWaiting();
          synchLog.print(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_println_v:
         {
          String p=(String)commands.getWaiting();
          synchLog.println(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_println_av:
         {
          String p[]=(String[])commands.getWaiting();
          synchLog.println(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_println_tov:
         {
          Object p=(Throwable)commands.getWaiting();
          synchLog.println(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_printStackTrace_v:
         {
          Throwable p=(Throwable)commands.getWaiting();
          synchLog.printStackTrace(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_print_tv:
         {
          String p1=(String)commands.getWaiting();
          String p2=(String)commands.getWaiting();
          synchLog.print(((p1==NULL_OBJECT)?null:p1), ((p2==NULL_OBJECT)?null:p2));
         } break;

    case CMDID_println_tv:
         {
          String p1=(String)commands.getWaiting();
          String p2=(String)commands.getWaiting();
          synchLog.println(((p1==NULL_OBJECT)?null:p1), ((p2==NULL_OBJECT)?null:p2));
         } break;

    case CMDID_println_tav:
         {
          String p1=(String)commands.getWaiting();
          String p2[]=(String[])commands.getWaiting();
          synchLog.println(((p1==NULL_OBJECT)?null:p1), ((p2==NULL_OBJECT)?null:p2));
         } break;

    case CMDID_println_ov:
         {
          String p1=(String)commands.getWaiting();
          Object p2=(Throwable)commands.getWaiting();
          synchLog.println(((p1==NULL_OBJECT)?null:p1), ((p2==NULL_OBJECT)?null:p2));
         } break;

    case CMDID_printStackTrace_tv:
         {
          String p1=(String)commands.getWaiting();
          Throwable p2=(Throwable)commands.getWaiting();
          synchLog.printStackTrace(((p1==NULL_OBJECT)?null:p1), ((p2==NULL_OBJECT)?null:p2));
         } break;

    case CMDID_setEnabled:
         {
          Boolean b=(Boolean)commands.getWaiting();
          synchLog.setEnabled(b.booleanValue());
         } break;

    case CMDID_setTraceLevels:
         {
          String p=(String)commands.getWaiting();
          synchLog.setTraceLevels(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_attachLog:
         {
          Logable p=(Logable)commands.getWaiting();
          synchLog.attachLog(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_detachLog:
         {
          Logable p=(Logable)commands.getWaiting();
          synchLog.detachLog(((p==NULL_OBJECT)?null:p));
         } break;

    case CMDID_setRedirectedExclusivelyToAttached:
         {
          Boolean b=(Boolean)commands.getWaiting();
          synchLog.setRedirectedExclusivelyToAttached(b.booleanValue());
         } break;
   }
  }
 }

 protected synchronized void postCommand(int cmdid, Object param1, Object param2)
 {
  commands.put(CMDIDs[cmdid]);

  if (param1!=null)
  {
   commands.put(param1);

   if (param2!=null)
   {
    commands.put(param2);
   }
  }
 }



 public synchronized void flush()
 {
  postCommand(CMDID_flush, null, null);
  flushSynchro.waitFor();
  flushSynchro.resetSignaled();
 }

 public synchronized void close()
 {
  flush();
  postCommand(CMDID_close, null, null);
  closed=true;
 }

 public synchronized boolean isClosed()
 {
  return closed;
 }


 public synchronized String getLogContent()
 {
  flush();
  return synchLog.getLogContent();
 }

 public synchronized void print(String str)
 {
  Object param=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_print_v, param, null);
 }

 public synchronized void println()
 {
  println((String)null);
 }

 public synchronized void println(String str)
 {
  Object param=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_println_v, param, null);
 }

 public synchronized void println(String str[])
 {
  Object param=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_println_av, param, null);
 }


 public synchronized void println(Object o)
 {
  Object param=((o!=null)?o:NULL_OBJECT);
  postCommand(CMDID_println_ov, param, null);
 }

 public synchronized void printStackTrace(Throwable tr)
 {
  Object param=((tr!=null)?tr:NULL_OBJECT);
  postCommand(CMDID_printStackTrace_v, param, null);
 }


 public synchronized void print(String traceLevels, String str)
 {
  Object param1=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  Object param2=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_print_tv, param1, param2);
 }

 public synchronized void println(String traceLevels, String str)
 {
  Object param1=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  Object param2=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_println_tv, param1, param2);
 }

 public synchronized void println(String traceLevels, String str[])
 {
  Object param1=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  Object param2=((str!=null)?str:NULL_OBJECT);
  postCommand(CMDID_println_tav, param1, param2);
 }


 public synchronized void println(String traceLevels, Object o)
 {
  Object param1=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  Object param2=((o!=null)?o:NULL_OBJECT);
  postCommand(CMDID_println_tov, param1, param2);
 }


 public synchronized void printStackTrace(String traceLevels, Throwable tr)
 {
  Object param1=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  Object param2=((tr!=null)?tr:NULL_OBJECT);
  postCommand(CMDID_printStackTrace_tv, param1, param2);
 }

 public synchronized void setEnabled(boolean on)
 {
  postCommand(CMDID_setEnabled, on, null);
 }

 public synchronized boolean getEnabled()
 {
  flush();
  return synchLog.getEnabled();
 }


 public synchronized void setTraceLevels(String traceLevels)
 {
  Object param=((traceLevels!=null)?traceLevels:NULL_OBJECT);
  postCommand(CMDID_setTraceLevels, param, null);
 }

 public synchronized String getTraceLevels()
 {
  flush();
  return synchLog.getTraceLevels();
 }

 public synchronized void attachLog(Logable anotherLogInCascade)
 {
  Object param=((anotherLogInCascade!=null)?anotherLogInCascade:NULL_OBJECT);
  postCommand(CMDID_attachLog, param, null);
 }

 public synchronized void detachLog(Logable oneLessLogInCascade)
 {
  Object param=((oneLessLogInCascade!=null)?oneLessLogInCascade:NULL_OBJECT);
  postCommand(CMDID_detachLog, param, null);
 }



 public synchronized void setRedirectedExclusivelyToAttached(boolean on)
 {
  postCommand(CMDID_setRedirectedExclusivelyToAttached, on, null);
 }

 public synchronized boolean isRedirectedExclusivelyToAttached()
 {
  flush();
  return synchLog.isRedirectedExclusivelyToAttached();
 }



 public synchronized boolean isTraceable(String traceLevels)
 {
  flush();
  return synchLog.isTraceable(traceLevels);
 }


}