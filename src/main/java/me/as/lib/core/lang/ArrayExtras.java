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


import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.collection.DefaultUniversalIterator;
import me.as.lib.core.collection.ListHashMap;
import me.as.lib.core.collection.UniversalIterator;
import me.as.lib.core.math.RandomExtras;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.as.lib.core.lang.Types.javaPrimitivesNames;


public class ArrayExtras
{
 // singleton
 private ArrayExtras(){}


 public static boolean isArray(Object o)
 {
  return (o!=null && o.getClass().isArray());
 }


 public static Class getElementTypeOfArrayOfPrimitive(Object o)
 {
  Class res=getElementTypeOfArray(o);

  if (StringExtras.select(javaPrimitivesNames, res.getName())<0)
  {
   throw new RuntimeException("'o' is not an array of elements of primitive type! ("+res.getName()+")");
  }

  return res;
 }


 public static Class getElementTypeOfArray(Object o)
 {
  Class res;

  try
  {
   Array.getLength(o); // throws IllegalArgumentException if o is not an array
   res=o.getClass().getComponentType();
   while (res.getName().startsWith("[")) res=res.getComponentType();
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("'o' is not an array!");
  }

  return res;
 }




 public static boolean isArrayOfPrimitive(Object o)
 {
  boolean res=true;

  try
  {
   getElementTypeOfArrayOfPrimitive(o);
  }
  catch (Throwable tr)
  {
   res=false;
  }

  return res;
 }

 public static void main(String args[])
 {
  String o[]=new String[]{"", "", ""};

  forEach(o, s ->
  {
   int t=s.length();
  });

 }


 public static <T> void forEach(Object array, Class<T> tClass, Consumer<T> consumer)
 {
  if (length(array)>0)
  {
   block:
   {
    if (isArray(array))
    {
     forEach((T[])array, consumer);
     break block;
    }

    if (array instanceof Iterable)
    {
     forEach((Iterable<T>)array, consumer);
     break block;
    }

    if (array instanceof Enumeration)
    {
     forEach((Enumeration<T>)array, consumer);
     break block;
    }

    throw new RuntimeException("Don't know how to make forEach for this kind of object "+array.getClass().getName());
   }
  }
 }


 public static <T> void forEach(T array[], Consumer<T> consumer)
 {
  if (hasElements(array))
   Arrays.asList(array).forEach(consumer);
 }

 public static <T> void forEach(Iterable<T> array, Consumer<T> consumer)
 {
  if (array!=null)
   array.forEach(consumer);
 }

 public static <T> void forEach(Enumeration<T> array, Consumer<T> consumer)
 {
  if (array!=null)
   for (;array.hasMoreElements();) consumer.accept(array.nextElement());
 }


 public static boolean hasElements(Object array)
 {
  return length(array)>0;
 }

 public static int length(Object array)
 {
  int res=0;

  if (array!=null)
  {
   if (array.getClass().isArray())
   {
    res=Array.getLength(array);
   }
   else
   {
    if (array instanceof Collection)
    {
     res=((Collection)array).size();
    }
    else
    {
     if (array instanceof Map)
     {
      res=((Map)array).size();
     }
     else
     {
      throw new IllegalArgumentException("'array' is neither an array nor a Collection nor a Map");
     }
    }
   }
  }

  return res;
 }


 public static Object addAll(Collection c, Object array)
 {
  int t, len=length(array);
  for (t=0;t<len;t++) c.add(Array.get(array, t));
  return array;
 }


 public static Object changeArrayType(Object array, String newElementType)
 {
  Object res;

  try
  {
   res=changeArrayType(array, Class.forName(newElementType));
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }


 public static Object changeArrayType(Object array, Class newElementType)
 {
  int t, len=length(array);
  Object res=Array.newInstance(newElementType, len);

  for (t=0;t<len;t++)
  {
   Array.set(res, t, Array.get(array, t));
  }

  return res;
 }



 public static int nullsCount(Object array)
 {
  int res=0;
  Object arr[]=(Object[])array;
  int t, len=length(arr);

  for (t=0;t<len;t++) if (arr[t]==null) res++;

  return res;
 }




 // returns the index of the object 'compare' in the array 'objs[]'
 // if 'compare' is not present in the array 'objs[]' returns -1;
 public static int select(Object array, Object compare)
 {
  int res=-1;
  int t, len=length(array);

  if (len>0)
  {
   if (array.getClass().isArray())
   {
    Object objs[]=(Object[])array;

    for (t=0;t<len && res==-1;t++)
    {
     if (ObjectExtras.areEqual(objs[t], compare)) res=t;
    }
   }
   else
   {
    if (array instanceof Collection)
    {
     Iterator iterator=((Collection)array).iterator();

     for (t=0;t<len && res==-1;t++)
     {
      if (ObjectExtras.areEqual(iterator.next(), compare)) res=t;
     }
    }
    else
    {
     throw new IllegalArgumentException("'array' is neither an array nor a Collection");
    }
   }
  }

  return res;
 }






 public static boolean areEqual(byte a[], byte b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }

 public static boolean areEqual(short a[], short b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(int a[], int b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(long a[], long b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(char a[], char b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(float a[], float b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(double a[], double b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }


 public static boolean areEqual(boolean a[], boolean b[])
 {
  boolean res=((a==null && b==null) || (a!=null && b!=null && a.length==b.length));
  if (res) for (int t=0;t<a.length && res;t++) res=(a[t]==b[t]);
  return res;
 }

 public static boolean areEqual(String a[], String b[])
 {
  return StringExtras.areEqual(a, b);
 }

 public static boolean areEqual(Object a, Object b)
 {
  return ObjectExtras.areEqual(a, b);
 }









 public static int indexOf(Object arrayToSearchInside, int startIndexInArrayToSearchInside, Object elementToFind)
 {
  if (length(arrayToSearchInside)==0) return -1;

  switch (select(Types.classes, arrayToSearchInside.getClass()))
  {
   case 10 /* byte[]    */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new byte[]{(Byte)elementToFind}, 0, 1);
   case 11 /* short[]   */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new short[]{(Short)elementToFind}, 0, 1);
   case 12 /* int[]     */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new int[]{(Integer)elementToFind}, 0, 1);
   case 13 /* long[]    */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new long[]{(Long)elementToFind}, 0, 1);
   case 14 /* char[]    */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new char[]{(Character)elementToFind}, 0, 1);
   case 15 /* float[]   */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new float[]{(Float)elementToFind}, 0, 1);
   case 16 /* double[]  */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new double[]{(Double)elementToFind}, 0, 1);
   case 17 /* boolean[] */:return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new boolean[]{(Boolean)elementToFind}, 0, 1);
   default:                return indexOf(arrayToSearchInside, startIndexInArrayToSearchInside, new Object[]{elementToFind}, 0, 1);
  }
 }


 public static int indexOf(Object arrayToSearchInside, int startIndexInArrayToSearchInside, Object arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res;

  switch (select(Types.classes, arrayToSearchInside.getClass()))
  {
   case 10 /* byte[]    */:res=_indexOf((byte[]   )arrayToSearchInside, startIndexInArrayToSearchInside, (byte[]   )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 11 /* short[]   */:res=_indexOf((short[]  )arrayToSearchInside, startIndexInArrayToSearchInside, (short[]  )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 12 /* int[]     */:res=_indexOf((int[]    )arrayToSearchInside, startIndexInArrayToSearchInside, (int[]    )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 13 /* long[]    */:res=_indexOf((long[]   )arrayToSearchInside, startIndexInArrayToSearchInside, (long[]   )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 14 /* char[]    */:res=_indexOf((char[]   )arrayToSearchInside, startIndexInArrayToSearchInside, (char[]   )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 15 /* float[]   */:res=_indexOf((float[]  )arrayToSearchInside, startIndexInArrayToSearchInside, (float[]  )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 16 /* double[]  */:res=_indexOf((double[] )arrayToSearchInside, startIndexInArrayToSearchInside, (double[] )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   case 17 /* boolean[] */:res=_indexOf((boolean[])arrayToSearchInside, startIndexInArrayToSearchInside, (boolean[])arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
   default:                res=_indexOf((Object[] )arrayToSearchInside, startIndexInArrayToSearchInside, (Object[] )arrayToFind, offsetOfArrayToFind, lengthOfArrayToFind);break;
  }

  return res;
 }


 private static int _indexOf(byte[] arrayToSearchInside, int startIndexInArrayToSearchInside, byte[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }



 private static int _indexOf(short[] arrayToSearchInside, int startIndexInArrayToSearchInside, short[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }



 private static int _indexOf(int[] arrayToSearchInside, int startIndexInArrayToSearchInside, int[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }




 private static int _indexOf(long[] arrayToSearchInside, int startIndexInArrayToSearchInside, long[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }


 private static int _indexOf(char[] arrayToSearchInside, int startIndexInArrayToSearchInside, char[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }


 private static int _indexOf(float[] arrayToSearchInside, int startIndexInArrayToSearchInside, float[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }



 private static int _indexOf(double[] arrayToSearchInside, int startIndexInArrayToSearchInside, double[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }


 private static int _indexOf(boolean[] arrayToSearchInside, int startIndexInArrayToSearchInside, boolean[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (arrayToSearchInside[t]==arrayToFind[nextOk+1+offsetOfArrayToFind])
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }


 private static int _indexOf(Object[] arrayToSearchInside, int startIndexInArrayToSearchInside, Object[] arrayToFind, int offsetOfArrayToFind, int lengthOfArrayToFind)
 {
  int res=-1;
  int t, len=arrayToSearchInside.length;
  int firstPos=-1, nextOk=-1;

  for (t=startIndexInArrayToSearchInside;t<len;t++)
  {
   if (ObjectExtras.areEqual(arrayToSearchInside[t], arrayToFind[nextOk+1+offsetOfArrayToFind]))
   {
    if (firstPos==-1) firstPos=t;
    nextOk++;
    if (nextOk==lengthOfArrayToFind-1) return firstPos;
   }
   else
   {
    if (firstPos>-1)
    {
     t=firstPos+1;
     firstPos=-1;
    }

    nextOk=-1;
   }
  }

  return res;
 }


 /**
  * Sets 'newElement' at 'index' of array (or java.extra.List) 'array'
  *
  * @param array       the array
  * @param index       at what index newElement shoudl be set
  * @param newElement  the element to set at index
  * @return            the element that was at index before setting it to newElement
  */
 public static Object setElementAt(Object array, int index, Object newElement)
 {
  Object res=elementAt(array, index);

  if (isArray(array))
  {
   Array.set(array, index, newElement);
  }
  else
  {
   if (array instanceof List)
   {
    ((List)array).set(index, newElement);
   }
   else
   {
    throw new RuntimeException("Dunno how to do 'setElementAt' for an istance of '"+array.getClass().getName()+"'");
   }
  }

  return res;
 }


 public static Object elementAt(Object array, int index)
 {
  if (array==null) throw new NullPointerException("array is null!");

  Object res;

  if (isArray(array))
  {
   res=Array.get(array, index);
  }
  else
  {
   if (array instanceof List)
   {
    res=((List)array).get(index);
   }
   else
   {
    throw new RuntimeException("Dunno how to do 'elementAt' for an istance of '"+array.getClass().getName()+"'");
   }
  }

  return res;
 }






 public static <A> A[] purgeNulls(A array[])
 {
  int nc=nullsCount(array);
  if (nc==0) return array;
  Object res;

  try
  {
   Object v;
   int t, ri=0, len=length(array);
   Class elemClass=array.getClass().getComponentType();
   res=Array.newInstance(elemClass, len-nc);

   for (t=0;t<len;t++)
   {
    v=Array.get(array, t);
    if (v!=null) Array.set(res, ri++, v);
   }

  } catch (Throwable tr){throw new RuntimeException(tr);}

  return (A[])res;
 }





 public static Object purgeEquals(Object array)
 {
  Object res;

  if (length(array)==0) res=array;
  else
  {
   try
   {
    LinkedList ll=new LinkedList();
    Class elemClassName=array.getClass().getComponentType();
    Object arr[]=(Object[])array;
    int t, len=length(arr);

    for (t=0;t<len;t++)
    {
     if (!ll.contains(arr[t])) ll.add(arr[t]);
    }

    res=ll.toArray((Object[])Array.newInstance(elemClassName, ll.size()));

   } catch (Throwable tr){res=null;tr.printStackTrace();}
  }

  return res;
 }


 public static <A> A clone(A array, int off, int len)
 {
  Object res=null;

  if (array!=null)
  {
   if (isArray(array))
   {
    try
    {
     res=Array.newInstance(array.getClass().getComponentType(), len);
     if (len>0) System.arraycopy(array, off, res, 0, len);
    } catch (Throwable ignore){}
   }
   else
   {
    if (array instanceof Collection)
    {
     // Collection collection=(Collection)ClassExtras.newInstanceByClass(array.getClass());
     Collection collection;

     try
     {
      collection=(Collection)ClassExtras.newInstanceByClass(array.getClass());
     }
     catch (Throwable tr)
     {
      collection=new ArrayList();
     }

     if (len>0)
     {
      if (off==0 && len==((Collection)array).size())
      {
       collection.addAll((Collection)array);
      }
      else
      {
       collection.addAll(Arrays.asList(((Collection)array).toArray()).subList(off, len));
      }
     }

     res=collection;
    }
    else
    {
     if (array instanceof Map)
     {
      try
      {
       res=array.getClass().getMethod("clone").invoke(array);
      }
      catch (Throwable tr)
      {
       throw new RuntimeException();
      }
     }
     else
     {
      throw new RuntimeException("Dunno how to clone an istance of '"+array.getClass().getName()+"'");
     }
    }
   }
  }

  return (A)res;
 }



 // it also clones Map
 public static <A> A clone(A array)
 {
  if (array!=null) return clone(array, 0, length(array));
  else return null;
 }


 public static <T> Collection<T> toCollection(T array[])
 {
  return toList(array);
 }

 public static <T> List<T> toList(T array[])
 {
  int len=length(array);
  ArrayList res=new ArrayList(length(array));
  if (len>0) for (int t=0;t<len;t++) res.add(Array.get(array, t));
  return res;
 }

 public static <A> A[] toArray(Object o)
 {
  return (A[])toArray(o, null, -1);
 }


 public static <C> C[] toArray(Object o, Class<C> componentType)
 {
  return toArray(o, componentType, -1);
 }

 public static <C> C[] toArray(Object o, Class<C> componentType, int maxResultArraySize)
 {
  if (o==null) return null;

  if (isArray(o))
  {
   if (maxResultArraySize<0 || length(o)==maxResultArraySize)
    return (C[])o;
   else
    toArray(Arrays.asList((Object[])o), componentType, maxResultArraySize);
  }

  if (o instanceof Iterable) return _i_toArray((Iterable)o, maxResultArraySize, componentType);
  if (o instanceof Iterator) return _i_toArray(() -> (Iterator)o, maxResultArraySize, componentType);
  if (o instanceof Map) return _i_toArray(((Map)o).values(), maxResultArraySize, componentType);
  if (o instanceof ListHashMap) return _i_toArray(((ListHashMap)o).values(), maxResultArraySize, componentType);

  throw new StillUnimplemented("Don't know how to convert to an array an instance of type '"+o.getClass().getName()+"'");
 }



 private static <C> C[] _i_toArray(Iterable i, int maxResultArraySize, Class<C> componentType)
 {
  if (i==null) return null;
  int len;
  Iterator it;

  if (i instanceof Collection) len=((Collection)i).size();
  else
  {
   ArrayList tmpAl=new ArrayList();
   it=i.iterator();
   while (it.hasNext()) {tmpAl.add(it.next());}
   i=tmpAl;
   len=tmpAl.size();
  }

  if (maxResultArraySize>=0 && len>maxResultArraySize) len=maxResultArraySize;
  Object res=null;

  if (len>0)
  {
   int t=0;
   it=i.iterator();
   Object next;

   while (it.hasNext() && t<len)
   {
    next=it.next();

    if (res==null)
    {
     if (componentType==null) componentType=(Class<C>)next.getClass();
     res=Array.newInstance(componentType, len);
    }

    Array.set(res, t++, next);
   }
  }

  return (C[])res;
 }


 public static String[] safeToArrayOfStrings(Object arrayOrSimilar, int off)
 {
  String res[];

  try
  {
   res=toArrayOfStrings(arrayOrSimilar, off);
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }


 public static String[] toArrayOfStrings(Object arrayOrSimilar, int off)
 {
  return toArrayOfStrings(arrayOrSimilar, off, length(arrayOrSimilar)-off);
 }

 public static String[] toArrayOfStrings(Object arrayOrSimilar, int off, int len)
 {
  Object o[]=new Object[len];

  for (int t=0;t<len;t++)
  {
   o[t]=elementAt(arrayOrSimilar, t+off);
  }

  return toArrayOfStrings(o);
 }


 public static String[] toArrayOfStrings(Object arrayOrSimilar)
 {
  String res[]=null;
  Object to, oa[]=toArray(arrayOrSimilar, Object.class);
  int t, len=length(oa);

  if (len>0)
  {
   res=new String[len];

   for (t=0;t<len;t++)
   {
    if (oa[t]!=null)
    {
     if (oa[t] instanceof Reference)
     {
      to=((Reference)oa[t]).get();

      if (to!=null)
       res[t]=to.toString();
      else
       res[t]="";
     }
     else
      res[t]=oa[t].toString();
    }
    else
     res[t]="";
   }
  }

  return res;
 }


 /**
  * PLESE NOTE: THIS DOES NOT CREATE A NEW ARRAY, THIS REVERSES THE PASSED ONE!!
  *
  * @param array
  * @param <A>
  * @return
  */
 public static <A> A reverse(Object array)
 {
  if (array instanceof List)
  {
   List orig=(List)array;
   int t, len=orig.size();
   int oppo=len-1;
   Object swap;

   if (len>1)
   {
    for (t=0;t<len;t++)
    {
     swap=orig.get(t);
     orig.set(t, orig.get(oppo));
     orig.set(oppo, swap);
     oppo--;
     if (oppo<=t) break;
    }
   }

   return (A)orig;
  }
  else
  {
   int t, len=length(array);
   int oppo=len-1;
   Object swap;

   if (len>1)
   {
    for (t=0;t<len;t++)
    {
     swap=Array.get(array, t);
     Array.set(array, t, Array.get(array, oppo));
     Array.set(array, oppo, swap);
     oppo--;
     if (oppo<=t) break;
    }
   }

   return (A)array;
  }
 }








 /**
  *   Shuffles all the elements inside 'array'
  *
  * @param array bla bla bla
  * @return it!
  */
 public static Object shuffle(Object array)
 {
  Random rnd=RandomExtras.getRandomNumberGenerator();
  return shuffle(rnd, RandomExtras.intRandom(2, 4), rnd.nextDouble()>0.5, 1.0, array);
 }


 public static Object shuffle(Random rnd, int rounds, boolean reverseEachRound, double probPerElement, Object array)
 {
  int newi, i, t, len=length(array);

  for (i=0;i<rounds;i++)
  {
   if (reverseEachRound) reverse(array);

   for (t=0;t<len;t++)
   {
    newi=t+rnd.nextInt(len-t);

    if (t!=newi && (probPerElement>=1.0 || rnd.nextDouble()<probPerElement))
    {
     swapElements(array, t, newi);
    }
   }
  }

  return array;
 }



 public static Object swapElements(Object array, int idx1, int idx2)
 {
  Object swap=Array.get(array, idx1);
  Array.set(array, idx1, Array.get(array, idx2));
  Array.set(array, idx2, swap);
  return array;
 }









 public static Class getComponentType(Object objArray[])
 {
  return ((objArray!=null) ? objArray.getClass().getComponentType() : null);
 }



 public static int[] toInts(Object objArray[])
 {
  int res[]=null;
  int len=length(objArray);

  if (len>0)
  {
   int t=0;
   res=new int[len];

   for (t=0;t<len;t++)
   {
    res[t]=Integer.parseInt(objArray[t].toString().trim());
   }
  }

  return res;
 }


 public static String[] toStrings(Iterator it)
 {
  return (String[])toObjects(it, String.class);
 }

 public static Object toObjects(Iterator it, Class arrayElementType)
 {
  return toObjects(it, arrayElementType, false);
 }


 public static Object toObjects(Iterator it, String arrayElementType)
 {
  return toObjects(it, arrayElementType, false);
 }


 public static Object toObjects(Iterator it, String arrayElementType, boolean reversing)
 {
  Object res;

  try
  {
   res=toObjects(it, Class.forName(arrayElementType), reversing);
  } catch (Throwable tr){res=null;}

  return res;
 }


 public static Object toObjects(Iterator it, Class arrayElementType, boolean reversing)
 {
  Object res=null;

  List l=new ArrayList();
  int i, t, size;

  for (;it.hasNext();) l.add(it.next());

  size=l.size();

  if (size>0)
  {
   res=Array.newInstance(arrayElementType, size);

   for (t=0;t<size;t++)
   {
    i=((reversing) ? size-(t+1) : t);

    try
    {
     Array.set(res, i, l.get(t));
    } catch (Throwable tr){Array.set(res, i, null);}
   }
  }

  return res;
 }






 public static int find(Object obj, Object objs[], int startIdx)
 {
  int res=-1;
  int t, len=length(objs);

  for (t=startIdx;t<len && res<0;t++)
  {
   if (ObjectExtras.areEqual(obj, objs[t])) res=t;
  }

  return res;
 }


 public static Object merge(Object array1, Object array2)
 {
  Object res=null;

  if ((array1==null || array2==null) ||
      (array1!=null && array2!=null && array1.getClass().getComponentType()==array2.getClass().getComponentType()))
  {
   if (array1!=null && array2==null) res=array1;
   else
   {
    if (array1==null && array2!=null) res=array2;
    else
    {
     if (array1!=null && array2!=null)
     {
      int l1=length(array1);
      int l2=length(array2);

      res=Array.newInstance(array1.getClass().getComponentType(), l1+l2);
      System.arraycopy(array1, 0, res, 0, l1);
      System.arraycopy(array2, 0, res, l1, l2);
     }
    }
   }
  }
  else
  {
   throw new RuntimeException("Types of the arrays are different");
  }

  return res;
 }



 public static Object prepose(Object newElementToAddAtStart, Object array)
 {
  Object res;

  if (array==null)
  {
   res=Array.newInstance(newElementToAddAtStart.getClass(), 1);
   Array.set(res, 0, newElementToAddAtStart);
  }
  else
  {
   int len=Array.getLength(array);
   res=Array.newInstance(array.getClass().getComponentType(), len+1);
   System.arraycopy(array, 0, res, 1, len);
   Array.set(res, 0, newElementToAddAtStart);
  }

  return res;
 }







 public static <O> O append(O array, Object newElementToAddAtEnd)
 {
  return append(array, newElementToAddAtEnd, null);
 }


 /**
  *
  *
  *
  * @param array                     the array
  * @param newElementToAddAtEnd      the single element to add
  * @return
  */
 public static <O> O append(O array, Object newElementToAddAtEnd, Supplier<O> creatorForWhenArrayIsNull)
 {
  Object res;

  if (array==null)
  {
   if (creatorForWhenArrayIsNull!=null)
    return append(creatorForWhenArrayIsNull.get(), newElementToAddAtEnd, null);

   res=Array.newInstance(newElementToAddAtEnd.getClass(), 1);
   Array.set(res, 0, newElementToAddAtEnd);
  }
  else
  {
   if (array instanceof Collection)
   {
    res=array;
    ((Collection)res).add(newElementToAddAtEnd);
   }
   else
   {
    int len=Array.getLength(array);
    res=Array.newInstance(array.getClass().getComponentType(), len+1);
    System.arraycopy(array, 0, res, 0, len);
    Array.set(res, len, newElementToAddAtEnd);
   }
  }

  return (O)res;
 }




 public static Object appendAll(Object array, Object stuffToAdd)
 {
  Object res;
  UniversalIterator ui=universalIterator(stuffToAdd);
  int t, len=ui.size();

  if (len>0)
  {
   if (array==null)
   {
    res=Array.newInstance(ui.getElementClass(), len);

    for (t=0;t<len;t++)
    {
     Array.set(res, t, ui.get(t));
    }
   }
   else
   {
    if (array instanceof Collection)
    {
     Collection col=(Collection)array;
     for (t=0;t<len;t++)
     {
      col.add(ui.get(t));
     }

     res=col;
    }
    else
    {
     int olen=Array.getLength(array);
     res=Array.newInstance(array.getClass().getComponentType(), olen+len);
     System.arraycopy(array, 0, res, 0, olen);

     for (t=0;t<len;t++)
     {
      Array.set(res, t+olen, ui.get(t));
     }
    }
   }
  } else res=array;

  return res;
 }



 public static UniversalIterator universalIterator(Object array)
 {
  return new DefaultUniversalIterator(array);
 }




 public static <A> A[] remove(A array[], A elementToRemove)
 {
  A res[]=array;
  int idx=indexOf(array, 0, elementToRemove);

  if (idx>=0)
  {
   Array.set(array, idx, null);
   res=purgeNulls(array);
  }

  return res;
 }


 public static <A> A[] removeLastElement(A array[])
 {
  return removeLastElements(array, 1);
 }


 public static <A> A[] removeLastElements(A array[], int howManyLastElements)
 {
  A res[]=array;

  if (howManyLastElements>0)
  {
   int arrLen=length(array);

   for (int t=0;t<howManyLastElements;t++)
   {
    Array.set(array, arrLen-1-t, null);
   }

   res=purgeNulls(array);
  }

  return res;

 }

 public static <A> A[] removeAt(A array[], int index)
 {
  A res[]=array;

  if (index>=0)
  {
   Array.set(array, index, null);
   res=purgeNulls(array);
  }

  return res;
 }


 public static boolean containsAtLeastOneOfThose(Object array[], Object those[])
 {
  boolean res=false;

  if (length(array)>0)
  {
   int t, len=length(those);

   if (len>0)
   {
    for (t=0;t<len && !res;t++)
    {
     res=contains(array, those[t]);
    }
   }
  }

  return res;
 }



 public static boolean contains(Object array[], Object contained)
 {
  boolean res=false;
  int t, len=length(array);

  if (len>0)
  {
   for (t=0;t<len && !res;t++)
   {
    if (array[t]==null && contained==null) res=true;
    else
    {
     if (contained!=null && array[t]!=null)
     {
      res=contained.equals(array[t]);
     }
    }
   }
  }

  return res;
 }


 public static boolean contains(int values[], int value)
 {
  boolean res=false;
  int len=((values!=null)?values.length:0);

  if (len>0)
  {
   for (int t=0;t<len && !res;t++)
   {
    res=(values[t]==value);
   }
  }

  return res;
 }



}
