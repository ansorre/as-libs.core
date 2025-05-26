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


import static me.as.lib.core.system.FileSystemExtras.exists;
import static me.as.lib.core.system.FileSystemExtras.fileLength;
import static me.as.lib.core.system.FileSystemExtras.loadFromFile;
import static me.as.lib.core.system.FileSystemExtras.saveInFile;
import static me.as.lib.core.lang.StringExtras.trim;


/**
 * VERY IMPORTANT WARNING !!!!!!!
 *
 *
 *
 *      THIS IS A FAKE!
 *      IT WORKS, BUT IN REALITY IT IS JUST A MemBytesRomm, there is no caching of portions of the file
 *      it just retains the whole file in memory (in the MemBytesRomm) so this is totally unuseful,
 *      I just made it to do some testing
 *
 *
 */
public class BufferedFileBytesRoom extends BytesRoomHandler
{
 public static final String EFM_READ_ONLY  = "r";
 public static final String EFM_READ_WRITE = "rw";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private String fileName;
 private boolean readOnly;
 private MemBytesRoom mbr=new MemBytesRoom();



 public BufferedFileBytesRoom(String fileName)
 {
  this.fileName=fileName;
  System.err.println("\n\n-------------> Please read BufferedFileBytesRoom WARNING in the source code...\n\n");
 }



 public BufferedFileBytesRoom(String fileName, String openMode)
 {
  this(fileName);
  open(openMode);
 }


 public String getFileName()
 {
  return fileName;
 }


 public boolean open(String mode)
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


/*
     RandomAccessFileX traf=new RandomAccessFileX(fileName, tm);
     RandomAccessFileContent rafc=new RandomAccessFileContent(traf);
     IOController ioc=new IOController(1024 * 16, rafc);
     raf=new BufferedRandomAccessIO(ioc);
*/
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


/*

      RandomAccessFile traf=new RandomAccessFile(fileName, tm);
      RandomAccessFileContent rafc=new RandomAccessFileContent(traf);
      IOController ioc=new IOController(1024 * 16, rafc);
      raf=new BufferedRandomAccessIO(ioc);
*/
     }
    }


    if (exists(fileName) && fileLength(fileName)>0)
    {
     mbr.setContent(loadFromFile(fileName));
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

  return res;
 }


 public synchronized boolean close()
 {
  boolean res=(status==S_OPENED);

  if (res)
  {
   // @todo
  }

  if (res) status=S_CLOSED;

  return res;
 }



 public long getCurrentPosition()
 {
  checkCanRead();
  return mbr.getCurrentPosition();
 }


 public boolean setCurrentPosition(long newPosition)
 {
  checkCanRead();
  return mbr.setCurrentPosition(newPosition);
 }



 public synchronized long getSize()
 {
  return mbr.getSize();
 }




 private boolean setRafSize(long newSize)
 {
  return mbr.setSize(newSize);
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
  boolean res;
  checkCanWrite();
  res=setRafSize(newSize);
  if (position>newSize) position=newSize;

  return res;
 }


 public synchronized boolean flush()
 {
  checkCanWrite();

  try
  {
   saveInFile(fileName, mbr.getContent());
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }

  return true;
 }






 public int read() throws me.as.lib.core.io.IOException
 {
  try
  {
   return mbr.read();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }


 public void write(int b) throws me.as.lib.core.io.IOException
 {
  try
  {
   mbr.write(b);
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }



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








 public int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   return mbr.read(b, off, len);
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }




 protected void writeBytes(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   checkCanWrite();
   mbr.writeBytes(b, off, len);
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }





}
