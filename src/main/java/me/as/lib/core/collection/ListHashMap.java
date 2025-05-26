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


import java.lang.reflect.*;
import java.util.*;
import java.io.*;



/* @todo: seems very slow, please adjust it !!! */

public class ListHashMap<K, V> implements Serializable, Cloneable
{
 private ArrayList<K> keys;
 private ArrayList<V> values;
 private HashMap<K, V> map;



 public ListHashMap()
 {
  keys=new ArrayList<K>();
  values=new ArrayList<V>();
  map=new HashMap<K, V>();
 }



 public ListHashMap(int initialCapacity)
 {
  keys=new ArrayList<K>(initialCapacity);
  values=new ArrayList<V>(initialCapacity);
  map=new HashMap<K, V>(initialCapacity);
 }




 public Object clone()
 {
  ListHashMap res;

  try
  {
   res=(ListHashMap)super.clone();
   res.keys=(ArrayList)keys.clone();
   res.values=(ArrayList)values.clone();
   res.map=(HashMap)map.clone();
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }


 public boolean equals(Object obj)
 {
  boolean res;

  try
  {
   ListHashMap<K, V> other=(ListHashMap<K, V>)obj;

   res=(keys.equals(other.keys) &&
        values.equals(other.values) &&
        map.equals(other.map));
  }
  catch (Throwable tr)
  {
   res=false;
  }

  return res;
 }


 public V put(K key, V value)
 {
  V res=map.put(key, value);
  int i=keys.indexOf(key);

  if (i<0)
  {
   keys.add(key);
   values.add(value);
  }
  else
  {
   values.set(i, value);
  }

  return res;
 }


 public V remove(K key)
 {
  V res=map.remove(key);
  if (res!=null)
  {
   int i=keys.indexOf(key);
   keys.remove(i);
   values.remove(i);
  }
  return res;
 }


 public int size()
 {
  return keys.size();
 }


 public int indexOf(K key)
 {
  return keys.indexOf(key);
 }


 public V get(K key)
 {
  return map.get(key);
 }


 public V get(int index)
 {
  return values.get(index);
 }

 public K getKey(int index)
 {
  return keys.get(index);
 }



 public V set(int index, K key, V value)
 {
  if (index<0 || index>=keys.size()) throw new IndexOutOfBoundsException("Offending index: "+index);
  V res=get(index);
  K oldKey=getKey(index);

  keys.set(index, key);
  values.set(index, value);
  map.remove(oldKey);
  map.put(key, value);

  return res;
 }


 public K[] keysToArray()
 {
  K res[]=null;
  int t, len;

  if ((len=keys.size())>0)
  {
   try {res=(K[])Array.newInstance(getKey(0).getClass(), len);} catch (Throwable ignore){}

   for (t=0;t<len;t++)
   {
    res[t]=getKey(t);
   }
  }

  return res;
 }



 public Collection<V> values()
 {
  return values;
//  return map.values();
 }

 public Collection<K> keys()
 {
  return keys;
//  return map.keySet();
 }



/*

nun s po fa, generic array creation non esiste in Java!!!!!!!!

 public V[] valuesToArray()
 {
  V res[]=null;
  int t, len;

  if ((len=keys.size())>0)
  {
   try {res=(V[])Array.newInstance(get(0).getClass(), len);} catch (Throwable ignore){}

   for (t=0;t<len;t++)
   {
    res[t]=values.get(t);
   }
  }

  return res;
 }
*/


 public V[] valuesToArray(V array[])
 {
  return values.toArray(array);
 }



 public void clear()
 {
  keys.clear();
  map.clear();
  values.clear();
 }




}
