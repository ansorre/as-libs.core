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


import me.as.lib.core.lang.ObjectExtras;

import java.lang.ref.*;
import java.util.*;


public class SimpleWeakList
{
 protected List list=new ArrayList();


 public synchronized void add(Object o)
 {
  list.add(new WeakReference(o));
 }


 public synchronized void remove(Object o)
 {
  WeakReference wr;
  Object co;

  for (Iterator i=list.iterator();i.hasNext();)
  {
   wr=(WeakReference)i.next();
   co=wr.get();

   if (co==null || ObjectExtras.areEqual(co, o)) i.remove();
  }
 }


 public synchronized boolean contains(Object o)
 {
  boolean res=false;
  WeakReference wr;
  Object co;

  for (Iterator i=list.iterator();i.hasNext() && !res;)
  {
   wr=(WeakReference)i.next();
   co=wr.get();

   if (co==null) i.remove();
   else if (co==o) res=true;
  }

  return res;
 }


 public synchronized Object[] elements()
 {
  WeakReference wr;
  Object co;
  List tmpList=new ArrayList();

  for (Iterator i=list.iterator();i.hasNext();)
  {
   wr=(WeakReference)i.next();
   co=wr.get();

   if (co==null) i.remove();
   else tmpList.add(co);
  }

  Object res[]=tmpList.toArray();
  tmpList=null;

  return res;
 }


 // I want it not synchronized
 public int approximateSize()
 {
  return list.size();
 }


 // I want it not synchronized
 public Object approximateElement(int idx)
 {
  Object res;

  try
  {
   res=((WeakReference)list.get(idx)).get();
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }


}



