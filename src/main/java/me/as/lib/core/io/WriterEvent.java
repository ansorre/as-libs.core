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

package me.as.lib.core.io;


import me.as.lib.core.event.EventWithID;


public class WriterEvent extends EventWithID
{
 public static final int WRITTEN_BYTES_COUNT_CHANGED  = USER_DEFINED_EVENT + 2000;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected int currentWrittenBytesCount;

 public WriterEvent(Object source, int currentWrittenBytesCount, int eventId)
 {
  super(source, eventId);
  this.currentWrittenBytesCount=currentWrittenBytesCount;
 }

 public int getCurrentWrittenBytesCount()
 {
  return currentWrittenBytesCount;
 }



}