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

package me.as.lib.core.log;


import java.io.*;


public class SimulOutputStream extends OutputStream
{
 MinimalLogable ml;


 public SimulOutputStream(MinimalLogable ml)
 {
  this.ml=ml;
 }

 public void write(int b)
 {
  write(new byte[]{(byte)b}, 0, 1);
 }

 public void write(byte b[])
 {
  write(b, 0, b.length);
 }

 public void write(byte b[], int off, int len)
 {
  ml.print(new String(b, off, len));
 }

 public void flush()
 {
  ml.flush();
 }

 public void close()
 {
  ml.close();
 }

}
