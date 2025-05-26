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
import me.as.lib.core.collection.Lifo;

import java.util.*;


public abstract class EventDrivenContextExecutor
{
 private static final EventDrivenContextExecutorRequest endRequest=new EventDrivenContextExecutorRequest(null, 0, null);

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private Lifo requestGroupsRequests=new Lifo();
 private HashMap requestGroups=new HashMap();
 private boolean mustGoOn=true;



 public EventDrivenContextExecutor()
 {
  start();
 }

 public synchronized void forwardRequest(EventDrivenContextExecutorRequest r)
 {
  forwardRequest(r, null);
 }

 public synchronized void forwardRequest(EventDrivenContextExecutorRequest r, Object requestGroup)
 {
  if (requestGroup==null) requestGroup=this;
  Fifo theRightFifo=(Fifo)requestGroups.get(requestGroup);

  if (theRightFifo==null)
  {
   theRightFifo=new Fifo();
   requestGroups.put(requestGroup, theRightFifo);
   //System.out.println("creata! ("+requestGroup.toString()+")");
  }

  theRightFifo.put(r);
  requestGroupsRequests.put(theRightFifo);
  //System.out.println("requestGroupsRequests.size()="+requestGroupsRequests.size());
 }



 protected synchronized void clearAllRequests()
 {
  requestGroupsRequests=null;
  requestGroups=null;
 }



 private synchronized void start()
 {
  Thread t=new Thread
  (
   new Runnable()
   {
    public void run()
    {
     Fifo theRightFifo;
     EventDrivenContextExecutorRequest r;

     while (mustGoOn)
     {
      theRightFifo=(Fifo)requestGroupsRequests.getWaiting();

      while (mustGoOn && theRightFifo!=null)
      {
       //System.out.println("theRightFifo.size()="+theRightFifo.size());

       r=(EventDrivenContextExecutorRequest)theRightFifo.get();
       theRightFifo=null;

       if (r!=null)
       {
        if (mustGoOn) _internal_handleRequest(r);


        /*
        if (requestGroupsRequests.size()>0)
        {
         if (requestGroupsRequests.whoIsTheNext()!=theRightFifo)
         {
          theRightFifo=null;
         }
        }
        */
       } //else theRightFifo=null;
      }
     }

     clearAllRequests();
    }
   }
  );

  t.start();
 }


 public synchronized void terminate()
 {
  if (mustGoOn)
  {
   mustGoOn=false;
   forwardRequest(endRequest);
  }
 }




 private void _internal_handleRequest(EventDrivenContextExecutorRequest r)
 {
  if (r!=endRequest)
  {
   handleRequest(r);
   EventDrivenContext context=r.getContext();

   if (context!=null)
   {
    EventDrivenContextEvent e=new EventDrivenContextEvent
    (
     this,
     context,
     EventDrivenContextEvent.EDCE_REQUEST_WAS_HANDLED,
     r
    );

    context.postEvent(e);
   }
  }
 }

 public abstract void handleRequest(EventDrivenContextExecutorRequest r);



}