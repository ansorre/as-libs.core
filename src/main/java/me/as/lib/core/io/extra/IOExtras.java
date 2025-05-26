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


import me.as.lib.core.collection.RamTable;
import me.as.lib.core.io.MinimalReader;
import me.as.lib.core.io.MinimalWriter;

import java.util.*;

import static me.as.lib.core.lang.ByteExtras.unZipBytes;
import static me.as.lib.core.lang.ByteExtras.zipBytes;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.newAutoString;


public class IOExtras
{
 public static final int CONVENIENT_TO_COMPRESS_MINIMUM_SIZE = 512;

 public static boolean writeBytes(MinimalWriter mw, byte b[])
 {
  int len=((b!=null)?b.length:0);
  boolean res=writeIntAsString(mw, len, "]");

  if (res)
  {
   if (len>0) res=mw.Write(b);
  }

  return res;
 }


 public static byte[] readBytes(MinimalReader mr)
 {
  byte res[]=null;
  int len=readIntAsString(mr, "]");

  if (len>0)
  {
   res=new byte[len];
   mr.Read(res);
  }

  return res;
 }


 public static int readIntAsString(MinimalReader mr, String endingChars) throws NumberFormatException
 {
  int res=-1;

  String str=mr.ReadUntilOneOfThese(endingChars, endingChars);
  res=Integer.parseInt(str);

  return res;
 }

 public static boolean writeIntAsString(MinimalWriter mw, int num)
 {
  return writeIntAsString(mw, num, null);
 }

 public static boolean writeIntAsString(MinimalWriter mw, int num, String additionalText)
 {
  StringBuilder sb=new StringBuilder();
  sb.append(num);
  if (additionalText!=null) sb.append(additionalText);
  return mw.Write(sb.toString());
 }


 public static String readPerfectString(MinimalReader mr)
 {
  int length=Integer.parseInt(mr.Readln());

  if (length>0)
  {
   char qchars[]=new char[length];
   mr.Read(qchars);
   return new String(qchars);
  } else return "";
 }

 public static void writePerfectString(MinimalWriter mw, String str)
 {
  int length=((str!=null)?str.length():0);
  String tmpStr=""+length;
  mw.Writeln(tmpStr);

  if (length>0)
  {
   char ch[]=new char[length];
   str.getChars(0, length, ch, 0);
   mw.Write(ch);
  }
 }



 public static RamTable readRamTable(MinimalReader mr)
 {
  RamTable res=new RamTable();
  int cols=readIntAsString(mr, ".");
  int rows=readIntAsString(mr, ".");

  if (cols>0 && rows>0)
  {
   boolean ok=true;
   int c, r;
   int len;
   byte bytes[];

   for (r=0;r<rows && ok;r++)
   {
    for (c=0;c<cols && ok;c++)
    {
     len=readIntAsString(mr, ".");
     if (len>0)
     {
      bytes=new byte[len];
      ok=(mr.Read(bytes)==len);
      res.setString(c, r, newAutoString(bytes));
     }
    }
   }
  }

  return res;
 }

 public static RamTable readZippedRamTable(MinimalReader mr)
 {
  RamTable res;

  if (mr.Read()==1)
  {
   int len=mr.ReadInt();
   byte d[]=new byte[len];
   mr.Read(d);

   MemBytesRoom mbr=new MemBytesRoom();

   try
   {
    mbr.setContent(unZipBytes(d));
    mbr.setCurrentPosition(0);
    res=readRamTable(mbr);
   } catch (Throwable tr){res=null;}

   //System.out.println("WAS REALLY ZIPPED ! ! !");
  }
  else
  {
   res=readRamTable(mr);
   //System.out.println("WASn't really zipped ! ! !");
  }

  return res;
 }

 public static boolean writeZippedRamTable(MinimalWriter mw, RamTable rt)
 {
  boolean res=true;
  MemBytesRoom mbr=new MemBytesRoom();
  writeRamTable(mbr, rt);

  if (mbr.getSize()>CONVENIENT_TO_COMPRESS_MINIMUM_SIZE)
  {
   try
   {
    mw.Write(1);
    byte d[]=zipBytes(mbr.getContent());
    mw.WriteInt(d.length);
    mw.Write(d);
   } catch (Throwable tr){res=false;}
  }
  else
  {
   mw.Write(0);
   try
   {
    mw.Write(mbr.getContent());
   } catch (Throwable tr){res=false;}
  }

  return res;
 }


 public static boolean writeRamTable(MinimalWriter mw, RamTable rt)
 {
  boolean res;

  String tmpStr;
  int rows=((rt!=null)?rt.getRowsCount():0);
  int cols=((rt!=null)?rt.getColsCount():0);

  if (rows==0 || cols==0)
  {
   rows=0;
   cols=0;
  }

  // first we write how many columns we have in the table...
  res=writeIntAsString(mw, cols, ".");

  // ... than how many rows
  if (res) res=writeIntAsString(mw, rows, ".");

  if (cols>0 && rows>0 && res)
  {
   int c, r;

   for (r=0;r<rows && res;r++)
   {
    for (c=0;c<cols && res;c++)
    {
     tmpStr=rt.getString(c, r);

     if (isNotBlank(tmpStr))
     {
      res=writeIntAsString(mw, tmpStr.length(), ".");
      if (res) res=mw.Write(tmpStr);
     } else res=mw.Write("0.");
    }
   }
  }

  return res;
 }


 public static boolean writeHashtable(MinimalWriter mw, Hashtable ht)
 {
  boolean res=false;
  Object key, value;

  try
  {
   for (Enumeration e=ht.keys();e.hasMoreElements();)
   {
    key=e.nextElement();
    value=ht.get(key);

    mw.Write(key.toString());
    mw.Write("=");
    if (value!=null) mw.Writeln(value.toString());
   }

  } catch (Throwable ignore){}

  mw.Writeln(".");

  return res;
 }


 public static Hashtable readHashtable(MinimalReader mr)
 {
  Hashtable res=new Hashtable();
  String line;
  int idx;

  try
  {
   do
   {
    line=mr.Readln();
    if (!line.equals("."))
    {
     idx=line.indexOf('=');
     res.put(line.substring(0, idx), line.substring(idx+1, line.length()));
    }
   } while (!line.equals("."));

  } catch (Throwable tr){res=null;}

  return res;
 }



}
