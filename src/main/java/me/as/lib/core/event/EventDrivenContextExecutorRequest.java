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


public class EventDrivenContextExecutorRequest
{
 private EventDrivenContext context;
 private int requestId;
 private Object requestParameter;

 public EventDrivenContextExecutorRequest(EventDrivenContext context, int requestId, Object requestParameter)
 {
  this.context=context;
  this.requestId=requestId;
  this.requestParameter=requestParameter;
 }

 public EventDrivenContext getContext()
 {
  return context;
 }

 public int getRequestId()
 {
  return requestId;
 }

 public Object getRequestParameter()
 {
  return requestParameter;
 }

}