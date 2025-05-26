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
import me.as.lib.core.extra.QuickSortExtras;
import me.as.lib.core.io.extra.MemBytesRoom;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.zip.*;

import static me.as.lib.core.lang.ClassExtras.getAsManyClassPathItemsAsPossible;
import static me.as.lib.core.lang.ClassExtras.getClassPackagePath;
import static me.as.lib.core.lang.StringExtras.startsWith;
import static me.as.lib.core.system.FileSystemExtras.asUnixPath;
import static me.as.lib.core.system.FileSystemExtras.getDirAndFilename;
import static me.as.lib.core.system.FileSystemExtras.isDirectory;
import static me.as.lib.core.system.FileSystemExtras.listAll;
import static me.as.lib.core.system.FileSystemExtras.listTheTree;
import static me.as.lib.core.system.FileSystemExtras.mergePath;
import static me.as.lib.core.lang.StringExtras.newAutoString;
import static me.as.lib.core.lang.StringExtras.replace;


public class ResourceExtras
{
 private static HashMap customResources=null;


 private ResourceExtras()
 {

 }


/*
 public static void playPackagedAudioClip(final String resourcepath)
 {
  playPackagedAudioClip(resourcepath, 5);
 }


 public static void playPackagedAudioClip(final String resourcepath, final int howManySeconds)
 {
  Thread tt=new Thread(new Runnable()
  {
   public void run()
   {
    AppletAudioClip aac=new AppletAudioClip(loadPackagedFile(resourcepath));
    aac.play();
    ThreadExtras.sleep(howManySeconds);
   }
  }, "SpeedAudioClipPlayer");

  tt.start();
 }
*/






 public static void addCustomResources(String name, HashMap namedResources)
 {
  synchronized (ResourceExtras.class)
  {
   if (customResources==null) customResources=new HashMap();
   customResources.put(name, namedResources);
  }
 }


 public static HashMap getCustomResources(String name)
 {
  HashMap res=null;

  synchronized (ResourceExtras.class)
  {
   if (customResources!=null)
   {
    res=(HashMap)customResources.get(name);
   }
  }

  return res;
 }


 public static void removeCustomResources(String name)
 {
  synchronized (ResourceExtras.class)
  {
   if (customResources!=null)
   {
    customResources.remove(name);
   }
  }
 }



 public static byte[] getCustomResourceBytes(String name)
 {
  byte res[]=null;

  if (customResources!=null)
  {
   HashMap rhm;
   String otherName=null;
   if (name.startsWith("/")) otherName=name.substring(1);

   for (Iterator i=customResources.keySet().iterator();i.hasNext() && res==null;)
   {
    rhm=(HashMap)customResources.get(i.next());
    if (rhm!=null)
    {
     res=(byte[])rhm.get(name);
     if (res==null && otherName!=null) res=(byte[])rhm.get(otherName);
    }
/*
    if (res==null)
    {
     System.out.println("was looking for "+name);
     System.out.println("there were only these:");

     for (Iterator it=rhm.keySet().iterator();it.hasNext();)
     {
      System.out.println(it.next().toString());
     }
    }
*/
   }
  }

  return res;
 }




 public static InputStream getCustomResourceAsStream(String name)
 {
  InputStream res=null;
  byte data[]=getCustomResourceBytes(name);

  if (data!=null)
  {
   MemBytesRoom mbr=new MemBytesRoom();

   try
   {
    mbr.setContent(data);
    res=mbr.toInputStream(0);
   } catch (Throwable tr){res=null;}
  }

  return res;
 }


 public static String loadPackagedText(String fname) throws PackagedFileNotFoundException
 {
  return newAutoString(loadPackagedFile(fname));
 }

 public static String loadPackagedText(Object samePackageObject, String subPackage, String fname) throws PackagedFileNotFoundException
 {
  return loadPackagedText(getClassPackagePath(samePackageObject.getClass())+subPackage+"/"+fname);
 }

 public static String loadPackagedText(Object samePackageObject, String fname) throws PackagedFileNotFoundException
 {
  return loadPackagedText(samePackageObject.getClass(), fname);
 }

 public static String loadPackagedText(Class samePackageClass, String fname) throws PackagedFileNotFoundException
 {
  return newAutoString(loadPackagedFile(samePackageClass, fname));
 }

 public static String loadPackagedText(Class samePackageClass, String subPackage, String fname) throws PackagedFileNotFoundException
 {
  return loadPackagedText(getClassPackagePath(samePackageClass)+subPackage+"/"+fname);
 }


 public static boolean isFile(String fname)
 {
  InputStream is;

  try
  {
   is=ResourceExtras.class.getResourceAsStream(fname);
  } catch (Throwable tr){is=null;}

  return (is instanceof BufferedInputStream);
 }



 public static MemBytesRoom loadPackagedBytesNE(String fname)
 {
  try
  {
   return loadPackagedBytes(fname);
  }
  catch (Throwable tr)
  {
   return null;
  }
 }


 public static MemBytesRoom loadPackagedBytes(String fname) throws PackagedFileNotFoundException, IOException
 {
  MemBytesRoom mbr=new MemBytesRoom();

  mbr.setContent(loadPackagedFile(fname));
  mbr.setCurrentPosition(0);

  return mbr;
 }


 public static byte[] loadPackagedFile(Class samePackageClass, String fname) throws PackagedFileNotFoundException
 {
  String name=asUnixPath(mergePath(asUnixPath(getClassPackagePath(samePackageClass)), fname));
  return loadPackagedFile(name);
 }


 public static byte[] loadPackagedFile(String fname) throws PackagedFileNotFoundException
 {
  byte res[];

  try
  {
   InputStream is;

   try
   {
    is=ResourceExtras.class.getResourceAsStream(fname);
   } catch (Throwable tr){is=null;}

   if (is==null)
   {
    res=getCustomResourceBytes(fname);
    if (res==null) throw new Throwable();
    else return res;
   }

   ByteArrayOutputStream baos=new ByteArrayOutputStream();
   int step=10000;
   byte temp[]=new byte[step];
   int readed;

   do
   {
    readed=is.read(temp);
    if (readed>=0)
    {
     baos.write(temp, 0, readed);
    }
   } while (readed>0);

   try {is.close();} catch (Throwable ignore){}
   res=baos.toByteArray();
  }
  catch (Throwable tr)
  {
   throw new PackagedFileNotFoundException("ResourceExtras.loadPackagedFile -> FILE NOT FOUND -> "+fname);
  }

  return res;
 }



 public static InputStream getPackagedResourceStream(String fname) throws PackagedFileNotFoundException, IOException
 {
  boolean throwException=false;
  InputStream is;

  try
  {
   try
   {
    is=ResourceExtras.class.getResourceAsStream(fname);
   } catch (Throwable tr){is=null;}

   if (is==null) is=getCustomResourceAsStream(fname);
   if (is==null) throwException=true;
  }
  catch (Throwable tr)
  {
   is=null;
   throwException=true;
  }

  if (throwException) throw new PackagedFileNotFoundException("ResourceExtras.getPackagedResourceStream -> FILE NOT FOUND -> "+fname);

  return is;
 }



 public static boolean doesPackagedFileExist(String fname)
 {
  boolean res=false;

  try
  {
   InputStream is;

   try
   {
    is=ResourceExtras.class.getResourceAsStream(fname);
   } catch (Throwable tr){is=null;}

   if (is==null) res=(getCustomResourceBytes(fname)!=null);
   else
   {
    is.close();
    res=true;
   }
  } catch (Throwable ignore){}

  return res;
 }


 public static byte[] loadResourceFromURL(String url)
 {
  /* @todo: to be implemented */
  throw new StillUnimplemented();
 }


/*

 // URGENT: todo DOES NOT WORK VERY WELL AFTER ALL (it works only when not in jar or not with Excelsior JET)
 public static String[] listResources(String path)
 {
  String res[]=null;
  ArrayList<String> al=listResources(path, null, null);

  if (al!=null && al.size()>0)
  {
   res=al.toArray(new String[al.size()]);
   res=QuickSortExtras.sort(res);
  }

  return res;
 }





 private static ArrayList<String> listResources(String path, String root, ArrayList<String> al)
 {
  try
  {
   URL url=ResourceExtras.class.getResource(path);
   String nroot, tmpF, tmpS=url.getFile();
   File f=new File(tmpS);

   if (f.isDirectory())
   {
    String fs[]=f.list();
    int t, len=ArrayExtras.length(fs);

    if (len>0)
    {
     if (al==null) al=new ArrayList<String>();

     for (t=0;t<len;t++)
     {
      if (StringExtras.hasChars(root))
      {
       tmpS=root+"/"+fs[t];
       nroot=root+"/"+fs[t];
      }
      else
      {
       tmpS=fs[t];
       nroot=fs[t];
      }

      tmpF=tmpS;
      tmpS=path+"/"+fs[t];

      if (new File(ResourceExtras.class.getResource(tmpS).getFile()).isDirectory())
      {
       tmpF+="/";
       listResources(tmpS, nroot, al);
      }

      al.add(tmpF);
     }
    }
   }

   // are them also in a jar?
   JarURLConnection conn;

   try
   {
    conn=(JarURLConnection)url.openConnection();
   } catch (Throwable tr){conn=null;}

   if (conn!=null)
   {
    if (al==null) al=new ArrayList<String>();
    JarFile jfile=conn.getJarFile();
    Enumeration e=jfile.entries();

    while (e.hasMoreElements())
    {
     al.add(((ZipEntry)e.nextElement()).getName());
    }
   }


   // todo urgent: look also in the custom resources!
   // ..... see --> getCustomResourceBytes ....


  }
  catch (Throwable tr)
  {
   throw new PackagedFileNotFoundException("ResourceExtras.listResources -> NOT FOUND -> "+path, tr);
  }

  return al;
 }

*/


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static void listResourcesInJarZip(String packagePath, String jarZipFileName, boolean alsoSubPackages, ArrayList<String> res)
 {
  String tmpS, qpp=replace(replace(packagePath, ".", "/")+"/", "//", "/");
  if (startsWith(qpp, "/")) qpp=qpp.substring(1);
  int plen=qpp.length();
  ZipFile zf=null;

  try
  {
   zf=new ZipFile(jarZipFileName);
   String s;

   for (Enumeration<? extends ZipEntry> e=zf.entries();e.hasMoreElements();)
   {
    s=e.nextElement().getName();

    if (s.length()>plen)
    {
     if (s.startsWith(qpp))
     {
      tmpS=s.substring(plen);

      if (s.lastIndexOf('/')>plen)
      {
       if (alsoSubPackages)
       {
        tmpS=replace(tmpS, "/", ".");
        res.add(tmpS);
       }
      }
      else
      {
       res.add(tmpS);
      }
     }
    }
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
  finally
  {
   if (zf!=null) {try{zf.close();}catch (Throwable ignore){}}
  }
 }



/*
 // 31% slower than the new version, still it was just 170 ms to execute with my lot of packages!
 private static void listResourcesInJarZip(String packagePath, String jarZipFileName, boolean alsoSubPackages, ArrayList<String> res)
 {
  int plen=packagePath.length();
  ZipFile zf;

  try
  {
   zf=new ZipFile(jarZipFileName);
   boolean goOn, doingDirs;
   String s, df[], odf[]=new String[2];

   for (Enumeration<? extends ZipEntry> e=zf.entries();e.hasMoreElements();)
   {
    s=e.nextElement().getName();
    odf[0]=s;

    do
    {
     odf=StringExtras.splitLast(odf[0], '/', odf);
     df=(String[])ArrayExtras.clone(odf);

     doingDirs=(StringExtras.length(df[1])>0 && StringExtras.length(df[0])>=plen);

     if (doingDirs)
     {
      df[0]=replace(df[0], "/", ".");
      if (df[0].startsWith(".")) df[0]=df[0].substring(1);

      if (alsoSubPackages) goOn=df[0].startsWith(packagePath);
      else goOn=df[0].equals(packagePath);

      if (goOn)
      {
       df[0]=df[0].substring(packagePath.length());
       if (df[0].startsWith(".")) df[0]=df[0].substring(1);
       if (df[0].length()>0) df[0]+=".";
       s=df[0]+df[1];
       if (!res.contains(s)) res.add(s);
      }
     }
    } while (doingDirs);
   }
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
   zf=null;
  }

  if (zf!=null) {try{zf.close();}catch (Throwable ignore){}}
 }
*/




 private static void listResourcesInDirectory(String packagePath, String directory, boolean alsoSubPackages, ArrayList<String> res)
 {
  String ppDir=replace(packagePath, ".", "/");
  String dir=mergePath(directory, ppDir);

  if (isDirectory(dir))
  {
   String tmpStr, ma, df[];
   List<String> files=((alsoSubPackages) ? listTheTree(dir) : listAll(dir));
   int t, len=ArrayExtras.length(files);

   for (t=0;t<len;t++)
   {
    ma=mergePath(dir, files.get(t));
    df=getDirAndFilename(ma);
    if (df[0].equals(dir)) res.add(df[1]);
    else
    {
     tmpStr=ma.substring(dir.length()+1);
     tmpStr=replace(tmpStr, File.separator, ".");
     res.add(tmpStr);
    }
   }
  }
 }


/*
 // THIS WAS REALLY TREMENDLY, EXTREMELY SLOW!!!!! it was because i always check the whole tree! Too BAD!
 private static void listResourcesInDirectory(String packagePath, String directory, boolean alsoSubPackages, ArrayList<String> res)
 {
  String dp=packagePath+directory;
  int dplen=dp.length();
  String df[], s[]=FileSystemExtras.listTheTree(directory);
  boolean goOn;
  int t, len=ArrayExtras.length(s);

  for (t=0;t<len;t++)
  {
   df=FileSystemExtras.getDirAndFilename(FileSystemExtras.adjustPath(directory+File.separator+s[t]));

   if (df[0].length()>=dplen)
   {
    df[0]=df[0].substring(directory.length()+1);

    df[0]=replace(df[0], File.separator, ".");
    if (df[0].startsWith(".")) df[0]=df[0].substring(1);

    if (alsoSubPackages) goOn=df[0].startsWith(packagePath);
    else goOn=df[0].equals(packagePath);

    if (goOn)
    {
     df[0]=df[0].substring(packagePath.length());
     if (df[0].startsWith(".")) df[0]=df[0].substring(1);
     if (df[0].length()>0) df[0]+=".";

     res.add(df[0]+df[1]);
    }
   }
  }
 }
*/


 public static String[] listResources(String packagePath)
 {
  return listResources(packagePath, false);
 }




 public static String[] listResources(String packagePath, boolean alsoSubPackages)
 {
  ArrayList<String> list=new ArrayList<>();
  int t, len;
  String cps[]=getAsManyClassPathItemsAsPossible();
  len=ArrayExtras.length(cps);

  for (t=0;t<len;t++)
  {
   if (isDirectory(cps[t]))
    listResourcesInDirectory(packagePath, cps[t], alsoSubPackages, list);
   else
    listResourcesInJarZip(packagePath, cps[t], alsoSubPackages, list);
  }

  return QuickSortExtras.sort(list.toArray(new String[list.size()]));
 }






}
