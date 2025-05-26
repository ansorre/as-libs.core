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

import java.util.*;
import javax.swing.*;


public class EventDrivenContext
{
 private static final int NO_LOOPING     = 0;
 private static final int NORMAL_LOOPING = 1;
 private static final int SWING_LOOPING  = 2;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private boolean isAlreadyInLoop=false;
 private boolean loopMustGoOn;
 private int typeOfLoop=NO_LOOPING;
 private Fifo eventsFifo=new Fifo();


 public synchronized void loopOnSwingEventQueue()
 {
  if (!isAlreadyInLoop)
  {
   isAlreadyInLoop=true;
   loopMustGoOn=true;
   typeOfLoop=SWING_LOOPING;
   eventsFifoPut(new EventDrivenContextEvent(this, EventDrivenContextEvent.EDCE_LOOP_STARTED));
  }
  else
  {
   throw new RuntimeException
   (
    "this EventDrivenContext is already in the loop()!"
   );
  }
 }



 public synchronized void loop()
 {
  if (!isAlreadyInLoop)
  {
   isAlreadyInLoop=true;
   loopMustGoOn=true;
   typeOfLoop=NORMAL_LOOPING;

   fireEventOccurred(new EventDrivenContextEvent(this, EventDrivenContextEvent.EDCE_LOOP_STARTED));

   do
   {
    peekAndWorkEvent();
   } while (loopMustGoOn);
  }
  else
  {
   throw new RuntimeException
   (
    "this EventDrivenContext is already in the loop()!"
   );
  }
 }


 public synchronized void exitLoop()
 {
  if (isAlreadyInLoop)
  {
   eventsFifoPut(new EventDrivenContextEvent(this, EventDrivenContextEvent.EDCE_EXIT_LOOP));
  }
  else
  {
   throw new RuntimeException
   (
    "this EventDrivenContext is NOT in the loop() at the moment!"
   );
  }
 }


 public void postEvent(EventDrivenContextEvent e)
 {
  eventsFifoPut(e);
 }


 private void eventsFifoPut(EventDrivenContextEvent e)
 {
  eventsFifo.put(e);

  if (typeOfLoop==SWING_LOOPING)
  {
   if (SwingUtilities.isEventDispatchThread()) peekAndWorkEvent();
   else SwingUtilities.invokeLater(peeker);
  }
 }


 private void peekAndWorkEvent()
 {
  EventDrivenContextEvent nextEvent=(EventDrivenContextEvent)eventsFifo.getWaiting();

  switch (nextEvent.getID())
  {
   case EventDrivenContextEvent.EDCE_EXIT_LOOP:
   {
    isAlreadyInLoop=false;
    loopMustGoOn=false;
    typeOfLoop=NO_LOOPING;

    fireEventOccurred(new EventDrivenContextEvent(this, EventDrivenContextEvent.EDCE_LOOP_ENDED));
   } break;

   default:fireEventOccurred(nextEvent);
  }
 }


 Runnable peeker=new Runnable()
 {
  public void run()
  {
   peekAndWorkEvent();
  }
 };



 // Listeners management

 protected BasicListenersManager listenersManager=new BasicListenersManager();

 public void addEventDrivenContextListener(EventDrivenContextListener listener)
 {listenersManager.addListener(listener);}

 public void removeEventDrivenContextListener(EventDrivenContextListener listener)
 {listenersManager.removeListener(listener);}

 Firer firer=new Firer<EventListener, EventObject>()
 {public void foreachAction(EventListener listener, EventObject param)
  {((EventDrivenContextListener)listener).eventOccurred((EventDrivenContextEvent)param);}};

 private void fireEventOccurred(EventDrivenContextEvent e)
 {listenersManager.foreachListener(firer, e);}

 // END - Listeners management

}



