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

package me.as.lib.core.io.extra;


import me.as.lib.core.collection.ListHashMap;
import me.as.lib.core.extra.CacheHelper;
import me.as.lib.core.extra.InMemoryCache;
import me.as.lib.core.io.BytesRoom;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.Binaryable;
import me.as.lib.core.lang.ByteExtras;
import me.as.lib.core.lang.StringExtras;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static me.as.lib.core.lang.ByteExtras.fromBytes;
import static me.as.lib.core.lang.ByteExtras.toBytes;
import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.doTheyMatch;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.areNotBlank;
import static me.as.lib.core.lang.StringExtras.purgeNullsAndEmpties;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.lang.StringExtras.splitLast;
import static me.as.lib.core.lang.StringExtras.startsWith;
import static me.as.lib.core.lang.StringExtras.stringOrEmpty;
import static me.as.lib.core.lang.StringExtras.toUpperCase;
import static me.as.lib.core.lang.StringExtras.unmerge;


public class FileSystemRoom
{
 private char separator;
 private String separatorS;
 private PiecedBytesRoom pbRoom;
 private InMemoryCache<Directory> dirsCache=new InMemoryCache<Directory>(40, 10);
 private CacheHelper<Directory> dirsCacheHelper=new CacheHelper<Directory>()
          {public Directory create(Object[] params){return createDirectoryCache(params[0].toString());}};


 public FileSystemRoom()
 {
  this(null, new MemBytesRoom());
 }



 public FileSystemRoom(String signature, BytesRoom room)
 {
  this(signature, room, '/');
 }



 public FileSystemRoom(String signature, BytesRoom room, char separator)
 {
  this.separator=separator;
  separatorS=""+separator;

  pbRoom=new PiecedBytesRoom(signature, room);
  if (pbRoom.piecesCount()==0)
  {
   pbRoom.addPiece(toBytes(new Directory())); // the root directory
  }
 }


 private Directory createDirectoryCache(String dirPath)
 {
  Directory res=null;

  if (isNotBlank(dirPath))
  {
   String df[]=getDirAndFilename(dirPath);
   Directory dir=getDirectoryFromCache(df[0]);
   if (dir.isDirectory(df[1]))
   {
    res=createDirectoryCache(dir.getPieceId(df[1]));
   }
  }
  else
  {
   res=createDirectoryCache(0);
  }

  return res;
 }




 private Directory createDirectoryCache(long pieceId)
 {
  Directory res=(Directory)fromBytes(pbRoom.getPiece(pieceId));
  res.pieceId=pieceId;
  return res;
 }




 private Directory getDirectoryFromCache(String dirPath)
 {
  return dirsCache.get(dirsCacheHelper, stringOrEmpty(adjustPathREFS(toUpperCase(dirPath))));
 }


 private boolean pathOk(String path)
 {
  return startsWith(path, separatorS);
 }




 public synchronized void flush()
 {
  pbRoom.flush();
 }




 // Ex.: from "\root\autoexec.bat" you get {"root", "autoexec.bat"}
 public String[] getDirAndFilename(String filePath)
 {
  if (filePath.endsWith(separatorS))
  {
   filePath=filePath.substring(0, filePath.length()-1);
  }

  return splitLast(adjustPath(filePath), separator);
 }



 // Ex.: from "c:\autoexec.bat" you get {"c:", "autoexec", ".bat"}
 public String[] getDirAndFilenameAndExtension(String filePath)
 {
  String res[]=new String[3];
  String t1[]=getDirAndFilename(filePath);
  String t2[]=splitLast(t1[1], '.');

  res[0]=t1[0];
  res[1]=t2[0];
  res[2]="."+t2[1];

  return res;
 }




 public synchronized boolean exists(String fname)
 {
  return (isFile(fname) || isDirectory(fname));
 }



 public synchronized boolean deleteFile(String path)
 {
  if (!pathOk(path) || StringExtras.length(path)<2) return false; // vorrai mica cancellare la root?????
  boolean res=isFile(path);

  if (res)
  {
   boolean wasDir=isDirectory(path);
   String df[]=getDirAndFilename(path);
   Directory dir=getDirectoryFromCache(df[0]);
   if (dir!=null) res=dir.deleteFile(pbRoom, df[1]);
   if (wasDir) dirsCache.clear();
  }

  return res;
 }




 public synchronized void deleteTree(String dname)
 {
  try
  {
   String root=((dname.endsWith(separatorS))?dname:dname+separator);
   String tmp, files[]=listTheTree(root);
   int t, len=ArrayExtras.length(files);

   for (t=0;t<len;t++)
   {
    tmp=root+separator+files[t];
    if (isFile(tmp))
    {
     deleteFile(tmp);
     files[t]=null;
    }
   }

   do
   {
    files=purgeNullsAndEmpties(files);
    len=ArrayExtras.length(files);

    for (t=0;t<len;t++)
    {
     tmp=root+separator+files[t];
     if (deleteFile(tmp))
     {
      files[t]=null;
     }
    }
   } while (len>0);
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }
 }



 public synchronized boolean renameFile(String currentFile, String renamedFile)
 {
  if (!pathOk(currentFile) || StringExtras.length(currentFile)<2) return false; // vorrai mica rinominare la root?????
  boolean res=exists(currentFile);

  if (res)
  {
   boolean wasDir=isDirectory(currentFile);
   String df[]=getDirAndFilename(currentFile);
   Directory dir=getDirectoryFromCache(df[0]);
   if (dir!=null) res=dir.rename(pbRoom, df[1], renamedFile);
   if (wasDir) dirsCache.clear();
  }

  return res;
 }





 public synchronized boolean isDirectory(String path)
 {
  boolean res=false;
  String df[]=getDirAndFilename(path);
  Directory dir=getDirectoryFromCache(df[0]);
  if (dir!=null) res=dir.isDirectory(df[1]);
  return res;
 }




 public synchronized boolean isFile(String path)
 {
  boolean res=false;
  String df[]=getDirAndFilename(path);
  Directory dir=getDirectoryFromCache(df[0]);
  if (dir!=null) res=dir.isFile(df[1]);
  return res;
 }







 public boolean isValidFileName(String path)
 {
  boolean res=isNotBlank(path);

  if (res)
  {
   String df[]=getDirAndFilename(path);
   res=(ArrayExtras.length(df)==2 && areNotBlank(df));
  }

  return res;
 }



 // returns:
 // -1    = the file does not exist
 // 0     = is a directory
 // other = the length of the file in bytes
 public synchronized long fileLength(String path)
 {
  long res=-1;
  String df[]=getDirAndFilename(path);
  Directory dir=getDirectoryFromCache(df[0]);
  if (dir!=null) res=dir.fileLength(pbRoom, df[1]);
  return res;
 }



 // returns:
 // 0 = the dir already exists
 // 1 = the dir has been created
 // 2 = the creation of the dir has failed
 public synchronized int testCreateDirectory(String dname)
 {
  int res=0;
  boolean bres=isDirectory(dname);

  if (!bres)
  {
   if (mkdirs(dname)) res=1;
   else res=2;
  }

  return res;
 }




 public synchronized boolean grantDirectory(String dname)
 {
  boolean res=isDirectory(dname);

  if (!res)
  {
   res=mkdirs(dname);
  }

  return res;
 }


 public synchronized boolean mkdirs(String dname)
 {
  boolean res=isDirectory(dname);

  if (!res)
  {
   if (!isFile(dname))
   {
    int i;
    StringBuilder dirs=new StringBuilder(separatorS);
    String s[]=unmerge(dname, separator);
    Directory cursor=getDirectoryFromCache("");
    boolean isDir;
    boolean isFile;

    for (i=0;i<s.length;i++)
    {
     isDir=cursor.isDirectory(s[i]);
     isFile=cursor.isFile(s[i]);
     if (isFile) return false;
     if (!isDir)
     {
      cursor.mkdir(pbRoom, s[i]);
      dirsCache.clearNulls();
     }

     dirs.append(s[i]);
     cursor=getDirectoryFromCache(dirs.toString());
     dirs.append(separator);
    }

    res=true;
   }
  }

  return res;
 }




 public synchronized String[] listAll(String dname, final String fileMask)
 {
  String res[]=null;

  if (pathOk(dname))
  {
   Directory dir=getDirectoryFromCache(dname);
   if (dir!=null)
   {
    FilenameFilter ff=null;
    if (fileMask!=null) ff=(dir1, name) -> doTheyMatch(name, fileMask, false);

    res=dir.listAll(ff);
   }
  }

  return res;
 }



 public synchronized String[] listAll(String dname)
 {
  return listAll(dname, null);
 }



 private synchronized String[] listDirsOrFiles(String dname, boolean wantDirs)
 {
  return listDirsOrFiles(dname, wantDirs, null);
 }


 private synchronized String[] listDirsOrFiles(final String dname, final boolean wantDirs, final String fileMask)
 {
  String res[]=null;

  if (pathOk(dname))
  {
   Directory dir=getDirectoryFromCache(dname);
   if (dir!=null)
   {
    FilenameFilter ff=new FilenameFilter()
    {
     public boolean accept(File dir, String name)
     {
      boolean isdir=isDirectory(mergePath(dname, name));
      boolean res=((wantDirs)?isdir:!isdir);

      if (res && isNotBlank(fileMask))
      {
       res=doTheyMatch(name, fileMask, false);
      }

      return res;
     }
    };

    res=dir.listAll(ff);
   }
  }

  return res;
 }



 public synchronized String[] listDirs(String dname)
 {
  return listDirsOrFiles(dname, true);
 }

 public synchronized String[] listDirs(String dname, String mask)
 {
  return listDirsOrFiles(dname, true, mask);
 }


 public synchronized String[] listFiles(String dname)
 {
  return listDirsOrFiles(dname, false);
 }


 /**
  *
  * @param dname
  * @param mask
  * @return      returns the list of found files (only names with extensions, not the directory!)
  */
 public synchronized String[] listFiles(String dname, String mask)
 {
  return listDirsOrFiles(dname, false, mask);
 }



 public synchronized String[] listTheTree(String dname)
 {
  String res[]=null;

  try
  {
   String root=((dname.endsWith(separatorS))?dname:dname+separator);
   res=ArrayExtras.toArray(listTheTree(dname, root, "", null), String.class);
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }

  return res;
 }



 private synchronized ArrayList<String> listTheTree(String dname, String root, String subDir, ArrayList<String> al)
 {
  if (al==null) al=new ArrayList<>();
  String tmp, files[]=listAll(dname);
  if (files!=null)
  {
   int t, len=ArrayExtras.length(files);

   for (t=0;t<len;t++)
   {
    tmp=subDir+files[t];
    al.add(tmp);
    tmp=root+tmp;
    listTheTree(tmp, root, subDir+files[t]+separatorS, al);
   }
  }

  return al;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public synchronized boolean copyFile(String sourceFname, String destFname)
 {
  return saveInFile(destFname, loadFromFile(sourceFname));
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public synchronized boolean saveInFile(String fname, String data)
 {
  boolean res;

  try
  {
   res=saveInFile(fname, data.getBytes(defaultCharsetName));
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }


 public synchronized boolean saveInFile(String fname, byte data[])
 {
  if (!pathOk(fname)) return false;
  boolean res=false;

  try
  {
   String df[]=getDirAndFilename(fname);
   String sdir=df[0];

   if (hasChars(sdir))
   {
    if (!exists(sdir)) mkdirs(sdir);
   }

   Directory dir=getDirectoryFromCache(sdir);
   if (dir!=null)
   {
    if (!dir.isDirectory(df[1]))
    {
     if (dir.isFile(df[1])) dir.deleteFile(pbRoom, df[1]);
     res=dir.saveInFile(pbRoom, df[1], data);
    }
   }
  } catch (Throwable tr){res=false;}

  return res;
 }







 public synchronized byte[] loadFromFile(String fname)
 {
  byte res[]=null;
  if (!pathOk(fname) || StringExtras.length(fname)<2) return res; // vorrai mica legger la root come fosse un file?????
  String df[]=getDirAndFilename(fname);
  Directory dir=getDirectoryFromCache(df[0]);
  if (dir!=null) res=dir.loadFromFile(pbRoom, df[1]);
  return res;
 }



 public synchronized String loadTextFromFile(String fname)
 {
  String res;

  try
  {
   res=loadTextFromFile(fname, defaultCharsetName);
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }





 public synchronized String loadTextFromFile(String fname, String charsetName) throws UnsupportedEncodingException
 {
  String res=null;

  byte data[]=loadFromFile(fname);
  if (data!=null) res=new String(data, charsetName);

  return res;
 }





 public synchronized String getNotExistentFileName(String dir, String fNameRoot, String fExtRoot)
 {
  return getNotExistentFileName(dir, fNameRoot, fExtRoot, 0);
 }


 public synchronized String getNotExistentFileName(String dir, String fNameRoot, String fExtRoot, int startNo)
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

   if (!dir.endsWith(separatorS)) dir+=separator;

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
  * occurrences of either / or \ in the current separator
  *
  * @param path String
  * @return String
  */
 public String adjustPath(String path)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=path;
   String okFS=separatorS;

   if (okFS.equals("\\"))
   {
    res=replace(res, "/", okFS);
   }
   else
   {
    res=replace(res, "\\", okFS);
    if (!okFS.equals("/")) res=replace(res, "/", okFS);
   }

   okFS=""+separator+separator;
   res=replace(res, okFS, ""+separator);
  }

  return res;
 }



 /**
  * The same of the above but this also scans for placeholders
  * changing them to the values listed in 'ht'.
  * For example if in 'ht' there is String "home/myhome/" corresponding
  * to the key "%MYHOME%" then this method replaces all the occurrences
  * of the string "%MYHOME%" eventually present in 'name' with
  * the string "home/myhome/"
  * Only String keys and String values of ht are considered.
  *
  * @param path String
  * @return String
  */
 public String adjustPath(String path, Map placeholders)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=path;

   if (placeholders!=null)
   {
    Object key;
    Object value;
    String old;
    boolean changed=true;
    int loopCount=0;
    int maxLoops=100;

    while (changed)
    {
     if (loopCount>maxLoops)
     {
      throw new RuntimeException("adjustPath(String name, Map placeholders) MAX LOOP COUNT REACHED!");
     }

     changed=false;

     for (Iterator i=placeholders.keySet().iterator();i.hasNext();)
     {
      key=i.next();
      value=placeholders.get(key);

      try
      {
       old=res;
       res=replace(res, (String)key, (String)value);
       if (!changed) changed=!StringExtras.areEqual(old, res);
      } catch (ClassCastException cce){}
     }
     loopCount++;
    }
   }
  }

  return adjustPath(res);
 }


 // Also Removes the Endig FileSeparator if it is there
 public String adjustPathREFS(String path)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=adjustPath(path);
   if (res.endsWith(separatorS)) res=res.substring(0, res.length()-1);
  }

  return res;
 }



 // Also Removes the Endig FileSeparator if it is there
 public String adjustPathREFS(String path, Map placeholders)
 {
  String res=null;

  if (isNotBlank(path))
  {
   res=adjustPath(path, placeholders);
   if (res.endsWith(separatorS)) res=res.substring(0, res.length()-1);
  }

  return res;
 }



 public String mergePath(String dir, String fileName)
 {
  return adjustPath(dir+separator+fileName);
 }


 public String mergeAll(String... dirs_and_subDirs)
 {
  StringBuilder sb=new StringBuilder();
  int t, len=ArrayExtras.length(dirs_and_subDirs);

  for (t=0;t<len;t++)
  {
   sb.append(dirs_and_subDirs[t]);
   sb.append(separator);
  }

  return adjustPath(sb.toString());
 }





 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static void fireException(String msg)
 {
  throw new me.as.lib.core.io.IOException(msg);
 }

 private static void fireException(Throwable tr)
 {
  throw new me.as.lib.core.io.IOException(tr);
 }



 static class DirectoryItem
 {
  String uppercaseName;
  String realName;
  int type=-1; // -1 = unknown, 0 = directory, 1 = file, (still unimplemented): 2 = hard link, 3 = soft link
  long pieceId;


  public void toBytes(BytesRoom br)
  {
   try
   {
    br.writeSmallString(uppercaseName);
    br.writeSmallString(realName);
    br.WriteInt(type);
    br.WriteLong(pieceId);
   } catch (Throwable tr){fireException(tr);}
  }


  public void fromBytes(BytesRoom br)
  {
   try
   {
    uppercaseName=br.readSmallString();
    realName=br.readSmallString();
    type=br.ReadInt();
    pieceId=br.ReadLong();
   } catch (Throwable tr){fireException(tr);}
  }

 }


 public static class Directory implements Binaryable
 {
  transient long pieceId;
  ListHashMap<String, DirectoryItem> items=new ListHashMap<String, DirectoryItem>();


  public byte[] toBytes()
  {
   byte res[]=null;

   try
   {
    MemBytesRoom mbr=new MemBytesRoom();
    int t, len=items.size();
    mbr.WriteInt(len);

    for (t=0;t<len;t++)
    {
     items.get(t).toBytes(mbr);
    }

    res=mbr.getContent();
   } catch (Throwable tr){fireException(tr);}

   return res;
  }




  public void fromBytes(byte bytes[])
  {
   items.clear();

   try
   {
    MemBytesRoom mbr=new MemBytesRoom();
    mbr.setContent(bytes);

    DirectoryItem di;
    int t, len=mbr.ReadInt();

    for (t=0;t<len;t++)
    {
     di=new DirectoryItem();
     di.fromBytes(mbr);
     items.put(di.uppercaseName, di);
    }
   } catch (Throwable tr){fireException(tr);}
  }





  long fileLength(PiecedBytesRoom pbRoom, String name)
  {
   long res=-1;
   DirectoryItem di=items.get(toUpperCase(name));

   if (di!=null)
   {
    res=0; // esiste sicuro, solo vediamo se è un file o una dir, se è dir diciamo che è grande 0 bytes!
    if (di.type==1)
    {
     res=pbRoom.getPieceSize(di.pieceId);
    }
   }

   return res;
  }



  boolean rename(PiecedBytesRoom pbRoom, String currentName, String newName)
  {
   boolean res=false;
   String key=toUpperCase(currentName);
   DirectoryItem di=items.get(key);

   if (di!=null)
   {
    items.remove(key);
    di.uppercaseName=toUpperCase(newName);
    di.realName=newName;
    items.put(di.uppercaseName, di);
    res=true;
   }

   if (res) pbRoom.setPiece(pieceId, ByteExtras.toBytes(this));

   return res;
  }



  boolean deleteFile(PiecedBytesRoom pbRoom, String name)
  {
   boolean res=false;
   String key=toUpperCase(name);
   DirectoryItem di;

   if (isFile(name))
   {
    di=items.get(key);
    pbRoom.setPiece(di.pieceId, null);
    items.remove(key);
    res=true;
   }
   else
   {
    if (isDirectory(name))
    {
     di=items.get(key);
     Directory tmpd=(Directory)ByteExtras.fromBytes(pbRoom.getPiece(di.pieceId));
     if (tmpd.items.size()==0)
     {
      pbRoom.setPiece(di.pieceId, null);
      items.remove(key);
      res=true;
     }
    }
   }

   if (res) pbRoom.setPiece(pieceId, ByteExtras.toBytes(this));

   return res;
  }


  long getPieceId(String name)
  {
   long res=-1;
   DirectoryItem di=items.get(toUpperCase(name));
   if (di!=null) res=di.pieceId;
   return res;
  }


  boolean isDirectory(String name)
  {
   boolean res=false;
   DirectoryItem di=items.get(toUpperCase(name));

   if (di!=null)
   {
    res=(di.type==0);
   }

   return res;
  }


  boolean isFile(String name)
  {
   boolean res=false;
   DirectoryItem di=items.get(toUpperCase(name));

   if (di!=null)
   {
    res=(di.type==1);
   }

   return res;
  }


  public String[] listAll(FilenameFilter filter)
  {
   String res[]=null;
   int t, len=items.size();

   if (len>0)
   {
    String tmpStr;
    res=new String[len];

    for (t=0;t<len;t++)
    {
     tmpStr=items.get(t).realName;
     if (filter==null || filter.accept(null, tmpStr)) res[t]=tmpStr;
     else res[t]=null;
    }

    res=purgeNullsAndEmpties(res);
   }

   return res;
  }



  byte[] loadFromFile(PiecedBytesRoom pbRoom, String fileName)
  {
   byte res[]=null;
   DirectoryItem di=items.get(toUpperCase(fileName));
   if (di!=null) res=pbRoom.getPiece(di.pieceId);
   return res;
  }



  boolean saveInFile(PiecedBytesRoom pbRoom, String fileName, byte data[])
  {
   boolean res=false;
   if (isFile(fileName) || isDirectory(fileName)) fireException("'"+fileName+"' already exists, cannot create a file with that name");

   DirectoryItem di=new DirectoryItem();
   di.uppercaseName=toUpperCase(fileName);
   di.realName=fileName;
   di.type=1; // 1 = file
   di.pieceId=pbRoom.addPiece(data);
   items.put(di.uppercaseName, di);
   pbRoom.setPiece(pieceId, ByteExtras.toBytes(this));
   res=true;

   return res;
  }


  void mkdir(PiecedBytesRoom pbRoom, String dirName)
  {
   if (isFile(dirName) || isDirectory(dirName)) fireException("'"+dirName+"' already exists, cannot create a dir with that name");

   DirectoryItem di=new DirectoryItem();
   di.uppercaseName=toUpperCase(dirName);
   di.realName=dirName;
   di.type=0; // 0 = directory
   di.pieceId=pbRoom.addPiece(ByteExtras.toBytes(new Directory()));
   items.put(di.uppercaseName, di);
   pbRoom.setPiece(pieceId, ByteExtras.toBytes(this));
  }



 };






 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 /*
 // TESTS SUCCESFUL ! ! !  but not everything has been tested! or not?


 public static void main(String args[])
 {
  // boolean fileOnDisk=true;
  //
   boolean fileOnDisk=false;


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  FileSystemRoom fsr;

  if (!fileOnDisk) fsr=new FileSystemRoom();
  else
  {
   String tfn=FileSystemExtras.getTemporaryFileName();
   FileBytesRoom fbr=new FileBytesRoom(tfn, false);
   fbr.open("rw");
   fsr=new FileSystemRoom(null, fbr);
  }



  if (fsr.saveInFile("badFileName.txt", "A file with an invalid file name"))
  {
   System.out.println("ERRRRRRRROR, did not recognize the bad file name");
  }

  fsr.saveInFile("/first.txt", "A file in the root directory");
  fsr.saveInFile("/sample/file.txt", "Hello, world!");
  fsr.saveInFile("/otherDir/otherFile.txt", "Other file content");
  fsr.saveInFile("/fullDir/innerDir/moreInner Dir/file one.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/moreInner Dir/file 2.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/moreInner Dir/file 3.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/a file.txt", "file content");
  fsr.saveInFile("/fullDir/fileA.txt", "file content");
  fsr.saveInFile("/fullDir/fileB.txt", "file content");
  fsr.saveInFile("/fullDir/fileC.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/file_I_.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/file_II_.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/file_III_.txt", "file content");
  fsr.saveInFile("/fullDir/innerDir/file_IV_.txt", "file content");


  System.out.println(fsr.loadTextFromFile("/sample/file.txt"));
  System.out.println(fsr.loadTextFromFile("/otherDir/otherFile.txt"));

  System.out.println("\n");StringExtras.systemOut(fsr.listAll("/", "*"));
  System.out.println("\n");StringExtras.systemOut(fsr.listAll("/", "s*"));
  System.out.println("\n");StringExtras.systemOut(fsr.listAll("/", "????????????")); // must return empty array
  System.out.println("\n");StringExtras.systemOut(fsr.listAll("/"));
  System.out.println("\n");StringExtras.systemOut(fsr.listAll(""));  // must return null array (because of bad file name)

  System.out.println("\n");StringExtras.systemOut(fsr.listDirs("/"));
  System.out.println("\n");StringExtras.systemOut(fsr.listDirs("/", "o*"));
  System.out.println("\n");StringExtras.systemOut(fsr.listFiles("/"));
  System.out.println("\n");StringExtras.systemOut(fsr.listFiles("/", "*s*"));


  System.out.println("\n");StringExtras.systemOut(fsr.listTheTree("/"));
  System.out.println("\n");StringExtras.systemOut(fsr.listTheTree("/fullDir"));


  //System.out.println("\n");StringExtras.systemOut(FileSystemExtras.listTheTree("C:\\tmp"));
 }

 */




}


