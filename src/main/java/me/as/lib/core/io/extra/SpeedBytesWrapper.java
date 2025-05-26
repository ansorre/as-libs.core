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
import me.as.lib.core.lang.StringExtras;

import java.io.*;

import static me.as.lib.core.lang.ByteExtras.javaByteToUnsignedByte;


public class SpeedBytesWrapper extends BytesRoomHandler
{
 private byte content[];
 private int offset=0;





 // similar to setContent(byte bytes[]) but the bytes are not copied but used directly.
 // This will work only for in memory implementation, all the others will revert to
 // setContent(byte bytes[]).
 // This is useful to avoid compying and recopying of buffers
 public void mountContent(byte bytes[]) throws me.as.lib.core.io.IOException
 {
  offset=0;
  setContent(bytes);
 }


 // similar to getContent() but the bytes returned are not copied but are the real internal buffer.
 // after this method has executed currentPosition will reset to 0 and size will be 0
 // This will work only for in memory implementation, all the others will revert to
 // getContent() with the addition that internal data is deleted and currentPosition will reset to 0 and size will be 0
 // This is useful to avoid compying and recopying of buffers
 public byte[] unmountContent() throws me.as.lib.core.io.IOException
 {
  byte res[]=content;
  content=null;
  position=0;
  offset=0;
  return res;
 }



 public synchronized void setContent(byte bytes[], int offset)
 {
  this.offset=offset;
  content=bytes;
  position=0;
 }


 public synchronized void setContent(byte bytes[]) throws me.as.lib.core.io.IOException
 {
  content=bytes;
  position=0;
  offset=0;
 }

 public long getSize()
 {
  return ArrayExtras.length(content)-offset;
 }


 public boolean setSize(long newSize)
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 public boolean flush()
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 public boolean open(String mode) throws me.as.lib.core.io.IOException
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 public boolean close()
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 public int read() throws me.as.lib.core.io.IOException
 {
  int res=-1;
  int pos=(int)position+offset;
  if (pos<content.length)
  {
   res=javaByteToUnsignedByte(content[pos]);
   position++;
  }

  return res;


/*
  int res=ByteExtras.javaByteToUnsignedByte(content[(int)position+offset]);
  position++;
  return res;
*/
 }


 public int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   System.arraycopy(content, (int)position+offset, b, off, len);
   position+=len;
   return len;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }


 public void write(int b) throws me.as.lib.core.io.IOException
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 protected void writeBytes(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  throw new me.as.lib.core.io.IOException("This method is not supported!");
 }


 // this is not needed, it's just to speed it
 public String readLine() throws me.as.lib.core.io.IOException
 {
  try
  {
   String res="";
   char ch;
   int start=(int)(position+offset);
   int end=start-1;
   boolean goon=(start<content.length);

   while (goon)
   {
    ++end;
    ch=(char)(content[end] & 0xFF);
    position++;

    goon=(position<content.length && ch!='\n');

    if (!goon)
    {
     while (end>start)
     {
      ch=(char)(content[end-1] & 0xFF);
      if (ch=='\r') end--;
      else
       break;
     }

     if (end>start)
     {
      res=new String(content, start, end-start);
     }
    }
   }

   if (position>=content.length && StringExtras.length(res)==0) throw new IOException("EOF");

   return res;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }



}
