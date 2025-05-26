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


import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.StringExtras;

import java.util.*;

import static me.as.lib.core.lang.StringExtras.getFirstEnclosedInteger;
import static me.as.lib.core.lang.StringExtras.speedStringToDate;


public class QuickSortExtras
{

 public static double[] sort(final double values[])
 {
  int len=((values!=null)?values.length:0);

  if (len>0)
  {
   new QuickSort(new QSortable()
   {
    double mid;
    public void setMid(int mididx, Object params)
    {
     mid=values[mididx];
    }

    // must return:
    // <0 if elem1<mid
    // 0 if elem1==mid
    // >0 if elem1>mid
    public int compareToMid(int elem1, Object params)
    {
     if (values[elem1]>mid) return  1;
     if (values[elem1]<mid) return -1;

     return 0;
    }


    public boolean swap(int elem1, int elem2, Object params)
    {
     double swap=values[elem1];
     values[elem1]=values[elem2];
     values[elem2]=swap;
     return true;
    }
   }, 0, len-1, null);
  }

  return values;
 }


 public static int[] sort(final int values[])
 {
  int len=((values!=null)?values.length:0);

  if (len>0)
  {
   new QuickSort(new QSortable()
   {
    int mid;
    public void setMid(int mididx, Object params)
    {
     mid=values[mididx];
    }

    // must return:
    // <0 if elem1<mid
    // 0 if elem1==mid
    // >0 if elem1>mid
    public int compareToMid(int elem1, Object params)
    {
     if (values[elem1]>mid) return  1;
     if (values[elem1]<mid) return -1;

     return 0;
    }


    public boolean swap(int elem1, int elem2, Object params)
    {
     int swap=values[elem1];
     values[elem1]=values[elem2];
     values[elem2]=swap;
     return true;
    }
   }, 0, len-1, null);
  }

  return values;
 }



 public static String[] sort(String str[])
 {
  return sort(str, false);
 }

 public static Calendar[] sort(final Calendar times[])
 {
  int len=ArrayExtras.length(times);

  if (len>1)
  {
   new QuickSort(new QSortable()
   {
    Calendar mid;

    public void setMid(int mididx, Object params)
    {
     mid=times[mididx];
    }

    // must return:
    // <0 if elem1<mid
    // 0 if elem1==mid
    // >0 if elem1>mid
    public int compareToMid(int elem1, Object params)
    {
     return times[elem1].compareTo(mid);
    }

    public boolean swap(int elem1, int elem2, Object params)
    {
     Calendar tmp=times[elem1];
     times[elem1]=times[elem2];
     times[elem2]=tmp;
     return true;
    }
   }, 0, len-1, null);
  }

  return times;
 }


 public static String[] sortStringsForLength(String str[], final boolean longerStringsUpper)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     String mid;

     public void setMid(int mididx, Object params)
     {
      mid=((String[])params)[mididx];
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      int res=0;
      String cur=((String[])params)[elem1];
      int e1Len=StringExtras.length(cur);
      int midLen=StringExtras.length(mid);

      if (e1Len<midLen) res=-1;
      else {if (e1Len>midLen) res=1;}

      if (res!=0 && longerStringsUpper) res*=-1;
      return res;
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }



 public static String[] sort(String str[], final boolean caseSensitive)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     String mid;

     public void setMid(int mididx, Object params)
     {
      mid=((String[])params)[mididx];
      if (!caseSensitive) mid=mid.toUpperCase();
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      String cur=((String[])params)[elem1];
      return ((caseSensitive)?cur.compareTo(mid):cur.toUpperCase().compareTo(mid));
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }




 public static String[] sortStrictIntegersInStrings(String[] str)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     int mid;

     public void setMid(int mididx, Object params)
     {
      mid=Integer.parseInt(((String[])params)[mididx]);
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      int curr;
      curr=Integer.parseInt(((String[])params)[elem1]);

      if (curr<mid) return -1;
      if (curr>mid) return 1;
      return 0;
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }




 public static String[] sortIntegersInStrings(String[] str)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     int mid;

     public void setMid(int mididx, Object params)
     {
      try
      {
       mid=Integer.parseInt(((String[])params)[mididx]);
      } catch (Throwable tr){mid=0;}
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      int curr;
      try
      {
       curr=Integer.parseInt(((String[])params)[elem1]);
      } catch (Throwable tr){curr=0;}

      if (curr<mid) return -1;
      if (curr>mid) return 1;
      return 0;
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }



 public static String[] sortIntegersEnclosedInStrings(String[] str)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     int mid;

     public void setMid(int mididx, Object params)
     {
      try
      {
       mid=getFirstEnclosedInteger(((String[])params)[mididx]);
      } catch (Throwable tr){mid=0;}
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      int curr;
      try
      {
       curr=getFirstEnclosedInteger(((String[])params)[elem1]);
      } catch (Throwable tr){curr=0;}

      if (curr<mid) return -1;
      if (curr>mid) return 1;
      return 0;
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }



 public static String[] sortDatesInStrings(String[] str, final String dateFormat)
 {
  if (str!=null)
  {
   if (str.length>1)
   {
    new QuickSort(new QSortable()
    {
     Date mid;

     public void setMid(int mididx, Object params)
     {
      try
      {
       mid=speedStringToDate(((String[])params)[mididx], dateFormat);
      } catch (Throwable tr){mid=new Date();}
     }

     // must return:
     // <0 if elem1<mid
     // 0 if elem1==mid
     // >0 if elem1>mid
     public int compareToMid(int elem1, Object params)
     {
      int res;
      Date curr;

      try
      {
       //System.out.print("elem1="+((String[])params)[elem1]+"  ");

       curr=speedStringToDate(((String[])params)[elem1], dateFormat);
      } catch (Throwable tr){curr=new Date();tr.printStackTrace();}

      if (curr.before(mid)) res=-1;
      else
      {
       if (curr.after(mid)) res=1;
       else res=0;
      }

      /*
      System.out.print("df="+dateFormat+"  ");

      switch (res)
      {
       case 0:System.out.println(curr.toString()+" = "+mid.toString());break;
       case 1:System.out.println(curr.toString()+" > "+mid.toString());break;
       case -1:System.out.println(curr.toString()+" < "+mid.toString());break;
      }
      */

      return res;
     }

     public boolean swap(int elem1, int elem2, Object params)
     {
      return swapStrings(elem1, elem2, (String[])params);
     }
    }, 0, str.length-1, str);
   }
  }

  return str;
 }


 private static boolean swapStrings(int elem1, int elem2, String params[])
 {
  String cur1=params[elem1];
  params[elem1]=params[elem2];
  params[elem2]=cur1;

  return true;
 }





}
