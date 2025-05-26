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

package me.as.lib.core.lang;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static me.as.lib.core.lang.ArrayExtras.select;
import static me.as.lib.core.lang.ClassExtras.getAllFields;
import static me.as.lib.core.lang.ClassExtras.getFieldValue_reallyBruteForce;
import static me.as.lib.core.lang.ClassExtras.setFieldValue_reallyBruteForce;


public class ObjectExtras
{
 // singleton
 private ObjectExtras(){}

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static final Object nullObject=new Object();

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static boolean basic_areEqual(Object obj1, Object obj2)
 {
  boolean res=(obj1==null && obj2==null);

  if (!res && (obj1!=null && obj2!=null))
  {
   res=(obj1==obj2);
   if (!res) res=obj1.equals(obj2);
  }

  return res;
 }



 public static boolean areEqual(Object a, Object b)
 {
  boolean res=basic_areEqual(a, b);

  if (!res && (a instanceof Map) && (b instanceof Map))
  {
   Map am=(Map)a;
   Map bm=(Map)b;

   if (am.size()==bm.size())
   {
    res=true;

    for (Object o : am.keySet())
    {
     if (!areEqual(am.get(o), bm.get(o)))
     {
      res=false;
      break;
     }
    }
   }

   return res;
  }

  if (!res)
  {
   boolean uncomputable=false;

   if (a!=null && b!=null)
   {
    Class c=a.getClass();
    String cn=c.getName();

    if (StringExtras.areEqual(cn, b.getClass().getName()))
    {
     if (cn.startsWith("["))
     {
      switch (select(Types.classes, c))
      {
       case Types._bytes   :res=ArrayExtras.areEqual((byte[])a, (byte[])b);break;
       case Types._shorts  :res=ArrayExtras.areEqual((short[])a, (short[])b);break;
       case Types._ints    :res=ArrayExtras.areEqual((int[])a, (int[])b);break;
       case Types._longs   :res=ArrayExtras.areEqual((long[])a, (long[])b);break;
       case Types._chars   :res=ArrayExtras.areEqual((char[])a, (char[])b);break;
       case Types._floats  :res=ArrayExtras.areEqual((float[])a, (float[])b);break;
       case Types._doubles :res=ArrayExtras.areEqual((double[])a, (double[])b);break;
       case Types._booleans:res=ArrayExtras.areEqual((boolean[])a, (boolean[])b);break;
       case Types._strings :res=StringExtras.areEqual((String[])a, (String[])b);break;
       default             :
        {
         if (cn.startsWith("[[")) uncomputable=true;
         else
         {
          int t, len=ArrayExtras.length(a);

          if (len==ArrayExtras.length(b))
          {
           Object aa[]=(Object[])a;
           Object bb[]=(Object[])b;

           res=true;

           for (t=0;t<len && res;t++)
           {
            res=areEqual(aa[t], bb[t]);
           }
          }
         }
        }
      }
     }
    }

    if (uncomputable)
    {
     throw new RuntimeException("ERROR: unable to compute areEqual for objects of class '"+cn+"'");
    }
   }
  }

  return res;
 }


 /**
  *
  * This method is used to copy the values of all enumerable own properties from one or more source objects to a target
  * object, when possibile, that means the values are copied when either the following conditions a) or b) occurs:
  *
  *    a) the target object has a field named as the soource field
  *    b) the target object has not a field named as the soource field but implements Map, in this case the value is set
  *       using put(fieldName, propertyValue)
  *
  * Note that for those sources which implements Map the copied properties are the keys/values of the Map.
  *
  * Properties in the target object will be overwritten by properties in the sources if they have the same key.
  * Later sources' properties will similarly overwrite earlier ones.
  *
  * Only non transient, non static fields are considered.
  *
  * target fields are set only if their Type isAssignable with source type, no casting of any type is made and no
  * exceptions are thrown when types do not match.
  *
  * WARNING: probably setters and getters should be used, and other different things could be implmeented but for the moment
  *          this method just does what is described.
  *
  * @param target
  * @param sources
  * @param <O>
  * @return the target object
  */
 public static <O> O assign(O target, Object... sources)
 {
  class Properties extends HashMap<String, Object>
  {
   Object source;
   boolean isMap;

   Properties(Object source)
   {
    this.source=source;
    isMap=(source instanceof Map);

    if (!isMap)
    {
     Field fs[]=getAllFields(source.getClass());
     int t, len=ArrayExtras.length(fs);

     for (t=0;t<len;t++)
     {
      int m=fs[t].getModifiers();

      if (!Modifier.isTransient(m) && !Modifier.isStatic(m))
      {
       String name=fs[t].getName();
       put(name, getFieldValue_reallyBruteForce(source, name));
      }
     }
    }
   }

   public Object get(String key)
   {
    if (isMap)
     return ((Map)source).get(key);
    else
     return super.get(key);
   }

   public Set<String> keySet()
   {
    if (isMap)
     return ((Map)source).keySet();
    else
     return super.keySet();
   }
  }

  O res=target;
  Map targetMap=(target instanceof Map ? (Map)target : null);
  Properties sourceP, targetP=new Properties(target);
  Object source;
  int t, len=ArrayExtras.length(sources);

  for (t=0;t<len;t++)
  {
   source=sources[t];

   if (source!=null)
   {
    sourceP=new Properties(source);

    if (targetMap!=null)
    {
     for (String key : sourceP.keySet())
      targetMap.put(key, sourceP.get(key));
    }
    else
    {
     Set<String> sks=sourceP.keySet();

     for (String key : targetP.keySet())
     {
      if (sks.contains(key))
       setFieldValue_reallyBruteForce(target, key, sourceP.get(key));
     }
    }
   }
  }

  return res;
 }


 public static <O> O itOrNullObject(O it)
 {
  if (it!=null)
   return it;
  else
   return (O)nullObject;
 }




 public static <K, V> HashMap<K, V> newHashMap(Object keys_values[])
 {
  HashMap res=null;
  int t, len=ArrayExtras.length(keys_values);

  if (len>0)
  {
   res=new HashMap();

   for (t=0;t<len;t+=2)
    res.put(keys_values[t], keys_values[t+1]);

  }

  return res;
 }

 public static HashMap specularHashMap(HashMap m)
 {
  HashMap res=null;

  if (m!=null)
  {
   res=new HashMap();
   Object o;
   int len=m.size();

   for (Iterator i=m.keySet().iterator();i.hasNext();)
   {
    o=i.next();
    res.put(m.get(o), o);
   }
  }

  return res;
 }


}




