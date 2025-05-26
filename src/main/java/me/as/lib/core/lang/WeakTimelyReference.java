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


import me.as.lib.core.extra.LazyRefresher;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.atomic.*;


/**
 *
 * This is a strong reference to an Objcet instance of type E for the first
 * millisToWeak milliseconds after which this becomes a WeakReference
 *
 */
public class WeakTimelyReference<E>
{
 private static final LazyRefresher weaker;
 private static final List<WeakTimelyReference> monitored=new LinkedList<WeakTimelyReference>();

 static
 {
  weaker=new LazyRefresher(20000)
  {
   protected void refresh()
   {
    scannAll();
   }
  };
  weaker.needRefresh();
 }

 private static void scannAll()
 {
  synchronized (monitored)
  {
//   System.out.println("WeakTimelyReference scannAll() monitored.size: "+monitored.size());
//   int deb_removed=0;

   long now=System.currentTimeMillis();
   WeakTimelyReference wtr;
   int t, len=ArrayExtras.length(monitored);

   for (t=0;t<len;t++)
   {
    wtr=monitored.get(t);

    synchronized (wtr)
    {
     if (wtr.weakAtMillis.get()<=now)
     {
      monitored.remove(t);

      wtr.weakReference=new WeakReference(wtr.referent);
      wtr.referent=null;

//      deb_removed++;

      t--;
      len--;
     }
    }
   }

//   System.out.println("WeakTimelyReference removed "+deb_removed);
  }

  weaker.needRefresh();
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private E referent;
 private WeakReference<E> weakReference=null;
 private long millisToWeak;
 private AtomicLong weakAtMillis=new AtomicLong(0);


 public WeakTimelyReference(E referent, long millisToWeak)
 {
  this.referent=referent;
  this.millisToWeak=millisToWeak;
  restart();

  synchronized (monitored)
  {
   monitored.add(this);
  }
 }


 public synchronized E get()
 {
  if (weakReference!=null) return weakReference.get();
  return referent;
 }


 public void restart()
 {
  if (get()!=null) weakAtMillis.set(System.currentTimeMillis()+millisToWeak);
 }


 public void dispose()
 {
  synchronized (monitored)
  {
   monitored.remove(this);
   referent=null;
   weakReference=null;
  }
 }

}
