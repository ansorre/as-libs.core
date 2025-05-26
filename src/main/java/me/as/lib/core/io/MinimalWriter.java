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


public interface MinimalWriter
{
 // Writes all the bytes of b
 boolean Write(byte b[]);

 // Writes the byte b
 boolean Write(int b);

 // Writes len bytes of b starting from off
 boolean Write(byte b[], int off, int len);

 boolean Write(char c[]);
 boolean Write(char c[], int off, int len);

 boolean Write(String str);
 boolean Writeln();
 boolean Writeln(String str);
 boolean Writeln(String str[]);

 // Writes a boolean as a one-byte value
 void WriteBoolean(boolean v);

 // Writes a byte
 void WriteByte(int v);

 // Writes a short
 void WriteShort(int v);

 // Writes a char
 void WriteChar(int v);

 // Writes an int
 void WriteInt(int v);

 // Writes a long
 void WriteLong(long v);

 // Writes a float
 void WriteFloat(float v);

 // Writes a double
 void WriteDouble(double v);


}


/*



    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .




 // Writes a boolean to the file as a one-byte value. The
 void WriteBoolean(boolean v)
 {
  Write(v ? 1 : 0);
 }


 // Writes a byte
 void WriteByte(int v)
 {
  Write(v);
 }


 // Writes a short
 void WriteShort(int v)
 {
  Write((v >>> 8) & 0xFF);
  Write((v >>> 0) & 0xFF);
 }


 // Writes a char
 void WriteChar(int v)
 {
  Write((v >>> 8) & 0xFF);
  Write((v >>> 0) & 0xFF);
 }


 // Writes an int
 void WriteInt(int v)
 {
  Write((v >>> 24) & 0xFF);
  Write((v >>> 16) & 0xFF);
  Write((v >>>  8) & 0xFF);
  Write((v >>>  0) & 0xFF);
 }


 // Writes a long
 void WriteLong(long v)
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
 void WriteFloat(float v)
 {
  WriteInt(Float.floatToIntBits(v));
 }


 // Writes a double
 void WriteDouble(double v)
 {
  WriteLong(Double.doubleToLongBits(v));
 }


*/
