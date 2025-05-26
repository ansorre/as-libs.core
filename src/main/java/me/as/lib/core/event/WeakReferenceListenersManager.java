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

package me.as.lib.core.event;


import me.as.lib.core.lang.SmartWeakReference;

import java.util.*;

import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;


/*

 // Listeners management

 protected WeakReferenceListenersManager listenersManager=new WeakReferenceListenersManager();

 public void add_xxx_Listener(_xxx_Listener listener)
 {listenersManager.addListener(listener);}

 public void remove_xxx_Listener(_xxx_Listener listener)
 {listenersManager.removeListener(listener);}

 Firer firer=new Firer()
 {public void foreachAction(EventListener listener, EventObject param)
  {((_xxx_Listener)listener)._Xxx_EventOccurred((_xxx_Event)param);}};

 public void fire_xxx_EventOccurred(_xxx_Event e)
 {listenersManager.foreachListener(firer, e);}

 // END - Listeners management

*/


public class WeakReferenceListenersManager<L extends EventListener>
{
 private static final long millisBetweenPurges=30000;

 private long lastPurgeMillis=0;
 private ArrayList<SmartWeakReference<L>> listeners=new ArrayList<>();


 private synchronized void purgeZombies(boolean force)
 {
  long currentTimeMillis=System.currentTimeMillis();
  if (force || currentTimeMillis-lastPurgeMillis>millisBetweenPurges)
  {
   lastPurgeMillis=currentTimeMillis;
   int vlen=0, len=listeners.size();

   for (Iterator<L> lins=getListeners();lins.hasNext();)
   {
    lins.next();
    vlen++;
   }

   if (vlen!=len)
   {
    ArrayList<SmartWeakReference<L>> newListeners=new ArrayList<SmartWeakReference<L>>();

    for (Iterator<L> lins=getListeners();lins.hasNext();)
    {
     newListeners.add(new SmartWeakReference<L>(lins.next()));
    }

    listeners=newListeners;
   }
  }
 }


 public synchronized void addListener(L listener)
 {
  SmartWeakReference tmp=new SmartWeakReference(listener);
  if (!listeners.contains(tmp)) listeners.add(tmp);
 }

 public synchronized void removeListener(EventListener listener)
 {
  listeners.remove(new SmartWeakReference(listener));
  purgeZombies(true);
 }


 public synchronized boolean areThereListeners()
 {
  purgeZombies(false);
  return (listeners.size()>0);
 }

 public synchronized int getListenersCount()
 {
  return listeners.size();
 }




 public Iterator<L> getListeners()
 {
  final ArrayList<SmartWeakReference<L>> cloned;

  synchronized (this)
  {
   cloned=(ArrayList<SmartWeakReference<L>>)listeners.clone();
  }

  return new Iterator<L>()
  {
   int index=0;
   L next;

   public boolean hasNext()
   {
    boolean res=false;
    next=null;

    while (!res && index<cloned.size())
    {
     res=((next=cloned.get(index).get())!=null);
     if (!res) index++;
    }

    return res;
   }


   public L next()
   {
    if (hasNext())
    {
     index++;
    }

    return next;
   }


   public void remove()
   {
    throw new RuntimeException("remove method is not supported here!");
   }
  };
 }


 public synchronized List<L> getListenersArrayList()
 {
  ArrayList<L> res=new ArrayList<L>();

  for (Iterator<L> lins=getListeners();lins.hasNext();)
  {
   res.add(lins.next());
  }

  return res;
 }






 // returns the same number that getListenersCount() would
 public int foreachListener(Firer firer, Object param)
 {
  ArrayList<SmartWeakReference<L>> cloned;

  synchronized (this)
  {
   cloned=(ArrayList<SmartWeakReference<L>>)listeners.clone();
  }

  int t, len=cloned.size();
  boolean needPurge=false;

  if (len>0)
  {
   L listener;

   for (t=0;t<len;t++)
   {
    listener=cloned.get(t).get();

    if (listener!=null)
    {
     try
     {
      firer.foreachAction(listener, (EventObject)param);
     }
     catch (Throwable tr)
     {
      System.err.println("EXCEPTION WHILE FIRING EVENT IN WeakReferenceListenersManager:");
      systemErrDeepCauseStackTrace(tr);
      //log.exception(tr);
     }
    } else needPurge=true;
   }
  }

  if (needPurge) purgeZombies(true);

  return len;


/*
  int t, len=listeners.size();

  if (len>0)
  {
   // we need a copy because a listener could remove from
   // the list during the hanfling of the event!
   ArrayList<SmartWeakReference<L>> al=(ArrayList<SmartWeakReference<L>>)listeners.clone();
   L listener;

   for (t=0;t<len;t++)
   {
    listener=al.get(t).get();
    if (listener!=null) firer.foreachAction(listener, param);
   }
  }

  return len;
*/
 }






 // returns the answer from listenerThatMustAnswer
 public Object foreachListener(AnsweredFirer firer, EventObject param, EventListener listenerThatMustAnswer)
 {
  ArrayList<SmartWeakReference<L>> cloned;

  synchronized (this)
  {
   cloned=(ArrayList<SmartWeakReference<L>>)listeners.clone();
  }


  boolean needPurge=false;
  Object res=null;
  int t, len=cloned.size();

  if (len>0)
  {
   L currentFiredListener;
   Object tmpRes;

   for (t=0;t<len;t++)
   {
    currentFiredListener=cloned.get(t).get();
    if (currentFiredListener!=null)
    {
     tmpRes=firer.foreachAction(currentFiredListener, param);
     if (listenerThatMustAnswer!=null && currentFiredListener.equals(listenerThatMustAnswer))
     {
      res=tmpRes;
     }
    } else needPurge=true;
   }
  }

  if (needPurge) purgeZombies(true);

  return res;



/*
  Object res=null;
  int t, len=listeners.size();

  if (len>0)
  {
   L currentFiredListener;
   Object tmpRes;
   // we need a copy because a listener could remove from
   // the list during the hanfling of the event!
   ArrayList<SmartWeakReference<L>> al=(ArrayList<SmartWeakReference<L>>)listeners.clone();

   for (t=0;t<len;t++)
   {
    currentFiredListener=al.get(t).get();
    if (currentFiredListener!=null)
    {
     tmpRes=firer.foreachAction(currentFiredListener, param);
     if (listenerThatMustAnswer!=null && currentFiredListener.equals(listenerThatMustAnswer))
     {
      res=tmpRes;
     }
    }
   }
  }

  return res;
*/
 }

}


