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


import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ExceptionExtras;
import me.as.lib.core.lang.StringExtras;

import java.io.*;
import java.util.*;

import static me.as.lib.core.lang.StringExtras.LINE_SEPARATOR;
import static me.as.lib.core.lang.StringExtras.enclose;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.merge;
import static me.as.lib.core.lang.StringExtras.trim;
import static me.as.lib.core.lang.StringExtras.unmerge;


public class LogableHandler implements Logable
{
 protected static final String lineSeparator=LINE_SEPARATOR;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private MinimalLogable ml;
 private String traceLevels=null;
 private boolean enabled=true;
 private boolean redirectedExclusivelyToAttached=false;
 private String _traceLevels=null;
 private boolean everything=false;
 private Vector logsInCascade=new Vector();
 private boolean mustTransform=true;


 public LogableHandler()
 {
  this(null, true);
 }

 public LogableHandler(MinimalLogable ml)
 {
  this.ml=ml;
 }

 public LogableHandler(MinimalLogable ml, boolean redirectedExclusivelyToAttached)
 {
  this(ml);
  setRedirectedExclusivelyToAttached(redirectedExclusivelyToAttached);
 }

 public LogableHandler(PrintStream ps)
 {
  ml=new PrintStreamMinimalLogable(ps);
 }

 static class PrintStreamMinimalLogable implements MinimalLogable
 {
  PrintStream ps;
  boolean closed=false;

  PrintStreamMinimalLogable(PrintStream ps)
  {
   this.ps=ps;
  }

  private boolean wasSLF4J=false;

  public void print(String str)
  {
   if (LoggingExtras.avoid_all_slf4j_Logging)
   {
    boolean isSLF4J=str.startsWith("SLF4J:");
    if (isSLF4J || (wasSLF4J && ("\r\n".equals(str) || "\n".equals(str))))
    {
     if (isSLF4J) wasSLF4J=true;
     return;
    }
   }

   wasSLF4J=false;
   if (!closed && StringExtras.length(str)>0)
    ps.print(str);
  }

  public void flush()
  {
   if (!closed) ps.flush();
  }

  public void close()
  {
   flush();
   closed=true;
  }

  public boolean isClosed()
  {
   return closed;
  }

  public String getLogContent()
  {
   return null;
  }
 }


 protected String transformText(String txt)
 {
  return txt;
 }


 protected void transformThrowable(Throwable tr)
 {

 }


 private String _transformText(String txt)
 {
  if (mustTransform) return transformText(txt);
  return txt;
 }


 protected synchronized void printUntrasformed(String txt)
 {
  mustTransform=false;
  print(txt);
  mustTransform=true;
 }


 public synchronized String getLogContent()
 {
  return ml.getLogContent();
 }

 public synchronized void print(String str)
 {
  if (enabled)
  {
   str=_transformText(str);

   if (!redirectedExclusivelyToAttached)
   {
    if (ml!=null) ml.print(str);
    else System.out.print(str);
   }

   int t, len=logsInCascade.size();
   if (len>0)
   {
    for (t=0;t<len;t++)
    {
     ((Logable)logsInCascade.elementAt(t)).print(str);
    }
   }
  }
 }


 public void println()
 {
  print(lineSeparator);
  if (enabled) flush();
 }


 public void println(String str)
 {
  print(str+lineSeparator);
  if (enabled) flush();
 }


 public synchronized void println(String str[])
 {
  if (str!=null)
  {
   int t, len=str.length;

   if (len>0)
   {
    for (t=0;t<len;t++) println(str[t]);
   }
  }
 }

 public synchronized void println(Object o)
 {
  /** @todo **/
  // please specialize the print basing it on the type of 'o'

  println(((o!=null)?o.toString():"null"));
 }



 public synchronized void printStackTrace(Throwable tr)
 {
  if (enabled)
  {
   transformThrowable(tr);

   if (!redirectedExclusivelyToAttached)
   {
    if (ml!=null) ml.print(ExceptionExtras.getStackTrace(tr));
    else tr.printStackTrace();
   }

   int t, len=logsInCascade.size();
   if (len>0)
   {
    for (t=0;t<len;t++)
    {
     ((Logable)logsInCascade.elementAt(t)).printStackTrace(tr);
    }
   }

   flush();
  }
 }


 public synchronized void print(String traceLevels, String str)
 {
  if (isTraceable(traceLevels))
   print(str);
 }

 public synchronized void println(String traceLevels, String str)
 {
  if (isTraceable(traceLevels))
   println(str);
 }

 public synchronized void println(String traceLevels, String str[])
 {
  if (str!=null)
  {
   int t, len=str.length;

   if (len>0)
   {
    for (t=0;t<len;t++) println(traceLevels, str[t]);
   }
  }
 }


 public synchronized void println(String traceLevels, Object o)
 {
  /** @todo **/
  // please specialize the print basing it on the type of 'o'

  println(traceLevels, ((o!=null)?o.toString():"null"));
 }


 public synchronized void printStackTrace(String traceLevels, Throwable tr)
 {
  if (isTraceable(traceLevels))
   printStackTrace(tr);
 }


 public synchronized void flush()
 {
  if (!redirectedExclusivelyToAttached)
  {
   if (ml!=null) {ml.flush();}
   else System.out.flush();
  }

  int t, len=logsInCascade.size();
  if (len>0)
  {
   for (t=0;t<len;t++)
   {
    ((Logable)logsInCascade.elementAt(t)).flush();
   }
  }
 }

 public void close()
 {
  if (ml!=null) {ml.close();}
 }

 public boolean isClosed()
 {
  if (ml!=null) return ml.isClosed();
  return false;
 }



 public synchronized void setEnabled(boolean on)
 {
  enabled=on;
 }

 public synchronized boolean getEnabled()
 {
  return enabled;
 }


 public synchronized void setTraceLevels(String __traceLevels)
 {
  traceLevels=__traceLevels;

  if (isNotBlank(traceLevels))
  {
   _traceLevels=","+merge(enclose(trim(unmerge(traceLevels, ',')), null, ","));

   everything=(_traceLevels.contains(",*,"));
  }
  else
  {
   _traceLevels=null;
   everything=false;
  }
 }

 public synchronized String getTraceLevels()
 {
  return traceLevels;
 }

 public synchronized void attachLog(Logable anotherLogInCascade)
 {
  logsInCascade.addElement(anotherLogInCascade);
 }

 public synchronized void detachLog(Logable anotherLogInCascade)
 {
  logsInCascade.removeElement(anotherLogInCascade);
 }



 public synchronized void setRedirectedExclusivelyToAttached(boolean on)
 {
  redirectedExclusivelyToAttached=on;
 }

 public synchronized boolean isRedirectedExclusivelyToAttached()
 {
  return redirectedExclusivelyToAttached;
 }



 public synchronized boolean isTraceable(String userLevels)
 {
  boolean res=enabled;

  if (res)
  {
   if (!everything && isNotBlank(userLevels) && isNotBlank(_traceLevels))
   {
    String levels[]=trim(unmerge(userLevels, ','));
    int len=ArrayExtras.length(levels);

    if (len>0)
    {
     res=false;

     for (int t=0;t<len && !res;t++)
     {
      res=(_traceLevels.contains(","+levels[t]+","));
     }
    }
   }
  }

  return res;
 }

}


