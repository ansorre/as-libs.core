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

package me.as.lib.core.extra;


/*

 TimeCounter tc=TimeCounter.start();
 System.out.println(tc.stopAndGetElapsedString());


 */

import static me.as.lib.core.lang.StringExtras.getLiteralTimeAmount;

public class TimeCounter
{
 // used by --->    int[] splitMillis(long millis)
 public static final long SIM  = 1000;
 public static final long MIM  = 60*SIM;
 public static final long HIM  = 60*MIM;
 public static final long DIM  = 24*HIM;
 public static final long WIM  = 7*DIM;
 public static final long MoIM = 4*WIM;
 public static final long YIM  = 12*MoIM;


 private static final long MILION = 1000000;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static TimeCounter start()
 {
  return start("noname");
 }

 public static TimeCounter start(String counterName)
 {
  return new TimeCounter(counterName).restart();
 }

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private String counterName;
 private long h_startTime;
 private long startTime=-1;
 private long endTime=-1;
 private long h_endTime;

 public TimeCounter()
 {
  this("noname");
 }

 public TimeCounter(String counterName)
 {
  this.counterName=counterName;
 }

 public String getName()
 {
  return counterName;
 }

 public long getStartTime()
 {
  return h_startTime;
 }

 public long getEndTime()
 {
  return h_endTime;
 }



 public long getElapsed()
 {
  if (endTime==-1) return (System.nanoTime()-startTime)/MILION;

  return (endTime-startTime)/MILION;
 }


 public String getElapsedString()
 {
  return getLiteralTimeAmount(getElapsed());
 }


 public String stopAndGetElapsedString()
 {
  stop();
  return getElapsedString();
 }


 public long stop()
 {
  endTime=System.nanoTime();
  h_endTime=System.currentTimeMillis();


  /*
    System.out.println
    (
     counterName+
     " lasted: "+
     StringExtras.equallySeparate(String.valueOf(getElapsed()), 3, ".")+
     " milliseconds"
    );
  */


  return getElapsed();
 }

 public TimeCounter restart()
 {
  endTime=-1;
  h_endTime=-1;
  h_startTime=System.currentTimeMillis();
  startTime=System.nanoTime();
  return this;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static int[] splitMillis(long millis)
 {
  int years, months, weeks, days, hours, minutes, seconds, milliseconds;
  long time=millis;

  years=(int)(time/YIM);
  time=time-(years*YIM);

  months=(int)(time/MoIM);
  time=time-(months*MoIM);

  weeks=(int)(time/WIM);
  time=time-(weeks*WIM);

  days=(int)(time/DIM);
  time=time-(days*DIM);

  hours=(int)(time/HIM);
  time=time-(hours*HIM);

  minutes=(int)(time/MIM);
  time=time-(minutes*MIM);

  seconds=(int)(time/1000);
  time=time-(seconds*1000);

  milliseconds=(int)time;

  return new int[]{years, months, weeks, days, hours, minutes, seconds, milliseconds};
 }


}









