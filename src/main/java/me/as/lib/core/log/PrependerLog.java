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

package me.as.lib.core.log;


import me.as.lib.core.lang.StringExtras;

import static me.as.lib.core.lang.StringExtras.replace;


public class PrependerLog extends LogableHandler
{
 private String textToBePrependedOnNewLines;
 private boolean nextTextNeedPrepend=true;


 public PrependerLog(String textToBePrependedOnNewLines)
 {
  super(new MinimalTextLog());
  this.textToBePrependedOnNewLines=textToBePrependedOnNewLines;
 }


 protected String transformText(String str)
 {
  if (nextTextNeedPrepend)
  {
   str=textToBePrependedOnNewLines+str;
   nextTextNeedPrepend=false;
  }

  if (StringExtras.areEqual(LogableHandler.lineSeparator, str))
  {
   nextTextNeedPrepend=true;
  }
  else
  {
   if (str.endsWith(LogableHandler.lineSeparator))
   {
    str=str.substring(0, str.length()-LogableHandler.lineSeparator.length());
    nextTextNeedPrepend=true;
   }

   str=replace(str, LogableHandler.lineSeparator, LogableHandler.lineSeparator+PrependerLog.this.textToBePrependedOnNewLines, true);

   if (nextTextNeedPrepend) str+=LogableHandler.lineSeparator;
  }

  return str;
 }


 protected void transformThrowable(Throwable tr)
 {
  printUntrasformed(textToBePrependedOnNewLines);
 }


}
