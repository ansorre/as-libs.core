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


public class BytesRoomJavaIoWriter extends java.io.Writer
{
 private MemBytesRoom mbr;

 public BytesRoomJavaIoWriter(BytesRoom br, long fromPosition) throws me.as.lib.core.io.IOException
 {
  mbr=new MemBytesRoom();
  mbr.setContent(br, fromPosition);
 }


 public void write(char cbuf[], int off, int len) throws me.as.lib.core.io.IOException
 {
  mbr.Write(cbuf, off, len);
 }


 public void flush() throws me.as.lib.core.io.IOException
 {
  mbr.flush();
 }


 public void close() throws me.as.lib.core.io.IOException
 {
  mbr.close();
 }


}
