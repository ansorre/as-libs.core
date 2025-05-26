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

package me.as.lib.core.lang;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static me.as.lib.core.lang.StringExtras.calendar2String;
import static me.as.lib.core.lang.StringExtras.cutOnOccurrences;
import static me.as.lib.core.lang.StringExtras.grantMinLength;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.simpleDF;
import static me.as.lib.core.lang.StringExtras.toLines;
import static me.as.lib.core.lang.StringExtras.trim;
import static me.as.lib.core.log.LogEngine.logErr;


public class ExceptionExtras
{

 public static Throwable getMostInnerCause(Throwable tr)
 {
  Throwable cursor=tr;

  do
  {
   if (cursor.getCause()!=null)
    cursor=cursor.getCause();
   else
    break;

  } while (true);

  return cursor;
 }


 public static String getMostInnerMessage(Throwable tr)
 {
  return getMostInnerCause(tr).getMessage();
 }


 public static void showStack(String msg)
 {
  System.err.println("\n\n----->INFORMATIONAL EXCEPTION<-----");
  System.err.println(getDeepCauseStackTrace(new RuntimeException(msg)));
 }



 public static String getStackTrace(Throwable tr)
 {
  ByteArrayOutputStream baos=new ByteArrayOutputStream();
  tr.printStackTrace(new PrintStream(baos));
  return baos.toString();
 }


 public static void systemErrStackTrace(Throwable tr)
 {
  System.err.println(getStackTrace(tr));
 }




 public static Throwable getDeepCause(Throwable tr)
 {
  boolean goOn=true;
  Throwable c, res=tr;

  do
  {
   c=res.getCause();
   if (c!=null) res=c;
   else goOn=false;
  } while (goOn);

  return res;
 }


 public static String getDeepCauseStackTrace(Throwable tr)
 {
  return getCompleteStackTrace(getDeepCause(tr));
 }




 public static String getCompleteStackTrace(Throwable tr)
 {
  StringBuilder sb=new StringBuilder();

  sb.append("EXCEPTION on thread \"").append(Thread.currentThread().getName()).append("\" @ ").
   append(calendar2String(CalendarExtras.now(), simpleDF)).append(": ");

  int count=0;
  Throwable cause=tr;
  String spaces="    ";

  while (cause!=null)
  {
   StackTraceElement[] trace=cause.getStackTrace();
   if (count>0) sb.append("Caused by: ");
   else sb.append("Exception: ");
   sb.append(cause.getClass().getName());sb.append("\n");

   for(int i=0;i<trace.length;i++)
   {
    if (i==0)
    {
     String message=cause.getMessage();
     sb.append("Details:\n");

     if (hasChars(message))
     {
      String m[]=trim(cutOnOccurrences(message, 100, " ,"));
      int t, len=ArrayExtras.length(m);

      for (t=0;t<len;t++)
      {
       sb.append(spaces);
       sb.append(" \"");
       sb.append(m[t]);
       sb.append("\"\n");
      }

     } else sb.append(" (NO DETAILS PROVIDED)\n");

     sb.append("Stack trace:\n");
    }

    sb.append(spaces);
    sb.append("at ");
    sb.append(trace[i].getClassName()+"."+
              trace[i].getMethodName()+"("+trace[i].getFileName()+
              ":"+trace[i].getLineNumber()+")");


    sb.append("\n");
   }

   cause=cause.getCause();
   count++;
  }

  //sb.append(printStackTrace(tr));

  return sb.toString();
 }



 public static void systemOutNow(String message)
 {
  System.out.println("["+calendar2String(CalendarExtras.now(), simpleDF)+"] "+message);
 }


 public static void systemErr(String message)
 {
  systemErr(message, true);
 }

 public static void systemErr(String message, boolean prependCurrentTime)
 {
  systemErrOut(System.err, message, prependCurrentTime);
 }

 public static void systemOut(String message, boolean prependCurrentTime)
 {
  systemErrOut(System.out, message, prependCurrentTime);
 }


 public static void systemErrOut(PrintStream errOut, String message, boolean prependCurrentTime)
 {
  if (prependCurrentTime)
   message="["+calendar2String(CalendarExtras.now(), simpleDF)+"] "+message;

  errOut.println(message);
 }



 public static void systemErrDeepCauseStackTrace(Throwable tr)
 {
  logErr.println(getDeepCauseStackTrace(tr));
 }


 public static void showErrorAtLine(String message, String offendingCode)
 {
  showErrorAtLine(message, "OFFENDING CODE", offendingCode);
 }

 public static void showErrorAtLine(String message, String offendingMessage, String offendingCode)
 {
  System.out.println(message);
  System.out.println(offendingMessage+":");

  String lines[]=toLines(offendingCode);
  int t, len=ArrayExtras.length(lines);
  for (t=0;t<len;t++) System.out.println(grantMinLength(""+(t+1), 4, ' ', false)+"  |"+lines[t]);

  System.out.flush();
 }




}
