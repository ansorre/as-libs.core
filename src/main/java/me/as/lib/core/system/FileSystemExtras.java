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


package me.as.lib.core.system;


import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.extra.BoxFor3;
import me.as.lib.core.extra.QuickSortExtras;
import me.as.lib.core.io.extra.FileBytesRoom;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.CalendarExtras;
import me.as.lib.core.lang.StringExtras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.generateRandomString;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.isBlank;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.newAutoString;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.lang.StringExtras.splitLast;
import static me.as.lib.core.log.LogEngine.logOut;


public class FileSystemExtras
{
 // singleton
 private FileSystemExtras(){}

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public enum CopyResult
 {
  wereNotDifferent,
  copiedFile,
  madeDirectory
 }




 private static <F> F handleException(Throwable tr)
 {
  if (tr instanceof RuntimeException)
   throw ((RuntimeException)tr);

  if (tr instanceof IOException)
   throw new me.as.lib.core.io.IOException(tr);
  else
   throw new RuntimeException(tr);
 }


 public static boolean exists(String fname)
 {
  if (isNotBlank(fname))
   return new File(fname).exists();

  return false;
 }



 public static boolean grantDirectory(String dname)
 {
  boolean res=isDirectory(dname);

  if (!res)
   res=mkdirs(dname);

  return res;
 }

 public static boolean mkdirs(String dname)
 {
  return new File(dname).mkdirs();
 }



 public static void deleteTheTree(String dname)
 {
  try
  {
   if (isBlank(dname))
    throw new RuntimeException("deleteTheTree with blank 'dname'");

   String root=((dname.endsWith(File.separator)) ? dname : dname+File.separator);
   String tmp;
   List<String> filesL=listTheTree(root);
   List<String> dirsL=new ArrayList<>();
   int t, len=ArrayExtras.length(filesL);

   for (t=0;t<len;t++)
   {
    tmp=root+filesL.get(t);

    if (isFile(tmp))
     deleteFile(tmp);
    else
     dirsL.add(tmp);
   }

   String files[]=ArrayExtras.toArrayOfStrings(dirsL);

   do // his do/while loop seems unnecessary, anyway it's not expensive and I don't remember
      // why I did it but I remember that I really needed to do it
   {
    files=StringExtras.purgeNullsAndEmpties(files);
    files=QuickSortExtras.sortStringsForLength(files, true);
    len=ArrayExtras.length(files);

    for (t=0;t<len;t++)
    {
     if (deleteFile(files[t]))
      files[t]=null;
    }
   } while (len>0);

  }
  catch (Throwable tr)
  {
   handleException(tr);
  }
 }



 public static boolean renameFile(String currentFile, String renamedFile)
 {
  boolean res;

  File oldF=new File(currentFile);
  File newF=new File(renamedFile);
  res=oldF.renameTo(newF);

  return res;
 }



 public static String getTemporaryFileName()
 {
  return getNotExistentFileName(getTemporaryDirectory(), "tmp", "tmp");
 }


 public static String getNotExistentFileName(String dir, String fNameRoot, String fExtRoot)
 {
  return getNotExistentFileName(dir, fNameRoot, fExtRoot, 0);
 }



 public static String getNotExistentFileName(String dir, String fNameRoot, String fExtRoot, int startNo)
 {
  String res=null;

  if (isDirectory(dir))
  {
   if (hasChars(fNameRoot)) fNameRoot=fNameRoot.trim();
   if (hasChars(fExtRoot)) fExtRoot=fExtRoot.trim();

   if (isNotBlank(fExtRoot))
   {
    if (!fExtRoot.startsWith(".")) fExtRoot="."+fExtRoot;
   }

   if (!dir.endsWith(File.separator)) dir+=File.separator;

   String tmpStr;
   StringBuilder sb;
   int tryNum=startNo;

   do
   {
    tmpStr=""+tryNum;
    sb=new StringBuilder();

    sb.append(dir);
    if (isNotBlank(fNameRoot)) sb.append(fNameRoot);
    sb.append(tmpStr);
    if (isNotBlank(fExtRoot)) sb.append(fExtRoot);

    tmpStr=sb.toString();
    tryNum++;
   } while (exists(tmpStr));

   res=tmpStr;
  }

  return res;
 }



 /**
  * This method adjust the String 'name' trasforming all the
  * occurrences of File.separator in /
  *
  * @param path String
  * @return String
  */
 public static String asUnixPath(String path)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=path;
   res=replace(res, "\\", "/");
   res=replace(res, "//", "/");
  }

  return res;
 }


 /**
  * This method adjust path trasforming all the
  * occurrences of either / or \ in the current File.separator
  *
  * @param path String
  * @return adjusted path
  */
 public static String adjustPath(String path)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=path;
   String okFS=File.separator;

   if (okFS.equals("\\"))
   {
    res=replace(res, "/", okFS);
   }
   else
   {
    res=File.separator+replace(res, "\\", okFS);
   }

   okFS=File.separator+File.separator;
   res=replace(res, okFS, File.separator);
  }

  return res;
 }


 public static String getCanonicalPath(File f)
 {
  try
  {
   return f.getCanonicalPath();
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }
 }


 public static String getCanonicalPath(String path)
 {
  String res;

  try
  {
   File f=new File(path);
   res=f.getCanonicalPath();
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }



 public static void runMakingATemporaryDirectoryAndDeletingItAtEnd(String insideDir, Consumer<String> runnable)
 {
  String theTemporaryDirectory=makeATemporaryDirectory(insideDir);

  try
  {
   runnable.accept(theTemporaryDirectory);
  }
  finally
  {
   ThreadExtras.executeLater(500, ()->
   {
    deleteTheTree(theTemporaryDirectory);
    deleteFile(theTemporaryDirectory);
   });
  }
 }


 public static String makeATemporaryDirectory(String insideDir)
 {
  String dir, res;
  boolean exists;

  synchronized (FileSystemExtras.class)
  {
   do
   {
    dir=getFileSystemCompatibleFileName(generateRandomString(12));
    res=mergePath(insideDir, dir);

    exists=exists(res);

   } while (exists);

   mkdirs(res);

   if (!exists(res))
    throw new RuntimeException("Cannot create dir '"+res+"'");
  }

  return res;
 }





 public static String getFileSystemCompatibleFileName(String name)
 {
  return getMorePermissiveFileSystemCompatibleFileName(name, ".");
 }

 public static final String default_extraPermissiveAllowedChars=" ~=%–-.&'#@$()[]{},×+_`!";

 public static String getMorePermissiveFileSystemCompatibleFileName(String name)
 {
  return getMorePermissiveFileSystemCompatibleFileName(name, default_extraPermissiveAllowedChars);
 }


 public static String getMorePermissiveFileSystemCompatibleFileName(String name, String extraPermissiveAllowedChars)
 {
  String res=null;

  try
  {
   name=StringExtras.betterTrimNl(name);
   name=StringExtras.deAccentize(name);

   StringBuilder sb=new StringBuilder();
   char ch;
   int t, len=name.length();

   for (t=0;t<len;t++)
   {
    ch=name.charAt(t);
    if ((hasChars(extraPermissiveAllowedChars) && extraPermissiveAllowedChars.indexOf(ch)>=0) ||
     Character.isLetter(ch) ||
     Character.isDigit(ch))
     sb.append(ch);
    else
     sb.append("_");
   }

   res=StringExtras.replaceAll(sb.toString(), " .", ".");
   res=StringExtras.replaceAll(res, ". ", ".");
   res=StringExtras.replaceAll(res, "  ", " ");

   res=StringExtras.replaceAll(sb.toString(), "__", "_");
   while (res.length()>1 && res.startsWith("_")) res=res.substring(1);
   while (res.length()>1 && res.endsWith("_")) res=res.substring(0, res.length()-1);

  } catch (Throwable ignore){}

  return res;
 }




 public static String mergePath(String... dirs_and_subDirs_and_filename)
 {
  StringBuilder sb=new StringBuilder();
  int t, len=ArrayExtras.length(dirs_and_subDirs_and_filename);

  for (t=0;t<len;t++)
  {
   sb.append(dirs_and_subDirs_and_filename[t]);
   sb.append(File.separator);
  }

  String res=adjustPath(sb.toString());

  while (res.endsWith(File.separator))
   res=res.substring(0, res.length()-1);

  return res;
 }




 public static List<String> listAll(String dpath)
 {
  return listAll(dpath, null);
 }

 public static List<String> listAll(String dpath, String matcher)
 {
  if (!hasChars(matcher)) matcher="*";
  List<String> res=new ArrayList<>();
  Path dir=Path.of(dpath);
  int dend=dpath.length()+1;

  try (DirectoryStream<Path> stream=Files.newDirectoryStream(dir, matcher))
  {
   for (Path p : stream)
   {
    res.add(p.toString().substring(dend));
   }
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return res;
 }



 public static List<String> listTheTree(String dname)
 {
  List<String> res;

  try
  {
   res=listTheTree(dname, new ArrayList(), false);
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return res;
 }


 public static List<String> listTheTree(String dname, Consumer<BoxFor3<String, BasicFileAttributes, List<String>>> consumer)
 {
  final List<String> res=new ArrayList<>();
  final BoxFor3<String, BasicFileAttributes, List<String>> box=new BoxFor3<>();

  try
  {
   Files.walkFileTree(Path.of(dname), new FileVisitor<>() {
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    {
     consumer.accept(box.set(dir.toString(), attrs, res));
     return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    {
     consumer.accept(box.set(file.toString(), attrs, res));
     return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc)
    {
     return FileVisitResult.CONTINUE;
    }

    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
    {
     return FileVisitResult.CONTINUE;
    }
   });

  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return res;
 }



 public static String getCurrentWorkingDirectory()
 {
  return Paths.get(".").toAbsolutePath().normalize().toString();
 }



/*
 // old version very good working bat 6 time slower!!!!!!
 private static List listTheTree(String dname, String root, String subDir, List list)
 {
  File f=new File(dname);

  if (f.isDirectory())
  {
   String tmp, files[]=f.list();
   int t, len=ArrayExtras.length(files);

   for (t=0;t<len;t++)
   {
    tmp=subDir+files[t];
    list.add(tmp);
    tmp=root+tmp;
    listTheTree(tmp, root, subDir+files[t]+File.separator, list);
   }
  }

  return list;
 }
*/


 static abstract class AbstractFileVisitor implements FileVisitor<Path>
 {
  int len;
  List list;

  AbstractFileVisitor(int len, List list)
  {
   this.len=len;
   this.list=list;
  }

 }

 static class FileInfoFileVisitor extends AbstractFileVisitor
 {

  FileInfoFileVisitor(int len, List list)
  {
   super(len, list);
  }

  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws me.as.lib.core.io.IOException
  {
   try
   {
    String pp=dir.toString().substring(len);
    FileInfo fi=new FileInfo();

    fi.relativePath=pp;
    fi.type=FileInfo.Type.directory;
    fi.lastModified=attrs.lastModifiedTime().toMillis();
    fi.size=attrs.size();

    list.add(fi);
   }
   catch (Throwable ignore) // will happen only the first time that is also what we want, because we want to skip the dname itself
   {}

   return FileVisitResult.CONTINUE;
  }

  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws me.as.lib.core.io.IOException
  {
   FileInfo fi=new FileInfo();

   fi.relativePath=file.toString().substring(len);
   fi.type=FileInfo.Type.file;
   fi.lastModified=attrs.lastModifiedTime().toMillis();
   fi.size=attrs.size();

   if (fi.size!=fileLength(file.toString()))
   {
    System.out.println("oohhhhhhhhhhhh");
   }

   list.add(fi);

   return FileVisitResult.CONTINUE;
  }

  public FileVisitResult visitFileFailed(Path file, IOException exc) throws me.as.lib.core.io.IOException
  {return FileVisitResult.CONTINUE;}

  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws me.as.lib.core.io.IOException
  {return FileVisitResult.CONTINUE;}

 }


 static class StringFileVisitor extends AbstractFileVisitor
 {

  StringFileVisitor(int len, List list)
  {
   super(len, list);
  }

  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws me.as.lib.core.io.IOException
  {
   try
   {
    list.add(dir.toString().substring(len));
   }
   catch (Throwable ignore) // will happen only the first time that is also what we want, because we want to skip the dname itself
   {}

   return FileVisitResult.CONTINUE;
  }

  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws me.as.lib.core.io.IOException
  {
   list.add(file.toString().substring(len));
   return FileVisitResult.CONTINUE;
  }

  public FileVisitResult visitFileFailed(Path file, IOException exc) throws me.as.lib.core.io.IOException
  {return FileVisitResult.CONTINUE;}

  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws me.as.lib.core.io.IOException
  {return FileVisitResult.CONTINUE;}

 }




 private static List<String> listTheTree(String dname, List<String> list, boolean returnFileInfos)
 {
  final Path start=Paths.get(dname);
  final int len=start.toString().length()+1;

  try
  {
   FileVisitor<Path> visitor;

   if (returnFileInfos)
   {
    visitor=new FileInfoFileVisitor(len, list);
   }
   else
   {
    visitor=new StringFileVisitor(len, list);
   }

   Files.walkFileTree(start, visitor);
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return list;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .




 public static String getTemporaryDirectory()
 {
  return System.getProperties().getProperty("java.io.tmpdir");
 }



 // Ex.: from "c:\autoexec.bat" you get {"c:", "autoexec.bat"}
 public static String[] getDirAndFilename(String filePath)
 {
  if (filePath.endsWith(File.separator))
  {
   filePath=filePath.substring(0, filePath.length()-1);
  }

  return splitLast(adjustPath(filePath), File.separator.charAt(0));
 }

 // Ex.: from "c:\autoexec.bat" you get {"c:", "autoexec", ".bat"}
 public static String[] getDirAndFilenameAndExtension(String filePath)
 {
  String res[]=new String[3];
  String t1[]=getDirAndFilename(filePath);
  String t2[]=splitLast(t1[1], '.');

  res[0]=t1[0];
  res[1]=((t2[0]!=null) ? t2[0] : t2[1]);
  res[2]=((t2[0]!=null) ? "."+t2[1] : null);

  return res;
 }


 public static String getFileExtension(String fileName)
 {
  String res;

  try
  {
   res=fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()).toLowerCase();
   if (!isNotBlank(res)) throw new RuntimeException();
  }
  catch (Throwable tr)
  {
   res="";
  }

  return res;
 }



 public static boolean setLastModified(String path, long time)
 {
  try
  {
   return new File(path).setLastModified(time);
  }
  catch (Throwable tr)
  {
   return false;
  }
 }




 public static long lastModified(String path)
 {
  try
  {
   return new File(path).lastModified();
  }
  catch (Throwable tr)
  {
   return -1L;
  }
 }

 public static boolean isDirectoryEmpty(String path)
 {
  if (isDirectory(path))
  {
   try (DirectoryStream<Path> dirStream=Files.newDirectoryStream(Path.of(path)))
   {
    return !dirStream.iterator().hasNext();
   }
   catch (Throwable tr)
   {
    return false;
   }
  }

  return false;
 }




 public static boolean isDirectory(String path)
 {
  boolean res;

  try
  {
   if (isNotBlank(path))
   {
    File tmpF=new File(path);
    res=(tmpF.exists() && tmpF.isDirectory());
   } else res=false;
  } catch (Throwable tr){res=false;}

  return res;
 }

 public static boolean isFile(String path)
 {
  boolean res=isNotBlank(path);

  if (res)
  {
   try
   {
    File tmpF=new File(path);
    res=(tmpF.exists() && tmpF.isFile());
   }
   catch (Throwable tr)
   {
    res=false;
   }
  }

  return res;
 }


 public static boolean isLink(String path)
 {
  boolean res=false;

  if (OSExtras.isSomeMicrosoftWindows())
  {
   if (isFile(path))
   {
    if (path.toLowerCase().endsWith(".lnk"))
    {
     byte b[]=loadFromFile(path);
     if (b!=null && b.length>4 && b[0]==76 && b[1]==0 && b[2]==0 && b[3]==0) res=true;
    }
   }
  }
  else
  {
   throw new StillUnimplemented();
  }

  return res;
 }


 public static void createSymbolicLink(String newDest, String existing)
 {

  try
  {
   if (OSExtras.isSomeLinux())
   {
    Path newLink=Paths.get(newDest);
    Path existingFile=Paths.get(existing);
    Files.createSymbolicLink(newLink, existingFile);

//    chmod +x public

   }
   else
   {
    if (OSExtras.isSomeMicrosoftWindows())
    {
     ExternalProcessSupport.exec(new String[]{"CMD", "/C", "mklink", "/J", newDest, existing});
    }
    else
    {
     throw new StillUnimplemented();
    }
   }
  }
  catch (Throwable tr)
  {
   handleException(tr);
  }
 }




 public static boolean isValidFileName(String path)
 {
  boolean res=isNotBlank(path);

  if (res)
  {
   String df[]=getDirAndFilename(path);
   res=(ArrayExtras.length(df)==2 && isNotBlank(df[0]) && isNotBlank(df[1]));
  }

  return res;
 }



 // returns:
 // -1    = the file does not exist
 // other = the length of the file in bytes
 public static long fileLength(String path)
 {
  long res=-1;

  if (isFile(path))
  {
   File tmpF=new File(path);
   res=tmpF.length();
  }

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static boolean areEqual(String sourceFname, String destFname)
 {
  boolean res;

  res=(isDirectory(sourceFname) && isDirectory(destFname));

  if (!res)
  {
   if (isFile(sourceFname) && isFile(destFname))
   {
    res=((lastModified(sourceFname)==lastModified(destFname)) &&
         (fileLength(sourceFname)==fileLength(destFname)));
   }
  }

  return res;
 }


 public static void copyFileIfTheyAreDifferent(String sourceFname, String destFname)
 {
  if (isFile(sourceFname))
  {
   if (isDirectory(destFname))
   {
    throw new RuntimeException("destination file '"+destFname+"' is a directory");
   }

   long destLastModified=-1;
   long destFileLength=-1;

   if (isFile(destFname))
   {
    destLastModified=lastModified(destFname);
    destFileLength=fileLength(destFname);
   }

   long sourceLastModified=lastModified(sourceFname);
   long sourceFileLength=fileLength(sourceFname);

   if (destFileLength!=sourceFileLength || destLastModified!=sourceLastModified)
   {
    copyFile(sourceFname, destFname);
   }
  }
  else
  {
   throw new RuntimeException("source file '"+sourceFname+"' is not a file!");
  }
 }



 public static boolean copyFile(String sourceFname, String destFname)
 {
  return copyFile(sourceFname, destFname, false);
 }


 public static boolean copyFile(String sourceFname, String destFname, boolean touch)
 {
  boolean res=saveInFile(destFname, loadFromFile(sourceFname));

  if (res)
  {
   if (touch)
   {
    setLastModified(destFname, CalendarExtras.now().getTimeInMillis());
   }
   else
   {
    setLastModified(destFname, lastModified(sourceFname));
   }
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static boolean saveInFile(String fname, String data)
 {
  boolean res;

  try
  {
   byte bdata[];

   if (StringExtras.length(data)>0)
   {
    bdata=data.getBytes(defaultCharsetName);
   }
   else
   {
    bdata=null;
   }

   res=saveInFile(fname, bdata);
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return res;
 }



 public static boolean appendToFile(String fname, byte data[])
 {
  boolean res=false;

  try
  {
   if (isFile(fname))
   {
    File f=new File(fname);
    FileOutputStream fos=new FileOutputStream(f, true);
    fos.write(data);
    fos.flush();
    fos.close();
    res=true;
   } else res=saveInFile(fname, data);
  } catch (Throwable tr){res=false;}

  return res;
 }


 public static CopyResult copyIfDifferent(String orig_fileOrDir, String dest_fileOrDir)
 {
  CopyResult res=CopyResult.wereNotDifferent;
  long orig_LM=lastModified(orig_fileOrDir);
  long dest_LM=lastModified(dest_fileOrDir);
  long orig_L=fileLength(orig_fileOrDir);
  long dest_L=fileLength(dest_fileOrDir);
  boolean orig_isF=isFile(orig_fileOrDir);
  boolean dest_isF=isFile(dest_fileOrDir);

  if (!(orig_isF==dest_isF && orig_LM==dest_LM && orig_L==dest_L))
  {
   if (orig_isF && isDirectory(dest_fileOrDir))
    throw new RuntimeException("Origin file is a file but destination is a directory!");

   if (!orig_isF && isFile(dest_fileOrDir))
    throw new RuntimeException("Origin file is a directory but destination is a file!");

   if (orig_isF)
   {
    saveInFile(dest_fileOrDir, loadFromFile(orig_fileOrDir));
    res=CopyResult.copiedFile;
   }
   else
   {
    mkdirs(dest_fileOrDir);
    res=CopyResult.madeDirectory;
   }

   setLastModified(dest_fileOrDir, orig_LM);
  }

  return res;
 }


 public static boolean saveInFile(String fname, byte data[])
 {
  boolean res;

  try
  {
   String dir=fname.substring(0, fname.lastIndexOf(File.separator));

   File f=new File(dir);
   if (!f.exists()) mkdirs(dir);

   f=new File(fname);
   FileOutputStream fos=new FileOutputStream(f);

   if (ArrayExtras.length(data)>0)
   {
    fos.write(data);
    fos.flush();
   }

   fos.close();
   res=true;
  } catch (Throwable tr){res=false;}

  return res;
 }




 public static byte[] loadFromFile(String fname)
 {
  return loadFromFile(fname, false);
 }


 public static byte[] loadFromFile(String fname, boolean useMemoryMappedFile)
 {
  byte res[]=null;
  RuntimeException throwThis=null;

  try
  {
   File f=new File(fname);
   long fl=f.length();
   if (fl>=Integer.MAX_VALUE)
    throwThis=new RuntimeException("File is too big ("+StringExtras.formatLong(fl)+" bytes) to be loaded in one byte array!");
   else
   {
    int size=(int)fl;

    if (size>0)
    {
     byte data[]=new byte[size];

     // DA USARSI SOLO IN CASI ASSAI PARTICOLARI! LEGGI IN FileBytesRoom.java!
     if (useMemoryMappedFile)
     {
      FileBytesRoom fbr=new FileBytesRoom(fname, true);

      if (fbr.open("r"))
      {
       fbr.read(data);
       fbr.close();
      }
      else
      {
       throw new me.as.lib.core.io.IOException("The file could not be opened in read mode!");
      }
     }
     else
     {
      FileInputStream fis=new FileInputStream(f);
      fis.read(data);
      fis.close();
     }

     res=data;
    }
   }
  } catch (Throwable tr) {res=null;}

  if (throwThis!=null)
   throw throwThis;

  return res;
 }


 public static String loadTextFromFile(String fname)
 {
  String res;

  try
  {
   res=loadTextFromFile(fname, defaultCharsetName);
  }
  catch (Throwable tr)
  {
   return handleException(tr);
  }

  return res;
 }





 public static String loadTextFromFile(String fname, String charsetName) throws UnsupportedEncodingException
 {
  String res=null;

  byte data[]=loadFromFile(fname);
  if (data!=null) res=newAutoString(data, charsetName);
  return res;
 }


 public static boolean deleteFileAndTheDirIfEmpty(String path)
 {
  boolean res=deleteFile(path);

  if (res)
  {
   String dir=getDirAndFilename(path)[0];

   if (isDirectoryEmpty(dir))
   {
    res=deleteFile(dir);
   }
  }

  return res;
 }



 public static boolean deleteFile(String path)
 {
  boolean res=false;

  if (isNotBlank(path))
  {
   File tmpF=new File(path);
   if (tmpF.exists())
   {
    res=tmpF.delete();
    if (!res)
    {
     System.gc();             // <-- incredibly strange both these two are
     ThreadExtras.sleep(1);   // <-- necessary sometimes (fortunately very rarely)
     res=tmpF.delete();
    }
   }
  }

  return res;
 }




}
