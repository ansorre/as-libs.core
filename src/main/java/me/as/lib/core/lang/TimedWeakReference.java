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


@SuppressWarnings({"UnusedDeclaration"})
public class TimedWeakReference<T>
{
 private T strong;
 private WeakReference<T> weak;
 private long millisToWeakness;
 private long lastAccessMillis;
 private long createMillis;


 public TimedWeakReference(T referent)
 {
  this(referent, 5);
 }

 public TimedWeakReference(T referent, int secondsToWeakness)
 {
  strong=referent;
  weak=new WeakReference(referent);
  this.millisToWeakness=secondsToWeakness*1000;
  createMillis=lastAccessMillis=System.currentTimeMillis();
  theWeakener.add(this);
 }


 public T get()
 {
  lastAccessMillis=System.currentTimeMillis();
  return weak.get();
 }

 public boolean isOlderThanSeconds(int seconds)
 {
  return (System.currentTimeMillis()-createMillis>seconds*1000);
 }

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private static class Weakener extends LazyRefresher
 {
  private ArrayList<TimedWeakReference> timedWeakReferences=new ArrayList<TimedWeakReference>();
  private ArrayList<TimedWeakReference> saved=new ArrayList<TimedWeakReference>();


  private Weakener()
  {
   super(1000, false);
   needRefresh();
  }


  protected void refresh()
  {
   synchronized (theWeakener)
   {
    int len=timedWeakReferences.size();

    if (len>0)
    {
     int t;
     TimedWeakReference twr;
     long now=System.currentTimeMillis();

     for (t=0;t<len;t++)
     {
      twr=timedWeakReferences.get(t);

      if (twr.weak.get()!=null)
      {
       if (now-twr.lastAccessMillis<twr.millisToWeakness) saved.add(twr);
       else twr.strong=null;
      }
     }

     if (saved.size()!=len)
     {
//      System.out.println("theWeakener weakened "+(len-saved.size())+" TimedWeakReferences...");
      timedWeakReferences=saved;
      saved=new ArrayList<TimedWeakReference>();
     } else saved.clear();
    }
   }

   needRefresh();
  }


  private void add(TimedWeakReference twr)
  {
   synchronized (theWeakener)
   {
    timedWeakReferences.add(twr);
   }
  }

 }

 private static final Weakener theWeakener=new Weakener();

}
