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


public class LogEngine
{

 public static final Logable logOut;
 public static final Logable logErr;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 private static final String thisClass="me.as.lib.core.log.LogEngine";
// private static final String originalSystemOut_key=thisClass+".originalSystemOut";
// private static final String originalSystemErr_key=thisClass+".originalSystemErr";
// private static final String systemOutWrapper_key=thisClass+".systemOutWrapper";
// private static final String systemErrWrapper_key=thisClass+".systemErrWrapper";

 private static Logable systemOutWrapper;
 private static Logable systemErrWrapper;

 static
 {
  synchronized (LogEngine.class)
  {
   PrintStream originalSystemOut=System.out;
   PrintStream originalSystemErr=System.err;

   systemOutWrapper=new LogableHandler(originalSystemOut);
   systemErrWrapper=new LogableHandler(originalSystemErr);

   Logable myOut=new LogableHandler(originalSystemOut);
   Logable myErr=new LogableHandler(originalSystemErr);

   PrintStream myOutPrintStream=new PrintStream(new SimulOutputStream(myOut));
   PrintStream myErrPrintStream=new PrintStream(new SimulOutputStream(myErr));

   System.setOut(myOutPrintStream);
   System.setErr(myErrPrintStream);

   logOut=getGlobalOut();
   logErr=getGlobalErr();
  }
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // public methods

 public static Logable getGlobalOut()
 {
  Logable res;

  synchronized (LogEngine.class)
  {
   res=systemOutWrapper;
  }

  return res;
 }

 public static void setGlobalOut(Logable log)
 {
  synchronized (LogEngine.class)
  {
   systemOutWrapper=log;
   System.setOut(new PrintStream(new SimulOutputStream(log)));
  }
 }



 public static Logable getGlobalErr()
 {
  Logable res;

  synchronized (LogEngine.class)
  {
   res=systemErrWrapper;
  }

  return res;
 }


 public static void setGlobalErr(Logable log)
 {
  synchronized (LogEngine.class)
  {
   systemErrWrapper=log;
   System.setErr(new PrintStream(new SimulOutputStream(log)));
  }
 }


}







