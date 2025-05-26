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
import me.as.lib.core.extra.Box;
import me.as.lib.core.io.BytesRoom;
import me.as.lib.core.io.extra.MemBytesRoom;
import me.as.lib.core.io.extra.SpeedBytesWrapper;
import me.as.lib.core.math.MathExtras;

import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.system.FileSystemExtras.adjustPath;
import static me.as.lib.core.system.FileSystemExtras.mkdirs;
import static me.as.lib.core.system.FileSystemExtras.saveInFile;
import static me.as.lib.core.lang.StringExtras.defaultCharsetName;


public class ByteExtras
{
 public static final int KILOBYTE = 1024;
 public static final int MEGABYTE = 1048576;


 public static String getMD5(byte bytes[])
 {
  String res;

  try
  {
   MessageDigest md=MessageDigest.getInstance("MD5");
   res=Base64.getEncoder().encodeToString(md.digest(bytes));
//   res=Base64.encodeBytes(md.digest(bytes));
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }





 // compares the b.length bytes of b with the b.length bytes in src
 // starting from src[startIdx] and returns
 // -2 if there are range problems
 // -1 if the bytes in src[] are lower than the bytes in b[]
 //  0 if the bytes in src[] are all equals to those in b[]
 //  1 if the bytes in src[] are bigger than those in b[]
 public static int compare(byte src[], byte b[], int startIdx)
 {
  int res=-2;

  if (src!=null && b!=null && startIdx>=0)
  {
   int slen=src.length;
   int blen=b.length;

   if (startIdx+blen<=slen)
   {
    res=0;
    int t=0;

    while (t<blen && res==0)
    {
     if (src[t+startIdx]<b[t]) res=-1;
     else
     {
      if (src[t+startIdx]>b[t]) res=1;
      else t++;
     }
    }
   }
  }

  return res;
 }



 public static boolean areEqual(byte bytes1[], byte bytes2[])
 {
  return (compare(bytes1, bytes2, 0)==0);
 }





 public static int find(byte src[], byte b, int startIdx)
 {
  int res=-1;

  if (src!=null && startIdx>=0)
  {
   int t, len=src.length;

   if (len>startIdx)
   {
    t=startIdx;

    while (t<len && res==-1)
    {
     if (src[t]==b) res=t;
     else t++;
    }
   }
  }

  return res;
 }

 public static int find(byte src[], byte b[], int startIdx)
 {
  int res=-1;

  if (src!=null && b!=null && startIdx>=0)
  {
   int slen=src.length;
   int tmp, blen=b.length;
   int si=startIdx;

   if (slen>startIdx && blen>0)
   {
    int ret=-3;

    while (res==-1 && ret!=-2)
    {
     tmp=find(src, b[0], si);

     if (tmp>=0)
     {
      ret=compare(src, b, tmp);
      if (ret==0) res=tmp;
      else si=tmp+1;
     } else ret=-2;
    }
   }
  }

  return res;
 }

 public static int find(byte src[], byte b)
 {
  return find(src, b, 0);
 }

 public static int find(byte src[], byte b[])
 {
  return find(src, b, 0);
 }

 public static Vector unmerge(byte src[], byte b)
 {
  byte bb[]=new byte[1];
  bb[0]=b;
  return unmerge(src, bb);
 }


 public static byte[] merge(byte[] b1, byte[] b2)
 {
  byte res[]=null;
  int l1=((b1!=null)?b1.length:0);
  int l2=((b2!=null)?b2.length:0);

  if (l1!=0 && l2!=0)
  {
   res=new byte[l1+l2];
   System.arraycopy(b1, 0, res, 0, l1);
   System.arraycopy(b2, 0, res, l1, l2);
  }
  else
  {
   if (l1!=0) res=b1;
   else if (l2!=0) res=b2;
  }

  return res;
 }




 public static Vector unmerge(byte src[], byte b[])
 {
  Vector res=null;

  if (src!=null && b!=null)
  {
   int slen=src.length;
   int blen=b.length;

   if (slen>=blen && blen>0)
   {
    boolean stillmore=true;
    int found=find(src, b, 0);
    int f2;
    byte piece[];
    int pieces;

    stillmore=(found>=0);

    if (found>0)
    {
     res=new Vector();
     pieces=found;
     piece=new byte[pieces];
     System.arraycopy(src, 0, piece, 0, pieces);
     res.addElement(piece);
    }

    while (stillmore)
    {
     if (res==null) res=new Vector();

     f2=find(src, b, found+blen);

     if (f2==-1)
     {
      f2=src.length;
      stillmore=false;
     }

     pieces=f2-(found+blen);

     if (pieces>0)
     {
      piece=new byte[pieces];
      System.arraycopy(src, found+blen, piece, 0, pieces);
      res.addElement(piece);
     } else res.addElement(null);

     found=f2;
    }
   }
  }

  return res;
 }

 public static boolean isNotEmpty(byte src[])
 {
  boolean res=false;

  if (src!=null)
  {
   res=(src.length>0);
  }

  return res;
 }

 public static byte[] replaceAll(byte src[], byte current[], byte newBytes[])
 {
  return replaceAll(src, current, newBytes, null);
 }

 public static byte[] replaceAll(byte src[], byte current[], byte newBytes[], Box<Integer> count)
 {
  byte res[]=src;

  if (isNotEmpty(src) && isNotEmpty(current))
  {
   int idx;

   do
   {
    idx=find(res, current);

    if (idx>=0)
    {
     res=replace(res, current, newBytes);

     if (count!=null)
      count.element++;
    }
   } while (idx>=0);
  }

  return res;
 }


 public static byte[] replace(byte src[], byte current[], byte newBytes[])
 {
  byte res[]=null;

  if (isNotEmpty(src) && isNotEmpty(current))
  {
   int idx=find(src, current);

   if (idx>=0)
   {
    int slen=src.length;
    int clen=current.length;

    ByteArrayOutputStream baos=new ByteArrayOutputStream(slen+((newBytes!=null)?newBytes.length:0));

    try
    {
     if (idx>0)
     {
      baos.write(src, 0, idx);
     }

     if (newBytes!=null)
     {
      baos.write(newBytes);
     }

     if (idx<slen-clen)
     {
      baos.write(src, idx+clen, slen-(idx+clen));
     }
    } catch (Throwable ignore){}

    res=baos.toByteArray();
   }
  }

  return res;
 }



 public static byte[] reverse(byte b[])
 {
  byte res[];

  if (b!=null)
  {
   int len=b.length;

   if (len>1)
   {
    res=new byte[len];
    for (int t=0;t<len;t++) res[t]=b[len-1-t];
   } else res=b;
  } else res=null;

  return res;
 }

 /**
  *
  * @param b
  * @param beginIndex - the beginning index, inclusive.
  * @param endIndex - the ending index, exclusive.
  * @return
  */

 public static byte[] subbytes(byte b[], int beginIndex, int endIndex)
 {
  int len=endIndex-beginIndex;
  byte res[]=new byte[len];

  System.arraycopy(b, beginIndex, res, 0, len);

  return res;
 }


 private static final byte oneByte[]=new byte[1];

 public static byte[] append(byte b[], int additional)
 {
  synchronized (oneByte)
  {
   oneByte[0]=(byte)additional;
   return append(b, oneByte);
  }
 }


 public static byte[] append(byte b[], byte additional[])
 {
  byte res[]=null;
  int ilen=((b!=null)?b.length:0);
  int aplen=((additional!=null)?additional.length:0);
  int nlen=ilen+aplen;

  if (nlen>0)
  {
   res=new byte[nlen];
   if (ilen>0) System.arraycopy(b, 0, res, 0, ilen);
   if (aplen>0) System.arraycopy(additional, 0, res, ilen, aplen);
  }

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static byte[] numberToBytes(Object number)
 {
  byte res[];
  MemBytesRoom mbr=new MemBytesRoom();

  try
  {
   switch (ArrayExtras.select(Types.classes2, number.getClass()))
   {
    // Short.class
    case 2:mbr.WriteShort((Short)number);break;

    // Integer.class
    case 3:mbr.WriteInt((Integer)number);break;

    // Long.class
    case 4:mbr.WriteLong((Long)number);break;

    //Float.class
    case 6:mbr.WriteFloat((Float)number);break;

    // Double.class
    case 7:mbr.WriteDouble((Double)number);break;
    default:throw new RuntimeException("Invalid number type: "+number.getClass().getName());
   };

   res=mbr.getContent();

  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }




 public static Object bytesToNumber(byte b[], Class numberClass)
 {
  Object res;
  SpeedBytesWrapper sbw=new SpeedBytesWrapper();

  try
  {
   sbw.setContent(b);

   switch (ArrayExtras.select(Types.classes2, numberClass))
   {
    // Short.class
    case 2:res=sbw.ReadShort();break;

     // Integer.class
    case 3:res=sbw.ReadInt();break;

     // Long.class
    case 4:res=sbw.ReadLong();break;

     //Float.class
    case 6:res=sbw.ReadFloat();break;

     // Double.class
    case 7:res=sbw.ReadDouble();break;
    default:throw new RuntimeException("Invalid numberClass: "+numberClass.getName());
   };

  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }




 /*
 public static void main(String args[])
 {
  long l=System.currentTimeMillis();
  long r=(Long)bytesToNumber(numberToBytes(l), Long.class);
  System.out.println("l:"+l);
  System.out.println("r:"+r);
 }
 */




 public static long toUnsignedInteger(int i)
 {
  // eh, eh!
  return Long.parseLong(Integer.toHexString(i), 16);
 }















 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .




 public static byte[] zipFiles(Map<String, byte[]> files) throws me.as.lib.core.io.IOException
 {
  try
  {
   byte bytes[];
   ByteArrayOutputStream baos=new ByteArrayOutputStream();
   ZipOutputStream zos=new ZipOutputStream(baos);

   for (String file : files.keySet())
   {
    bytes=files.get(file);
 //   if (bytes!=null && bytes.length>0)
    {
     ZipEntry ze=new ZipEntry(file);
     ze.setMethod(ZipEntry.DEFLATED);
     zos.putNextEntry(ze);
     zos.write(bytes);
    }
   }

   zos.finish();
   zos.close();
   baos.flush();
   return baos.toByteArray();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }





 public static byte[] zipBytes(byte b[]) throws me.as.lib.core.io.IOException
 {
  try
  {
   ByteArrayOutputStream baos=new ByteArrayOutputStream();
   ZipOutputStream zos=new ZipOutputStream(baos);
   ZipEntry ze=new ZipEntry("d");
   ze.setMethod(ZipEntry.DEFLATED);
   zos.putNextEntry(ze);
   zos.write(b);
   zos.finish();
   zos.close();
   baos.flush();
   return baos.toByteArray();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }



 /*
 public static byte[] unZipBytes(byte b[]) throws me.as.lib.core.io.IOException
 {
  byte res[]=null;
  ByteArrayInputStream bais=new ByteArrayInputStream(b);
  ZipInputStream zis=new ZipInputStream(bais);
  zis.getNextEntry();
  res=readZippedData(zis, -1);
  return res;
 }
 */




 public static byte[] unZipBytes(byte b[]) throws me.as.lib.core.io.IOException
 {
  try
  {
   byte res[];
   SpeedBytesWrapper sbw=new SpeedBytesWrapper();
   sbw.setContent(b);
   ZipInputStream zis=new ZipInputStream(sbw.toInputStream(0));
   zis.getNextEntry();
   res=readZippedData(zis, -1);
   return res;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }




 public static void unZipOnDisk(byte zippedBytes[], String destDirectory) throws me.as.lib.core.io.IOException
 {
  ByteArrayInputStream bais=new ByteArrayInputStream(zippedBytes);
  ZipInputStream zis=new ZipInputStream(bais);
  ZipEntry ze;
  String name, fname;
  byte uncompressedData[];

  do
  {
   try
   {
    ze=zis.getNextEntry();
   } catch (Throwable tr){ze=null;}

   if (ze!=null)
   {
    name=ze.getName();

    fname=adjustPath(destDirectory+File.separator+name);
//    System.out.println("\n\nfname="+fname);

    if (!ze.isDirectory())
    {
     uncompressedData=readZippedData(zis, ze.getSize());
     saveInFile(fname, uncompressedData);
    } else mkdirs(fname);
   }
  } while (ze!=null);
 }


 public static HashMap<String, byte[]> unZipInMemory(byte zippedBytes[]) throws me.as.lib.core.io.IOException
 {
  HashMap<String, byte[]> res=null;
  ByteArrayInputStream bais=new ByteArrayInputStream(zippedBytes);
  ZipInputStream zis=new ZipInputStream(bais);
  ZipEntry ze;
  String name;
  byte uncompressedData[];

  do
  {
   try
   {
    ze=zis.getNextEntry();
   } catch (Throwable tr){ze=null;}

   if (ze!=null)
   {
    if (!ze.isDirectory())
    {
     uncompressedData=readZippedData(zis, ze.getSize());
     name=ze.getName();

     if (res==null) res=new HashMap<>();
     res.put(name, uncompressedData);
    }
   }
  } while (ze!=null);

  return res;
 }


 private static int _unzip_buf_size=2000000;
 private static byte _bu_Buffer[]=new byte[_unzip_buf_size];
 private static byte tmp_unzip_Buffer[]=new byte[_unzip_buf_size];


 //private static int deb_tmp_maxSize=0;



 public static byte[] readZippedData(InputStream zis, long uncompressedSize) throws me.as.lib.core.io.IOException
 {
  try
  {
   byte res[];

   synchronized (_bu_Buffer)
   {
    MemBytesRoom mbr=new MemBytesRoom();
    byte oldMbrBytes[]=mbr.unmountContent();

    if (uncompressedSize>_bu_Buffer.length)
    {
     _bu_Buffer=new byte[(int)uncompressedSize];
     tmp_unzip_Buffer=new byte[(int)uncompressedSize];
    } else uncompressedSize=_bu_Buffer.length;

    mbr.mountContent(_bu_Buffer);

    int step=(int)uncompressedSize;
    byte temp[]=tmp_unzip_Buffer;
    int readed;

    do
    {
     readed=zis.read(temp, 0, step);
     if (readed>=0)
     {
      mbr.write(temp, 0, readed);
     }
    } while (readed>0);

    res=new byte[(int)mbr.getCurrentPosition()];
    _bu_Buffer=mbr.unmountContent();
    mbr.mountContent(oldMbrBytes);

    if (_bu_Buffer.length!=tmp_unzip_Buffer.length) tmp_unzip_Buffer=new byte[_bu_Buffer.length];

    System.arraycopy(_bu_Buffer, 0, res, 0, res.length);
   }

   return res;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }





 public static byte[] copyInNew(byte b[], int off, int len)
 {
  byte res[]=new byte[len];
  System.arraycopy(b, off, res, 0, len);
  return res;
 }


 public static byte[] string2bytesZeroEnded(String str)
 {
  byte res[]=null;

  if (str!=null)
  {
   int len=str.length();

   if (len>0)
   {
    byte tmp[]=str.getBytes();

    res=new byte[tmp.length+1];
    System.arraycopy(tmp, 0, res, 0, tmp.length);
    res[tmp.length]=0;
   }
  }

  return res;
 }


 public static int length(byte b[])
 {
  return ((b!=null)?b.length:0);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // these are a lot of methods to serialize a lot of java objects avoiding the ever
 // changing mechanisms for serialization included in Java

 //private static final int autoZipSizeThreshold  = 2000000000;

 // under 4096 it's not useful to zip because File Systems usually never allocate less than 4096 for a file
 private static final int autoZipSizeThreshold  = 4097;
// private static final int autoZipSizeThreshold  = 400000000;



 // When data is zipped, to the short describing the java type is added zippedDataModifier (8000)
 // and an integer (4 bytes) will follow (before the data itself) indicating how many bytes
 // of zipped data will follow.
 private static final short zippedDataModifier = 8000;

 // These shorts describe the java type of the data that will follow.
 // These are all the (toBytes/fromBytes)able java types supported so far.
 // Primitive types are not allowed (they are first converted to the appropriate java classe),
 // but arrays of primitives are!
 // Also arrays of non-primitive types are supported (some of them!).

 private static final short type_null       =  0;
 private static final short type_byte       =  1;
 private static final short type_Byte       =  2;
 private static final short type_short      =  3;
 private static final short type_Short      =  4;
 private static final short type_int        =  5;
 private static final short type_Integer    =  6;
 private static final short type_long       =  7;
 private static final short type_Long       =  8;
 private static final short type_char       =  9;
 private static final short type_Character  = 10;
 private static final short type_float      = 11;
 private static final short type_Float      = 12;
 private static final short type_double     = 13;
 private static final short type_Double     = 14;
 private static final short type_boolean    = 15;
 private static final short type_Boolean    = 16;
 private static final short type_Class      = 17;
 private static final short type_Object     = 18;
 private static final short type_String     = 19;
 private static final short type_HashMap    = 20;
 private static final short type_RamTable   = 21;
 private static final short type_Calendar   = 22;

 private static final short type_array        = zippedDataModifier-1;
 private static final short type_binaryable   = zippedDataModifier-2;
 private static final short type_serializable = zippedDataModifier-3;




 // Arrays are converted to bytes this way:
 // first there is the short 'type_array' (modified by zippedDataModifier if data is zipped)
 // then will follow a short describing the java type of the elements of the array
 // then will follow a short describing how many dimension the array has
 // then will follow the data (zipped if 'type_array' has the zippedDataModifier in it)
 // the data is organized this way:
 //   - a number of integers (4 bytes) equal to the number of the dimensions of the array. These
 //     integers indicates the position (in the array) of the element, so they are indexes
 //   - the element (without any type descriptor (you already know that!)
 //   - repeat indexes and element until all the elments are written
 //   - you know that there are no more elements when all the indexes are equal to Integer.MAX_VALUE


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static final Class supportedClasses[]=new Class[]
 {
  null,
  byte.class,
  Byte.class,
  short.class,
  Short.class,
  int.class,
  Integer.class,
  long.class,
  Long.class,
  char.class,
  Character.class,
  float.class,
  Float.class,
  double.class,
  Double.class,
  boolean.class,
  Boolean.class,
  Class.class,
  Object.class,
  String.class,
  HashMap.class,
  RamTable.class,
  GregorianCalendar.class
 };




 public static Object fromBytes(byte bytes[])
 {
  return fromBytes(bytes, defaultCharsetName);
 }




 public static byte[] toBytes(Object o)
 {
  return toBytes(o, 0);
 }

 public static byte[] toBytes(Object o, int bufferSizeHint)
 {
  return toBytes(o, defaultCharsetName, bufferSizeHint);
 }


 public static byte[] toBytes(Object o, String charsetName)
 {
  return toBytes(o, charsetName, 0);
 }





 public static byte[] toBytes(Object o, boolean disableZip)
 {
  return toBytes(o, 0, disableZip);
 }

 public static byte[] toBytes(Object o, int bufferSizeHint, boolean disableZip)
 {
  return toBytes(o, defaultCharsetName, bufferSizeHint, disableZip);
 }



 public static byte[] toBytes(Object o, String charsetName, boolean disableZip)
 {
  return toBytes(o, charsetName, 0, disableZip);
 }




 public static byte[] toBytes(Object o, String charsetName, int bufferSizeHint)
 {
  return toBytes(o, charsetName, bufferSizeHint, false);
 }



 public static byte[] toBytes(Object o, String charsetName, int bufferSizeHint, boolean disableZip)
 {
  byte res[];
  MemBytesRoom mbr;
  if (bufferSizeHint>0) mbr=new MemBytesRoom(bufferSizeHint);
  else mbr=new MemBytesRoom();

  try
  {
   toBytes(o, charsetName, mbr);
   mbr.setSize(mbr.getCurrentPosition());
   res=mbr.getContent();

   if (!disableZip && res!=null && res.length>autoZipSizeThreshold)
   {
    mbr.clear();
    mbr.writeShort(zippedDataModifier);
    byte zipped[]=zipBytes(res);
    mbr.writeInt(zipped.length);
    mbr.write(zipped);

    byte tres[]=mbr.getContent();

    // se č stato compresso troppo poco preferiamo conservare la versione non zippata
    if (MathExtras.areDifferentMoreThanPercent(tres.length, res.length, 15))
    {
     res=tres;
    }
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("ByteExtras.toBytes exception", tr);
  }

  return res;
 }







 public static Object fromBytes(byte bytes[], String charsetName)
 {
  Object res;
  SpeedBytesWrapper sbw=new SpeedBytesWrapper();

  try
  {
   sbw.setContent(bytes);
   res=fromBytes(sbw, charsetName);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("fromBytes exception", tr);
  }

  return res;
 }


/*

 public static void main(String args[])
 {
  Object arr[]=new Object[]{"Caio", Calendar.getInstance(), "FINE"};

  byte b[]=toBytes(arr);

  Object arr1[]=(Object[])fromBytes(b);

  System.out.println(arr1[2].toString());
 }
*/




 public static Object fromBytes(BytesRoom mbr)
 {
  return fromBytes(mbr, defaultCharsetName);
 }


 public static Object fromBytes(BytesRoom br, String charsetName)
 {
  Object res;

  try
  {
   long oldPos=br.getCurrentPosition();
   int typeIndex=br.ReadShort();

   if (typeIndex==zippedDataModifier)
   {
    // we first need to unzip the data!
    int zippedSize=br.ReadInt();
    byte zipped[]=new byte[zippedSize];
    br.Read(zipped);

    SpeedBytesWrapper sbw=new SpeedBytesWrapper();
    sbw.setContent(unZipBytes(zipped));
    br=sbw;

   } else br.setCurrentPosition(oldPos);


   //TimeCounter tc=TimeCounter.start();

   res=fromBytes(charsetName, br);

   //tc.stop();if (!StringExtras.areEqual(tc.getElapsedString(), "0ms")) System.out.println("fromBytes: "+tc.getElapsedString());

  }
  catch (Throwable tr)
  {
   throw new RuntimeException("fromBytes exception", tr);
  }

  return res;
 }




 private static Object fromBytes(String charsetName, BytesRoom mbr)
 {
  int typeIndex=mbr.ReadShort();
  return fromBytes(charsetName, mbr, typeIndex);
 }






 private static Object fromBytes(String charsetName, BytesRoom mbr, int typeIndex)
 {
  return fromBytes(charsetName, mbr, typeIndex, null);
 }



 private static Object fromBytes(String charsetName, BytesRoom br, int typeIndex, String oClassName)
 {
  Object res=null;

  switch (typeIndex)
  {
   case type_null        :break; // our Object res is already null, we have finished here!
   case type_byte        :
   case type_Byte        :res=br.ReadByte();break;
   case type_short       :
   case type_Short       :res=br.ReadShort();break;
   case type_int         :
   case type_Integer     :res=br.ReadInt();break;
   case type_long        :
   case type_Long        :res=br.ReadLong();break;
   case type_char        :
   case type_Character   :res=br.ReadChar();break;
   case type_float       :
   case type_Float       :res=br.ReadFloat();break;
   case type_double      :
   case type_Double      :res=br.ReadDouble();break;
   case type_boolean     :
   case type_Boolean     :res=(br.ReadByte()==1);break;
   case type_Class       :res=classFromBytes(charsetName, br);break;
   case type_Object      :res=objectFromBytes(charsetName, br);break;
   case type_String      :res=stringFromBytes(charsetName, br);break;
   case type_HashMap     :res=hashMapFromBytes(charsetName, br);break;
   case type_RamTable    :res=ramTableFromBytes(charsetName, br);break;
   case type_Calendar    :res=calendarFromBytes(charsetName, br);break;
   case type_array       :res=arrayFromBytes(charsetName, br);break;
   case type_binaryable  :res=binaryableFromBytes(charsetName, oClassName, br);break;
   case type_serializable:res=serializableFromBytes(br);break;
   default               :
   {
    throw new RuntimeException("ByteExtras.fromBytes is unable to work on objects of typeIndex: "+typeIndex);
   }
  }

  return res;
 }


 public static void toBytes(Object o, BytesRoom mbr)
 {
  toBytes(o, defaultCharsetName, mbr);
 }



 // @placeholder REAL ENTRY!
 public static void toBytes(Object o, String charsetName, BytesRoom mbr)
 {
  int typeIndex=getTypeIndex(o);
  mbr.WriteShort(typeIndex);
  if (typeIndex>type_null) toBytes(o, charsetName, mbr, typeIndex);
 }



 private static void toBytes(Object o, String charsetName, BytesRoom mbr, int typeIndex)
 {
  toBytes(o, charsetName, mbr, typeIndex, null);
 }


 private static void toBytes(Object o, String charsetName, BytesRoom mbr, int typeIndex, String oClassName)
 {
  switch (typeIndex)
  {
   case type_byte        :
   case type_Byte        :mbr.WriteByte(((Byte)o).byteValue());break;
   case type_short       :
   case type_Short       :mbr.WriteShort(((Short)o).shortValue());break;
   case type_int         :
   case type_Integer     :mbr.WriteInt(((Integer)o).intValue());break;
   case type_long        :
   case type_Long        :mbr.WriteLong(((Long)o).longValue());break;
   case type_char        :
   case type_Character   :mbr.WriteChar(((Character)o).charValue());break;
   case type_float       :
   case type_Float       :mbr.WriteFloat(((Float)o).floatValue());break;
   case type_double      :
   case type_Double      :mbr.WriteDouble(((Double)o).doubleValue());break;
   case type_boolean     :
   case type_Boolean     :mbr.WriteByte(((((Boolean)o).booleanValue())?1:0));break;
   case type_Class       :classToBytes((Class)o, charsetName, mbr);break;
   case type_Object      :objectToBytes(o, charsetName, mbr);break;
   case type_String      :stringToBytes((String)o, charsetName, mbr);break;
   case type_HashMap     :hashMapToBytes((HashMap)o, charsetName, mbr);break;
   case type_RamTable    :ramTableToBytes((RamTable)o, charsetName, mbr);break;
   case type_Calendar    :calendarToBytes((Calendar)o, charsetName, mbr);break;
   case type_array       :arrayToBytes(o, charsetName, mbr);break;
   case type_binaryable  :binaryableToBytes(o, oClassName, charsetName, mbr);break;
   case type_serializable:serializableToBytes(o, mbr);break;
  }
 }



 private static short getTypeIndex(Object o)
 {
  int typeIndex;

  if (o==null) typeIndex=type_null;
  else typeIndex=getTypeIndexByClass(o, o.getClass());

  return (short)typeIndex;
 }


 private static short getTypeIndexByClass(Object o, Class c)
 {
  int typeIndex=ArrayExtras.select(supportedClasses, c);

  if (typeIndex==-1)
  {
   if (isArray(c))
   {
    typeIndex=type_array;
   }
   else
   {
    //if (ClassExtras.doesImplement(c, Binaryable.class))
    if ((o!=null && (o instanceof Binaryable)) || ClassExtras.doesImplement(c, Binaryable.class))
    /*
      NOTE _A_: this complex 'if' is necessary because ClassExtras.doesImplement is very slow,
                we try to use it as few times as possible
     */
    {
     typeIndex=type_binaryable;
    }
    else
    {
     //if (ClassExtras.doesImplement(c, Serializable.class))
     if ((o!=null && (o instanceof Serializable)) || ClassExtras.doesImplement(c, Serializable.class))
     // see NOTE _A_
     {
      typeIndex=type_serializable;
     }
     else
     {
      throw new RuntimeException(
              "getTypeIndexByClass is unable to work on objects of the java type: "+c.getName());
     }
    }
   }
  }

  return (short)typeIndex;
 }


 // arrays

 private static boolean isArray(Class c)
 {
  return c.getName().startsWith("[");
 }

 private static void arrayToBytes(Object o, String charsetName, BytesRoom mbr)
 {
  try
  {
/*
   String arrayClassName=o.getClass().getName();
   Class arrayElementsClass=ClassExtras.classFromName(arrayClassName.substring(1));
   String arrayElementsClassName=arrayElementsClass.getName();
   int typeOfTheElementsIndex=getTypeIndexByClass(arrayElementsClass);

   */
   Class arrayElementsClass=o.getClass().getComponentType();
   String arrayElementsClassName=arrayElementsClass.getName();
   int typeOfTheElementsIndex=getTypeIndexByClass(null, arrayElementsClass);

   // we really need to send the whole name of the class of the elements of the array, otherwise the reading
   // would be hard like hell!
   stringToBytes(arrayElementsClassName, charsetName, mbr);

   // we also need to write once and for all the typeIndex of the elements
   mbr.WriteShort(typeOfTheElementsIndex);

   int t, len=Array.getLength(o);

   // just before writing the elements themselves, we add the length of the array
   mbr.WriteInt(len);

//   Object el;
//   String skip, skipName=null;

   for (t=0;t<len;t++)
   {
    /*
    el=Array.get(o, t);
    if (el==null) mbr.Write(0);
    else
    {
     mbr.Write(1);
     toBytes(el, charsetName, mbr, typeOfTheElementsIndex, arrayElementsClassName);
    }
    */


/*
    skip=arrayElementsClassName;
    el=Array.get(o, t);
    if (el!=null && skipName==null)
    {
     skipName=el.getClass().getName();
    }

    if (el!=null && skipName!=null)
    {
     if (StringExtras.areEqual(skipName, el.getClass().getName())) skip=skipName;
    }

    toBytes(el, charsetName, mbr, typeOfTheElementsIndex, skip);
*/

    toBytes(Array.get(o, t), charsetName, mbr, typeOfTheElementsIndex, arrayElementsClassName);
   }

  }
  catch (Throwable tr)
  {
   throw new RuntimeException("arrayToBytes exception", tr);
  }
 }




 /*
 private static void binaryableToBytes(Object o, String charsetName, BytesRoom mbr)
 {
  binaryableToBytes(o, null, charsetName, mbr);
 }
 */




 private static void binaryableToBytes(Object o, String skipOClassName, String charsetName, BytesRoom mbr)
 {
  String oClassName=o.getClass().getName();

  if (StringExtras.areEqual(oClassName, skipOClassName))
  {
   stringToBytes("!", charsetName, mbr);
  } else stringToBytes(oClassName, charsetName, mbr);

  byte bytes[]=((Binaryable)o).toBytes();
  mbr.WriteInt(bytes.length);
  mbr.Write(bytes);
 }



 public static void serializableToBytes(Object o, BytesRoom mbr)
 {
  try
  {
   ObjectOutput s = new ObjectOutputStream(mbr.toOutputStream(mbr.getCurrentPosition()));
   s.writeObject(o);
   s.flush();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }




 /*
 private static Object arrayFromBytes(String charsetName, BytesRoom mbr)
 {
  TimeCounter tc=TimeCounter.start();



  Object res=__arrayFromBytes(charsetName, mbr);


  //tc.stop();if (!StringExtras.areEqual(tc.getElapsedString(), "0ms")) System.out.println("fromBytes: "+tc.getElapsedString());
  tc.stop();System.out.println("fromBytes: "+tc.getElapsedString());


  return res;
 }
 */






 private static Object arrayFromBytes(String charsetName, BytesRoom mbr)
 {
  Object res;

  try
  {
   // let's read the whole name of the class of the array
   String arrayElementsClassName=stringFromBytes(charsetName, mbr);
   Class arrayElementsClass=ClassExtras.classFromName(arrayElementsClassName);

   // we also need to read once and for all the typeIndex of the elements
   int typeOfTheElementsIndex=mbr.ReadShort();

   // just before reading the elements themselves, we read the length of the array
   int t, len=mbr.ReadInt();

   res=Array.newInstance(arrayElementsClass, len);
   Object element;

   for (t=0;t<len;t++)
   {
    /*
    if (mbr.Read()==0)
    {
     element=null;
    }
    else
    {
     element=fromBytes(charsetName, mbr, typeOfTheElementsIndex, arrayElementsClassName);
    }
    */

    element=fromBytes(charsetName, mbr, typeOfTheElementsIndex, arrayElementsClassName);

    // here is where we are forced to explicitly distinguish between primitives and non-primitives elements!
    switch (typeOfTheElementsIndex)
    {
     case type_byte    :Array.setByte(res, t, ((Byte)element).byteValue());break;
     case type_short   :Array.setShort(res, t, ((Short)element).shortValue());break;
     case type_int     :Array.setInt(res, t, ((Integer)element).intValue());break;
     case type_long    :Array.setLong(res, t, ((Long)element).longValue());break;
     case type_char    :Array.setChar(res, t, ((Character)element).charValue());break;
     case type_float   :Array.setFloat(res, t, ((Float)element).floatValue());break;
     case type_double  :Array.setDouble(res, t, ((Double)element).doubleValue());break;
     case type_boolean :Array.setBoolean(res, t, ((Boolean)element).booleanValue());break;
     default           :Array.set(res, t, element);
    }
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("arrayToBytes exception", tr);
  }

  return res;
 }



 private static Object binaryableFromBytes(String charsetName, BytesRoom mbr)
 {
  return binaryableFromBytes(charsetName, null, mbr);
 }


 private static String lastClassName=null;
 private static Class lastClass=null;



 private static Object binaryableFromBytes(String charsetName, String oClassNAme, BytesRoom mbr)
 {
  Object res=null;
  String className=stringFromBytes(charsetName, mbr);

  if (!isNotBlank(className) ||
      StringExtras.areEqual(className, "!")) className=oClassNAme;

  //if (hasNonBlankChars(oClassNAme)) className=oClassNAme;       <------- BUG ! ! !

  int size=mbr.ReadInt();
  byte bytes[]=new byte[size];
  mbr.Read(bytes);

  try
  {
   Class c;

   if (StringExtras.areEqual(className, lastClassName)) c=lastClass;
   else
   {
    c=Class.forName(className);
    lastClass=c;
    lastClassName=className;
   }

   Binaryable b=(Binaryable)c.getConstructor().newInstance();
   b.fromBytes(bytes);
   res=b;
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(
           "fromBytes is unable to work on objects of type: "+className, tr);
  }

  return res;
 }




 public static Object serializableFromBytes(BytesRoom mbr)
 {
  Object res=null;

  try
  {
   ObjectInputStream s = new ObjectInputStream(mbr.toInputStream(mbr.getCurrentPosition()));
   res=s.readObject();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

  return res;
 }











 // Class

 private static void classToBytes(Class c, String charsetName, BytesRoom mbr)
 {
  String className=null;
  if (c!=null) className=c.getName();
  stringToBytes(className, charsetName, mbr);
 }


 private static Class classFromBytes(String charsetName, BytesRoom mbr)
 {
  Class res=null;
  String className=stringFromBytes(charsetName, mbr);

  if (isNotBlank(className))
  {
   try
   {
    res=ClassExtras.classFromName(className);
   }
   catch (Throwable tr)
   {
    throw new RuntimeException("classFromBytes exception", tr);
   }
  }

  return res;
 }


 // Object

 private static void objectToBytes(Object o, String charsetName, BytesRoom mbr)
 {
  if (o==null) mbr.WriteShort(type_null);
  else
  {
   if (o.getClass()==Object.class) mbr.WriteShort(type_Object);
   else
   {
    toBytes(o, charsetName, mbr);
   }
  }
 }

 private static Object objectFromBytes(String charsetName, BytesRoom mbr)
 {
  Object res;
  int typeIndex=mbr.ReadShort();

  switch (typeIndex)
  {
   case type_null:res=null;break;
   case type_Object:res=new Object();break;
   default:res=fromBytes(charsetName, mbr, typeIndex);
  }

  return res;
 }


 // String

 private static void stringToBytes(String str, String charsetName, BytesRoom mbr)
 {
  try
  {
   byte bytes[]=((str!=null)?str.getBytes(charsetName):null);
   int len=((bytes!=null)?bytes.length:0);

   mbr.WriteInt(len);
   if (len>0) mbr.Write(bytes);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("stringToBytes(...) exception", tr);
  }
 }


 private static String stringFromBytes(String charsetName, BytesRoom mbr)
 {
  try
  {
   String res=null;
   int len=mbr.ReadInt();

   if (len>0)
   {
    synchronized (_bu_Buffer)
    {
     byte bytes[];

     if (_bu_Buffer.length>=len) bytes=_bu_Buffer;
     else bytes=new byte[len];

     mbr.Read(bytes, 0, len);
     res=new String(bytes, 0, len, charsetName);
    }
   }

   return res;
  }
  catch (Throwable tr)
  {
   throw new RuntimeException("stringFromBytes(...) exception", tr);
  }
 }

 // HashMap

 private static void hashMapToBytes(HashMap hashMap, String charsetName, BytesRoom mbr)
 {
  Object key;
  Object value;

  for (Iterator i=hashMap.keySet().iterator();i.hasNext();)
  {
   key=i.next();
   value=hashMap.get(key);

   toBytes(key, charsetName, mbr);
   toBytes(value, charsetName, mbr);
  }

  toBytes(null, charsetName, mbr);
 }


 private static HashMap hashMapFromBytes(String charsetName, BytesRoom mbr)
 {
  HashMap res=new HashMap();
  Object key;
  Object value;

  do
  {
   key=fromBytes(charsetName, mbr);

   if (key!=null)
   {
    value=fromBytes(charsetName, mbr);
    res.put(key, value);
   }
  } while (key!=null);

  return res;
 }

 // RamTable

 private static void ramTableToBytes(RamTable ramTable, String charsetName, BytesRoom mbr)
 {
  int columns=ramTable.getColsCount();
  int rows=ramTable.getRowsCount();

  mbr.WriteInt(columns);
  mbr.WriteInt(rows);

  if (columns>0 && rows>0)
  {
   Object cellContent;
   int c, r;

   for (r=0;r<rows;r++)
   {
    for (c=0;c<columns;c++)
    {
     cellContent=ramTable.getObject(c, r);
     if (cellContent!=null)
     {
      mbr.WriteInt(c);
      mbr.WriteInt(r);
      toBytes(cellContent, charsetName, mbr);
     }
    }
   }

   mbr.WriteInt(Integer.MAX_VALUE);
  }
 }


 private static RamTable ramTableFromBytes(String charsetName, BytesRoom mbr)
 {
  RamTable res=new RamTable();

  int columns=mbr.ReadInt();
  int rows=mbr.ReadInt();

  res.forceColsAndRows(columns, rows);

  if (columns>0 && rows>0)
  {
   Object cellContent;
   int c, r;

   do
   {
    c=mbr.ReadInt();

    if (c!=Integer.MAX_VALUE)
    {
     r=mbr.ReadInt();
     cellContent=fromBytes(charsetName, mbr);
     res.setObject(c, r, cellContent);
    }
   } while (c!=Integer.MAX_VALUE);
  }

  return res;
 }



 // Calendar

 private static void calendarToBytes(Calendar calendar, String charsetName, BytesRoom mbr)
 {
  try
  {
   mbr.writeLong(calendar.getTimeInMillis());
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }

 }




 private static Calendar calendarFromBytes(String charsetName, BytesRoom mbr)
 {
  Calendar res=Calendar.getInstance();
  res.setTimeInMillis(mbr.ReadLong());
  return res;
 }





 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .



 private static String getPassword(char password[])
 {
  if (password==null || password.length==0) password=new char[]{'5', 's', 'p'};
  String res=new String(password);
  if (res.length()<5) res+="default_compleasy_password_addings";

  return res;
 }


 public static byte[] decriptPasswordProtectedBytes(byte data[], char password[]) throws Throwable
 {
  byte res[];

  try
  {
   res=_decriptPasswordProtectedBytes(data, password);
  }
  catch (Throwable tr)
  {
   throw new IOException("Invalid pawword!");
  }

  return res;
 }


 private static byte[] _decriptPasswordProtectedBytes(byte data[], char password[]) throws Throwable
 {
  String pwd=getPassword(password);
  String md5=StringExtras.replace(StringExtras.getMD5(pwd).substring(0, 22), "+", new String(new char[]{4}));
  String r_md5=StringExtras.replace(StringExtras.getMD5(StringExtras.reverse(pwd)).substring(0, 22), "+", new String(new char[]{5}));
  String fsb=StringExtras.reverse(new String(data, defaultCharsetName));
  StringBuilder sb=new StringBuilder();
  StringBuilder sbP=new StringBuilder();

  int rc=0, c=0, skip=md5.length(), t, len=fsb.length();

  for (t=0;t<len;t++)
  {
   if (skip==0 && c==0)
   {
    if (rc==0)
    {
     if (!StringExtras.areEqual(md5, sbP.toString())){throw new Throwable();}
     rc=1;
    }
    else
    {
     if (!StringExtras.areEqual(r_md5, sbP.toString())){throw new Throwable();}
     rc=0;
    }

    sbP=new StringBuilder();
   }

   if (skip>0)
   {
    skip--;
    sbP.append(fsb.charAt(t));
   }
   else
   {
    sb.append(fsb.charAt(t));
    c++;
    if (c==md5.length()*3)
    {
     skip=md5.length();
     c=0;
    }
   }
  }

  String b64=sb.toString();

  return Base64.getDecoder().decode(b64);
 }


 public static byte[] encriptPasswordProtectingBytes(byte data[], char password[]) throws Throwable
 {
  String pwd=getPassword(password);
  String md5=StringExtras.replace(StringExtras.getMD5(pwd).substring(0, 22), "+", new String(new char[]{4}));
  String r_md5=StringExtras.replace(StringExtras.getMD5(StringExtras.reverse(pwd)).substring(0, 22), "+", new String(new char[]{5}));
  String b64=Base64.getEncoder().encodeToString(data);
//  String b64=Base64.encodeBytes(data, Base64.DONT_BREAK_LINES | Base64.GZIP);
  StringBuilder sb=new StringBuilder();
  sb.append(md5);

  int rc=1, c=0, t, len=b64.length();

  for (t=0;t<len;t++)
  {
   sb.append(b64.charAt(t));
   c++;
   if (c==md5.length()*3)
   {
    if (rc==0)
    {
     sb.append(md5);
     rc=1;
    }
    else
    {
     sb.append(r_md5);
     rc=0;
    }

    c=0;
   }
  }

  return StringExtras.reverse(sb.toString()).getBytes(defaultCharsetName);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .




 private static String speedHexBytes[]=null;
 static
 {
  speedHexBytes=new String[256];
  for (int t=0;t<256;t++) speedHexBytes[t]=StringExtras.grantLength(Integer.toHexString(t), 2, '0', false);
 }



 public static String getHexStringOfByte(byte b)
 {
  int i=b;
  return speedHexBytes[(i & 0xFF)];
 }

 public static byte getByteFromHexString(String hexBytes)
 {
  int res=StringExtras.select(speedHexBytes, hexBytes);
  if (res<0) throw new RuntimeException("Invalid string '"+hexBytes+"' for getByteFromHexString");
  return (byte)res;
 }








 // bitwise operations on bytes




 public static int byteAND(int b1, int b2)
 {
  return bitwiseOperation(0, b1, b2);
 }

 public static int byteOR(int b1, int b2)
 {
  return bitwiseOperation(1, b1, b2);
 }

 public static int byteXOR(int b1, int b2)
 {
  return bitwiseOperation(2, b1, b2);
 }

 public static int byteNOT(int b1)
 {
  return bitwiseOperation(3, b1, 0);
 }


 public static int bitwiseOperation(int op, int b1, int b2)
 {
  int res=0;

  switch (op)
  {
   // AND
   case 0:res=b1 & b2;break;
   // OR
   case 1:res=b1 | b2;break;
   // XOR
   case 2:res=b1 ^ b2;break;
   // NOT
   case 3:res=~res;break;
  }

  return res;
 }


 // END - bitwise operations on bytes





 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // converts varius primitive type to/from their binary rappresentation



 public static int javaByteToUnsignedByte(byte b)
 {
  int res=b;
  return (res & 0xFF); // This is how you "cast" a byte that's intended to be unsigned.
 }

 public static byte unsignedByteToJavaByte(int unsignedByte)
 {
  return (byte)((short)unsignedByte & 0xFF);
 }



 public static byte[] doubleToBytes(double v)
 {
  return doubleToBytes(v, new byte[8]);
 }


 public static byte[] doubleToBytes(double dv, byte bytes[])
 {
  long v=Double.doubleToLongBits(dv);
  bytes[0]=(byte)((int)(v >>> 56) & 0xFF);
  bytes[1]=(byte)((int)(v >>> 48) & 0xFF);
  bytes[2]=(byte)((int)(v >>> 40) & 0xFF);
  bytes[3]=(byte)((int)(v >>> 32) & 0xFF);
  bytes[4]=(byte)((int)(v >>> 24) & 0xFF);
  bytes[5]=(byte)((int)(v >>> 16) & 0xFF);
  bytes[6]=(byte)((int)(v >>>  8) & 0xFF);
  bytes[7]=(byte)((int)(v >>>  0) & 0xFF);
  return bytes;
 }


 public static double bytesToDouble(byte b[])
 {
  int ch1 = (((int)b[0]) & 0xFF);
  int ch2 = (((int)b[1]) & 0xFF);
  int ch3 = (((int)b[2]) & 0xFF);
  int ch4 = (((int)b[3]) & 0xFF);
  int i1=((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  ch1 = (((int)b[4]) & 0xFF);
  ch2 = (((int)b[5]) & 0xFF);
  ch3 = (((int)b[6]) & 0xFF);
  ch4 = (((int)b[7]) & 0xFF);
  int i2=((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  long l=((long)(i1) << 32) + (i2 & 0xFFFFFFFFL);
  double res=Double.longBitsToDouble(l);
  return res;
 }


 /*
 public static void main(String args[])
 {
  double v=2.340;
  System.out.println((Double)bytesToNumber(doubleToBytes(v), Double.class));
  System.out.println(bytesToDouble(doubleToBytes(v)));
 }
 */








 public static byte[] intToBytes(int v)
 {
  return intToBytes(v, new byte[4]);
 }



 public static byte[] intToBytes(int v, byte b[])
 {
  b[0]=(byte)((v >>> 24) & 0xFF);
  b[1]=(byte)((v >>> 16) & 0xFF);
  b[2]=(byte)((v >>>  8) & 0xFF);
  b[3]=(byte)((v >>>  0) & 0xFF);
  return b;
 }



 public static int bytesToInt(byte b[])
 {
  int ch1 = b[0];
  int ch2 = b[1];
  int ch3 = b[2];
  int ch4 = b[3];
  return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
 }


 public static short bytesToShort(byte b[])
 {
  int ch1 = b[0];
  int ch2 = b[1];
  return (short)((ch1 << 8) + (ch2 << 0));
 }




}

