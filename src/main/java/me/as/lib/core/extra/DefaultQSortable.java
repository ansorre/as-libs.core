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

package me.as.lib.core.extra;


import me.as.lib.core.lang.ArrayExtras;

public abstract class DefaultQSortable implements QSortable
{
 protected Object array;
 protected Object middle;


 public DefaultQSortable(Object array)
 {
  this(array, null);
 }

 public DefaultQSortable(Object array, Object params)
 {
  this.array=array;
  new QuickSort(this, 0, ArrayExtras.length(array)-1, params);
 }


 public boolean swap(int elem1, int elem2, Object params)
 {
  Object el1=ArrayExtras.elementAt(array, elem1);
  ArrayExtras.setElementAt(array, elem1, ArrayExtras.elementAt(array, elem2));
  ArrayExtras.setElementAt(array, elem2, el1);

  return true;
 }

 public void setMid(int mididx, Object params)
 {
  middle=ArrayExtras.elementAt(array, mididx);
 }

 public abstract int compareToMid(int elem1, Object params);



}
