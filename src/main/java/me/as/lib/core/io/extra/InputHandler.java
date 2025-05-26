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
import me.as.lib.core.io.MinimalReader;
import me.as.lib.core.io.ReaderEvent;
import me.as.lib.core.io.ReaderListener;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.haveChars;
import static me.as.lib.core.lang.StringExtras.newAutoString;


/*
 // speed use of this class:

 protected InputHandler inH=null;

 public boolean isOn(){return inH.isOn();}
 public int read(){return inH.read();}
 public int read(byte b[]){return inH.read(b);}
 public int read(byte b[], int off, int len){return inH.read(b, off, len);}
 public String readln(){return inH.readln();}
 public int available(){return inH.available();}
 public boolean close(){return inH.close();}
 public void addReaderListener(ReaderListener l){inH.addReaderListener(l);}
 public void removeReaderListener(ReaderListener l){inH.removeReaderListener(l);}
 public int getReadBytesCount(){return inH.getReadBytesCount();}
 // interface MinimalReader
 public int Read(byte b[]){return inH.Read(b);}
 public int Read(){return inH.Read();}
 public int Read(byte b[], int off, int len){return inH.Read(b, off, len);}
 public int Read(char c[]){return inH.Read(c);}
 public int Read(char c[], int off, int len){return inH.Read(c, off, len);}
 public String Readln(){return inH.Readln();}
 public String ReadUntilOneOfThese(String eolChars, String removeFromEnd){return inH.ReadUntilOneOfThese(eolChars, removeFromEnd);}
 public boolean ReadBoolean(){return inH.ReadBoolean();}
 public byte ReadByte(){return inH.ReadByte();}
 public int ReadUnsignedByte(){return inH.ReadUnsignedByte();}
 public short ReadShort(){return inH.ReadShort();}
 public int ReadUnsignedShort(){return inH.ReadUnsignedShort();}
 public char ReadChar(){return inH.ReadChar();}
 public int ReadInt(){return inH.ReadInt();}
 public long ReadLong(){return inH.ReadLong();}
 public float ReadFloat(){return inH.ReadFloat();}
 public double ReadDouble(){return inH.ReadDouble();}
 // END - interface MinimalReader

*/


public class InputHandler implements me.as.lib.core.io.Reader
{
 protected InputStream bis;
 protected boolean ison;

 public InputHandler()
 {

 }

 public InputHandler(InputStream is)
 {
  this(is, 512);
 }

 public InputHandler(InputStream is, int bufferSize)
 {
  init(is, bufferSize);
 }

 public void init(InputStream is, int bufferSize)
 {
  if (bufferSize>0) bis=new BufferedInputStream(is, bufferSize);
  else bis=is;
  ison=true;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 private int readBytesCount=0;
 private synchronized void incReadBytesCount(int bc)
 {
  if (bc>0)
  {
   readBytesCount+=bc;
   if (readerListeners.areThereListeners())
   {
    try
    {
     fireReaderEvent(new ReaderEvent(bis, readBytesCount, ReaderEvent.READ_BYTES_COUNT_CHANGED));
    } catch (Throwable tr) {tr.printStackTrace();}
   }
  }
 }


 public synchronized int bis_available() throws me.as.lib.core.io.IOException
 {
  try
  {
   return bis.available();
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }


 public synchronized int bis_read() throws me.as.lib.core.io.IOException
 {
  try
  {
   int res=bis.read();
   if (res>=0) incReadBytesCount(1);
   return res;
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }

 public synchronized int _bis_read_(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  try
  {
   return bis.read(b, off, len);
  }
  catch (Throwable tr)
  {
   throw new me.as.lib.core.io.IOException(tr);
  }
 }

 public synchronized int bis_read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  int res;

  if (readerListeners.areThereListeners())
  {
   int block=(len/100);
   int rbytes=0;
   int rbytes_now;
   int thisBlock;

   if (block<1000) block=1000;

   while (rbytes<len && isOn())
   {
    thisBlock=block;
    if (thisBlock>len-rbytes) thisBlock=len-rbytes;
    rbytes_now=_bis_read_(b, off+rbytes, thisBlock);
    if (rbytes_now>0) rbytes+=rbytes_now;
    incReadBytesCount(rbytes_now);
   }

   res=rbytes;
  }
  else
  {
   res=_bis_read_(b, off, len);
   incReadBytesCount(res);
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public boolean isOn()
 {
  return ison;
 }

 public int read()
 {
  int res;

  try
  {
   res=bis_read();
  }
  catch (Throwable tr)
  {
   ison=false;
   res=-1;
  }

  return res;
 }

 public int read(byte b[])
 {
  return read(b, 0, b.length);
 }



 public int read(byte b[], int off, int len)
 {
  int res;

  try
  {
   res=0;
   while (res<len)
   {
    res+=bis_read(b, off+res, len-res);
   }
  }
  catch (Throwable tr)
  {
   ison=false;
   res=-1;
  }

  return res;
 }







 // TESTED: SUCCESS!
 public static int ReadChars(MinimalReader mr, char c[], int off, int len)
 {
  int res=0;

  try
  {
   String s="";
   byte bb[]=new byte[len*3];
   int read, idx=0;
   boolean ended=false;

   while (!ended && s.length()<len)
   {
    read=mr.Read();
    if (read==-1) ended=true;
    else
    {
     bb[idx++]=(byte)read;
     s=new String(bb, 0, idx, defaultCharsetName);
    }
   }

   if (s.length()>0)
   {
    char ca[]=s.toCharArray();

    System.arraycopy(ca, 0, c, off, ca.length);
    res=ca.length;

    if (res>len)
    {
     System.out.println("e mo?????????");
    }

   } else res=-1;
  }
  catch (Throwable tr)
  {
   res=-1;
  }

  return res;
 }




/*
 public static int ReadChars(MinimalReader mr, char c[], int off, int len)
 {
  int res=0;

  try
  {
   // todo URGENT: must find a way to do this without using the "only Sun" classes!
   try
   {
    ClassExtras.classFromName("sun.io.ByteToCharConverter");
   }
   catch (Throwable tr)
   {
    throw new RuntimeException("This JDK does not have sun.io.ByteToCharConverter class! ", tr);
   }


   sun.io.ByteToCharConverter btc=sun.io.ByteToCharConverter.getDefault();
   byte bb[]=new byte[len];
   mr.Read(bb);

   while (res<len)
   {
    try
    {
     btc.convert(bb, 0, len, c, off, off+len);
     res=len;
    }
    catch (sun.io.ConversionBufferFullException cbfe)
    //catch (ConversionBufferFullException cbfe)
    {
     System.out.println("E m� che cosa si false in questo caso ?????");
    }
   }
  }
  catch (Throwable tr)
  {
   res=-1;
  }

  return res;
 }
*/


 public int Read(char c[])
 {
  return Read(c, 0, c.length);
 }


 public int Read(char c[], int off, int len)
 {
  return ReadChars(this, c, off, len);
 }





 public String readln()
 {
  String res=null;
  MemBytesRoom mbr=new MemBytesRoom();

  try
  {
   boolean basta_cosi=false;
   boolean prev_was_r=false;
   int ch;

   while (!basta_cosi)
   {
    ch=bis_read();

    if (ch==-1)
    {
     ison=false;
     return null;
    }

    if (ch=='\n') basta_cosi=true;
    else
    {
     if (ch=='\r')
     {
      if (prev_was_r) mbr.write('\r');
      prev_was_r=true;
     }
     else
     {
      if (prev_was_r) {mbr.write('\r');prev_was_r=false;}
      mbr.write(ch);
     }
    }
   }

   try
   {
    res=newAutoString(mbr.getContent());
   } catch (Throwable tr){res=null;}
  }
  catch (Throwable tr)
  {
   ison=false;
   res=null;
  }

  return res;
 }






 /*
 public String readln()
 {
  String res=null;

  try
  {
   ByteArrayOutputStream bas=new ByteArrayOutputStream();
   boolean basta_cosi=false;
   boolean prev_was_r=false;
   int ch;

   while (!basta_cosi)
   {
    ch=bis_read();

    if (ch==-1)
    {
     ison=false;
     return null;
    }

    if (ch=='\n') basta_cosi=true;
    else
    {
     if (ch=='\r')
     {
      if (prev_was_r) bas.write('\r');
      prev_was_r=true;
     }
     else
     {
      if (prev_was_r) {bas.write('\r');prev_was_r=false;}
      bas.write(ch);
     }
    }
   }

   try
   {
    res=new String(bas.toByteArray());
   } catch (Throwable tr){res=null;}
  }
  catch (Throwable tr)
  {
   ison=false;
   res=null;
  }

  return res;
 }
 */




 public String readUntilOneOfThese(String eolChars, String removeFromEnd)
 {
  StringBuilder input = new StringBuilder();
  int c;

  while (((c=read())!=-1) && (eolChars.indexOf((char)c)<0))
  {
   input.append((char)c);
  }

  if (c==-1)
  {
   ison=false;
   if (input.length()==0) return null;
  }

  String res=input.toString();

  if (haveChars(res, removeFromEnd))
  {
   boolean removed;
   char ec;

   do
   {
    ec=res.charAt(res.length()-1);

    if (removeFromEnd.indexOf(ec)>=0)
    {
     res=res.substring(0, res.length()-1);
     removed=true;
    } else removed=false;

   } while (removed && res.length()>0);
  }

  return res;
 }



 public int available()
 {
  int res;

  try
  {
   res=bis_available();
  }
  catch (Throwable tr)
  {
   ison=false;
   res=-1;
  }

  return res;
 }




 // ReaderListener

 protected BasicListenersManager readerListeners=new BasicListenersManager();

 public void addReaderListener(ReaderListener l)
 {readerListeners.addListener(l);}

 public void removeReaderListener(ReaderListener l)
 {readerListeners.removeListener(l);}

 Firer firer=new Firer()
 {public void foreachAction(EventListener listener, EventObject param)
  {((ReaderListener)listener).readerEventOccurred((ReaderEvent)param);}};

 public void fireReaderEvent(ReaderEvent e)
 {readerListeners.foreachListener(firer, e);}

 // END - ReaderListener


 public synchronized int getReadBytesCount()
 {
  return readBytesCount;
 }


 // interface MinimalReader

 public int Read(byte b[])
 {
  return read(b);
 }

 public int Read()
 {
  return read();
 }

 public int Read(byte b[], int off, int len)
 {
  return read(b, off, len);
 }

 public String Readln()
 {
  return readln();
 }


 public String ReadUntilOneOfThese(String eolChars, String removeFromEnd)
 {
  return readUntilOneOfThese(eolChars, removeFromEnd);
 }

 // Reads a boolean
 public boolean ReadBoolean()
 {
  int ch = Read();
  //if (ch < 0) throw new EOFException();
  return (ch != 0);
 }


 // Reads a signed eight-bit value
 public byte ReadByte()
 {
  int ch = Read();
  // if (ch < 0) throw new EOFException();
  return (byte)(ch);
 }

 // Reads an unsigned eight-bit number
 public int ReadUnsignedByte()
 {
  int ch = Read();
  //if (ch < 0) throw new EOFException();
  return ch;
 }

 // Reads a signed 16-bit number
 public short ReadShort()
 {
  int ch1 = Read();
  int ch2 = Read();
  //if ((ch1 | ch2) < 0) throw new EOFException();
  return (short)((ch1 << 8) + (ch2 << 0));
 }

 // Reads an unsigned 16-bit number
 public int ReadUnsignedShort()
 {
  int ch1 = Read();
  int ch2 = Read();
  // if ((ch1 | ch2) < 0) throw new EOFException();
  return (ch1 << 8) + (ch2 << 0);
 }


 // Reads a Unicode character
 public char ReadChar()
 {
  int ch1 = Read();
  int ch2 = Read();
  // if ((ch1 | ch2) < 0) throw new EOFException();
  return (char)((ch1 << 8) + (ch2 << 0));
 }


 // Reads a signed 32-bit integer
 public int ReadInt()
 {
  int ch1 = Read();
  int ch2 = Read();
  int ch3 = Read();
  int ch4 = Read();
  // if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
  return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
 }


 // Reads a signed 64-bit integer from this file
 public long ReadLong()
 {
  return ((long)(ReadInt()) << 32) + (ReadInt() & 0xFFFFFFFFL);
 }


 // Reads a float
 public float ReadFloat()
 {
  return Float.intBitsToFloat(ReadInt());
 }


 // Reads a double
 public double ReadDouble()
 {
  return Double.longBitsToDouble(ReadLong());
 }


 // END - interface MinimalReader

}






