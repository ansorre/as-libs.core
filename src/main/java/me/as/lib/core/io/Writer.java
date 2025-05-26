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

package me.as.lib.core.io;


public interface Writer extends MinimalWriter
{

 boolean isOn();

 // write the byte b
 boolean write(int b);

 // write len bytes of b starting from off
 boolean write(byte b[], int off, int len);

 // write all the bytes of b
 boolean write(byte b[]);

 boolean write(String str);
 boolean writeln();
 boolean writeln(String str);
 boolean writeln(String str[]);

 boolean flush();

 void addWriterListener(WriterListener l);
 void removeWriterListener(WriterListener l);

 int getWrittenBytesCount();


}

