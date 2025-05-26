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

package me.as.lib.core.collection;


import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ObjectExtras;

import java.util.*;


public abstract class Pile<E>
{
 public static final int builtinDefaultIncrement=20;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 protected int defaultIncrement=builtinDefaultIncrement;
 protected Object elements[];
 protected int initCapacity;


 public Pile()
 {
  this(builtinDefaultIncrement);
 }

 public Pile(int initCapacity)
 {
  this.initCapacity=initCapacity;
  elements=new Object[initCapacity];
 }

 public synchronized void setDefaultIncrement(int di)
 {
  defaultIncrement=di;
 }

 public synchronized int getDefaultIncrement()
 {
  return defaultIncrement;
 }

 protected synchronized void extend()
 {
  extend(getDefaultIncrement());
 }

 protected synchronized void extend(int increment)
 {
  int curLen=elements.length;
  Object newElements[]=new Object[curLen+increment];
  System.arraycopy(elements, 0, newElements, 0, curLen);
  elements=newElements;
 }

 public synchronized void clear()
 {
  Object element;

  for (int t=0;t<elements.length;t++)
  {
   element=elements[t];
   elements[t]=null;
   onElementRemoved((E)element);
  }

  elements=new Object[initCapacity];
 }


 public synchronized boolean contains(E element)
 {
  boolean res=false;
  int len;

  if ((len=ArrayExtras.length(elements))>0)
  {
   for (int t=0;t<len && !res;t++) res=ObjectExtras.areEqual(elements[t], element);
  }

  return res;
 }


 public abstract Enumeration<E> elements();


 public void onElementRemoved(E element)
 {

 }


 public void onElementInserted(E element)
 {

 }


}

