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


import me.as.lib.core.collection.SimpleWeakList;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.lang.ArrayExtras;

import javax.swing.*;


public abstract class LazyRefresher
{
 private long millis;
 private long lastNeedRefreshMillis=-1;
 private long lastRefreshedMillis=-1;
 private long firstNeedRefreshMillis=-1;
 private boolean busy=false;
 private boolean refreshAgain=false;

 // if reactive==true then the refresh happens immediately
 // (without waiting millis) when needrefresh is called for the first time
 private boolean reactive=true;

 private long maxLatency=-1;
 private boolean callOnAwtEventDispatchThread=false;


 public LazyRefresher()
 {
  this(1000);
 }


 public LazyRefresher(long millis)
 {
  this(millis, -1);
 }


 public LazyRefresher(long millis, long maxLatency)
 {
  setLazynessMillis(millis);
  lastRefreshedMillis=System.currentTimeMillis();
  registerLazyRefresher(this);
  setMaxLatency(maxLatency);
 }


 public LazyRefresher(long millis, boolean callOnAwtEventDispatchThread)
 {
  this(millis);
  this.callOnAwtEventDispatchThread=callOnAwtEventDispatchThread;
 }


 public LazyRefresher(long millis, long maxLatency, boolean callOnAwtEventDispatchThread)
 {
  this(millis, maxLatency, true, callOnAwtEventDispatchThread);
 }


 public LazyRefresher(long millis, long maxLatency, boolean reactive, boolean callOnAwtEventDispatchThread)
 {
  this(millis, maxLatency);
  this.reactive=reactive;
  this.callOnAwtEventDispatchThread=callOnAwtEventDispatchThread;
 }



// lazyUpdater.setMaxLatency(def_lazyUpdaterMaxLatency);
// public void dispose()

 public long getMaxLatency()
 {
  return maxLatency;
 }


 public void setMaxLatency(long maxLatency)
 {
  this.maxLatency=maxLatency;
 }



 public void dispose()
 {
  unregisterLazyRefresher(this);
 }


 public synchronized void setLazynessMillis(long millis)
 {
  this.millis=millis;
 }

 public synchronized long getLazynessMillis()
 {
  return millis;
 }



 public synchronized void reset()
 {
  lastRefreshedMillis=-1;
  lastNeedRefreshMillis=-1;
  firstNeedRefreshMillis=-1;
 }


 public synchronized boolean doesNeedRefresh()
 {
  return (firstNeedRefreshMillis!=-1);
 }

 public synchronized void needRefresh()
 {
  lastNeedRefreshMillis=System.currentTimeMillis();
  if (firstNeedRefreshMillis==-1) firstNeedRefreshMillis=lastNeedRefreshMillis;
 }


 public synchronized long getLastNeedRefreshMillis()
 {
  return lastNeedRefreshMillis;
 }


 //protected synchronized void goDoRefresh(long computedMillis)
 protected void goDoRefresh(long computedMillis)
 {
  boolean entered=false;
  boolean ib=false;

  synchronized (this)
  {
   if (lastNeedRefreshMillis==computedMillis)
   {
    ib=isBusy();
    if (!ib)
    {
     lastNeedRefreshMillis=-1;
     entered=true;
    }
   }
  }

  if (entered) i_refresh();

  synchronized (this)
  {
   if (entered)
   {
    lastRefreshedMillis=System.currentTimeMillis();
    firstNeedRefreshMillis=-1;
   }

   if (ib) refreshAgain=true;
  }


  /*
  if (lastNeedRefreshMillis==computedMillis)
  {
   if (!isBusy())
   {
    lastNeedRefreshMillis=-1;
    i_refresh();
    lastRefreshedMillis=System.currentTimeMillis();
    firstNeedRefreshMillis=-1;
   } else refreshAgain=true;
  }
  */

 }


 //public synchronized void refreshImmediately()
 public void refreshImmediately()
 {
  goDoRefresh(lastNeedRefreshMillis);
 }



 //public synchronized void setBusy(boolean b)
 public void setBusy(boolean b)
 {
  boolean goon=false;

  synchronized (this)
  {
   busy=b;
   if (!busy && refreshAgain)
   {
    refreshAgain=false;
    goon=true;
   }
  }

  if (goon) goDoRefresh(lastNeedRefreshMillis);



  /*
  busy=b;
  if (!busy && refreshAgain)
  {
   refreshAgain=false;
   goDoRefresh(lastNeedRefreshMillis);
//   System.out.println("---------] refreshAgain [---------!!!!!!!!!!!!!!!!!!!!");
  }
  */



 }





 public synchronized boolean isBusy()
 {
  return busy;
 }

 private Runnable runner=null;

 protected void i_refresh()
 {
  if (callOnAwtEventDispatchThread)
  {
   if (!SwingUtilities.isEventDispatchThread())
   {
    if (runner==null) runner=new Runnable(){public void run(){ii_refresh();}};

    SwingUtilities.invokeLater(runner);
    //try{SwingUtilities.invokeAndWait(runner);}catch (Throwable tr){tr.printStackTrace();}

   } else ii_refresh();
  } else ii_refresh();
 }


 protected void ii_refresh()
 {
  try
  {
   refresh();
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }
 }

 protected abstract void refresh();

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 private static LazyRefresherSupportThread lrst=null;
// private static long lastSleepAmount=-1;

 private static void registerLazyRefresher(LazyRefresher lr)
 {
  synchronized (LazyRefresher.class)
  {
   if (lrst==null) lrst=new LazyRefresherSupportThread();
   lrst.register(lr);
  }
 }

 private static void unregisterLazyRefresher(LazyRefresher lr)
 {
  synchronized (LazyRefresher.class)
  {
   if (lrst==null) lrst=new LazyRefresherSupportThread();
   lrst.unregister(lr);
  }
 }




 static class LazyRefresherSupportThread implements Runnable
 {
  SimpleWeakList lazyRefreshers=new SimpleWeakList();

  LazyRefresherSupportThread()
  {
   Thread t=new Thread(this, "LazyRefresherSupportThread");
   t.setDaemon(true);
   t.start();
  }

  public void register(LazyRefresher lr)
  {
   if (lazyRefreshers.contains(lr))
   {
    throw new RuntimeException("Cannot register more than one time the same LazyRefresher instance!");
   }
   else
   {
//    lastSleepAmount=-1;
    lazyRefreshers.add(lr);
   }
  }

  public void unregister(LazyRefresher lr)
  {
   if (lazyRefreshers.contains(lr))
   {
    lazyRefreshers.remove(lr);
   }
  }



  public void run()
  {
   while (true)
   {
    ThreadExtras.sleep(checkLazyRefresher());
   }
  }


  private long checkLazyRefresher()
  {
//   if (lastSleepAmount==-1)
//   {
    long res=1000;
    long millidiff, lazym, lnrm, currentMillis=System.currentTimeMillis();
    Object lazyes[];

    synchronized (LazyRefresher.class)
    {
     lazyes=lazyRefreshers.elements();
    }

    LazyRefresher lr;
    int t, len=ArrayExtras.length(lazyes);

    for (t=0;t<len;t++)
    {
     lr=(LazyRefresher)lazyes[t];
     lnrm=lr.getLastNeedRefreshMillis();
     lazym=lr.getLazynessMillis();

     if (res>lazym) res=lazym;

     if (lnrm>-1)
     {
      millidiff=lazym-(currentMillis-lnrm);

      boolean b0=millidiff<0;
      boolean b1=currentMillis-lr.lastRefreshedMillis>lazym && lr.reactive;
      boolean b1b=currentMillis-lnrm>lazym;
      boolean b011b=b0 || b1 || b1b;
      boolean b2=lr.maxLatency!=-1;
      boolean b3=lr.firstNeedRefreshMillis!=-1;
      boolean b4=currentMillis-lr.firstNeedRefreshMillis>lr.maxLatency;
      boolean b234=b2 && b3 && b4;
      boolean finalB=b011b || b234;

//      if (millidiff<0)
//      if ((millidiff<0 || currentMillis-lr.lastRefreshedMillis>lazym) ||
//          (lr.maxLatency!=-1 && lr.firstNeedRefreshMillis!=-1 && currentMillis-lr.firstNeedRefreshMillis>lr.maxLatency))
      if (finalB)
      {
       lr.goDoRefresh(lnrm);
      }
      else
      {
       if (res>millidiff) res=millidiff;
      }
     }
    }

//    lastSleepAmount=res;
//   }

//   System.out.println("checkLazyRefresher: "+res);

//   return lastSleepAmount;
   return res;
  }

 }



}
