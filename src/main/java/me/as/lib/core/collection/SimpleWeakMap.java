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


import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Set;


public class SimpleWeakMap<K, V>
{
 protected HashMap<K, WeakReference<V>> map=new HashMap<>();


 public synchronized V put(K key, V value)
 {
  V res=get(key);

  map.put(key, new WeakReference<>(value));

  return res;
 }


 public synchronized V get(K key)
 {
  WeakReference<V> wr=map.get(key);
  V res=((wr!=null)?wr.get():null);
  if (res==null && wr!=null) map.remove(key);
  return res;
 }


 public synchronized V remove(K key)
 {
  WeakReference<V> wr=map.remove(key);
  return ((wr!=null)?wr.get():null);
 }


 public synchronized Set<K> keySet()
 {
  return map.keySet();
 }


}
