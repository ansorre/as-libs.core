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


import java.util.concurrent.atomic.*;


public class SimpleSynchro
{
 private int signaled=0;
 private AtomicInteger waiting=new AtomicInteger(0);


 public int getSignaledCount()
 {
  int res;
  synchronized (this) {res=signaled;}
  return res;
 }

 public void resetSignaled()
 {
  synchronized (this) {signaled=0;}
 }


 public boolean waitFor()
 {
  return waitFor(-1);
 }


 public boolean waitFor(long timeout)
 {
  waiting.incrementAndGet();
  boolean res;

  try
  {
   synchronized (this)
   {
    if (signaled==0)
    {
     if (timeout>=0) wait(timeout);
     else wait();

     synchronized (this)  // because wait releases ownership of the monitor!
     {
      if (res=(signaled!=0))
      {
       signaled--;
      }
     }
    }
    else
    {
     res=true;
     signaled--;
    }
   }
  }
  catch (Throwable tr)
  {
   res=false;
  }

  waiting.decrementAndGet();
  return res;
 }


 public boolean signal()
 {
  return signal(1);
 }


 public boolean signal(int howManyTimes)
 {
  boolean res=false;

  try
  {
   synchronized (this)
   {
    signaled+=howManyTimes;
    for (int t=0;t<howManyTimes;t++) notify();
    res=true;
   }
  }
  catch (Throwable tr)
  {
   res=false;
  }

  return res;
 }


 public boolean signalAll()
 {
  return signal(Math.max(1, waiting.get()));
 }


 /**
  *
  * NOTE: this does not signal if there are no waiters
  *
  * @return false if there where no waiters otherwise return what signal would
  */
 public boolean signalAllWaiters()
 {
  int w=waiting.get();
  if (w>0) return signal(w);
  return false;
 }


}




