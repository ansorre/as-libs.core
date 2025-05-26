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


import java.util.EventObject;


public class EventWithID extends EventObject
{
 // basic event IDs
 public static final int UNKNOWN_EVENT       =   0;
 public static final int USER_DEFINED_EVENT  =  10;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected int eventId;

 public EventWithID(Object source, int eventId)
 {
  super(source);
  this.eventId=eventId;
 }

 public int getID()
 {
  return eventId;
 }


}



