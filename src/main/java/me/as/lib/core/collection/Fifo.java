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


import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ObjectExtras;

import java.util.*;
import java.util.concurrent.atomic.*;

import static me.as.lib.core.lang.ExceptionExtras.systemErr;
import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;


public class Fifo<E> extends Pile<E>
{
 private static final String noNullPlease="'null' is not supported!";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected int elemCount=0;
 protected int zeroIndex=0;
 protected int maxSizeLimit=Integer.MAX_VALUE;
 private AtomicLong lastSignalMillis=new AtomicLong(Long.MIN_VALUE);
 private AtomicLong lastSignalAllMillis=new AtomicLong(Long.MIN_VALUE);



 public Fifo()
 {
  super();
 }

 public Fifo(int initCapacity)
 {
  super(initCapacity);
 }


 public Fifo(int initCapacity, int maxSizeLimit)
 {
  super(initCapacity);
  this.maxSizeLimit=maxSizeLimit;
 }




 public synchronized int getMaxSizeLimit()
 {
  return maxSizeLimit;
 }


 public synchronized void setMaxSizeLimit(int maxSizeLimit)
 {
  this.maxSizeLimit=maxSizeLimit;
  checkMaxSizeLimit();
 }


 private void checkMaxSizeLimit()
 {
  while (size()>maxSizeLimit) get();
 }



 public synchronized int size()
 {
  return elemCount;
 }

 public synchronized void clear()
 {
  super.clear();
  elemCount=0;
  zeroIndex=0;
 }


 private int preparePut(int howMany)
 {
  if (zeroIndex+elemCount+howMany>elements.length)
  {
   int allRequired=elemCount+howMany;

   if (allRequired>elements.length)
   {
    extend((allRequired-elements.length)+getDefaultIncrement());
   }
   else
   {
    System.arraycopy(elements, zeroIndex, elements, 0, elemCount);
    zeroIndex=0;
   }
  }

  return zeroIndex+elemCount;
 }


 protected synchronized void extend(int increment)
 {
  Object newElements[]=new Object[elements.length+increment];
  System.arraycopy(elements, zeroIndex, newElements, 0, elemCount);
  elements=newElements;
  zeroIndex=0;
 }


 public synchronized void putAheadOfAll(E o)
 {
  if (o==null) throw new RuntimeException(noNullPlease);

  preparePut(1);

  if (elemCount>0)
  {
   System.arraycopy(elements, zeroIndex, elements, zeroIndex+1, elemCount);
  }

  elements[zeroIndex]=o;
  elemCount++;
  onElementInserted(o);

  checkMaxSizeLimit();

  notify();
 }


 public synchronized void put(E o[], int off, int len)
 {
  if (o==null) throw new RuntimeException(noNullPlease);

  if (len>0)
  {
   int idx=preparePut(len);
   int end=idx+len;

   for (int io=off;idx<end;idx++,io++)
   {
    if (o[io]==null) throw new RuntimeException(noNullPlease);
    elements[idx]=o[io];
    elemCount++;
    onElementInserted(o[io]);
   }

   checkMaxSizeLimit();

   notify();
  }
 }


 public synchronized void put(E o[])
 {
  put(o, 0, ArrayExtras.length(o));
 }


 public synchronized void put(E o, int howManyTimes)
 {
  if (o==null) throw new RuntimeException(noNullPlease);

  if (howManyTimes>0)
  {
   int idx=preparePut(howManyTimes);
   int end=idx+howManyTimes;

   for (;idx<end;idx++)
   {
    elements[idx]=o;
    elemCount++;
    onElementInserted(o);
   }

   checkMaxSizeLimit();

   notify();
  }

 }



 public synchronized void put(E o)
 {
  if (o==null) throw new RuntimeException(noNullPlease);

  int idx=preparePut(1);
  elements[idx]=o;
  elemCount++;
  onElementInserted(o);

  checkMaxSizeLimit();

  notify();
 }



 public synchronized void signal()
 {
  lastSignalMillis.set(System.nanoTime());
  notify();
 }


 public synchronized void signalAll()
 {
  lastSignalAllMillis.set(System.nanoTime());
  notifyAll();
 }





 // WARNING: this method leaves the returned Object in the Fifo
 public synchronized E whoIsTheNext()
 {
  return (E)((elemCount>0) ? elements[zeroIndex]:null);
 }




 private synchronized boolean hasBeenNotified(long nano)
 {
  boolean res=false;
  long lsm=lastSignalMillis.get();

  if (lsm!=Long.MIN_VALUE && lsm-nano>0)
  {
   res=true;
   lastSignalMillis.set(Long.MIN_VALUE);
  }

  if (!res)
  {
   lsm=lastSignalAllMillis.get();

   if (lsm!=Long.MIN_VALUE && lsm-nano>0)
   {
    res=true;
   }
  }

  return res;
 }





 private synchronized E waitIt(boolean getting, long millis) throws InterruptedException
 {
  boolean notified=false;
  Long nano=System.nanoTime();
  Object res;
  long allNanos=millis*1000000;

  do
  {
   synchronized (this)
   {
    res=(getting ? get()
                 : whoIsTheNext());

    if (res==null && !notified)
    {
     if (millis>=0)
     {
      if (millis>0)
      {
       if (millis>1000000)
       {
        try
        {
         throw new StillUnimplemented();
        }
        catch (Throwable tr)
        {
         systemErrDeepCauseStackTrace(tr);
         systemErr("---------> MILLIS = "+millis, false);
        }
       }


//       try {wait(millis);} catch (Throwable tr){tr.printStackTrace();}
       wait(millis);
       millis=0;
      }
      else
      {
       // ??? this is just because of ----> System.nanoTime()-nano<millis*1000000));
//       try {wait(25);} catch (Throwable tr){tr.printStackTrace();}
       wait(25);
      }
     }
     else
     {
//      try {wait();} catch (Throwable tr){tr.printStackTrace();}
      wait();
     }

     notified=hasBeenNotified(nano);
    }
   }
  } while (res==null && !notified &&
           (millis<0 || System.nanoTime()-nano<allNanos));

  return (E)res;
 }




 // WARNING: this method leaves the returned Object in the Fifo
 public synchronized E waitNext()
 {
  try
  {
   return waitIt(false, -1);
  }
  catch (InterruptedException ie)
  {
   return null;
  }
 }



 public E getWaiting()
 {
  try
  {
   return waitIt(true, -1);
  }
  catch (InterruptedException ie)
  {
   return null;
  }
 }



 // WARNING: this method leaves the returned Object in the Fifo
 public synchronized E waitNext(long millis)
 {
  try
  {
   return waitIt(false, millis);
  }
  catch (InterruptedException ie)
  {
   return null;
  }
 }



 public E getWaiting(long millis)
 {
  try
  {
   return waitIt(true, millis);
  }
  catch (InterruptedException ie)
  {
   return null;
  }
 }




 public synchronized E get()
 {
  Object res=null;

  if (elemCount>0)
  {
   res=elements[zeroIndex];
   elements[zeroIndex]=null;
   elemCount--;
   zeroIndex++;
   onElementRemoved((E)res);
  }

  return (E)res;
 }

 // this is CONTRARY to what you expect from a FIFO ! ! !
 // the first element enumerated is the last added using put(...)
 // so the last element enumerated was the first added using put(...)
 // NOTE: elements are not removed from the Fifo
 public synchronized Enumeration<E> elements()
 {
  Vector<E> v=new Vector<E>();
  int t=size();

  while (t>0)
  {
   t--;
   v.addElement((E)elements[zeroIndex+t]);
  }

  return v.elements();
 }


 // this is what you expect from a FIFO
 // the first element enumerated is the FIRST added using put(...)
 // so the last element enumerated was the LAST added using put(...)
 public synchronized Enumeration<E> getAllAndClear()
 {
  Vector<E> v=new Vector<E>();
  int t=0, len=size();

  while (t<len)
  {
   v.addElement((E)elements[zeroIndex+t]);
   t++;
  }

  clear();

  return v.elements();
 }




 // this is what you expect from a FIFO
 // the first element put in list is the FIRST added using put(...)
 public synchronized int getSomeRemoving(int maxElementsToReturn, ArrayList list)
 {
  int res=0;
  Object e;

  while (elemCount>0 && res<maxElementsToReturn)
  {
   e=elements[zeroIndex];
   list.add(e);
   elements[zeroIndex]=null;
   elemCount--;
   zeroIndex++;
   res++;
   onElementRemoved((E)e);
  }

  return res;
 }






 /**
  *
  * @param idx
  * @return  for idx==0 returns the oldest added element
  */
 public synchronized E elementAt(int idx)
 {
  return (E)elements[zeroIndex+idx];
 }




 public synchronized boolean contains(E o)
 {
  boolean res=false;
  int t=size();

  while (t>0 && !res)
  {
   t--;
   res=ObjectExtras.areEqual(elements[zeroIndex+t], o);
  }

  return res;
 }


 public synchronized boolean areAllEqual(E o)
 {
  boolean res=true;
  int t=size();

  while (t>0 && res)
  {
   t--;
   res=(elements[zeroIndex+t]==o);
  }

  return res;
 }


 public synchronized E remove(E o)
 {
  Object res=((contains(o))?o:null);

  if (res!=null)
  {
   boolean passed=false;
   int t=0, len=size();

   while (t<len)
   {
    if (t==len-1) elements[zeroIndex+t]=null;
    else
    {
     if (!passed) passed=(elements[zeroIndex+t]==o);
     if (passed) elements[zeroIndex+t]=elements[zeroIndex+t+1];
    }

    t++;
   }

   elemCount--;
   onElementRemoved((E)res);
  }

  return (E)res;
 }



 // element zero is the oldest one (first added using put(...))
 // can't be done better?????
 public synchronized E[] toArray(E array[])
 {
  int i=0;
  for (Enumeration<E> e=elements();e.hasMoreElements();) array[i++]=e.nextElement();
  return (E[])ArrayExtras.reverse(array);
 }



}
