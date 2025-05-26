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

import java.io.RandomAccessFile;
import java.nio.*;
import java.nio.channels.*;

import static me.as.lib.core.lang.ByteExtras.javaByteToUnsignedByte;
import static me.as.lib.core.system.FileSystemExtras.exists;
import static me.as.lib.core.lang.StringExtras.trim;


/**
 *
 *  A T T E N Z I O N E:
 *
 *     Per ottenere le super-performance dei memory mapped file conviene usare la modalità memory
 *     mapped solo quando si intende leggere o scrivere molti bytes in un solo boccone (più megabytes
 *     è, meglio è) e soprattutto quando lo spazio nel file già c'è, ossia non si supera la dimensione
 *     attuale del file in uso. Oppure si usi prima setSize(...) alla dimensione massima e poi si scrive
 *     con la modalità memory mapped. In tutti gli altri casi (letturine piccole, scritturine piccole
 *     oltre il bordo della dimensione attuale del file) con i memory mapped si ha un *decremento* delle
 *     prestazioni, invece di un incremento.
 *     Se invece si fanno letturone grosse assai in un solo boccone col memory mapped si hanno almeno il
 *     doppio delle performance (tempi dimezzati di lettura). Questo, almeno, è quanto ho benchmarkato
 *     sotto Windows XP.
 *
 *
 *  A T T E N Z I O N E   2:
 *
 *     Questa classe non ho capito se davvero funziona bene oppure no. Mi sembra che ci siano casini in
 *     alcuni casi. In particolare è certo che se la si usa in modalità memory mapped con file che vengono
 *     creati e disctrutti (o la loro dimensione viene variata) di continuo succedono una quantità di casini
 *     che non finisce mai!
 *
 *
 *
 */


public class FileBytesRoom extends BytesRoomHandler
{
 public static final String EFM_READ_ONLY  = "r";
 public static final String EFM_READ_WRITE = "rw";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private String fileName;
 private boolean readOnly;
 private boolean memoryMapped;
 private byte oneByteIOBuffer[]=new byte[]{0};
 private RandomAccessFile raf=null;

 private MappedByteBuffer fileMap=null;
 private int fileMapZeroIndex;
 private long mappedFileSize;

 private String lastOpenMode;



 public FileBytesRoom(String fileName)
 {
  this(fileName, true);
 }

 public FileBytesRoom(String fileName, boolean memoryMapped)
 {
  this.fileName=fileName;
  this.memoryMapped=memoryMapped;
 }


 public FileBytesRoom(String fileName, boolean memoryMapped, String openMode)
 {
  this(fileName, memoryMapped);
  open(openMode);
 }


 public boolean isMemoryMapped()
 {
  return memoryMapped;
 }



 public String getFileName()
 {
  return fileName;
 }


 public boolean open(String mode)
 {
  return open(lastOpenMode=mode, memoryMapped, 0, 0);
 }


 private synchronized boolean open(String mode, boolean memoryMapped, int newFileMapZeroIndex, int newFileMapZize)
 {
  boolean res=(status==S_CLOSED);

  if (res)
  {
   try
   {
    boolean readWrite;
    String tm=trim(mode).toLowerCase();

    if (tm.indexOf('w')>=0)
    {
     readWrite=true;
    }
    else
    {
     if (tm.indexOf('r')>=0)
     {
      readWrite=false;
     }
     else
     {
      throw new me.as.lib.core.io.IOException("Unknown open mode string: '"+mode+"'");
     }
    }

    if (readWrite)
    {
     readOnly=false;
     raf=new RandomAccessFile(fileName, tm);
    }
    else
    {
     readOnly=true;
     if (!exists(fileName))
     {
      throw new me.as.lib.core.io.IOException("The file: '"+fileName+
                                              "' does not exist. Cannot create it with open mode string: '"+mode+"'");
     }
     else
     {
      raf=new RandomAccessFile(fileName, tm);
     }
    }

    if (memoryMapped)
    {
     FileChannel fc=raf.getChannel();
     mappedFileSize=fc.size();
     fileMapZeroIndex=newFileMapZeroIndex;
     long okSize=newFileMapZize;
     boolean notOk=true;
     int noyOkCount=0;
//     if (okSize==0 || okSize>mappedFileSize) okSize=mappedFileSize/10;
     if (okSize==0 || okSize>mappedFileSize) okSize=mappedFileSize;

     do
     {
      try
      {
       fileMap=null; // to free the previous one so that if we do a System.gc(); it will really free the previous fileMap!
       fileMap=fc.map(readWrite?FileChannel.MapMode.READ_WRITE:FileChannel.MapMode.READ_ONLY, fileMapZeroIndex, okSize);
       notOk=false;
      }
      catch (Throwable tr)
      {
//       System.gc();
       okSize-=okSize/10;
//       System.out.println("okSize: "+okSize);
       noyOkCount++;

       if (noyOkCount>10)
       {
        fc.close();
        raf.close();
        raf=null;
        throw new me.as.lib.core.io.IOException(tr);
       }
      }

//      System.out.println("okSize: "+okSize+"  mappedFileSize: "+mappedFileSize);

     } while (notOk);

     fc.close();
     raf.close();
     raf=null;
    }
   }
   catch (Throwable tr)
   {
    res=false;
//    tr.printStackTrace();
   }
  }

  if (res)
  {
   status=S_OPENED;
   position=0;
  }

  this.memoryMapped=memoryMapped;

  return res;
 }


 public synchronized boolean close()
 {
  boolean res=(status==S_OPENED);

  if (res)
  {
   try
   {
    if (memoryMapped)
    {
     if (!readOnly) fileMap.force();
     fileMap=null;
    }
    else
    {
     raf.close();
     raf=null;
    }
   }
   catch (Throwable tr)
   {
    tr.printStackTrace();
    res=false;
   }
  }

  if (res) status=S_CLOSED;

  return res;
 }



 public synchronized long getCurrentPosition()
 {
  checkCanRead();
  return super.getCurrentPosition();
 }


 public synchronized boolean setCurrentPosition(long newPosition)
 {
  checkCanRead();
  return super.setCurrentPosition(newPosition);
 }



 public synchronized long getSize()
 {
  long res;
  checkCanRead();

  if (memoryMapped)
  {
   res=mappedFileSize;
  }
  else
  {
   try
   {
    res=raf.length();
   }
   catch (Throwable tr)
   {
    tr.printStackTrace();
    res=-1;
   }
  }

  return res;
 }




 private boolean setRafSize(long newSize)
 {
  boolean res;

  try
  {
   raf.setLength(newSize);
   res=true;
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
   res=false;
  }

  return res;
 }





 // similar to setContent(byte bytes[]) but the bytes are not copied but used directly.
 // This will work only for in memory implementation, all the others will revert to
 // setContent(byte bytes[]).
 // This is useful to avoid compying and recopying of buffers
 public void mountContent(byte bytes[]) throws me.as.lib.core.io.IOException
 {
  setContent(bytes);
 }

 // similar to getContent() but the bytes returned are not copied but are the real internal buffer.
 // after this method has executed currentPosition will reset to 0 and size will be 0
 // This will work only for in memory implementation, all the others will revert to
 // getContent() with the addition that internal data is deleted and currentPosition will reset to 0 and size will be 0
 // This is useful to avoid compying and recopying of buffers
 public byte[] unmountContent() throws me.as.lib.core.io.IOException
 {
  byte res[]=getContent();
  setSize(0);
  return res;
 }







 public synchronized boolean setSize(long newSize)
 {
  boolean res=false;
  checkCanWrite();

  if (memoryMapped)
  {
   if (newSize!=getSize())
   {
    int capacity=fileMap.capacity();
    long oldPosition=position;
    close();
    open(lastOpenMode, false, fileMapZeroIndex, capacity);
    res=setRafSize(newSize);
    close();
    open(lastOpenMode, true, fileMapZeroIndex, capacity);
    position=oldPosition;
   }
  } else res=setRafSize(newSize);

  if (position>newSize) position=newSize;

  return res;
 }


 public synchronized boolean flush()
 {
  checkCanWrite();

  if (memoryMapped)
  {
   fileMap.force();
  }
  else
  {
   try
   {
    raf.getChannel().force(true);
   }
   catch (Throwable tr)
   {
    throw new me.as.lib.core.io.IOException(tr);
   }
  }

  return true;
 }






 public int read() throws me.as.lib.core.io.IOException
 {
  int res;

  synchronized (oneByteIOBuffer)
  {
   read(oneByteIOBuffer, 0, 1);
   res=javaByteToUnsignedByte(oneByteIOBuffer[0]);
  }

  return res;
 }


 public void write(int b) throws me.as.lib.core.io.IOException
 {
  synchronized (oneByteIOBuffer)
  {
   oneByteIOBuffer[0]=(byte)b;
   write(oneByteIOBuffer, 0, 1);
  }
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private void checkCanWrite()
 {
  if (status==S_CLOSED)
   throw new me.as.lib.core.io.IOException("Cannot write to file '"+fileName+"', it is closed, first open it");

  if (readOnly)
   throw new me.as.lib.core.io.IOException("Cannot write to file '"+fileName+"', it was opened in read only mode");
 }

 private void checkCanRead()
 {
  if (status==S_CLOSED)
   throw new me.as.lib.core.io.IOException("Cannot read from file '"+fileName+"', it is closed, first open it");
 }








 public synchronized int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   int res=-1;
   checkCanRead();

   if (memoryMapped)
   {
    grantMapRegionAndPosition(len);
 //   if (!fileMap.isLoaded()) fileMap.load();
    fileMap.get(b, off, res=len);
   }
   else
   {
    raf.seek(position);
    res=raf.read(b, off, len);
   }

   position+=len;
   return res;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }




 protected synchronized void writeBytes(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   checkCanWrite();

   if (memoryMapped)
   {
    if (position+len>=mappedFileSize) setSize(position+len);
    grantMapRegionAndPosition(len);
    fileMap.put(b, off, len);
   }
   else
   {
    raf.seek(position);
    raf.write(b, off, len);
   }

   position+=len;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }




 private void grantMapRegionAndPosition(int howManyBytesAhead) throws me.as.lib.core.io.IOException
 {
  if (!isInsideCurrentMapRegion(position) || !isInsideCurrentMapRegion(position+howManyBytesAhead))
  {
   int capacity=fileMap.capacity();

   if (howManyBytesAhead>capacity)
   {
    throw new me.as.lib.core.io.IOException("The buffer required for the operation is too big, there is not enough memory for it!");
   }
   else
   {
    int cap_5=capacity/5;
    int newFileMapZeroIndex=(int)(position-cap_5);
    if (newFileMapZeroIndex+capacity>mappedFileSize) newFileMapZeroIndex=(int)(mappedFileSize-capacity);
    if (newFileMapZeroIndex<0) newFileMapZeroIndex=0;

    long oldPosition=position;
    close();
    open(lastOpenMode, true, newFileMapZeroIndex, capacity);
    position=oldPosition;
//    System.out.println("_____.._____");
   }
  }

  fileMap.position((int)position-fileMapZeroIndex);
 }


 private boolean isInsideCurrentMapRegion(long index)
 {
  return (index>=fileMapZeroIndex && index<=fileMapZeroIndex+fileMap.capacity());
 }


}
