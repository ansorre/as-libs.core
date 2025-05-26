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

package me.as.lib.core.collection;

import me.as.lib.core.lang.ArrayExtras;

import java.util.*;


public class SortedList<E extends Comparable>
{
 public static final int ASCENDING = 0;
 public static final int DESCENDING = 1;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private ArrayList<E> list;
 private int order=ASCENDING;

 public SortedList()
 {
  list=new ArrayList<E>();
 }

 public SortedList(int initialCapacity)
 {
  list=new ArrayList<E>(initialCapacity);
 }

 public SortedList(Collection<? extends E> c)
 {
  list=new ArrayList<E>(c);
 }



 public int getOrder()
 {
  return order;
 }


 /**
  *
  * @param order      SortedList.ASCENDING or SortedList.DESCENDING
  */
 public void setOrder(int order)
 {
  if (this.order!=order)
  {
   this.order=order;
   int s=size();

   if (s>1)
   {
    Object arr=ArrayExtras.reverse(list.toArray());
    list.clear();
    ArrayExtras.addAll(list, arr);
   }
  }
 }






 public int size()
 {
  return list.size();
 }


 public void clear()
 {
  list.clear();
 }


 /**
  *
  * @param e1
  * @param e2
  * @return   -1 if e1 should be before e2 according to the current sort 'order'
  *            1 if e1 should be after e2 according to the current sort 'order'
  *            0 if (e1 equals e2)
  */
 private int whereShouldSeat(E e1, E e2)
 {
  int res, c=e1.compareTo(e2);
  if (c<0) res=-1;
  else
  {
   if (c>0) res=1;
   else res=0;
  }

  if (order==DESCENDING) res*=-1;
  return res;
 }


 public boolean add(E e)
 {
  boolean res=false;
  int s=list.size();

  if (s==0) res=list.add(e);
  else
  {
   int seat=whereShouldSeat(e, get(0));

   if (seat==-1)
   {
    list.add(0, e);
    res=true;
   }
   else
   {
    seat=whereShouldSeat(e, get(s-1));
    if (seat==1)
    {
     list.add(e);
     res=true;
    }
    else
    {
     int up=0;
     int dn=s;
     int cur=(up+dn)/2;

     do
     {
      seat=whereShouldSeat(e, get(cur));

      if (seat!=0)
      {
       if (seat>0) up=cur;
       else {if (seat<0) dn=cur;}
       cur=(up+dn)/2;
      }
     } while (seat!=0 && cur!=dn && cur!=up);

     list.add(cur+1, e);
     res=true;
    }
   }
  }

  return res;
 }


 public E remove(int index)
 {
  return list.remove(index);
 }


 public boolean remove(E e)
 {
  return list.remove(e);
 }


 // @todo must be speeded using the same kind of search used in 'public boolean add(E e)'
 public int indexOf(E e)
 {
  return list.indexOf(e);
 }


 public E get(int index)
 {
  return list.get(index);
 }

 public E[] toArray(E[] arr)
 {
  return list.toArray(arr);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


/*
 // test: SUCCESS
 public static void main(String args[])
 {
//  final SortedList<String> l=new SortedList<String>();
  final SortedList<Double> l=new SortedList<Double>();

  Thread tt=new Thread(new Runnable(){

   private void deb_showlist()
   {
    System.out.println("\n");
//    String arr[]=l.toArray(new String[l.size()]);
    Double arr[]=l.toArray(new Double[l.size()]);
    int t, len=ArrayExtras.length(arr);

    for (t=0;t<len;t++)
    {
     System.out.println(arr[t]);
    }
   }

   public void run()
   {
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

    try
    {
     boolean running=true;
     String line;

     while (running)
     {
      line=br.readLine();

      if (line.toUpperCase().equals("QUIT")) running=false;
      else
      {
       if (line.equals("^"))
       {
        int o=l.getOrder();
        if (o==ASCENDING) l.setOrder(DESCENDING);else l.setOrder(ASCENDING);
        deb_showlist();
       }
       else
       {
//        l.add(line);
        l.add(Double.parseDouble(line));
        deb_showlist();
       }
      }
     }
    }
    catch (java.io.IOException e)
    {
     e.printStackTrace();
    }
   }
  });


  tt.start();
 }

*/


}
