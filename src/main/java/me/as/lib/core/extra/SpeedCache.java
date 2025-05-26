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


import java.util.*;


public class SpeedCache
{
 protected ArrayList list=new ArrayList();
 protected HashMap hasMap=new HashMap();
 protected int maxElements;
 protected int reorganizeThreshold;

 public SpeedCache()
 {
  this(200);
 }

 public SpeedCache(int maxElements)
 {
  setMaxElements(maxElements);
  list=new ArrayList();
  hasMap=new HashMap();
 }


 public synchronized int getMaxElements()
 {
  return maxElements;
 }


 public synchronized void setMaxElements(int me)
 {
  maxElements=me;
  reorganizeThreshold=(maxElements/3)*2;
  reorganize();
 }


 public synchronized void put(Object key, Object element)
 {
  ///*
  list.add(key);
  hasMap.put(key, element);
  reorganize();
  //*/
 }


 public synchronized Object get(Object key)
 {
  Object res=hasMap.get(key);

  //System.out.print(" get...");

  if (res!=null)
  {
   //System.out.println(" success");
   //long et, it=System.currentTimeMillis();

   list.remove(key);
   list.add(key);

   //et=System.currentTimeMillis();System.out.println("get lasting = "+(et-it));
  } //else System.out.println(" no s");

  return res;
 }


 public synchronized Object remove(Object key)
 {
  list.remove(key);
  return hasMap.remove(key);
 }


 protected synchronized void reorganize()
 {
  synchronized (list)
  {
   int curSize=list.size();
   Object key;

   if (curSize>maxElements)
   {
    // IMPORTANT ---> Speed test has been made: average 16 millis to remove 1000 elements. It's ok!

    //System.out.print("reorganizing..."+curSize);
    //long et, it=System.currentTimeMillis();
    //int deleted=0;

    do
    {
     hasMap.remove(list.remove(0));
     curSize--;
     //deleted++;
    } while (curSize>reorganizeThreshold);

    //et=System.currentTimeMillis();
    //System.out.println("reorganize lasting ("+deleted+")= "+(et-it));
    //System.out.println(" ..."+curSize);
   }
  }
 }



}