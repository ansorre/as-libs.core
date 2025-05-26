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

import java.util.*;


public class KeyValue<K, V>
{
 public K key=null;
 public V value=null;

 public KeyValue()
 {

 }

 public KeyValue(K key, V value)
 {
  this();
  this.key=key;
  this.value=value;
 }

 public static KeyValue[] fromMap(Map map)
 {
  KeyValue res[]=null;

  if (map!=null)
  {
   ArrayList v=new ArrayList();
   Object key;

   for (Iterator i=map.keySet().iterator();i.hasNext();)
   {
    key=i.next();
    v.add(new KeyValue(key, map.get(key)));
   }

   res=ArrayExtras.toArray(v, KeyValue.class);
  }

  return res;
 }


}