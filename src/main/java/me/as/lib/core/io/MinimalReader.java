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


public interface MinimalReader
{
 int Read(byte b[]);

 int Read();

 int Read(byte b[], int off, int len);

 int Read(char c[]);
 int Read(char c[], int off, int len);

 String Readln();

 String ReadUntilOneOfThese(String eolChars, String removeFromEnd);


 // Reads a boolean
 boolean ReadBoolean();

 // Reads a signed eight-bit value
 byte ReadByte();

 // Reads an unsigned eight-bit number
 int ReadUnsignedByte();

 // Reads a signed 16-bit number
 short ReadShort();

 // Reads an unsigned 16-bit number
 int ReadUnsignedShort();

 // Reads a Unicode character
 char ReadChar();

 // Reads a signed 32-bit integer
 int ReadInt();

 // Reads a signed 64-bit integer from this file
 long ReadLong();

 // Reads a float
 float ReadFloat();

 // Reads a double
 double ReadDouble();


}

