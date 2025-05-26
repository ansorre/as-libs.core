/*
 * Copyright 2023 Antonio Sorrentini
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


import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.StillUnimplemented;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


@SuppressWarnings({"NullableProblems", "SuspiciousMethodCalls", "JavaDoc", "Convert2streamapi"})
public class ScopedMap<K, V> implements Map<K, V>, Cloneable, Serializable
{
 private Stack<BoxFor2<String, HashMap<K, V>>> stacked=new Stack<>();
 private HashMap<K, V> current;


 public ScopedMap()
 {
  stacked=new java.util.Stack<>();
  pushScope();
 }


 public void pushScope()
 {
  stacked.push(new BoxFor2<>("#", current=new HashMap<>()));
 }

 public void popScope()
 {
  stacked.pop();
  current=stacked.get(stacked.size()-1).element2;
 }


 public int size()
 {
  return keySet().size();
 }


 public boolean isEmpty()
 {
  return (size()==0);
 }


 public boolean containsKey(Object key)
 {
  return keySet().contains(key);
 }


 public boolean containsValue(Object value)
 {
  return values().contains(value);
 }


 public V get(Object key)
 {
  V res=null;
  int t, len=ArrayExtras.length(stacked);

  for (t=len-1;t>=0 && res==null;t--)
  {
   res=stacked.get(t).element2.get(key);
  }

  return res;
 }

 public V put(K key, V value)
 {
/*
  if ("page_title".equals(key))
  {
   System.out.println("sjljs df jk-----> ");
  }

*/
  V res=get(key);
  current.put(key, value);
  return res;
 }


 /**
  * WARNING: remove only if present in the current scope!
  * @param key
  * @return
  */
 public V remove(Object key)
 {
  return current.remove(key);
 }


 public void putAll(Map<? extends K, ? extends V> m)
 {
  current.putAll(m);
 }


 /**
  * WARNING: clears only the current scope!
  *
  * @return
  */
 public void clear()
 {
  current.clear();
 }



 @SuppressWarnings("NullableProblems")
 final class ScopedKeySet extends AbstractSet<K>
 {
  ArrayList<K> keys;
  ScopedKeySet(ArrayList<K> keys){this.keys=keys;}
  public final int size(){return keys.size();}
  public final void clear(){ScopedMap.this.clear();}
  public final Iterator<K> iterator(){return keys.iterator();}
  public final boolean contains(Object o){return keys.contains(o);}
  public final boolean remove(Object key){throw new RuntimeException("Modification not allowed!");}
  public final Spliterator<K> spliterator(){throw new RuntimeException("spliterator not allowed!");}
  public final void forEach(Consumer<? super K> action){keys.forEach(action);}
 }


 public Set<K> keySet()
 {
  ArrayList<K> keys=new ArrayList<>();
  int t, len=ArrayExtras.length(stacked);

  for (t=0;t<len;t++)
  {
   HashMap<K, V> hm=stacked.get(t).element2;

   for (K k : hm.keySet())
   {
    if (!keys.contains(k)) keys.add(k);
   }
  }

  return new ScopedKeySet(keys);
 }


 public Collection<V> values()
 {
  ArrayList<V> values=new ArrayList<>();

  for (K k : keySet())
  {
   values.add(get(k));
  }

  return values;
 }


 public Set<Entry<K, V>> entrySet()
 {
  throw new StillUnimplemented();
 }


 public V getOrDefault(Object key, V defaultValue)
 {
  V res=get(key);
  if (res==null) res=defaultValue;
  return res;
 }


 public void forEach(BiConsumer<? super K, ? super V> action)
 {
  if (action==null)
   throw new NullPointerException();

  Set<K> keys=keySet();

  for (K k : keys)
  {
   action.accept(k, get(k));
  }
 }


 /**
  * WARNING: replaceAll only in the current scope!
  *
  */
 public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function)
 {
  current.replaceAll(function);
 }

 public V putIfAbsent(K key, V value)
 {
  V res=get(key);
  if (res==null) current.put(key, value);
  return res;
 }

 /**
  * WARNING: removes only from the current scope!
  *
  * @param key
  * @return
  */
 public boolean remove(Object key, Object value)
 {
  return current.remove(key, value);
 }

 /**
  * WARNING: replaces only in the current scope!
  *
  * @param key
  * @return
  */
 public boolean replace(K key, V oldValue, V newValue)
 {
  return current.replace(key, oldValue, newValue);
 }

 /**
  * WARNING: replaces only in the current scope!
  *
  * @param key
  * @return
  */
 public V replace(K key, V value)
 {
  return current.replace(key, value);
 }

 public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
 {
  throw new StillUnimplemented();
 }

 public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
 {
  throw new StillUnimplemented();
 }

 public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
 {
  throw new StillUnimplemented();
 }

 public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)
 {
  throw new StillUnimplemented();
 }

}
