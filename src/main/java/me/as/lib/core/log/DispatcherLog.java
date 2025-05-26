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


import me.as.lib.core.lang.ArrayExtras;

import java.util.*;


public class DispatcherLog implements MinimalLogable
{
 ArrayList<MinimalLogable> receivers=new ArrayList<MinimalLogable>();
 boolean closed=false;


 public DispatcherLog(MinimalLogable... mls)
 {
  receivers.add(new MinimalTextLog());
  receivers.addAll(ArrayExtras.toCollection(mls));
 }


 public void print(String str)
 {
  for (MinimalLogable l : receivers) l.print(str);
 }


 public void flush()
 {
  for (MinimalLogable l : receivers) l.flush();
 }


 public void close()
 {
  for (MinimalLogable l : receivers) l.close();
  closed=true;
 }


 public boolean isClosed()
 {
  return closed;
 }


 public String getLogContent()
 {
  return receivers.get(0).getLogContent();
 }

}
