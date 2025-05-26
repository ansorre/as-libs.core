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


public class DamnCharsToBytes
{

 public static String getDamnStringFromTheseBytes(byte b[])
 {
  String res;

  try
  {
   int t, len=b.length;
   StringBuilder sb=new StringBuilder();

   for (t=0;t<len;t++)
   {
    sb.append(getThisByteDamnChar(b[t]));
   }

   res=sb.toString();

  } catch (Throwable tr){res=null;}

  return res;
 }

 public static byte[] getDamnBytesFromThisString(String s)
 {
  byte res[];

  try
  {
   int t, len=s.length();
   res=new byte[len];

   for (t=0;t<len;t++)
   {
    res[t]=getThisCharDamnByte(s.charAt(t));

   }

  } catch (Throwable tr){res=null;}

  return res;
 }


 public static byte getThisCharDamnByte(char ch)
 {

  return (byte)ch;
 }


 public static char getThisByteDamnChar(byte b)
 {

  return (char)b;
 }


}
