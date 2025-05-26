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


import me.as.lib.core.lang.ArrayExtras;

import static me.as.lib.core.lang.ByteExtras.javaByteToUnsignedByte;


public class MemBytesRoom extends BytesRoomHandler
{
 protected int dynIcrementedSum=0;
 protected int dynIcrementedCount=0;

 protected int defIncSize=256;
 protected static final int defaultInitSize=0;

 protected long size;
 protected byte bytes[]=null;


 public MemBytesRoom()
 {

 }


 public MemBytesRoom(long initialSize)
 {
  init(initialSize);
 }


 private void init(long initialSize)
 {
  if (initialSize>0) defIncSize=(int)(initialSize/2);
  setSize(initialSize);
 }






 // this method does not change the getCurrentPosition() result
 /*
 public synchronized byte[] getContent() throws me.as.lib.core.io.IOException
 {
  int size=(int)getSize();
  byte res[]=new byte[size];

  if (size>0)
  {
   long op=getCurrentPosition();
   setCurrentPosition(0);
   readFully(res);
   setCurrentPosition(op);
  }

  return res;
 }
 */


 public synchronized byte[] getContent() throws me.as.lib.core.io.IOException
 {
  byte res[]=new byte[(int)size];
  if (size>0) System.arraycopy(bytes, 0, res, 0, (int)size);
  return res;
 }


 // similar to setContent(byte bytes[]) but the bytes are not copied but used directly.
 // This will work only for in memory implementation, all the others will revert to
 // setContent(byte bytes[]).
 // This is useful to avoid compying and recopying of buffers
 public void mountContent(byte bytes[]) throws me.as.lib.core.io.IOException
 {
  this.bytes=bytes;
  size=ArrayExtras.length(bytes);
  position=0;
 }


 // similar to getContent() but the bytes returned are not copied but are the real internal buffer.
 // after this method has executed currentPosition will reset to 0 and size will be 0
 // This will work only for in memory implementation, all the others will revert to
 // getContent() with the addition that internal data is deleted and currentPosition will reset to 0 and size will be 0
 // This is useful to avoid compying and recopying of buffers
 public byte[] unmountContent() throws me.as.lib.core.io.IOException
 {
  byte res[]=bytes;

  size=0;
  bytes=null;
  position=0;

  return res;
 }


/*
 // really unusefull because almost never the interna� buffers equals the number of bytes written so far!
 public byte[] unmountContent(boolean rightSize)
 {
  int realSize=(int)size;
  byte res[];

  if (rightSize && bytes!=null && realSize!=bytes.length) res=getContentQuick();
  else res=bytes;

  size=0;
  bytes=null;
  position=0;

  return res;
 }
*/




 public int getStatus()
 {
  return S_OPENED;
 }


 public synchronized long getSize()
 {
  return size;
 }


// private static int deb_maxResizes=0;

 public synchronized boolean setSize(long newSize)
 {
  boolean res;

  if (newSize!=size)
  {
   if (newSize<size)
   {
    size=newSize;
    if (position>size) position=size;
    res=true;
   }
   else
   {
    int blen=((bytes!=null) ? bytes.length:0);

    if (newSize>blen)
    {
     // gestione pi� dinamica dell'incremento,
     // quando si incrementa molto e spesso incrementiamo
     // l'incremento di default.
     // Ci� velocizza moltissimo le espansioni, ovviamente a
     // scapito del maggior consumo di memoria
     if (dynIcrementedCount>1)
     {
//      int newDefIncSize=(int)(((float)dynIcrementedSum)/(((float)dynIcrementedCount)/2.0));
      int newDefIncSize=(int)(((float)dynIcrementedSum)/(((float)dynIcrementedCount)/5.0));
      if (newDefIncSize>defIncSize) defIncSize=newDefIncSize;

/*
      if (dynIcrementedCount>deb_maxResizes)
      {
       deb_maxResizes=dynIcrementedCount;
       System.out.println("deb_maxResizes: "+deb_maxResizes);
      }
*/
     }


//     long newOkSize=blen+defIncSize;
     long newOkSize=newSize+defIncSize;
//     if (newOkSize<=newSize) newOkSize=newSize+5;

     byte newbytes[]=new byte[(int)newOkSize];

     if (bytes!=null)
     {
      System.arraycopy(bytes, 0, newbytes, 0, (int)size);
      dynIcrementedSum+=newbytes.length-bytes.length;
     } else dynIcrementedSum+=newbytes.length;

     dynIcrementedCount++;


     bytes=newbytes;
     size=newSize;
     res=true;
    }
    else
    {
     size=newSize;
     res=true;
    }
   }
  } else res=true;

  return res;
 }

 public boolean flush()
 {
  return true;
 }

 public boolean open(String mode) throws me.as.lib.core.io.IOException
 {
  return true;
 }

 public boolean close()
 {
  return true;
 }

 // interface DataInput

 public synchronized int read() throws me.as.lib.core.io.IOException
 {
  int res=-1;

  if (position<size)
  {
//res=bytes[(int)position];
//   res=(bytes[(int)position] & 0xff);
   res=javaByteToUnsignedByte(bytes[(int)position]);
   position++;
  }

  return res;
 }

 public synchronized int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  int res=-1;

  if (position<size)
  {
   if (position+len>size) res=(int)(size-position);
   else res=len;

   System.arraycopy(bytes, (int)position, b, off, res);
   position+=res;
  }

  return res;
 }

 // END - interface DataInput


 // interface DataOutput

 public synchronized void write(int b) throws me.as.lib.core.io.IOException
 {
  if (position>=size) setSize(size+1);
  bytes[(int)position]=(byte)b;
  position++;
 }


 protected synchronized void writeBytes(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  if (b!=null && len>0)
  {
   long ppl=position+len;

   if (ppl>size) setSize(ppl);

   System.arraycopy(b, off, bytes, (int)position, len);

   position=ppl;
  }
 }

 // END - interface DataOutput



}

