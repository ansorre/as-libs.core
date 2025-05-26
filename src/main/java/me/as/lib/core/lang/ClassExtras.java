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


import me.as.lib.core.collection.RamTable;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.system.FileSystemExtras;
import me.as.lib.core.system.OSExtras;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static me.as.lib.core.concurrent.ThreadExtras.getAllRunningThreads;
import static me.as.lib.core.lang.ResourceExtras.listResources;
import static me.as.lib.core.lang.StringExtras.doTheyMatch;
import static me.as.lib.core.lang.StringExtras.endsWith;
import static me.as.lib.core.lang.StringExtras.splitLast;
import static me.as.lib.core.system.FileSystemExtras.adjustPath;
import static me.as.lib.core.system.FileSystemExtras.mergePath;
import static me.as.lib.core.lang.StringExtras.getOccurrencesCount;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.indexOf;
import static me.as.lib.core.lang.StringExtras.mergeEnclosing;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.lang.StringExtras.select;
import static me.as.lib.core.lang.StringExtras.unmerge;
import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;


public class ClassExtras implements Types
{
 // singleton
 private ClassExtras(){}


 public static final String specialClasses[]=new String[]
 {
  "java.lang.Byte",
  "java.lang.Short",
  "java.lang.Integer",
  "java.lang.Long",
  "java.lang.Character",
  "java.lang.Float",
  "java.lang.Double",
  "java.lang.Boolean",
  "java.lang.String"
 };


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static boolean isInstanceOf(Class left, Class right)
 {
  // almost equivalent to  left instanceof right
  return right.isAssignableFrom(left);
 }




 public static boolean isByteType(Class clazz)
 {
  return (clazz==Byte.class || clazz==Byte.TYPE);
 }

 public static boolean isShortType(Class clazz)
 {
  return (clazz==Short.class || clazz==Short.TYPE);
 }

 public static boolean isIntegerType(Class clazz)
 {
  return (clazz==Integer.class || clazz==Integer.TYPE);
 }

 public static boolean isLongType(Class clazz)
 {
  return (clazz==Long.class || clazz==Long.TYPE);
 }

 public static boolean isCharacterType(Class clazz)
 {
  return (clazz==Character.class || clazz==Character.TYPE);
 }

 public static boolean isFloatType(Class clazz)
 {
  return (clazz==Float.class || clazz==Float.TYPE);
 }

 public static boolean isDoubleType(Class clazz)
 {
  return (clazz==Double.class || clazz==Double.TYPE);
 }

 public static boolean isBooleanType(Class clazz)
 {
  return (clazz==Boolean.class || clazz==Boolean.TYPE);
 }




 public static Class getPrimitiveClass(String name) throws ClassNotFoundException
 {
  Class res;
  int idx=StringExtras.select(javaPrimitivesNames, name);
  if (idx>=0 && idx<javaPrimitivesNames.length-1)
   res=classes[idx+1];
  else
   throw new ClassNotFoundException(name+" is not a java primitive class!");

  return res;
 }



 public static Object loadAndInstanciateWithClassLoader(String classLoaderClassName, String className)
 {
  Object res=null;

  try
  {
   ((ClassLoader)Class.forName(classLoaderClassName).getConstructor().newInstance()).loadClass(className).getConstructor().newInstance();
  }
  catch (Throwable tr)
  {
   systemErrDeepCauseStackTrace(tr);
  }

  return res;
 }


 public static Object loadAndInstanciateWithClassLoader(ClassLoader cl, String className)
 {
  Object res=null;

  try
  {
   res=cl.loadClass(className).getConstructor().newInstance();
  }
  catch (Throwable tr)
  {
   systemErrDeepCauseStackTrace(tr);
  }

  return res;
 }







 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static Class classFromName_NE(String className)
 {
  Class res;

  try
  {
   res=classFromName(className);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }


 public static Class classFromName(String className, String fallbackClassName) throws ClassNotFoundException
 {
  Class res;

  try
  {
   res=classFromName(className);
  }
  catch (ClassNotFoundException tr)
  {
   if (isNotBlank(fallbackClassName))
   {
    res=classFromName(getFallbackClassName(className, fallbackClassName));
   } else throw tr;
  }

  return res;
 }


 public static boolean classExists(String className)
 {
  boolean res;

  try
  {
   Class theClass=Class.forName(className);
   res=(theClass!=null);
  }
  catch (Throwable tr)
  {
   res=false;
  }

  return res;
 }


 public static Class classFromNameNoException(String className)
 {
  try
  {
   return classFromName(className);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


 public static Class classFromNameNE(String className)
 {
  try
  {
   return classFromName(className);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


 public static Class classFromName(String className) throws ClassNotFoundException
 {
  if (!isNotBlank(className)) throw new ClassNotFoundException("ClassExtras.classFromName exception: className is null");
  Class res;

  try
  {
   res=classesClasses[select(classesNames, className)];
  }
  catch (Throwable tr)
  {
   try
   {
    res=Class.forName(className);
   }
   catch (Throwable tr1)
   {
    if (StringExtras.areEqual(className, "byte[]")) res=byte[].class;
    else if (StringExtras.areEqual(className, "short[]")) res=short[].class;
    else if (StringExtras.areEqual(className, "int[]")) res=int[].class;
    else if (StringExtras.areEqual(className, "long[]")) res=long[].class;
    else if (StringExtras.areEqual(className, "char[]")) res=char[].class;
    else if (StringExtras.areEqual(className, "float[]")) res=float[].class;
    else if (StringExtras.areEqual(className, "double[]")) res=double[].class;
    else if (StringExtras.areEqual(className, "boolean[]")) res=boolean[].class;
    else if (StringExtras.areEqual(className, "byte[][]")) res=byte[][].class;
    else if (StringExtras.areEqual(className, "short[][]")) res=short[][].class;
    else if (StringExtras.areEqual(className, "int[][]")) res=int[][].class;
    else if (StringExtras.areEqual(className, "long[][]")) res=long[][].class;
    else if (StringExtras.areEqual(className, "char[][]")) res=char[][].class;
    else if (StringExtras.areEqual(className, "float[][]")) res=float[][].class;
    else if (StringExtras.areEqual(className, "double[][]")) res=double[][].class;
    else if (StringExtras.areEqual(className, "boolean[][]")) res=boolean[][].class;
    else if (StringExtras.areEqual(className, "B") || StringExtras.areEqual(className, "byte")) res=byte.class;
    else if (StringExtras.areEqual(className, "S") || StringExtras.areEqual(className, "short")) res=short.class;
    else if (StringExtras.areEqual(className, "I") || StringExtras.areEqual(className, "int")) res=int.class;
    else if (StringExtras.areEqual(className, "J") || StringExtras.areEqual(className, "long")) res=long.class;
    else if (StringExtras.areEqual(className, "C") || StringExtras.areEqual(className, "char")) res=char.class;
    else if (StringExtras.areEqual(className, "F") || StringExtras.areEqual(className, "float")) res=float.class;
    else if (StringExtras.areEqual(className, "D") || StringExtras.areEqual(className, "double")) res=double.class;
    else if (StringExtras.areEqual(className, "Z") || StringExtras.areEqual(className, "boolean")) res=boolean.class;
    else
    {
     if (className.startsWith("L") &&  className.endsWith(";"))
     {
      res=classFromName(className.substring(1, className.length()-1));
     }
     else
     {
      if (className.endsWith("[]"))
      {
       int i=className.indexOf('[');
       String claP=className.substring(0, i);
       String arrP=className.substring(i);
       int dims=getOccurrencesCount(arrP, "]");
       int dimsArr[]=new int[dims];
       for (int t=0;t<dims;t++) dimsArr[t]=0;
       Object arr=Array.newInstance(Class.forName(claP), dimsArr);
       res=arr.getClass();
      }
      else
      {
       throw new ClassNotFoundException("ClassExtras.classFromName exception", tr1);
      }
     }
    }
   }
  }

  return res;
 }



 public static Object newMemberClassInstanceByClass(Object outerClassInstance, Class clas)
 {
  Object res;

  try
  {
   Class oclas=outerClassInstance.getClass();

   Constructor ctor=clas.getConstructor(oclas);

   res=ctor.newInstance(outerClassInstance);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }



 public static Object newMemberClassInstanceByClassName(Object outerClassInstance, String className)
 {
  Object res;

  try
  {
   Class oclas=outerClassInstance.getClass();
   Class clas=oclas.getClassLoader().loadClass(className);

   Constructor ctor=clas.getConstructor(oclas);

   res=ctor.newInstance(outerClassInstance);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }


 public static Class[] toClasses(Object... instances)
 {
  Class res[]=null;
  int t, len=ArrayExtras.length(instances);

  if (len>0)
  {
   res=new Class[len];

   for (t=0;t<len;t++)
   {
    res[t]=instances[t].getClass();
   }
  }

  return res;
 }



 public static <T> T newInstanceByClass(Class<T> clazz, Object... initargs)
 {
  T res;

  try
  {
   res=clazz.getConstructor(toClasses(initargs)).newInstance(initargs);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }


 public static <T> T newInstanceByClass(Class<T> clazz)
 {
  T res;

  try
  {
   res=clazz.getConstructor().newInstance();
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }








 public static <T> T newInstanceByClassName(String className)
 {
  return (T)newInstanceByClassName(className, null);
 }


 public static Object newInstanceByClassName(ClassLoader cl, String className)
 {
  return newInstanceByClassName(cl, className, null);
 }



 public static Object newInstanceByClassName(String className, String fallbackClassName)
 {
  Object res;

  try
  {
   res=Class.forName(className).getConstructor().newInstance();
  }
  catch (Throwable tr)
  {
   if (isNotBlank(fallbackClassName))
   {
    res=newInstanceByClassName(getFallbackClassName(className, fallbackClassName), null);
   } else throw new RuntimeException(tr);
  }

  return res;
 }



 public static Object newInstanceByClassName(ClassLoader cl, String className, String fallbackClassName)
 {
  Object res;

  try
  {
   res=cl.loadClass(className).getConstructor().newInstance();
  }
  catch (Throwable tr)
  {
   if (isNotBlank(fallbackClassName))
   {
    res=newInstanceByClassName(cl, getFallbackClassName(className, fallbackClassName), null);
   } else throw new RuntimeException(tr);
  }

  return res;
 }



 private static String getFallbackClassName(String className, String fallbackClassName)
 {
  String res=null;

  if (isNotBlank(className) && isNotBlank(fallbackClassName))
  {
   String s[]=unmerge(className, '.');

   s[s.length-1]=fallbackClassName;

   res=mergeEnclosing(s, ".", null).substring(1);
  }

  return res;
 }



 public static Object newInnerClassInstanceByClassName(Object outerClassInstance, String className)
 {
  return newInnerClassInstanceByClassName(outerClassInstance.getClass(), outerClassInstance, className);
 }


 public static Object newInnerClassInstanceByClassName(Class outerClassClass, Object outerClassInstance, String className)
 {
  Object res;

  try
  {
   Class clazz=Class.forName(className);
   Constructor ctor=clazz.getConstructor(outerClassClass);
   res=ctor.newInstance(outerClassInstance);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .





 public static boolean doesImplement(Class _class, Class _interface)
 {
  return ArrayExtras.select(getClassAllSuperclassesAllInterfacesOf(_class), _interface)>=0;
 }



 public static String getClassNameWithoutPath(Object o)
 {
  return getClassNameWithoutPath(o.getClass());
 }

 public static String getClassNameWithoutPath(Class c)
 {
  String tmpStr=c.getName();
  return tmpStr.substring(tmpStr.lastIndexOf('.')+1);
 }




 public static Class[] getClassAllSuperclassesAllInterfacesOf(Object o)
 {
  return getClassAllSuperclassesAllInterfacesOf(o.getClass());
 }


 public static Class[] getClassAllSuperclassesAllInterfacesOf(Class theClass)
 {
  ArrayList classes=new ArrayList();

  getClassAllSuperclassesAllInterfacesOf(theClass, classes);

  Class res[]=(Class[])classes.toArray(new Class[classes.size()]);
  res=(Class[])ArrayExtras.purgeEquals(res);

  return res;
 }




 public static Class[] getClassAllInterfacesOf(Object o)
 {
  return getClassAllInterfacesOf(o.getClass());
 }


 public static Class[] getClassAllInterfacesOf(Class theClass)
 {
  Class res[]=getClassAllSuperclassesAllInterfacesOf(theClass);
  int t, len=ArrayExtras.length(res);

  for (t=0;t<len;t++)
  {
   if (!res[t].isInterface()) res[t]=null;
  }

  res=(Class[])ArrayExtras.purgeNulls(res);

  return res;
 }






 private static void getClassAllSuperclassesAllInterfacesOf(Class theClass, List classes)
 {
  classes.add(theClass);
  Class superClass=theClass.getSuperclass();

  if (superClass!=null)
  {
   getClassAllSuperclassesAllInterfacesOf(superClass, classes);
  }

  Class interfaces[]=theClass.getInterfaces();
  int t, len=ArrayExtras.length(interfaces);

  for (t=0;t<len;t++)
  {
   getClassAllSuperclassesAllInterfacesOf(interfaces[t], classes);
  }
 }


 public static Class[] getClassAllSuperclassesOf(Object o)
 {
  return getClassAllSuperclassesOf(o.getClass());
 }


 public static Class[] getClassAllSuperclassesOf(Class theClass)
 {
  ArrayList classes=new ArrayList();

  getClassAllSuperclassesOf(theClass, classes);

  Class res[]=(Class[])classes.toArray(new Class[classes.size()]);
  res=(Class[])ArrayExtras.purgeEquals(res);

  return res;
 }


 private static void getClassAllSuperclassesOf(Class theClass, List classes)
 {
  classes.add(theClass);
  Class superClass=theClass.getSuperclass();

  if (superClass!=null)
  {
   getClassAllSuperclassesOf(superClass, classes);
  }
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static String[] getAsManyTomcatClassPathItemsAsPossible() throws Throwable
 {
  LinkedList<String> lres=new LinkedList<String>();
  LinkedList<String> fres=new LinkedList<String>();
  String comlo=System.getProperty("common.loader");

  if (isNotBlank(comlo))
  {
   String tmpKey, tmpValue, cps[]=unmerge(comlo, ',');
   int idx, eidx;
   int t, len=ArrayExtras.length(cps);

   for (t=0;t<len;t++)
   {
    idx=indexOf(cps[t], "${");

    if (idx>=0)
    {
     try
     {
      eidx=indexOf(cps[t], "}", idx);
      tmpKey=cps[t].substring(idx+2, eidx);
      tmpValue=System.getProperty(tmpKey);
      tmpValue=replace(cps[t], "${"+tmpKey+"}", tmpValue);
      lres.add(adjustPath(tmpValue));
     } catch (Throwable ignore){}
    }
    else
    {
     lres.add(adjustPath(cps[t]));
    }
   }
  }

  String fname, tmpStr, fileMask;
  int idx, t, len=lres.size();

  for (t=0;t<len;t++)
  {
   tmpStr=lres.get(t);

   try
   {
    if (tmpStr.indexOf('*')>=0)
    {
     idx=tmpStr.lastIndexOf(File.separatorChar);
     fileMask=tmpStr.substring(idx+1);

     tmpStr=tmpStr.substring(0, idx);
     List<String> fs=FileSystemExtras.listAll(tmpStr, fileMask);
     int f, flen=ArrayExtras.length(fs);

     for (f=0;f<flen;f++)
     {
      fname=mergePath(tmpStr, fs.get(f));
      if (!fres.contains(fname)) fres.add(fname);
     }

     tmpStr=null;
    }
   }
   catch (Throwable tr)
   {
    tmpStr=null;
   }

   if (isNotBlank(tmpStr))
   {
    if (!fres.contains(tmpStr)) fres.add(tmpStr);
   }
  }

//  common.loader = C:/Disks/C/Programs/stable/64bit/Apache/Tomcat/lib,C:/Disks/C/Programs/stable/64bit/Apache/Tomcat/lib/*.jar,${catalina.home}/lib,${catalina.home}/lib/*.jar,${catalina.home}/shared_lib/classes,${catalina.home}/shared_lib/others/*.jar

  return ArrayExtras.toArrayOfStrings(fres);
 }




 public static String[] getAsManyClassPathItemsAsPossible()
 {
  String cp=System.getProperty("java.class.path");
  String cps[]=null;

  if (OSExtras.isSomeMicrosoftWindows())
  {
   cps=unmerge(cp, ';');
  }
  else
  {
   if (OSExtras.isSomeUnix())
   {
    cps=unmerge(cp, ':');
   } else throw new RuntimeException("Cannot execute listResources on current operating system ("+System.getProperty("os.name")+")");
  }

  String tryTomcat[];

  try
  {
   tryTomcat=getAsManyTomcatClassPathItemsAsPossible();
  }
  catch (Throwable tr)
  {
   tryTomcat=null;
  }

  if (ArrayExtras.length(tryTomcat)>0) cps=(String[])ArrayExtras.merge(cps, tryTomcat);

  return cps;
 }



 public static List<Class> getAllClassesInPackage(String packagePath, boolean alsoFromSubPackages)
 {
  // INITIAL time: 2s 569ms
  // and now: 120ms  WOW!!!!!! (I strongly modified for speed ResourceExtras.listResources)

  //  TimeCounter tc=TimeCounter.start();

  List<Class> res=new LinkedList<Class>();

  try
  {
   String s[]=ResourceExtras.listResources(packagePath, alsoFromSubPackages);
   if (!packagePath.endsWith(".")) packagePath+=".";

   int t, len=ArrayExtras.length(s);

   for (t=0;t<len;t++)
   {
    if (endsWith(s[t], ".class"))
    {
     try
     {
      String ss=packagePath+s[t];
      ss=ss.substring(0, ss.length()-6);

      res.add(Class.forName(ss));
     } catch (Throwable ignore){}
    }
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  //  System.out.println("time: "+tc.stopAndGetElapsedString());
  //  System.exit(0);

  return res;
 }


 public static <I> List<Class<I>> getClassesInPackageInstancing(String packag, Class<I> interfac)
 {
  List<Class<I>> res=new ArrayList<>();
  String ress[]=listResources(packag, true);

  int t, len=ArrayExtras.length(ress);

  for (t=0;t<len;t++)
  {
   if (ress[t].endsWith(".class"))
   {
    Class<I> jbc;
    String cName=StringExtras.replace(packag+"."+ress[t], "/", ".");
    cName=StringExtras.replace(cName, ".class", null).substring(1);
    cName=StringExtras.replace(cName, "..", ".");

    if (isInstanceOf(jbc=classFromNameNE(cName), interfac) &&
        !jbc.isInterface() &&
        !Modifier.isAbstract(jbc.getModifiers()))
     res.add(jbc);

   }
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .









 // ClassLoaderS


 public static void installClassLoaderWhereverPossible(String classOfNewClassLoader)
 {
  try
  {
   installClassLoaderWhereverPossible(Class.forName(classOfNewClassLoader));
  }
  catch (ClassNotFoundException cnfe)
  {
   cnfe.printStackTrace();
  }
 }


 public static void installClassLoaderWhereverPossible(Class classOfNewClassLoader)
 {
  try
  {
   Object cnstrctrArg[]=new Object[1];
   Constructor cnstrctr=classOfNewClassLoader.getConstructor(ClassLoader.class);
   ClassLoader ccl, mcl;
   Thread ct;
   Iterator ti=getAllRunningThreads().iterator();

   for (;ti.hasNext();)
   {
    ct=(Thread)ti.next();
    ccl=ct.getContextClassLoader();
    //if (ccl!=null)
    {
     try
     {
      cnstrctrArg[0]=ccl;
      mcl=(ClassLoader)cnstrctr.newInstance(cnstrctrArg);
      ct.setContextClassLoader(mcl);
      //System.out.println(ct.toString()+" oldCL="+ccl.toString()+" newCL="+mcl.toString());
     }
     catch (Throwable tr)
     {
      tr.printStackTrace();
     }
    }
   }
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }
 }



 public static RamTable getPublicStaticFinalFiels
 (
  Class cl,
  boolean includeSuperclassesAndInterfaces,
  Class fieldTypes[]
 )
 {
  return getPublicStaticFinalFiels(cl, includeSuperclassesAndInterfaces, fieldTypes, null);
 }



 public static RamTable getPublicStaticFinalFiels
 (
  Class cl,
  boolean includeSuperclassesAndInterfaces,
  Class fieldTypes[],
  String fieldNamesMatchs[]
 )
 {
  RamTable res=new RamTable();
  Class fieldClass, ac, cs[];
  Field fields[];
  int t, f, flen, mod, curRow=0;
  String key, fieldName;

  if (includeSuperclassesAndInterfaces)
  {
   cs=getClassAllSuperclassesAllInterfacesOf(cl);
  } else cs=new Class[]{cl};

  int c, clen=ArrayExtras.length(cs);

  for (c=0;c<clen;c++)
  {
   ac=cs[c];

   fields=ac.getDeclaredFields();
   flen=ArrayExtras.length(fields);

   for (f=0;f<flen;f++)
   {
    mod=fields[f].getModifiers();

    fieldClass=fields[f].getType();
    fieldName=fields[f].getName();

    if (Modifier.isPublic(mod) &&
        Modifier.isStatic(mod) &&
        Modifier.isFinal(mod) &&
        (ArrayExtras.find(fieldClass, fieldTypes, 0)>=0) &&
        doTheyMatch(fieldName, fieldNamesMatchs, true))
    {
     try
     {
      res.setObject(0, curRow, fieldClass);
      res.setString(1, curRow, fieldName);
      res.setObject(2, curRow, fields[f].get(null));
      curRow++;
     } catch (Throwable tr){tr.printStackTrace();}
    }
   }
  }

  return res;
 }



 /**
  * Given a method 'm' this method returns a very speed signature omittng a lot of stuff.
  * This is usefull only when you need a very speed (and when imprecision is acceptable) key
  * to use in HashMap and the like. Use at your own risk.
  * @param m
  * @return
  */
 public static String getVerySpeedSignature(Method m)
 {
  StringBuilder sb=new StringBuilder().append(getVerySpeedType(m.getReturnType())).append(" ").append(m.getName()).append("(");
  Class pts[]=m.getParameterTypes();
  int t, len=ArrayExtras.length(pts);

  if (len>0)
  {
   for (t=0;t<len;t++)
   {
    if (t>0) sb.append(",");
    sb.append(getVerySpeedType(pts[t]));
   }
  }

  sb.append(")");

  return sb.toString();
 }


 public static URL getFromWhereClassHasBeenLoaded(Object obj)
 {
  Class objc=obj.getClass();
  ClassLoader loader =objc .getClassLoader();
  String cres=replace(objc.getName(), ".", "/")+".class";
  return loader.getResource(cres);
 }


 /**
  * See the description for 'getVerySpeedSignature'
  * @param c
  * @return
  */
 public static String getVerySpeedType(Class c)
 {
  return getVerySpeedType(c.getName());
 }

 public static String getVerySpeedType(String className)
 {
  return splitLast(className, '.')[1];
 }


 public static String getClassParentPackagePath(Class c)
 {
  return getClassParentPackagePath(c, true);
 }

 public static String getClassParentPackagePath(Class c, boolean convertPointsToSlashes)
 {
  return getClassPackagePath(c, convertPointsToSlashes, 1);
 }


 public static String getClassPackagePath(Class c)
 {
  return getClassPackagePath(c, true);
 }

 public static String getClassPackagePath(Class c, boolean convertPointsToSlashes)
 {
  return getClassPackagePath(c, convertPointsToSlashes, 0);
 }


 public static String getClassPackagePath(Class c, boolean convertPointsToSlashes, int skipLast)
 {
  String res=c.getName();
  int idx;
  skipLast++;

  while (skipLast>0)
  {
   skipLast--;

   idx=res.lastIndexOf('.');
   if (idx>=0) res=res.substring(0, idx);
  }

  res+=".";
  if (convertPointsToSlashes) res="/"+replace(res, ".", "/");

  return res;
 }



 public static String[] splitToPackageAndName(Class clas)
 {
  return splitToPackageAndName(clas.getName());
 }

 public static String[] splitToPackageAndName(String className)
 {
  return splitLast(className, '.');
 }







 public static String getVerySpeedInstance(Object o)
 {
  return new StringBuilder(getVerySpeedType(o.getClass())).append('@').append(getJVMObjectInstance(o)).toString();
 }

 public static String getJVMObjectInstance(Object o)
 {
  String res;

  try
  {
   res=o.toString();
   res=res.substring(res.lastIndexOf('@')+1);
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }





 public static String getNearestCallingClassWithPublicStaticMainMethod()
 {
  String res=getNearestCallingClassWithPublicStaticMainMethod(Thread.currentThread());

  if (res==null)
  {
   Map<Thread, StackTraceElement[]> aste=Thread.getAllStackTraces();

   for (StackTraceElement ste[] : aste.values())
   {
    res=getNearestCallingClassWithPublicStaticMainMethod(ste);
    if (res!=null) break;
   }
  }

  return res;
 }


 public static String getNearestCallingClassWithPublicStaticMainMethod(Thread thread)
 {
  return getNearestCallingClassWithPublicStaticMainMethod(thread.getStackTrace());
 }


 private static String getNearestCallingClassWithPublicStaticMainMethod(StackTraceElement ste[])
 {
  String res=null;
  int t, len=ArrayExtras.length(ste);

  for (t=0;t<len && res==null;t++)
  {
   if (isStaticMainMethod(ste[t]))
   {
    res=ste[t].getClassName();
   }
  }

  return res;
 }






/*
 public static String getNearestCallingClassWithPublicStaticMainMethod()
 {
  String res=null;
  Throwable tr=new Throwable();
  StackTraceElement ste[]=tr.getStackTrace();
  int t, len=ArrayExtras.length(ste);

  for (t=0;t<len && res==null;t++)
  {
   if (isStaticMainMethod(ste[t]))
   {
    res=ste[t].getClassName();
   }
  }

  Thread th=Thread.currentThread();
  th.get


  return res;
 }
*/



 private static boolean isStaticMainMethod(StackTraceElement ste)
 {
  boolean res=false;

  if (ste.getMethodName().equals("main"))
  {
   try
   {
    Class c=Class.forName(ste.getClassName());
    Method m=c.getMethod("main", String[].class);
    int modifiers=m.getModifiers();
    res=(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers));
   } catch (Throwable ignore){}
  }

  return res;
 }






 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .




 public static boolean isNumeric(Object o)
 {
  return ((o instanceof Byte) ||
          (o instanceof Short) ||
          (o instanceof Integer) ||
          (o instanceof Long) ||
          (o instanceof Float) ||
          (o instanceof Double));
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static boolean hasAnnotation_itOrSupersOrInterfaces(Class clas, Class<? extends Annotation> annotationClass)
 {
  boolean res=false;
  Annotation annos[]=ClassExtras.getAllAnnotationsFromItAndSupersAndInterfaces(clas);
  int t, len=ArrayExtras.length(annos);

  for (t=0;t<len && !res;t++)
  {
   res=isInstanceOf(annos[t].getClass(), annotationClass);
  }

  return res;
 }



 public static <A extends Annotation> BoxFor2<A, Class>[] getAnnotationsFromItOrSupersOrInterfacesWithSourceClass(Class clas, Class<A> annotationClass)
 {
  BoxFor2<A, Class> res[]=null;
  BoxFor2<Annotation, Class> okAnnos[]=getAllAnnotationsFromItAndSupersAndInterfacesWithSourceClass(clas);
  int t, len=ArrayExtras.length(okAnnos);

  if (len>0)
  {
   List<BoxFor2<Annotation, Class>> allAnnos=new LinkedList<BoxFor2<Annotation, Class>>();

   for (t=0;t<len;t++)
   {
    if (isInstanceOf(okAnnos[t].element1.getClass(), annotationClass)) allAnnos.add(okAnnos[t]);
   }

   res=(BoxFor2<A, Class>[])ArrayExtras.toArray(allAnnos, BoxFor2.class);
  }

  return res;
 }



 public static <A extends Annotation> A[] getAnnotationsFromItOrSupersOrInterfaces(Class clas, Class<A> annotationClass)
 {
  List<A> lres=new LinkedList<A>();
  Annotation annos[]=ClassExtras.getAllAnnotationsFromItAndSupersAndInterfaces(clas);
  int t, len=ArrayExtras.length(annos);

  for (t=0;t<len;t++)
  {
   if (isInstanceOf(annos[t].getClass(), annotationClass)) lres.add((A)annos[t]);
  }

  return ArrayExtras.toArray(lres, annotationClass);
 }


 public static Annotation[] getAllAnnotationsFromItAndSupersAndInterfaces(Class clas)
 {
  Annotation res[]=null;
  BoxFor2<Annotation, Class> tos[]=getAllAnnotationsFromItAndSupersAndInterfacesWithSourceClass(clas);
  int t, len=ArrayExtras.length(tos);

  if (len>0)
  {
   res=new Annotation[len];
   for (t=0;t<len;t++) res[t]=tos[t].element1;
  }

  return res;
 }


 public static BoxFor2<Annotation, Class>[] getAllAnnotationsFromItAndSupersAndInterfacesWithSourceClass(Class clas)
 {
  BoxFor2<Annotation, Class> res[];
  Annotation tres[];
  List<BoxFor2<Annotation, Class>> allAnnos=new LinkedList<BoxFor2<Annotation, Class>>();
  Class alls[]=getClassAllSuperclassesAllInterfacesOf(clas);
  int a, alen, t, len=ArrayExtras.length(alls);

  for (t=0;t<len;t++)
  {
   tres=alls[t].getAnnotations();
   alen=ArrayExtras.length(tres);

   for (a=0;a<alen;a++)
   {
    allAnnos.add(new BoxFor2<Annotation, Class>(tres[a], alls[t]));
   }
  }

  res=(BoxFor2<Annotation, Class>[])ArrayExtras.toArray(allAnnos, BoxFor2.class);
  return res;
 }





 public static String getMethodKey(Method method)
 {
  StringBuilder sb=new StringBuilder(method.getName()).
          append("|").append(method.getReturnType().getName()).append("(");

  Class params[]=method.getParameterTypes();

  if (ArrayExtras.length(params)>0)
  {
   int z=0;
   for (Class p : params)
   {
    if (z>0) sb.append(",");
    sb.append(p.getName());
    z++;
   }
  }

  sb.append(")");

  return String.valueOf(sb.toString().hashCode());
 }




 /**
  *
  * @param c the class
  * @return all the fields of a class both public, private and so on, from this class as well from all the anchestor
  */
 public static Field[] getAllFields(Class c)
 {
  HashMap<String, Field> fields=new HashMap<>();
  Field fs[];

  Class cs[]=getClassAllSuperclassesAllInterfacesOf(c);
  int t, len=ArrayExtras.length(cs);

  for (t=0;t<len;t++)
  {
   fs=cs[t].getDeclaredFields();
   if (ArrayExtras.length(fs)>0) for (Field f : fs) fields.put(f.toString(), f);
  }

  return fields.values().toArray(new Field[fields.size()]);
 }


 public static void mapAllFields(Class c, Consumer<HashMap<String, Field>> consumer)
 {
  Field fields[]=getAllFields(c);
  int t, len=ArrayExtras.length(fields);
  final HashMap<String, Field> mappedFields=new HashMap<>();

  for (t=0;t<len;t++)
   mappedFields.put(fields[t].getName(), fields[t]);

  consumer.accept(mappedFields);
 }


 /**
  *
  * @param c class
  * @param a annotation
  * @return all the fields of a class both public, private and so on, from this class as well from all
  * the ancestors that are annotated with a
  */
 public static Field[] getAllAnnotatedFields(Class c, Class<? extends Annotation> a)
 {
  Field res[]=getAllFields(c);
  int t, len=ArrayExtras.length(res);
  ArrayList<Field> tmp=new ArrayList<Field>(len);

  for (t=0;t<len;t++)
  {
   if (res[t].getAnnotation(a)!=null) tmp.add(res[t]);
  }

  return ArrayExtras.toArray(tmp, Field.class);
 }



 public static Field[] getAllFieldsByType(Class c, Class type)
 {
  Field res[]=getAllFields(c);
  int t, len=ArrayExtras.length(res);
  ArrayList<Field> tmp=new ArrayList<Field>(len);

  for (t=0;t<len;t++)
  {
   if (isInstanceOf(res[t].getType(), type)) tmp.add(res[t]);
  }

  return ArrayExtras.toArray(tmp, Field.class);
 }




 private static final Object nullField=new Object();
 private static final Map<String, Object> classFieldName_field_cache=new HashMap<>();

 public static Field searchField(Class clas, String fieldName)
 {
  String key=clas.getName()+"|"+fieldName;
  Object cached=classFieldName_field_cache.get(key);

  if (cached==null)
   classFieldName_field_cache.put(key, cached=Objects.requireNonNullElse(_i_searchField(clas, fieldName), nullField));

  if (cached==nullField) return null;
  return (Field)cached;
 }


 private static Field _i_searchField(Class clas, String fieldName)
 {
  Field res=null;
  Class cursor=clas;

  do
  {
   try
   {
    res=cursor.getDeclaredField(fieldName);
   } catch (Throwable ignore){}

   if (res==null)
   {
    cursor=cursor.getSuperclass();
   }
  } while (res==null && cursor!=null);

  return res;
 }



 public static Method searchMethod(Class clas, String methodName, Class<?>... methodParameterTypes)
 {
  Method res=null;

  Class cursor=clas;

  do
  {
   try
   {
    res=cursor.getDeclaredMethod(methodName, methodParameterTypes);
   } catch (Throwable ignore){}

   if (res==null)
   {
    cursor=cursor.getSuperclass();
   }
  } while (res==null && cursor!=null);

  return res;
 }






 /**
  *
  * @param c the class
  * @return all the methods of a class both public, private and so on, from this class as well from all the ancestor
  */
 public static Method[] getAllMethods(Class c)
 {
  HashMap<String, Method> methods=new HashMap<>();
  Method ms[];

  Class cs[]=getClassAllSuperclassesAllInterfacesOf(c);
  int t, len=ArrayExtras.length(cs);

  for (t=0;t<len;t++)
  {
   ms=cs[t].getDeclaredMethods();
   if (ArrayExtras.length(ms)>0) for (Method m : ms) methods.put(m.toString(), m);
  }

  return methods.values().toArray(new Method[methods.size()]);
 }




 /**
  *
  * @param c class
  * @param a annotation
  * @return all the methods of a class both public, private and so on, from this class as well from all the
  * ancestors that are annotated with a
  */
 public static Method[] getAllAnnotatedMethods(Class c, Class<? extends Annotation> a)
 {
  Method res[]=getAllMethods(c);
  int t, len=ArrayExtras.length(res);
  LinkedList<Method> tmp=new LinkedList<Method>();

  for (t=0;t<len;t++)
  {
   if (res[t].getAnnotation(a)!=null) tmp.add(res[t]);
  }

  return ArrayExtras.toArray(tmp, Method.class);
 }





 /**
  *
  *  deepClone does exactly what a method called deepClone is supposed to do!!!
  *
  * @param o the object to deeply clone!
  * @return
  */
 public static Object deepClone(Object o)
 {
  Object res=null;

  try
  {
   // Write the object out to a byte array
   ByteArrayOutputStream bos=new ByteArrayOutputStream();
   ObjectOutputStream out=new ObjectOutputStream(bos);
   out.writeObject(o);
   out.flush();
   out.close();

   // Make an input stream from the byte array and read
   // a copy of the object back in.
   ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
   res=in.readObject();
  }
  catch (java.io.IOException e)
  {
   e.printStackTrace();
  }
  catch (ClassNotFoundException cnfe)
  {
   cnfe.printStackTrace();
  }

  return res;
 }



 public static Field getDeclaredField(Class clazz, String fieldName)
 {
  try
  {
   return clazz.getDeclaredField(fieldName);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


 public static <R> R getFieldValue_defaultValue(Object source, String fieldName, R defaultValue)
 {
  R res;

  try
  {
   res=(R)ClassExtras.getFieldValue_bruteForce(source, fieldName);
   if (res==null) res=defaultValue;
  }
  catch (Throwable tr)
  {
   res=defaultValue;
  }

  return res;
 }



 public static void setFieldValue_bruteForce(Object source, String fieldName, Object value)
 {
  set_get_FieldValue_bruteForce(false, source, fieldName, value, false);
 }


 public static <R> R getFieldValue_bruteForce(Object source, String fieldName)
 {
  return set_get_FieldValue_bruteForce(true, source, fieldName, null, false);
 }


 public static void setFieldValue_reallyBruteForce(Object source, String fieldName, Object value)
 {
  set_get_FieldValue_bruteForce(false, source, fieldName, value, true);
 }


 public static <R> R getFieldValue_reallyBruteForce(Object source, String fieldName)
 {
  return set_get_FieldValue_bruteForce(true, source, fieldName, null, true);
 }



 private static <R> R set_get_FieldValue_bruteForce(boolean get, Object source, String fieldName, Object value, boolean reallyBrute)
 {
  Object res;
  Field f=null;

  try
  {
   Class cla=source.getClass();

   if (reallyBrute) f=searchField(cla, fieldName);
   else f=cla.getDeclaredField(fieldName);

   res=set_get_FieldValue_bruteForce(get, source, f, value);
  }
  catch (Throwable tr)
  {
   if (value instanceof String && f!=null && f.getType().isEnum())
   {
    try
    {
     value=Enum.valueOf((Class<? extends Enum>)f.getType(), (String)value);
     res=set_get_FieldValue_bruteForce(get, source, f, value);
    }
    catch (Throwable tr2)
    {
     throw new RuntimeException(tr2);
    }
   }
   else
   {
    throw new RuntimeException(tr);
   }
  }

  return (R)res;
 }


 public static void setFieldValue_bruteForce(Object source, Field field, Object value)
 {
  set_get_FieldValue_bruteForce(false, source, field, value);
 }

 public static Object getFieldValue_bruteForce(Object source, Field field)
 {
  return set_get_FieldValue_bruteForce(true, source, field, null);
 }



 @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
 public static Object set_get_FieldValue_bruteForce(boolean get, Object source, Field field, Object value)
 {
  Object res=null;

  try
  {
   // this is needed because, of course, if multithreading call for the same field are made.... what would happen in
   /*HERE*/

   synchronized (field)
   {
    boolean isa=field.canAccess(source);
    if (!isa) field.setAccessible(true);

   /*HERE*/

    if (get) res=field.get(source);
    else field.set(source, value);

    if (!isa) field.setAccessible(false);
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }



 public static Method getSetterMethodForField(Class clas, String fieldName, Class fieldType)
 {
  Method res=null;
  String methodName="set"+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1);

  try
  {
//   res=clas.getMethod(methodName, fieldType);
   res=searchMethod(clas, methodName, fieldType);
  } catch (Throwable ignore){}

  return res;

 }


 public static String fullEnumName(Enum key)
 {
  String cn=key.getClass().getName();
  cn=cn.substring(cn.lastIndexOf(".")+1);
  cn=replace(cn, "$", ".");

  return cn+"."+key.toString();
 }



 /**
  * A common method for all enums since they can't have another base class
  *
  * @param <T>    Enum type
  * @param c      enum type. All enums must be all caps.
  * @param string case insensitive
  * @return corresponding enum, or null
  */
 public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string)
 {
  if (c!=null && string!=null)
  {
   try
   {
    return Enum.valueOf(c, string);
   }
   catch (IllegalArgumentException ignore)
   {

   }
  }

  return null;
 }




}

