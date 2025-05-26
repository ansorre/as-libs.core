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


public class BytesRoomJavaIoReader extends java.io.Reader
{
 private BytesRoom br;
 private int fromPosition;


 public BytesRoomJavaIoReader(BytesRoom br, long fromPosition) throws me.as.lib.core.io.IOException
 {
  this.br=br;
  this.fromPosition=(int)fromPosition;
 }

 public int read(char cbuf[], int off, int len) throws me.as.lib.core.io.IOException
 {
  return br.Read(cbuf, fromPosition+off, len);
 }


 public void close() throws me.as.lib.core.io.IOException
 {
  br.close();
 }


}
