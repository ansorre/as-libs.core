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


public class EventDrivenContextEvent extends EventWithObject
{
 public static final int EDCE_LOOP_STARTED        = USER_DEFINED_EVENT +  1;
 public static final int EDCE_EXIT_LOOP           = USER_DEFINED_EVENT +  2;
 public static final int EDCE_LOOP_ENDED          = USER_DEFINED_EVENT +  3;
 public static final int EDCE_REQUEST_WAS_HANDLED = USER_DEFINED_EVENT +  4;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 protected EventDrivenContext context;


 public EventDrivenContextEvent(EventDrivenContext context, int eventId)
 {
  this(context, context, eventId, null);
 }

 public EventDrivenContextEvent(EventDrivenContext context, int eventId, Object obj)
 {
  this(context, context, eventId, obj);
 }

 public EventDrivenContextEvent(Object source, EventDrivenContext context, int eventId, Object obj)
 {
  super(source, eventId, obj);
  this.context=context;
 }

 public EventDrivenContext getContext()
 {
  return context;
 }



 public EventDrivenContextExecutorRequest getEventDrivenContextExecutorRequest()
 {
  EventDrivenContextExecutorRequest res;

  try
  {
   res=(EventDrivenContextExecutorRequest)getObject();
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }



}