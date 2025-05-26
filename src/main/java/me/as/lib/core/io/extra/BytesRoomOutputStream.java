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

import java.io.OutputStream;


public class BytesRoomOutputStream extends OutputStream
{
 protected BytesRoom br;

 public BytesRoomOutputStream(BytesRoom br)
 {
  this(br, 0);
 }

 public BytesRoomOutputStream(BytesRoom br, long fromPosition)
 {
  this.br=br;
  if (fromPosition>=0) this.br.setCurrentPosition(fromPosition);
 }


 public void write(int b) throws me.as.lib.core.io.IOException
 {
  if (!br.Write(b)) throw new me.as.lib.core.io.IOException("Cannot write: write(int b) ");
 }

 public void write(byte b[]) throws me.as.lib.core.io.IOException
 {
  if (!br.Write(b)) throw new me.as.lib.core.io.IOException("Cannot write: write(int b[]) ");
 }

 public void write(byte b[], int off, int len) throws me.as.lib.core.io.IOException
 {
  if (!br.Write(b, off, len)) throw new me.as.lib.core.io.IOException("Cannot write: write(byte b[], int off, int len) ");
 }


 public void flush() throws me.as.lib.core.io.IOException
 {
  if (!br.flush()) throw new me.as.lib.core.io.IOException("Cannot flush: flush() ");
 }

 public void close() throws me.as.lib.core.io.IOException
 {
  if (!br.close()) throw new me.as.lib.core.io.IOException("Cannot close: close() ");
 }


}
