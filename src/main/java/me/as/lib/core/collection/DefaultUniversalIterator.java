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


import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.lang.ArrayExtras;

import java.util.*;


public class DefaultUniversalIterator<E> implements UniversalIterator<E>
{
 private Class elementClass;
 private int size;
 private Object oArray[]=null;
 private Collection cArray=null;


 public DefaultUniversalIterator(Object elements)
 {
  this(elements, null);
 }


 public DefaultUniversalIterator(Object elements, Class elClass)
 {
  if (elements==null)
  {
   if (elClass==null) elementClass=Object.class;
   else elementClass=elClass;
   size=0;
  }
  else
  {
   if (ArrayExtras.isArray(elements))
   {
    oArray=(Object[])elements;

    if (elClass==null) elementClass=ArrayExtras.getComponentType(oArray);
    else elementClass=elClass;

    size=ArrayExtras.length(oArray);
   }
   else
   {
    if (elements instanceof Collection)
    {
     cArray=(Collection)elements;
     size=cArray.size();
     if (elClass!=null) elementClass=elClass;
     else autoComputeElementClass();
    }
    else
    {
     if (elements instanceof Map)
     {
      cArray=new LinkedList();
      Map map=(Map)elements;

      for (Object key : map.keySet()) cArray.add(map.get(key));

      if (elClass!=null) elementClass=elClass;
      else autoComputeElementClass();
     }
     else
     {
      if (elements instanceof Iterator)
      {
       cArray=new LinkedList();
       Iterator i=(Iterator)elements;

       while (i.hasNext()) cArray.add(i.next());
      }
      else
      {
       throw new StillUnimplemented();
      }
     }
    }
   }
  }
 }



 private void autoComputeElementClass()
 {
  Object o;
  Class clas;
  ArrayList<Class> classes=new ArrayList<>();
  int t, len=size();

  for (t=0;t<len;t++)
  {
   o=get(t);
   if (o!=null)
   {
    clas=o.getClass();
    if (!classes.contains(clas)) classes.add(clas);
   }
  }

  if (classes.size()==1) elementClass=classes.get(0);
  else elementClass=Object.class;
 }




 public Class getElementClass()
 {
  return elementClass;
 }



 public int size()
 {
  return size;
 }



 public E get(int index)
 {
  E res=null;

  if (index<0 || index>=size) throw new ArrayIndexOutOfBoundsException(index);
  else
  {
   if (oArray!=null) res=(E)oArray[index];
   if (cArray!=null)
   {
    if (cArray instanceof List)
    {
     res=(E)((List)cArray).get(index);
    }
    else
    {
     // what todo ?
     throw new StillUnimplemented();
    }
   }
  }

  return res;
 }

}
