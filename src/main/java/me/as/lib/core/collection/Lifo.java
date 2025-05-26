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

package me.as.lib.core.collection;


public class Lifo<E> extends Fifo<E>
{

 public Lifo()
 {
  super();
 }

 public Lifo(int initCapacity)
 {
  super(initCapacity);
 }


 // WARNING: this method leaves the returned Object in the Fifo
 public synchronized E whoIsTheNext()
 {
  return (elemCount>0) ? (E)elements[zeroIndex+elemCount-1] :null;
 }


 public synchronized E get()
 {
  E res=null;

  if (elemCount>0)
  {
   res=(E)elements[zeroIndex+elemCount-1];
   elements[zeroIndex+elemCount-1]=null;
   elemCount--;
   onElementRemoved(res);
  }

  return res;
 }


}
