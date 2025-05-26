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

import java.text.*;
import java.util.*;

import static me.as.lib.core.lang.StringExtras.calendar2String;
import static me.as.lib.core.lang.StringExtras.unmerge;


public class CalendarExtras
{

 public static final long oneSecond=1000;
 public static final long halfMinute=30*oneSecond;
 public static final long oneMinute=halfMinute*2;
 public static final long twoMinutes=oneMinute*2;
 public static final long threeMinutes=oneMinute*3;
 public static final long fiveMinutes=oneMinute*5;
 public static final long tenMinutes=fiveMinutes*2;

 public static final long halfHour=30*oneMinute;
 public static final long oneHour=2*halfHour;

 public static final long oneDay=oneMinute*60*24;

 public static final long oneMonth=oneDay*30;

 public static final long oneYear=oneDay*365;


 public static final int midnight[]=new int[]{0, 0, 0, 0};
 public static final SimpleDateFormat simpleDay=new SimpleDateFormat("dd/MM/yyyy");


 private static Calendar _c_=Calendar.getInstance(); // I use it to clone new instancs, way more speed than Calendar.getInstance()


 public static Calendar newInstance(Date date)
 {
  return ((date!=null) ? newInstance(date.getTime()) : null);
 }

 public static Date getTime(Calendar calendar)
 {
  return ((calendar!=null) ? calendar.getTime() : null);
 }


 public static Calendar newInstance(long millis)
 {
  Calendar res=(Calendar)_c_.clone();
  res.setTimeInMillis(millis);
  return res;
 }


 public static Calendar newInstance(int year, int month, int dayOfMonth)
 {
  return newInstance(year, month, dayOfMonth, 0, 0, 0, 0);
 }

 public static Calendar newInstance(int year, int month, int dayOfMonth, int hour, int minute, int second, int millisecond)
 {
  Calendar res=(Calendar)_c_.clone();
  setYearMonthDay(res, year, month, dayOfMonth);
  setHourMinutesSecondsAndMillis(res, hour, minute, second, millisecond);
  return res;
 }


 public static Calendar now()
 {
  return Calendar.getInstance(); // <-- cannot clone here, I need current time!!!
 }


 public static boolean isToday(Calendar cal)
 {
  boolean res=false;

  if (cal!=null)
  {
   res=isSameDay(Calendar.getInstance(), cal);
  }

  return res;
 }


 public static Calendar clone(Calendar time)
 {
  return ((time!=null)?(Calendar)time.clone():null);
 }


 public static String getDayKey(Calendar time)
 {
  return calendar2String(time, simpleDay);
 }



 public static Calendar startOfDay(Calendar time)
 {
  return trimToDay(clone(time));
 }

 public static Calendar startOfMonth(Calendar time)
 {
  Calendar res=clone(time);
  setHourMinutesSecondsAndMillis(res, midnight);
  res.set(Calendar.DAY_OF_MONTH, 1);
  return res;
 }





 public static String getSpeedDayKey(Calendar time)
 {
  return new StringBuilder().append(time.get(Calendar.YEAR)).append("/").append(time.get(Calendar.DAY_OF_YEAR)).toString();
 }

 public static Calendar getDayBySpeedDayKey(String speedDayKey)
 {
  String s[]=unmerge(speedDayKey, '/');
  Calendar res=(Calendar)_c_.clone();
  res.set(Calendar.YEAR, Integer.parseInt(s[0]));
  res.set(Calendar.DAY_OF_YEAR, Integer.parseInt(s[1]));
  return trimToDay(res);
 }


 public static Calendar add(Calendar source, int field, int amount)
 {
  Calendar res=clone(source);
  res.add(field, amount);
  return res;
 }




 public static Calendar trimToField(Calendar time, int field)
 {
  switch (field)
  {
   case Calendar.YEAR:
   {
    time.set(Calendar.MONTH, Calendar.JANUARY);
   }

   case Calendar.MONTH:
   {
    time.set(Calendar.WEEK_OF_MONTH, 1);
   }

   case Calendar.WEEK_OF_MONTH:
   case Calendar.WEEK_OF_YEAR:
   {
    time.set(Calendar.DAY_OF_MONTH, 1);
   }


   case Calendar.DAY_OF_MONTH:
   case Calendar.DAY_OF_WEEK:
   case Calendar.DAY_OF_WEEK_IN_MONTH:
   case Calendar.DAY_OF_YEAR:
   {
    time.set(Calendar.HOUR_OF_DAY, 0);
   }


   case Calendar.HOUR:
   case Calendar.HOUR_OF_DAY:
   {
    time.set(Calendar.MINUTE, 0);
   }


   case Calendar.MINUTE:
   {
    time.set(Calendar.SECOND, 0);
   }

   case Calendar.SECOND:
   {
    time.set(Calendar.MILLISECOND, 0);
   }

  }

  return time;
 }


/*
 // test: SUCCESS
 public static void main(String args[])
 {
  Calendar now=now();

  log.debugging(StringExtras.calendar2String(now, StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.YEAR           ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.MONTH          ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.WEEK_OF_YEAR   ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.DAY_OF_YEAR    ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.HOUR_OF_DAY    ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.MINUTE         ), StringExtras.c2s2cDF));
  log.debugging(StringExtras.calendar2String(trimToField(clone(now), Calendar.SECOND         ), StringExtras.c2s2cDF));
 }
*/




 public static Calendar trimToMillis(Calendar time, long millis)
 {
//  long deb_were=time.getTimeInMillis();               // <---- remove, just debugging
//  String deb_before=StringExtras.calendar2String(time); // <---- remove, just debugging

  time.setTimeInMillis((time.getTimeInMillis()/millis)*millis);

//  System.out.println("millis where: "+deb_were+"    millis are: "+time.getTimeInMillis()+             // <---- remove, just debugging
//                     "  time before: "+deb_before+"  time after: "+StringExtras.calendar2String(time)); // <---- remove, just debugging

  return time;
 }



 public static Calendar trimToDay(Calendar time)
 {
  setHourMinutesSecondsAndMillis(time, midnight);
  return time;
 }

 public static Calendar trimToMonth(Calendar time)
 {
  setHourMinutesSecondsAndMillis(time, midnight);
  time.set(Calendar.DAY_OF_MONTH, 1);
  return time;
 }


 public static Calendar setHourMinutesSecondsAndMillis(Calendar dest, Calendar orig)
 {
  return setHourMinutesSecondsAndMillis(dest, new int[]{
   orig.get(Calendar.HOUR_OF_DAY), orig.get(Calendar.MINUTE), orig.get(Calendar.SECOND), orig.get(Calendar.MILLISECOND)
  });
 }


 public static Calendar setHourMinutesSecondsAndMillis(Calendar c, int hmsm[])
 {
  return setHourMinutesSecondsAndMillis(c, hmsm[0], hmsm[1], hmsm[2], hmsm[3]);
 }

 public static Calendar setHourMinutesSecondsAndMillis(Calendar c, int hour, int minutes, int seconds, int millis)
 {
  if (c!=null)
  {
   c.set(Calendar.MILLISECOND, millis);
   c.set(Calendar.SECOND, seconds);
   c.set(Calendar.MINUTE, minutes);
   c.set(Calendar.HOUR_OF_DAY, hour);
  }

  return c;
 }



 public static Calendar setYearMonthDay(Calendar dest, Calendar source)
 {
  return setYearMonthDay(dest, source.get(Calendar.YEAR), source.get(Calendar.MONTH), source.get(Calendar.DAY_OF_MONTH));
 }

 public static Calendar setYearMonthDay(Calendar c, int ymd[])
 {
  return setYearMonthDay(c, ymd[0], ymd[1], ymd[2]);
 }

 public static Calendar setYearMonthDay(Calendar c, int year, int month, int day)
 {
  if (c!=null)
  {
   c.set(Calendar.YEAR, year);
   c.set(Calendar.MONTH, month);
   c.set(Calendar.DAY_OF_MONTH, day);
  }

  return c;
 }


 public static final int SAME_MINUTE = 0;
 public static final int SAME_HOUR   = 1;
 public static final int SAME_DAY    = 2;
 public static final int SAME_WEEK   = 3;
 public static final int SAME_MONTH  = 4;
 public static final int SAME_YEAR   = 5;



 public static boolean isSame(Calendar time1, Calendar time2, int whatSame)
 {
  boolean res=false;

  switch (whatSame)
  {
   case SAME_MINUTE:res=isSameMinute(time1, time2);break;
   case SAME_HOUR  :res=isSameHour(time1, time2);break;
   case SAME_DAY   :res=isSameDay(time1, time2);break;
   case SAME_WEEK  :res=isSameWeek(time1, time2);break;
   case SAME_MONTH :res=isSameMonth(time1, time2);break;
   case SAME_YEAR  :res=isSameYear(time1, time2);break;
  }

  return res;
 }





 public static boolean isSameYear(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.YEAR)==time2.get(Calendar.YEAR));
 }


 public static boolean isSameMonth(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.YEAR)==time2.get(Calendar.YEAR) &&
          time1.get(Calendar.MONTH)==time2.get(Calendar.MONTH));
 }


 public static boolean isSameWeek(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.WEEK_OF_YEAR)==time2.get(Calendar.WEEK_OF_YEAR));
 }



 public static boolean isSameDay(long time1, long time2)
 {
  Calendar t1=(Calendar)_c_.clone();
  Calendar t2=(Calendar)_c_.clone();

  t1.setTimeInMillis(time1);
  t2.setTimeInMillis(time2);

  return isSameDay(t1, t2);
 }


 public static boolean isSameDay(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.YEAR)==time2.get(Calendar.YEAR) &&
          time1.get(Calendar.DAY_OF_YEAR)==time2.get(Calendar.DAY_OF_YEAR));
 }


 public static boolean isSameHour(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.YEAR)==time2.get(Calendar.YEAR) &&
          time1.get(Calendar.DAY_OF_YEAR)==time2.get(Calendar.DAY_OF_YEAR) &&
          time1.get(Calendar.HOUR_OF_DAY)==time2.get(Calendar.HOUR_OF_DAY));
 }


 public static boolean isSameMinute(Calendar time1, Calendar time2)
 {
  return (time1!=null && time2!=null &&
          time1.get(Calendar.YEAR)==time2.get(Calendar.YEAR) &&
          time1.get(Calendar.DAY_OF_YEAR)==time2.get(Calendar.DAY_OF_YEAR) &&
          time1.get(Calendar.HOUR_OF_DAY)==time2.get(Calendar.HOUR_OF_DAY) &&
          time1.get(Calendar.MINUTE)==time2.get(Calendar.MINUTE));
 }



 public static int[] toRelevantFields(long timeMillis)
 {
  return toRelevantFields(newInstance(timeMillis), null);
 }


 public static int[] toRelevantFields(long timeMillis, int result[])
 {
  return toRelevantFields(newInstance(timeMillis), result);
 }


 public static int[] toRelevantFields(Calendar time)
 {
  return toRelevantFields(time, null);
 }


 public static int[] toRelevantFields(Calendar time, int result[])
 {
  int res[]=result;
  if (ArrayExtras.length(res)<8) res=new int[8];

  res[0]=time.get(Calendar.YEAR);
  res[1]=time.get(Calendar.MONTH);
  res[2]=time.get(Calendar.WEEK_OF_YEAR);
  res[3]=time.get(Calendar.DAY_OF_YEAR);
  res[4]=time.get(Calendar.HOUR_OF_DAY);
  res[5]=time.get(Calendar.MINUTE);
  res[6]=time.get(Calendar.SECOND);
  res[7]=time.get(Calendar.MILLISECOND);

  return res;
 }





 /**
  *
  * @param time
  * @param hmsm an array with hour, minutes, seconds, milliseconds
  * @return
  *
  * &gt; 0 if time hour, minutes, seconds, milliseconds is &gt; hmsm
  * 0 if time hour, minutes, seconds, milliseconds is == hmsm
  * &lt; 0 if time hour, minutes, seconds, milliseconds is &lt; hmsm
  *
  * day, months and all the rest are not evaluated
  *
  */
 public static int compareHourMinutesSecondsAndMillis(Calendar time, int[] hmsm)
 {
  int h=time.get(Calendar.HOUR_OF_DAY);
  int m=time.get(Calendar.MINUTE);
  int s=time.get(Calendar.SECOND);
  int ms=time.get(Calendar.MILLISECOND);
  int timeT=(h*60*60*1000)+(m*60*1000)+(s*1000)+ms;
  int hmsmT=(hmsm[0]*60*60*1000)+(hmsm[1]*60*1000)+(hmsm[2]*1000)+hmsm[3];

  return (timeT-hmsmT);
 }


 public static long millisDiff(Calendar time1, Calendar time2)
 {
  return (time1.getTimeInMillis()-time2.getTimeInMillis());
 }


 public static void copy(Calendar source, Calendar dest)
 {
  if (source!=null && dest!=null)
  {
   dest.set(Calendar.YEAR,        source.get(Calendar.YEAR       ));
   dest.set(Calendar.DAY_OF_YEAR, source.get(Calendar.DAY_OF_YEAR));
   dest.set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY));
   dest.set(Calendar.MINUTE,      source.get(Calendar.MINUTE     ));
   dest.set(Calendar.SECOND,      source.get(Calendar.SECOND     ));
   dest.set(Calendar.MILLISECOND, source.get(Calendar.MILLISECOND));
  }
 }



 public static Calendar newDay(int year, int month, int day)
 {
  return setYearMonthDay(setHourMinutesSecondsAndMillis((Calendar)_c_.clone(), 0, 0, 0, 0), year, month, day);
 }

 public static int hoursBetween(Calendar remote, Calendar recent)
 {
  return minutesBetween(remote, recent)/60;
 }

 public static int minutesBetween(Calendar remote, Calendar recent)
 {
  return (int)((recent.getTimeInMillis()-remote.getTimeInMillis())/60000L);
 }


 public static int daysBetween(Calendar remoteDay, Calendar recentDay)
 {
  int res=0;
  Calendar remote=trimToDay(clone(remoteDay));
  Calendar recent=trimToDay(clone(recentDay));

  if (remote.compareTo(recent)!=0)
  {
   if (remote.compareTo(recent)>0)
   {
    Calendar swap=remote;
    remote=recent;
    recent=swap;
   }

/*
   System.out.println(StringExtras.calendar2String(remote));
   System.out.println(StringExtras.calendar2String(recent));
   System.out.println("-------");
*/

   res=1+(int)((recent.getTimeInMillis()-remote.getTimeInMillis())/CalendarExtras.oneDay);

//   while (remote.compareTo(recent)!=0) {remote.add(Calendar.DAY_OF_YEAR, 1);res++;}
  }

  return res;
 }


 public static int indexOf(Calendar times[], Calendar timeToLookFor)
 {
  int res=-1;
  int t, len=ArrayExtras.length(times);

  for (t=0;t<len;t++)
  {
   if (times[t].compareTo(timeToLookFor)==0)
   {
    res=t;
    break;
   }
  }

  return res;
 }


 public static Calendar[] getFromDayToDay(Calendar allDays[], Calendar firstInclusiveDay, Calendar lastInclusiveDay)
 {
  int t, len=ArrayExtras.length(allDays);
  int firstIdx=-1;
  int lastIdx=len-1;
  boolean tooMuch;

  for (t=0;t<len;t++)
  {
   if (firstIdx==-1)
   {
    if (isSameDay(firstInclusiveDay, allDays[t]) || allDays[t].compareTo(firstInclusiveDay)>0)
    {
     firstIdx=t;
    }
   }

   if ((tooMuch=(allDays[t].compareTo(lastInclusiveDay)>0)) || CalendarExtras.isSameDay(lastInclusiveDay, allDays[t]))
   {
    lastIdx=t;
    if (tooMuch) lastIdx--;
    break;
   }
  }

  if (firstIdx<0) firstIdx=0;
  if (lastIdx<firstIdx) lastIdx=firstIdx;

  len=lastIdx-firstIdx+1;
  Calendar res[]=new Calendar[len];
  System.arraycopy(allDays, firstIdx, res, 0, len);
  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static boolean isBetween(Calendar time, Calendar startTime, Calendar endTime)
 {
  return (time.compareTo(startTime)>=0 && time.compareTo(endTime)<=0);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .



 public static int getCurrentYear()
 {
  return CalendarExtras.now().get(Calendar.YEAR);
 }






 // January is ZERO
 public static String getMonthName(int monthIndex)
 {
  return getMonthName(monthIndex, Locale.getDefault());
 }


 // January is ZERO
 public static String getMonthName(int monthIndex, Locale loc)
 {
  Calendar c=newInstance(2000, monthIndex, 1);
  SimpleDateFormat sdf=new SimpleDateFormat("MMMMMMMMMMMMMMMMMMMMM", loc);
  return sdf.format(c.getTime());
 }


 public static boolean areSameField(int field, Calendar c0, Calendar c1)
 {
  boolean res=false;

  if (c0!=null && c1!=null)
  {
   res=(c0.get(field)==c1.get(field));
  }

  return res;
 }


/*
 public static boolean areSameMonth(Calendar c0, Calendar c1)
 {
  return areSameField(Calendar.MONTH, c0, c1);
 }


 public static boolean areSameYear(Calendar c0, Calendar c1)
 {
  return areSameField(Calendar.YEAR, c0, c1);
 }
*/


 public static int getDaysInMonth(Calendar c)
 {
/*
  Calendar start=CalendarExtras.clone(c);
  start.set(Calendar.DAY_OF_MONTH, 1);

  Calendar end=CalendarExtras.clone(c);
  end.add(Calendar.DAY_OF_MONTH, 30);
  end.set(Calendar.DAY_OF_MONTH, 1);
  end.add(Calendar.DAY_OF_MONTH, -11);
*/

  Calendar start=CalendarExtras.clone(c);
  start.set(Calendar.DAY_OF_MONTH, 1);

  Calendar end=CalendarExtras.clone(start);
  end.add(Calendar.MONTH, 1);
  end.add(Calendar.DAY_OF_MONTH, -1);

  int res=daysBetween(start, end)+1;

  return res;
 }


 public static int getDaysToNextMonth(Calendar c)
 {
  Calendar end=CalendarExtras.clone(c);
  end.add(Calendar.DAY_OF_MONTH, 30);
  end.set(Calendar.DAY_OF_MONTH, 1);
  end.add(Calendar.DAY_OF_MONTH, -1);

  int res=daysBetween(c, end);
  return res;
 }


 public static Calendar getLastDayOfPreviousMonth(Calendar c)
 {
  Calendar res=clone(c);

  while (res.get(Calendar.MONTH)==c.get(Calendar.MONTH))
  {
   res.add(Calendar.DAY_OF_YEAR, -1);
  }

  return res;
 }

 public static String getDayOfWeekShortName(Calendar cal, Locale loc) {

  DateFormatSymbols symbols = new DateFormatSymbols(loc);
  String[] dayNames = symbols.getShortWeekdays();
  String res = dayNames[cal.get(Calendar.DAY_OF_WEEK)];

  return res;

 }


}
