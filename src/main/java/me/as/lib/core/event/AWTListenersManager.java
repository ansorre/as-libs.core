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


import me.as.lib.core.collection.Fifo;
import me.as.lib.core.extra.BoxFor3;

import javax.swing.*;
import java.util.*;
import java.awt.*;


/*

 // Listeners management

 protected AWTListenersManager listenersManager=new AWTListenersManager();

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


public class AWTListenersManager<L extends EventListener, E extends EventObject> extends BasicListenersManager<L, E>
{
 public static final Fifo<BoxFor3> awtCache=new Fifo<>();

 public static ArrayList<JComponent> toBeRepaintedImmediately=new ArrayList<>();
 private static Dimension sizeCache=new Dimension();



 static class AWTDispatcher implements Runnable
 {
  ArrayList<BoxFor3> tos;

  public AWTDispatcher()
  {

  }

  public void setList(ArrayList<BoxFor3> tos)
  {
   this.tos=tos;
  }


  public void run()
  {
   BoxFor3 to;
   AWTListenersManager man;
   Firer firer;
   EventObject param;
   int t, len=tos.size();

//   System.out.println("tos.size(): "+tos.size());

   for (t=0;t<len;t++)
   {
    to=tos.get(t);
    man=(AWTListenersManager)to.element1;
    firer=(Firer)to.element2;
    param=(EventObject)to.element3;


/*
    //debug
    try
    {
     IBEvent ibe=(IBEvent)param;
     boolean debug=ibe.getContract().m_symbol.equals("EUR.USD") && ibe.getField()==0 && ibe.getID()==IBEvent.IBE_tickSize;
     if (debug) System.out.println("bid size: "+ibe.getSize()+"    ^4^");
    }
    catch (Throwable tr)
    {
     //        tr.printStackTrace();
    }

*/




    man.super_foreachListener(firer, param);
   }

   JComponent comp;
   len=toBeRepaintedImmediately.size();

   if (len>0)
   {
    if (len>1) System.out.println("toBeRepaintedImmediately.size: "+len);

    for (t=0;t<len;t++)
    {
     comp=toBeRepaintedImmediately.get(t);
     comp.getSize(sizeCache);
     comp.repaint();

     //      Graphics g=comp.getGraphics();comp.paint(g);g.dispose();
     //      comp.paintImmediately(0, 0, sizeCache.width, sizeCache.height);
    }

    toBeRepaintedImmediately.clear();
   }

  }
 }


 static Runnable hopeForRepainting=() -> {};

 static
 {
  Thread tt=new Thread(() ->
  {
   ArrayList<BoxFor3> tos=new ArrayList<>();
   BoxFor3 to;
   AWTDispatcher awtDispatcher=new AWTDispatcher();

   do
   {
    to=awtCache.getWaiting();

//          int debug=awtCache.size();
//          System.out.println("------> awtCache.size(): "+(debug+1));

    try
    {
     tos.add(to);

     int howMany=2;
     int size=awtCache.size();
     if (size>100) howMany=1000;
     else if (size>50) howMany=75;
     else if (size>25) howMany=35;
     else if (size>5) howMany=7;
     awtCache.getSomeRemoving(howMany, tos);


//      Enumeration<BoxFor3> etos=awtCache.getAllAndClear();
//      for (;etos.hasMoreElements();) tos.add(etos.nextElement());


/*
     //debug
     int t, len=tos.size();
     for (t=0;t<len;t++)
     {
      try
      {
       IBEvent ibe=(IBEvent)((BoxFor3)tos.get(t)).third;
       boolean debug=ibe.getContract().m_symbol.equals("EUR.USD") && ibe.getField()==0 && ibe.getID()==IBEvent.IBE_tickSize;
       if (debug) System.out.println("bid size: "+ibe.getSize()+"    ^3b^");
      }
      catch (Throwable tr)
      {
       //        tr.printStackTrace();
      }
     }
*/



//      TimeCounter tc=TimeCounter.start();


     awtDispatcher.setList(tos);
     SwingUtilities.invokeAndWait(awtDispatcher);
     SwingUtilities.invokeAndWait(hopeForRepainting);

//      tc.stop();if (tc.getElapsed()>100) System.out.println("--- tos.size: "+tos.size()+" ---> "+tc.getElapsedString());

    }
    catch (Throwable tr)
    {
     tr.printStackTrace();
    }

    tos.clear();
   } while (true);
  }, "IBWrapper.EventDispatcher_Thread");

  tt.setDaemon(true);
  tt.start();
 }


 public void addToBeRepaintedImmediately(JComponent comp)
 {
  if (!toBeRepaintedImmediately.contains(comp))
  {
   toBeRepaintedImmediately.add(comp);
  }
 }



 private int super_foreachListener(final Firer<L, E> firer, final E param)
 {
  return super.foreachListener(firer, param);
 }


 // returns the same number that getListenersCount() would
 public int foreachListener(final Firer<L, E> firer, final E param)
 {
  int len=listeners.size();

/*
  //debug
  try
  {
   IBEvent ibe=(IBEvent)param;
   boolean debug=ibe.getContract().m_symbol.equals("EUR.USD") && ibe.getField()==0 && ibe.getID()==IBEvent.IBE_tickSize;
   if (debug) System.out.println("bid size: "+ibe.getSize()+"    ^3^");
  }
  catch (Throwable tr)
  {
   //        tr.printStackTrace();
  }
*/


  if (len>0)
  {
   if (SwingUtilities.isEventDispatchThread())
   {
    super_foreachListener(firer, param);
//    System.out.println("--------------> isEventDispatchThread!!!");
   }
   else
   {
    try
    {
     awtCache.put(new BoxFor3(this, firer, param));
    } catch (Throwable ignore){}
   }
  }

  return len;
 }



 public Object super_foreachListener(AnsweredFirer firer, EventObject param, L listenerThatMustAnswer)
 {
  return super.foreachListener(firer, param, listenerThatMustAnswer);
 }


 // returns the answer from listenerThatMustAnswer
 public Object foreachListener(final AnsweredFirer firer, final EventObject param, final L listenerThatMustAnswer)
 {
  Object res=null;
  int len=listeners.size();

  if (len>0)
  {
   if (SwingUtilities.isEventDispatchThread())
   {
    res=super_foreachListener(firer, param, listenerThatMustAnswer);
   }
   else
   {
    try
    {
     class _runner_ implements Runnable
     {
      Object res=null;
      public void run()
      {
       res=super_foreachListener(firer, param, listenerThatMustAnswer);
      }
     }

     _runner_ runner=new _runner_();

     SwingUtilities.invokeAndWait(runner);
     res=runner.res;

    } catch (Throwable ignore){}
   }
  }

  return res;
 }



}
