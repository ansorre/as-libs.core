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


public class EventWithMessage extends EventWithID
{
 protected String message=null;

 public EventWithMessage(Object source, int eventId)
 {
  super(source, eventId);
 }

 public EventWithMessage(Object source, int eventId, String message)
 {
  this(source, eventId);
  this.message=message;
 }

 public String getMessage()
 {
  return message;
 }


}

