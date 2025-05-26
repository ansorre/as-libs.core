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


package me.as.lib.core.report;


public class Problem
{
 public enum Type
 {
  warning,
  error,
  showstopper,
  showstopperNoPrefix,
  none
 }

 public Type type;
 public String message;

 public Problem()
 {

 }

 public Problem(Type type, String message)
 {
  this.type=type;
  this.message=message;
 }

 public String toString()
 {
  StringBuilder sb=new StringBuilder();

  switch (type)
  {
   case warning    :sb.append("WARNING: ");break;
   case error      :sb.append("ERROR: ");break;
   case showstopper:sb.append("FATAL ERROR: ");break;
  }

  sb.append(message);

  return sb.toString();
 }

}
