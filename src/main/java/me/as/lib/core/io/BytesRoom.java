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


import java.io.*;


public interface BytesRoom extends DataInput, DataOutput, MinimalReader, MinimalWriter
{
 int S_UNKNOWN_ERROR  =  0;
 int S_CLOSED         =  1;
 int S_OPENED         =  2;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 // after this getCurrentPosition() returns zero
 void setContent(BytesRoom br) throws me.as.lib.core.io.IOException;

 // after this getCurrentPosition() returns zero
 // if fromPosition==-1 the current position in br is not changed
 void setContent(BytesRoom br, long fromPosition) throws me.as.lib.core.io.IOException;

 // after this getCurrentPosition() returns zero
 void setContent(String str) throws me.as.lib.core.io.IOException;

 // after this getCurrentPosition() returns zero
 void setContent(InputStream is) throws me.as.lib.core.io.IOException;

 // after this getCurrentPosition() returns zero
 void setContent(byte bytes[]) throws me.as.lib.core.io.IOException;

 // this method does not change the getCurrentPosition() result
 byte[] getContent() throws me.as.lib.core.io.IOException;


 // similar to setContent(byte bytes[]) but the bytes are not copied but used directly.
 // This will work only for in memory implementation, all the others will revert to
 // setContent(byte bytes[]).
 // This is useful to avoid compying and recopying of buffers
 void mountContent(byte bytes[]) throws me.as.lib.core.io.IOException;

 // similar to getContent() but the bytes returned are not copied but are the real internal buffer.
 // after this method has executed currentPosition will reset to 0 and size will be 0
 // This will work only for in memory implementation, all the others will revert to
 // getContent() with the addition that internal data is deleted and currentPosition will reset to 0 and size will be 0
 // This is useful to avoid compying and recopying of buffers
 byte[] unmountContent() throws me.as.lib.core.io.IOException;


 // delete all the bytes of BytesRoom and set the current position to zero
 void clear();

 long getSize();
 boolean setSize(long newSize);

 long getCurrentPosition();
 boolean setCurrentPosition(long newPosition);

 boolean flush();

 boolean open(String mode) throws me.as.lib.core.io.IOException;
 boolean close();
 int getStatus();

 String readUntilOneOfThese(String eolChars, String removeFromEnd) throws me.as.lib.core.io.IOException;

 String readSmallString() throws me.as.lib.core.io.IOException;
 String readMediumString() throws me.as.lib.core.io.IOException;
 String readLargeString() throws me.as.lib.core.io.IOException;

 void writeNewLine() throws me.as.lib.core.io.IOException;
 void writeSmallString(String str) throws me.as.lib.core.io.IOException;
 void writeMediumString(String str) throws me.as.lib.core.io.IOException;
 void writeLargeString(String str) throws me.as.lib.core.io.IOException;

 // if newPosition==-1 the current position in the BytesRoom is not changed
 InputStream toInputStream(long newPosition);

 // if newPosition==-1 the current position in the BytesRoom is not changed
 OutputStream toOutputStream(long newPosition);

 java.io.Reader toReader(long newPosition);
 java.io.Writer toWriter(long newPosition);





}

