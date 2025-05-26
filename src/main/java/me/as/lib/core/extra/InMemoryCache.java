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

import static me.as.lib.core.lang.StringExtras.getQuickUniqueKey;


public class InMemoryCache<E>
{
 private HashMap<String, E> cacheElements=new HashMap<>();
 private HashMap<String, Long> lastAccessTimes=new HashMap<>();
 private HashMap<String, Object> nullObjects=new HashMap<>();
 private int maxCachedElements;
 private int pruneAmount;
 private static final Object nullObject=new Object();
 private InMemoryCacheDiscardListener<E> discardListener=null;



 public InMemoryCache()
 {
  this(200, 10);
 }

 public InMemoryCache(int maxCachedObjects)
 {
  this(maxCachedObjects, maxCachedObjects/20);
 }

 public InMemoryCache(int maxCachedObjects, int pruneAmount)
 {
  this(null, maxCachedObjects, pruneAmount);
 }


 public InMemoryCache(InMemoryCacheDiscardListener<E> discardListener, int maxCachedObjects, int pruneAmount)
 {
  this.discardListener=discardListener;
  this.maxCachedElements=maxCachedObjects;
  this.pruneAmount=pruneAmount;
 }





 private E alertDiscarded(E e)
 {
  if (discardListener!=null)
  {
   discardListener.onDiscard(e);
  }

  return e;
 }



 /**
  * The discardListener is useful when you are caching something that must be flushed (or the like) before being
  * discarded. For example, if you are caching a piece of file that has been modified in memory, you must write the
  * changes to the disk before discarding this cache
  *
  * @param dl
  */
 public synchronized void setDiscardListener(InMemoryCacheDiscardListener<E> dl)
 {
  discardListener=dl;
 }


 public synchronized InMemoryCacheDiscardListener<E> getDiscardListener()
 {
  return discardListener;
 }




 public synchronized int size()
 {
  return cacheElements.size();
 }




 public synchronized Collection<E> cachedElements()
 {
  return cacheElements.values();
 }



 public synchronized void clearNulls()
 {
  nullObjects.clear();
 }



 public synchronized void clear()
 {
  if (cacheElements.size()>0)
  {
   for (E e : cacheElements.values()) alertDiscarded(e);
   cacheElements.clear();
  }

  lastAccessTimes.clear();
  nullObjects.clear();
 }




 public synchronized E remove(Object... params)
 {
  String key=getQuickUniqueKey(params);
  E res=alertDiscarded(cacheElements.remove(key));
  lastAccessTimes.remove(key);
  nullObjects.remove(key);
  return res;
 }




 public synchronized E get(Object... params)
 {
  return get(null, params);
 }





 public synchronized E get(CacheHelper<E> cHelper, Object... params)
 {
  boolean newWasAdded=false;
  String key=getQuickUniqueKey(params);
  E res=cacheElements.get(key);

  if (res==null)
  {
   if (nullObjects.get(key)==null && cHelper!=null)
   {
    try
    {
     res=cHelper.create(params);
    }
    catch (Throwable tr)
    {
     nullObjects.put(key, nullObject);
     throw new RuntimeException(tr);
    }

    if (res!=null)
    {
     cacheElements.put(key, res);
     newWasAdded=true;
    } else nullObjects.put(key, nullObject);
   }
  }

  if (res!=null) lastAccessTimes.put(key, System.currentTimeMillis());
  if (newWasAdded) prune();

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private void prune()
 {
  int size;
  if ((size=cacheElements.size())>maxCachedElements)
  {
   long toBeSorted[]=new long[size];
   String keys[]=new String[size];
   int i=0;

   for (String key : lastAccessTimes.keySet())
   {
    keys[i]=key;
    toBeSorted[i]=lastAccessTimes.get(key);
    i++;
   }

   sorter.sort(toBeSorted, keys);

   for (i=0;i<pruneAmount;i++)
   {
    lastAccessTimes.remove(keys[i]);
    alertDiscarded(cacheElements.remove(keys[i]));
   }
  }
 }


 private Sorter sorter=new Sorter();
 private QuickSort quickSorter=new QuickSort();

 class Sorter implements QSortable
 {
  long milliss[];
  String keys[];

  long mid;


  void sort(long milliss[], String keys[])
  {
   this.milliss=milliss;
   this.keys=keys;
   quickSorter.sort(this, 0, milliss.length-1, null);
  }


  public void setMid(int mididx, Object params)
  {
   mid=milliss[mididx];
  }


  // must return:
  // <0 if elem1<mid
  // 0 if elem1==mid
  // >0 if elem1>mid
  public int compareToMid(int elem1, Object params)
  {
   if (milliss[elem1]<mid) return -1;
   if (milliss[elem1]>mid) return  1;
   return 0;
  }


  public boolean swap(int elem1, int elem2, Object params)
  {
   long lswap=milliss[elem1];
   String sswap=keys[elem1];

   milliss[elem1]=milliss[elem2];
   keys[elem1]=keys[elem2];

   milliss[elem2]=lswap;
   keys[elem2]=sswap;

   return true;
  }

 };





}
