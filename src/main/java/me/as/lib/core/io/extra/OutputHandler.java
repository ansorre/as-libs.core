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


import me.as.lib.core.event.BasicListenersManager;
import me.as.lib.core.event.Firer;
import me.as.lib.core.io.MinimalWriter;
import me.as.lib.core.io.WriterEvent;
import me.as.lib.core.io.WriterListener;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.*;

import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.hasChars;


/*
// speed use of this class:

 protected OutputHandler outH=null;

 public boolean isOn(){return outH.isOn();}
 public boolean write(int b){return outH.write(b);}
 public boolean write(byte b[]){return outH.write(b);}
 public boolean write(byte b[], int off, int len){return outH.write(b, off, len);}
 public boolean write(String str){return outH.write(str);}
 public boolean writeln(){return outH.writeln();}
 public boolean writeln(String str){return outH.writeln(str);}
 public boolean writeln(String str[]){return outH.writeln(str);}
 public boolean flush(){return outH.flush();}
 public boolean close(){return outH.close();}
 public void addWriterListener(WriterListener l){outH.addWriterListener(l);}
 public void removeWriterListener(WriterListener l){outH.removeWriterListener(l);}
 public int getWrittenBytesCount(){return outH.getWrittenBytesCount();}
 // interface MinimalWriter
 public boolean Write(byte b[]){return outH.Write(b);}
 public boolean Write(int b){return outH.Write(b);}
 public boolean Write(byte b[], int off, int len){return outH.Write(b, off, len);}
 public boolean Write(char c[]){return outH.Write(c);}
 public boolean Write(char c[], int off, int len){return outH.Write(c, off, len);}
 public boolean Write(String str){return outH.Write(str);}
 public boolean Writeln(){return outH.Writeln();}
 public boolean Writeln(String str){return outH.Writeln(str);}
 public boolean Writeln(String str[]){return outH.Writeln(str);}
 public void WriteBoolean(boolean v){outH.WriteBoolean(v);}
 public void WriteByte(int v){outH.WriteByte(v);}
 public void WriteShort(int v){outH.WriteShort(v);}
 public void WriteChar(int v){outH.WriteChar(v);}
 public void WriteInt(int v){outH.WriteInt(v);}
 public void WriteLong(long v){outH.WriteLong(v);}
 public void WriteFloat(float v){outH.WriteFloat(v);}
 public void WriteDouble(double v){outH.WriteDouble(v);}
 // END - interface MinimalWriter

*/


public class OutputHandler implements me.as.lib.core.io.Writer
{
 protected OutputStream bos;
 protected boolean ison;

 public OutputHandler()
 {

 }

 public OutputHandler(OutputStream os)
 {
  this(os, 512);
 }

 public OutputHandler(OutputStream os, int bufferSize)
 {
  init(os, bufferSize);
 }

 public void init(OutputStream os, int bufferSize)
 {
  if (bufferSize>0) bos=new BufferedOutputStream(os, bufferSize);
  else bos=os;
  ison=true;
 }

 public boolean isOn()
 {
  return ison;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 private int writtenBytesCount=0;
 private synchronized void incWrittenBytesCount(int bc)
 {
  if (bc>0)
  {
   writtenBytesCount+=bc;
   if (writerListeners.areThereListeners())
   {
    try
    {
     fireWriterEvent(new WriterEvent(bos, writtenBytesCount, WriterEvent.WRITTEN_BYTES_COUNT_CHANGED));
    } catch (Throwable tr) {tr.printStackTrace();}
   }
  }
 }



 // write the byte b
 public boolean write(int b)
 {
  boolean res;

  try
  {
   bos.write(b);
   incWrittenBytesCount(1);
   res=true;
  }
  catch (Throwable tr)
  {
   ison=false;
   res=false;
  }

  return res;
 }


 // write all the bytes of b
 public boolean write(byte b[])
 {
  return write(b, 0, b.length);
 }

 // write len bytes of b starting from off
 public boolean write(byte b[], int off, int len)
 {
  boolean res;

  try
  {
   if (writerListeners.areThereListeners())
   {
    int block=(len/100);
    int wbytes=0;
    int thisBlock;

    if (block<1000) block=1000;

    while (wbytes<len && isOn())
    {
     thisBlock=block;
     if (thisBlock>len-wbytes) thisBlock=len-wbytes;
     bos.write(b, off+wbytes, thisBlock);
     incWrittenBytesCount(thisBlock);
     wbytes+=thisBlock;
    }

    res=true;
   }
   else
   {
    bos.write(b, off, len);
    incWrittenBytesCount(len);
    res=true;
   }
  }
  catch (Throwable tr)
  {
   ison=false;
   res=false;
  }

  return res;
 }

 public boolean write(String str)
 {
  boolean res=true;

  if (hasChars(str))
  {
   res=write(str.getBytes());
  }

  return res;
 }

 public boolean writeln()
 {
  boolean res=write("\r\n");
  if (res) res=flush();
  return res;
 }


 public boolean writeln(String str)
 {
  boolean res=write(str);
  if (res) res=writeln();
  return res;
 }


 public boolean writeln(String str[])
 {
  boolean res=false;

  if (str!=null)
  {
   int t, len=str.length;

   if (len>0)
   {
    res=true;
    for (t=0;t<len && res;t++)
    {
     res=write(str[t]);
     if (res) res=write("\r\n");
    }

    if (res) res=flush();
   }
  }

  return res;
 }


 public boolean flush()
 {
  boolean res;

  try
  {
   bos.flush();
   res=true;
  }
  catch (Throwable tr)
  {
   ison=false;
   res=false;
  }

  return res;
 }



 // WriterListener
 protected BasicListenersManager writerListeners=new BasicListenersManager();

 public void addWriterListener(WriterListener l)
 {writerListeners.addListener(l);}

 public void removeWriterListener(WriterListener l)
 {writerListeners.removeListener(l);}

 Firer firer=new Firer()
 {public void foreachAction(EventListener listener, EventObject param)
  {((WriterListener)listener).writerEventOccurred((WriterEvent)param);}};

 public void fireWriterEvent(WriterEvent e)
 {writerListeners.foreachListener(firer, e);}
 // END - WriterListener

 public synchronized int getWrittenBytesCount()
 {
  return writtenBytesCount;
 }




 // interface MinimalWriter

 // write all the bytes of b
 public boolean Write(byte b[])
 {
  return write(b);
 }

 // write the byte b
 public boolean Write(int b)
 {
  return write(b);
 }

 // write len bytes of b starting from off
 public boolean Write(byte b[], int off, int len)
 {
  return write(b, off, len);
 }

 public boolean Write(char c[])
 {
  return Write(c, 0, c.length);
 }



 // todo NOT TESTED AT ALL!
 public static boolean WriteChars(MinimalWriter mw, char c[], int off, int len)
 {
  boolean res;

  try
  {
   String s=new String(c, off, len);
   res=mw.Write(s.getBytes(defaultCharsetName));
  } catch (Throwable tr){res=false;}

  return res;
 }



/*
 public static boolean WriteChars(MinimalWriter mw, char c[], int off, int len)
 {
  boolean res;
  CharToByteConverter ctb=CharToByteConverter.getDefault();
  int end=off+len;
  int nBytes=len*3;
  byte bb[]=new byte[nBytes];

  try
  {
   int bblen=ctb.convert(c, off, end, bb, 0, nBytes);
   res=mw.Write(bb, 0, bblen);
  } catch (Throwable tr){res=false;}

  return res;
 }
*/

 public boolean Write(char c[], int off, int len)
 {
  return WriteChars(this, c, off, len);
 }

 public boolean Write(String str)
 {
  return write(str);
 }

 public boolean Writeln()
 {
  return writeln();
 }

 public boolean Writeln(String str)
 {
  return writeln(str);
 }

 public boolean Writeln(String str[])
 {
  return writeln(str);
 }


 // Writes a boolean to the file as a one-byte value. The
 public void WriteBoolean(boolean v)
 {
  Write(v ? 1 : 0);
 }


 // Writes a byte
 public void WriteByte(int v)
 {
  Write(v);
 }


 // Writes a short
 public void WriteShort(int v)
 {
  Write((v >>> 8) & 0xFF);
  Write((v >>> 0) & 0xFF);
 }


 // Writes a char
 public void WriteChar(int v)
 {
  Write((v >>> 8) & 0xFF);
  Write((v >>> 0) & 0xFF);
 }


 // Writes an int
 public void WriteInt(int v)
 {
  byte b[]=new byte[4];

  b[0]=(byte)((v >>> 24) & 0xFF);
  b[1]=(byte)((v >>> 16) & 0xFF);
  b[2]=(byte)((v >>>  8) & 0xFF);
  b[3]=(byte)((v       ) & 0xFF);
  Write(b);

/*
  Write((v >>> 24) & 0xFF);
  Write((v >>> 16) & 0xFF);
  Write((v >>>  8) & 0xFF);
  Write((v >>>  0) & 0xFF);
*/
 }


 // Writes a long
 public void WriteLong(long v)
 {
  Write((int)(v >>> 56) & 0xFF);
  Write((int)(v >>> 48) & 0xFF);
  Write((int)(v >>> 40) & 0xFF);
  Write((int)(v >>> 32) & 0xFF);
  Write((int)(v >>> 24) & 0xFF);
  Write((int)(v >>> 16) & 0xFF);
  Write((int)(v >>>  8) & 0xFF);
  Write((int)(v >>>  0) & 0xFF);
 }


 // Writes a float
 public void WriteFloat(float v)
 {
  WriteInt(Float.floatToIntBits(v));
 }


 // Writes a double
 public void WriteDouble(double v)
 {
  WriteLong(Double.doubleToLongBits(v));
 }


 // END - interface MinimalWriter

}
