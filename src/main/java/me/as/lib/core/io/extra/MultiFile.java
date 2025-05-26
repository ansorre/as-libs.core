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


import me.as.lib.core.extra.QuickSortExtras;
import me.as.lib.core.lang.StringExtras;
import me.as.lib.core.system.FileSystemExtras;

import java.util.*;
import java.io.*;

import static me.as.lib.core.lang.ByteExtras.fromBytes;
import static me.as.lib.core.lang.ByteExtras.toBytes;
import static me.as.lib.core.lang.ByteExtras.unZipBytes;
import static me.as.lib.core.lang.ByteExtras.zipBytes;
import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.doTheyMatch;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.lang.StringExtras.splitLast;


public class MultiFile
{
 private static final HashMap<String, ArrayList<MultiFileEntry>> multiFileEntries=new HashMap<String, ArrayList<MultiFileEntry>>();
 private static final HashMap<String, HashMap<String, MultiFileEntry>> multiFileEntriesHashMap=new HashMap<String, HashMap<String, MultiFileEntry>>();
 private String multiFilePath;
 private MultiFileEntry lastFound_MultiFileEntry=null;

 public MultiFile(String multiFilePath)
 {
  this.multiFilePath=multiFilePath;
 }


 public String getMultiFilePath()
 {
  return multiFilePath;
 }

 private String adjustFileName(String fileName)
 {
  fileName=replace(fileName, "\\", "/");
  fileName=replace(fileName, "//", "/");
  if (!fileName.startsWith("/")) fileName="/"+fileName;
  return fileName;
 }


 private HashMap<String, MultiFileEntry> getEntriesHashMap()
 {
  grantEntries();
  return multiFileEntriesHashMap.get(multiFilePath);
 }


 private ArrayList<MultiFileEntry> grantEntries()
 {
  ArrayList<MultiFileEntry> res;

  synchronized (multiFileEntries)
  {
   res=multiFileEntries.get(multiFilePath);

   if (res==null)
   {
    HashMap<String, MultiFileEntry> mfeHashMap=new HashMap<String, MultiFileEntry>();

    res=new ArrayList<MultiFileEntry>();
    long multiFileLength=0;

    if (FileSystemExtras.exists(multiFilePath))
    {
     multiFileLength=FileSystemExtras.fileLength(multiFilePath);
    }

    if (multiFileLength==0) FileSystemExtras.deleteFile(multiFilePath);
    else
    {
     try
     {
      RandomAccessFile raf=new RandomAccessFile(multiFilePath, "r");
      MultiFileEntry mfe;
      boolean moreEntries;
      int curPos=0;

      do
      {
       res.add(mfe=readEntry(raf));

       mfe.entry_position_in_MultiFile=curPos;curPos+=mfe.entryBytesLength;
       mfe.fileData_position_in_MultiFile=curPos;curPos+=(int)mfe.fileLength;

       if (!mfe.isDeleted) mfeHashMap.put(mfe.fileName, mfe);

       moreEntries=(mfe.fileLength+raf.getFilePointer()<multiFileLength);
       if (moreEntries) raf.skipBytes((int)mfe.fileLength);
      } while (moreEntries);

      raf.close();
     }
     catch (Throwable tr)
     {
      throw new me.as.lib.core.io.IOException(tr);
     }
    }

    multiFileEntries.put(multiFilePath, res);
    multiFileEntriesHashMap.put(multiFilePath, mfeHashMap);
//    debugEntries(res);
   }
  }

  return res;
 }


 public void dispose()
 {
  synchronized (multiFileEntries)
  {
   multiFileEntries.remove(multiFilePath);
   multiFileEntriesHashMap.remove(multiFilePath);
  }
 }


 private void writeEntry(RandomAccessFile raf, MultiFileEntry mfe)
 {
  synchronized (multiFileEntries)
  {
   try
   {
    long ifp=raf.getFilePointer();
    raf.writeBoolean(mfe.isDeleted);
    raf.writeLong(mfe.fileLength);
    byte data[]=toBytes(mfe.fileName);
    raf.writeInt(data.length);
    raf.write(data);
    mfe.entryBytesLength=(int)(raf.getFilePointer()-ifp);
    mfe.fileData_position_in_MultiFile=mfe.entry_position_in_MultiFile+mfe.entryBytesLength;
   }
   catch (Throwable tr)
   {
    throw new me.as.lib.core.io.IOException(tr);
   }
  }
 }


 private MultiFileEntry readEntry(RandomAccessFile raf)
 {
  MultiFileEntry res=new MultiFileEntry();

  synchronized (multiFileEntries)
  {
   try
   {
    long ifp=raf.getFilePointer();
    res.isDeleted=raf.readBoolean();
    res.fileLength=raf.readLong();
    int nameLen=raf.readInt();
    byte data[]=new byte[nameLen];
    raf.read(data);
    res.fileName=(String)fromBytes(data);
    res.entryBytesLength=(int)(raf.getFilePointer()-ifp);
   }
   catch (Throwable tr)
   {
    throw new me.as.lib.core.io.IOException(tr);
   }
  }

  return res;
 }


 public boolean saveInFile(String fname, String data)
 {
  return saveInFile(fname, data.getBytes());
 }


 public boolean saveInFile(String fileName, byte data[])
 {
  boolean res;
  fileName=adjustFileName(fileName);

  try
  {
   synchronized (multiFileEntries)
   {
    deleteFile(fileName);
    ArrayList<MultiFileEntry> entries=grantEntries();
    MultiFileEntry mfe=new MultiFileEntry();
    mfe.isDeleted=false;

    int dlen=((data!=null)?data.length:0);
    if (dlen>0)
    {
     data=zipBytes(data);
     dlen=data.length;
    }

    mfe.fileLength=((data!=null)?data.length:0);
    mfe.fileName=fileName;

    if (entries.size()>0)
    {
     MultiFileEntry pmfe=entries.get(entries.size()-1);
     mfe.entry_position_in_MultiFile=pmfe.fileData_position_in_MultiFile+(int)pmfe.fileLength;
    }
    else
    {
     mfe.entry_position_in_MultiFile=0;
    }

    entries.add(mfe);
    getEntriesHashMap().put(mfe.fileName, mfe);


    long multiFileLength=0;
    if (FileSystemExtras.exists(multiFilePath)) multiFileLength=FileSystemExtras.fileLength(multiFilePath);
    RandomAccessFile raf=new RandomAccessFile(multiFilePath, "rw");
    if (multiFileLength>0) raf.seek(multiFileLength);
    writeEntry(raf, mfe);
    raf.write(data);
    raf.close();
    res=true;
   }
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }




 private int getFileEntryBytesPos(String fileName)
 {
  return _getBytesPos(fileName, true);
 }

 private int getFileBytesPos(String fileName)
 {
  return _getBytesPos(fileName, false);
 }


 private int _speed_getBytesPos(String fileName, boolean entry)
 {
  int res=-1;

  synchronized (multiFileEntries)
  {
   HashMap<String, MultiFileEntry> ehm=getEntriesHashMap();
   if (ehm!=null)
   {
    MultiFileEntry mfe=ehm.get(fileName);
    if (mfe!=null)
    {
     lastFound_MultiFileEntry=mfe;
     res=((entry)?mfe.entry_position_in_MultiFile:mfe.fileData_position_in_MultiFile);
    }
   }
  }

  return res;
 }


 private int _getBytesPos(String fileName, boolean entry)
 {
//  int res1=_slow_getBytesPos(fileName, entry);
  int res2=_speed_getBytesPos(fileName, entry);

//  if (res1!=res2)
//  {
//   System.out.println("ERRRRRRRRRRRRROR _________________----> res1= "+res1+"   but  res2="+res2);
//  }
//  return res1;
  return res2;
 }


 private int _slow_getBytesPos(String fileName, boolean entry)
 {
  synchronized (multiFileEntries)
  {
   int res=0;
   ArrayList<MultiFileEntry> entries=grantEntries();

   for (MultiFileEntry mfe : entries)
   {
    if (!mfe.isDeleted && StringExtras.areEqual(fileName, mfe.fileName, false))
    {
     if (!entry) res+=mfe.entryBytesLength;
     lastFound_MultiFileEntry=mfe;
     return res;
    }

    res+=mfe.entryBytesLength;
    res+=mfe.fileLength;
   }
  }

  return -1;
 }



 private MultiFileEntry getMultiFileEntry(String fileName)
 {
  synchronized (multiFileEntries)
  {
   if (lastFound_MultiFileEntry!=null && StringExtras.areEqual(lastFound_MultiFileEntry.fileName, fileName))
   {
    return lastFound_MultiFileEntry;
   }
   else
   {
    HashMap<String, MultiFileEntry> ehm=getEntriesHashMap();
    lastFound_MultiFileEntry=ehm.get(fileName);
    return lastFound_MultiFileEntry;
   }
  }
 }



/*
 private MultiFileEntry getMultiFileEntry(String fileName)
 {
  synchronized (multiFileEntries)
  {
   if (lastFound_MultiFileEntry!=null && StringExtras.areEqual(lastFound_MultiFileEntry.fileName, fileName))
   {
    return lastFound_MultiFileEntry;
   }
   else
   {
    ArrayList<MultiFileEntry> entries=grantEntries();

    for (MultiFileEntry mfe : entries)
    {
     if (!mfe.isDeleted && StringExtras.areEqual(fileName, mfe.fileName, false))
     {
      lastFound_MultiFileEntry=mfe;
      return mfe;
     }
    }
   }
  }

  return null;
 }
*/


 public boolean exists(String fileName)
 {
  return (getFileBytesPos(adjustFileName(fileName))>0);
 }


 public byte[] loadFromFile(String fileName)
 {
  byte res[];
  fileName=adjustFileName(fileName);

  try
  {
   synchronized (multiFileEntries)
   {
    int pos=getFileBytesPos(fileName);
    if (pos>=0)
    {
     MultiFileEntry mfe=getMultiFileEntry(fileName);
     res=new byte[(int)mfe.fileLength];

     if (mfe.fileLength>0)
     {
      RandomAccessFile raf=new RandomAccessFile(multiFilePath, "r");
      raf.seek(pos);
      raf.read(res);
      raf.close();
      res=unZipBytes(res);
     }
    } else res=null;
   }
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }


 public String loadTextFromFile(String fileName)
 {
  return loadTextFromFile(fileName, defaultCharsetName);
 }


 public String loadTextFromFile(String fileName, String charsetName)
 {
  String res;

  try
  {
   res=new String(loadFromFile(fileName), charsetName);
  }
  catch (Throwable tr)
  {
   res=null;
//   tr.printStackTrace();
  }

  return res;
 }


 public boolean deleteFile(String fileName)
 {
  boolean res;
  fileName=adjustFileName(fileName);

  try
  {
   synchronized (multiFileEntries)
   {
    int pos=getFileEntryBytesPos(fileName);
    if (pos>=0)
    {
     MultiFileEntry mfe=getMultiFileEntry(fileName);
     mfe.isDeleted=true;
     getEntriesHashMap().remove(mfe.fileName);

     RandomAccessFile raf=new RandomAccessFile(multiFilePath, "rw");
     raf.seek(pos);
     raf.writeBoolean(true);
     raf.close();
     res=true;

    } else res=false;
   }
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }


 public boolean isFile(String path)
 {
  return exists(path);
 }


 public boolean isDirectory(String path)
 {
  if (isFile(path)) return false;
  else
  {
   HashMap<String, MultiFileEntry> ehm=getEntriesHashMap();

   if (!path.endsWith("/")) path+="/";

   for (String p : ehm.keySet())
   {
    if (p.startsWith(path)) return true;
   }
  }

  return false;
 }




 private String[] _list(String dname, boolean files, boolean directories, String mask, boolean treeMode)
 {
  HashMap<String, MultiFileEntry> ehm=getEntriesHashMap();

  // note (1)
  // these two HashMaps are used just to avoid the extreme slowiness of ArrayList.contains
  // I put 'myArray' with the key and later I use:
  // 'if (HashMap.get(what)==null)...' instrad of 'if (!ArrayList.contains) ...'
  HashMap<String, Object> yetTested=new HashMap<>();
  HashMap<String, Object> yetAdded=new HashMap<>();

  ArrayList<String> al=new ArrayList<String>();
  String myArray[]=new String[2];
  String p, toAdd;

  if (!dname.equals("/"))
  {
   while (dname.endsWith("/")) dname=dname.substring(0, dname.length()-1);
  }


  for (String op : ehm.keySet())
  {
   if (!op.startsWith(dname)) continue;
   p=op;

   do
   {
    toAdd=null;
    String e=p.substring(dname.length());

    if (treeMode || e.indexOf('/')<1)
    {
     if ((files && isFile(p)) || (directories && isDirectory(p)))
     {
      if (treeMode)
      {
       if (!StringExtras.areEqual(p, dname))
       {
        toAdd=e;
        if (toAdd.startsWith("/")) toAdd=toAdd.substring(1);
       }
      }
      else
      {
       toAdd=splitLast(p, '/', myArray)[1];

       if (!StringExtras.areEqual(replace("/"+dname+"/"+toAdd, "//", "/"), p))
       {
        toAdd=null;
       }
      }
     }

     if (toAdd!=null)
     {
      if (yetAdded.get(toAdd)==null)   // read the note (1)
      {
       yetAdded.put(toAdd, myArray);  // read the note (1)
       if (!hasChars(mask) || doTheyMatch(toAdd, mask, false))
       {
        al.add(toAdd);
       }
      }
     }
    }

    if (yetTested.get(p)==null)   // read the note (1)
    {
     yetTested.put(p, myArray);   // read the note (1)

     if (p.lastIndexOf("/")>0)
     {
      p=splitLast(p, '/', myArray)[0];

      if (yetTested.get(p)==null)   // read the note (1)
      {
       yetTested.put(p, myArray);  // read the note (1)
      } else p=null;

     } else p=null;
    } else p=null;

   } while (p!=null);
  }

  return ((al.size()>0) ? QuickSortExtras.sort(al.toArray(new String[al.size()])) : null);
 }




 public String[] listAll(String dname, final String mask)
 {
  return _list(dname, true, true, mask, false);
 }


 public String[] listAll(String dname)
 {
  return _list(dname, true, true, null, false);
 }


 public String[] listDirs(String dname)
 {
  return _list(dname, false, true, null, false);
 }


 public String[] listDirs(String dname, String mask)
 {
  return _list(dname, false, true, mask, false);
 }


 public String[] listFiles(String dname)
 {
  return _list(dname, true, false, null, false);
 }


 public String[] listFiles(String dname, String mask)
 {
  return _list(dname, true, false, mask, false);
 }


 public String[] listTheTree(String dname)
 {
  return _list(dname, true, true, null, true);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private void debugEntries(ArrayList<MultiFileEntry> entries)
 {
  System.out.println("------ MultiFile: "+multiFilePath);
  System.out.println("------ "+entries.size()+" entries");

  int c=0;

  for (MultiFileEntry mfe : entries)
  {
   c++;

//   if (mfe.isDeleted)
   {
    System.out.println("-- entry "+c+" deleted: "+mfe.isDeleted+
                       "  fileName: "+mfe.fileName+
                       "  fileLength: "+mfe.fileLength+
                       "  (ebl: "+mfe.entryBytesLength+")");
   }
  }
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 class MultiFileEntry
 {
  boolean isDeleted;
  long fileLength;
  String fileName;

  int entryBytesLength;


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
  // used only at runtime
  int entry_position_in_MultiFile;
  int fileData_position_in_MultiFile;

 }




}
