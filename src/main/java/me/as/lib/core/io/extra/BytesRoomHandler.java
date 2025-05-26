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


import me.as.lib.core.io.BytesRoom;
import me.as.lib.core.io.IOException;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

import static me.as.lib.core.lang.StringExtras.getBytes;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.haveChars;
import static me.as.lib.core.lang.StringExtras.newAutoString;


public abstract class BytesRoomHandler implements BytesRoom
{
 protected int status=S_CLOSED;
 protected long position=0;

 // after this getCurrentPosition() returns zero
 public void setContent(BytesRoom br) throws me.as.lib.core.io.IOException
 {
  setContent(br, 0);
 }

 // after this getCurrentPosition() returns zero
 // if fromPosition==-1 the current position in br is not changed
 public void setContent(BytesRoom br, long fromPosition) throws me.as.lib.core.io.IOException
 {
  clear();

  if (br!=null)
  {
   long len=br.getSize();

   if (fromPosition==-1) fromPosition=br.getCurrentPosition();

   if (len>0 && fromPosition<len)
   {
    byte data[]=new byte[(int)(len-fromPosition)];
    long oldPos=br.getCurrentPosition();

    br.setCurrentPosition(fromPosition);
    try{ br.readFully(data); }catch (Throwable tr){ throw new IOException(tr); }
    br.setCurrentPosition(oldPos);

    write(data);
   }

   setCurrentPosition(0);
  }
 }


 // after this getCurrentPosition() returns zero
 public synchronized void setContent(String str) throws me.as.lib.core.io.IOException
 {
  if (hasChars(str))
   setContent(getBytes(str));
  else
   clear();
 }


 // after this getCurrentPosition() returns zero
 public synchronized void setContent(InputStream is) throws me.as.lib.core.io.IOException
 {
  try
  {
   if (is!=null)
   {
    int bytesread;
    byte buff[]=new byte[4096];
    boolean goon=true;
    setSize(0);
    setCurrentPosition(0);

    while (goon)
    {
     bytesread=is.read(buff);
     if (bytesread==-1) goon=false;
     else write(buff, 0, bytesread);
    }

    setCurrentPosition(0);
   } else clear();
  }
  catch (Throwable tr)
  {
   throw new IOException(tr);
  }
 }

 // after this getCurrentPosition() returns zero
 public synchronized void setContentFAST(InputStream is, byte buff[]) throws me.as.lib.core.io.IOException
 {
  try
  {
   if (is!=null)
   {
    int bytesread;
    boolean goon=true;
    setSize(0);
    setCurrentPosition(0);

    while (goon)
    {
     bytesread=is.read(buff);
     if (bytesread==-1) goon=false;
     else write(buff, 0, bytesread);
    }

    setCurrentPosition(0);
   }
   else
    clear();
  }
  catch (Throwable tr)
  {
   throw new IOException(tr);
  }
 }


 // after this getCurrentPosition() returns zero
 public synchronized void setContent(byte bytes[]) throws me.as.lib.core.io.IOException
 {
  if (bytes!=null)
  {
   int len=bytes.length;

   setSize(0);
   if (len>0)
   {
    setCurrentPosition(0);
    write(bytes);
   }

   setCurrentPosition(0);
  } else clear();
 }


 // this method does not change the getCurrentPosition() result
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



 // similar to setContent(byte bytes[]) but the bytes are not copied but used directly.
 // This will work only for in memory implementation, all the others will revert to
 // setContent(byte bytes[]).
 // This is useful to avoid compying and recopying of buffers
 public abstract void mountContent(byte bytes[]) throws me.as.lib.core.io.IOException;


 // similar to getContent() but the bytes returned are not copied but are the real internal buffer.
 // after this method has executed currentPosition will reset to 0 and size will be 0
 // This will work only for in memory implementation, all the others will revert to
 // getContent() with the addition that internal data is deleted and currentPosition will reset to 0 and size will be 0
 // This is useful to avoid compying and recopying of buffers
 public abstract byte[] unmountContent() throws me.as.lib.core.io.IOException;



 public synchronized long getCurrentPosition()
 {
  long curSize=getSize();
  if (position>curSize) position=curSize;
  return position;
 }


 public synchronized boolean setCurrentPosition(long newPosition)
 {
  boolean res=true;

  if (newPosition<0) newPosition=0;

  if (position!=newPosition)
  {
   if (newPosition>getSize())
   {
    res=setSize(newPosition+1);
    if (res) position=newPosition;
   } else position=newPosition;
  }

  return res;
 }


 public int getStatus()
 {
  return status;
 }

 // if newPosition==-1 the current position in the BytesRoom is not changed
 public InputStream toInputStream(long newPosition)
 {
  InputStream res;

  try
  {
   res=new BytesRoomInputStream(this, newPosition);
  } catch (Throwable tr){res=null;}

  return res;
 }

 // if newPosition==-1 the current position in the BytesRoom is not changed
 public OutputStream toOutputStream(long newPosition)
 {
  OutputStream res;

  try
  {
   res=new BytesRoomOutputStream(this, newPosition);
  } catch (Throwable tr){res=null;}

  return res;
 }


 public java.io.Reader toReader(long newPosition)
 {
  java.io.Reader res;

  try
  {
   res=new BytesRoomJavaIoReader(this, newPosition);
  } catch (Throwable tr){res=null;}

  return res;
 }


 public java.io.Writer toWriter(long newPosition)
 {
  java.io.Writer res;

  try
  {
   res=new BytesRoomJavaIoWriter(this, newPosition);
  } catch (Throwable tr){res=null;}

  return res;
 }




 // delete all the bytes of BytesRoom and set the current position to zero
 public void clear()
 {
  setSize(0);
 }


 public abstract long getSize();
 public abstract boolean setSize(long newSize);
 public abstract boolean flush();
 public abstract boolean open(String mode) throws me.as.lib.core.io.IOException;
 public abstract boolean close();

 // interface DataInput

 public abstract int read() throws me.as.lib.core.io.IOException;
 public abstract int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException;

 public int read(byte b[]) throws me.as.lib.core.io.IOException
 {
  return read(b, 0, b.length);
 }





 public boolean readBoolean() throws me.as.lib.core.io.IOException
 {
/*
  int ch = this.read();
//  if (ch<0) throw new EOFException();
  return (ch!=0);
*/
  return (this.read()!=0);
 }

 public byte readByte() throws me.as.lib.core.io.IOException
 {
/*
  int ch=this.read();
//  if (ch<0) throw new EOFException();
  return (byte)(ch);
*/

  return (byte)this.read();
 }

 public int readUnsignedByte() throws me.as.lib.core.io.IOException
 {
/*
  int ch = this.read();
//  if (ch<0) throw new EOFException();
  return ch;
*/
  return this.read();
 }


 private byte intBBuff[]=new byte[4];

 public short readShort() throws me.as.lib.core.io.IOException
 {
/*
  int ch1 = this.read();
  int ch2 = this.read();
//  if ((ch1 | ch2) < 0) throw new EOFException();
  return (short)((ch1 << 8) + (ch2 << 0));
*/

  this.read(intBBuff, 0, 2);
  return (short)(((((int)intBBuff[0]) & 0xFF) << 8) +
                ((((int)intBBuff[1]) & 0xFF)));

 }


 public int readUnsignedShort() throws me.as.lib.core.io.IOException
 {
/*
  int ch1 = this.read();
  int ch2 = this.read();
//  if ((ch1 | ch2) < 0) throw new EOFException();
  return (ch1 << 8) + (ch2 << 0);
*/

  this.read(intBBuff, 0, 2);
  return (int)(((((int)intBBuff[0]) & 0xFF) << 8) +
                ((((int)intBBuff[1]) & 0xFF)));

 }

 public char readChar() throws me.as.lib.core.io.IOException
 {
/*
  int ch1 = this.read();
  int ch2 = this.read();
//  if ((ch1 | ch2) < 0) throw new EOFException();
  return (char)((ch1 << 8) + (ch2 << 0));
*/

  this.read(intBBuff, 0, 2);
  return (char)(((((int)intBBuff[0]) & 0xFF) << 8) +
                ((((int)intBBuff[1]) & 0xFF)));


 }




 public int readInt() throws me.as.lib.core.io.IOException
 {


/*

  int ch1 = this.read();
  int ch2 = this.read();
  int ch3 = this.read();
  int ch4 = this.read();
//  if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
  return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

*/

  this.read(intBBuff, 0, 4);
  return (((((int)intBBuff[0]) & 0xFF) << 24) +
          ((((int)intBBuff[1]) & 0xFF) << 16) +
          ((((int)intBBuff[2]) & 0xFF) << 8) +
          ((((int)intBBuff[3]) & 0xFF)));
 }

 public long readLong() throws me.as.lib.core.io.IOException
 {
  return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
 }

 public float readFloat() throws me.as.lib.core.io.IOException
 {
  return Float.intBitsToFloat(readInt());
 }

 public double readDouble() throws me.as.lib.core.io.IOException
 {
  return Double.longBitsToDouble(readLong());
 }

 public String readLine() throws me.as.lib.core.io.IOException
 {
  StringBuilder input=new StringBuilder();
  int c;

  while (((c=read())!=-1) && (c!= '\n'))
  {
   input.append((char)c);
  }

  if ((c==-1) && (input.length()==0))
  {
   return null;
  }

  String res=input.toString();

  if (hasChars(res))
  {
   while (res.endsWith("\r"))
   {
    res=res.substring(0, res.length()-1);
   }
  }

  return res;
 }


 public String readUntilOneOfThese(String eolChars, String removeFromEnd) throws me.as.lib.core.io.IOException
 {
  StringBuilder input = new StringBuilder();
  int c;

  while (((c=read())!=-1) && (eolChars.indexOf((char)c)<0))
  {
   input.append((char)c);
  }

  if ((c==-1) && (input.length()==0))
  {
   return null;
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


 private String _readString(int len) throws me.as.lib.core.io.IOException
 {
  String res=null;

  if (len>0)
  {
   byte chars[]=new byte[len];

   readFully(chars);
   res=newAutoString(chars);
  }

  return res;
 }

 public String readSmallString() throws me.as.lib.core.io.IOException
 {
  return _readString(read());
 }

 public String readMediumString() throws me.as.lib.core.io.IOException
 {
  return _readString(readUnsignedShort());
 }

 public String readLargeString() throws me.as.lib.core.io.IOException
 {
  return _readString(readInt());
 }



 public String readUTF() throws me.as.lib.core.io.IOException
 {
  try
  {
   return DataInputStream.readUTF(this);
  }
  catch (Throwable tr)
  {
   throw new IOException(tr);
  }
 }

 public void readFully(byte b[]) throws me.as.lib.core.io.IOException
 {
  readFully(b, 0, b.length);
 }

 public void readFully(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  int n=0;

  while (n<len)
  {
   int count = read(b, off + n, len - n);
   if (count<0) throw new IOException(new EOFException());
   n+=count;
  }
 }

 public int skipBytes(int n) throws me.as.lib.core.io.IOException
 {
  setCurrentPosition(getCurrentPosition()+n);
  return n;
 }

 // END - interface DataInput


 // interface DataOutput

 public abstract void write(int b) throws me.as.lib.core.io.IOException;
 protected abstract void writeBytes(byte b[], int off, int len) throws me.as.lib.core.io.IOException;

 public void write(byte b[]) throws me.as.lib.core.io.IOException
 {
  writeBytes(b, 0, b.length);
 }

 public void write(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  writeBytes(b, off, len);
 }

 public void writeBoolean(boolean v) throws me.as.lib.core.io.IOException
 {
  write(v ? 1 : 0);
 }

 public void writeByte(int v) throws me.as.lib.core.io.IOException
 {
  write(v);
 }

 public void writeShort(int v) throws me.as.lib.core.io.IOException
 {
  write((v >>> 8) & 0xFF);
  write((v >>> 0) & 0xFF);
 }

 public void writeChar(int v) throws me.as.lib.core.io.IOException
 {
  write((v >>> 8) & 0xFF);
  write((v >>> 0) & 0xFF);
 }

 public void writeInt(int v) throws me.as.lib.core.io.IOException
 {
  write((v >>> 24) & 0xFF);
  write((v >>> 16) & 0xFF);
  write((v >>>  8) & 0xFF);
  write((v >>>  0) & 0xFF);
 }

 public void writeLong(long v) throws me.as.lib.core.io.IOException
 {
  write((int)(v >>> 56) & 0xFF);
  write((int)(v >>> 48) & 0xFF);
  write((int)(v >>> 40) & 0xFF);
  write((int)(v >>> 32) & 0xFF);
  write((int)(v >>> 24) & 0xFF);
  write((int)(v >>> 16) & 0xFF);
  write((int)(v >>>  8) & 0xFF);
  write((int)(v >>>  0) & 0xFF);
 }

 public void writeFloat(float v) throws me.as.lib.core.io.IOException
 {
  writeInt(Float.floatToIntBits(v));
 }

 public void writeDouble(double v) throws me.as.lib.core.io.IOException
 {
  writeLong(Double.doubleToLongBits(v));
 }

 public void writeBytes(String s) throws me.as.lib.core.io.IOException
 {
  byte b[]=s.getBytes();
  writeBytes(b, 0, b.length);
 }

 public void writeChars(String s) throws me.as.lib.core.io.IOException
 {
  int clen = s.length();
  int blen = 2*clen;
  byte[] b = new byte[blen];
  char[] c = new char[clen];
  s.getChars(0, clen, c, 0);
  for (int i = 0, j = 0; i < clen; i++)
  {
   b[j++] = (byte)(c[i] >>> 8);
   b[j++] = (byte)(c[i] >>> 0);
  }

  writeBytes(b, 0, blen);
 }


 public void writeNewLine() throws me.as.lib.core.io.IOException
 {
  // todo: damn, otherwise it does not work on unix and mac os x
  writeBytes("\r\n");
//  writeBytes(System.getProperty("line.separator"));
 }


 public void writeSmallString(String str) throws me.as.lib.core.io.IOException
 {
  int len=0;

  if (str!=null) len=str.length();
  write(len);
  if (len>0) writeBytes(str);
 }

 public void writeMediumString(String str) throws me.as.lib.core.io.IOException
 {
  int len=0;

  if (str!=null) len=str.length();
  writeShort(len);
  if (len>0) writeBytes(str);
 }

 public void writeLargeString(String str) throws me.as.lib.core.io.IOException
 {
  int len=0;

  if (str!=null) len=str.length();
  writeInt(len);
  if (len>0) writeBytes(str);
 }



 public void writeUTF(String str) throws me.as.lib.core.io.IOException
 {
  int strlen = str.length();
  int utflen = 0;

  for (int i = 0 ; i < strlen ; i++)
  {
   int c = str.charAt(i);
   if ((c >= 0x0001) && (c <= 0x007F))
   {
    utflen++;
   }
   else if (c > 0x07FF)
   {
    utflen += 3;
   }
   else
   {
    utflen += 2;
   }
  }

  if (utflen > 65535) throw new IOException(new UTFDataFormatException());

  write((utflen >>> 8) & 0xFF);
  write((utflen >>> 0) & 0xFF);

  for (int i = 0 ; i < strlen ; i++)
  {
   int c = str.charAt(i);
   if ((c >= 0x0001) && (c <= 0x007F))
   {
    write(c);
   }
   else if (c > 0x07FF)
   {
    write(0xE0 | ((c >> 12) & 0x0F));
    write(0x80 | ((c >>  6) & 0x3F));
    write(0x80 | ((c >>  0) & 0x3F));

   }
   else
   {
    write(0xC0 | ((c >>  6) & 0x1F));
    write(0x80 | ((c >>  0) & 0x3F));
   }
  }
 }


 // END - interface DataOutput



 // interface MinimalWriter

 // write all the bytes of b
 public boolean Write(byte b[])
 {
  boolean res;

  try
  {
   write(b);
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 // write the byte b
 public boolean Write(int b)
 {
  boolean res;

  try
  {
   write(b);
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 // write len bytes of b starting from off
 public boolean Write(byte b[], int off, int len)
 {
  boolean res;

  try
  {
   write(b, off, len);
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 public boolean Write(char c[])
 {
  return Write(c, 0, c.length);
 }

 public boolean Write(char c[], int off, int len)
 {
  return OutputHandler.WriteChars(this, c, off, len);
 }

 public boolean Write(String str)
 {
  boolean res;

  try
  {
   writeBytes(str);
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 public boolean Writeln()
 {
  boolean res;

  try
  {
   writeNewLine();
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 public boolean Writeln(String str)
 {
  boolean res;

  try
  {
   writeBytes(str);
   writeNewLine();
   res=true;
  } catch (IOException ioe){res=false;}

  return res;
 }

 public boolean Writeln(String str[])
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
     res=Write(str[t]);
     if (res) res=Writeln();
    }

    if (res) res=flush();
   }
  }

  return res;
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
  Write((v >>> 24) & 0xFF);
  Write((v >>> 16) & 0xFF);
  Write((v >>>  8) & 0xFF);
  Write((v >>>  0) & 0xFF);
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



 // interface MinimalReader

 public int Read(byte b[])
 {
  int res;

  try
  {
   readFully(b);
   res=b.length;
  } catch (IOException ioe){res=-1;}

  return res;
 }

 public int Read()
 {
  int res;

  try
  {
   res=readUnsignedByte();
  } catch (IOException ioe){res=-1;}

  return res;
 }

 public int Read(byte b[], int off, int len)
 {
  int res;

  try
  {
   readFully(b, off, len);
   res=len;
  } catch (IOException ioe){res=-1;}

  return res;
 }


 public int Read(char c[])
 {
  return Read(c, 0, c.length);
 }

 public int Read(char c[], int off, int len)
 {
  return InputHandler.ReadChars(this, c, off, len);
 }

 public String Readln()
 {
  String res;

  try
  {
   res=readLine();
  }
  catch (IOException ioe)
  {
   res=null;
  }

  return res;
 }

 public String ReadUntilOneOfThese(String eolChars, String removeFromEnd)
 {
  String res;

  try
  {
   res=readUntilOneOfThese(eolChars, removeFromEnd);
  } catch (IOException ioe){res=null;}

  return res;
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

