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

import java.io.InputStream;


public class BytesRoomInputStream extends InputStream
{
 protected BytesRoom br;

 public BytesRoomInputStream(BytesRoom br) throws me.as.lib.core.io.IOException
 {
  this(br, 0);
 }

 // if fromPosition==-1 the current position in br is not changed
 public BytesRoomInputStream(BytesRoom br, long fromPosition) throws me.as.lib.core.io.IOException
 {
  this.br=br;
  if (fromPosition>=0) this.br.setCurrentPosition(fromPosition);
 }


 public int read() throws me.as.lib.core.io.IOException
 {
  int res;

  try
  {
   res=br.Read();
  }
  catch (Throwable tr)
  {
   res=-1;
  }

  return res;
 }

 public int read(byte b[]) throws me.as.lib.core.io.IOException
 {
  return read(b, 0, b.length);
 }

 public int read(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  int howMany=Math.min(len, available());
  int res=-1;
  if (howMany>0) res=br.Read(b, off, howMany);
  return res;
 }

 public long skip(long n) throws me.as.lib.core.io.IOException
 {
  long curPos=br.getCurrentPosition();
  br.setCurrentPosition(curPos+n);
  return br.getCurrentPosition()-curPos;
 }

 public int available() throws me.as.lib.core.io.IOException
 {
  return (int)(br.getSize()-br.getCurrentPosition());
 }

 public void close() throws me.as.lib.core.io.IOException
 {

 }

 public synchronized void mark(int readlimit)
 {

 }

 public synchronized void reset() throws me.as.lib.core.io.IOException
 {

 }

 public boolean markSupported()
 {
  return false;
 }



}
