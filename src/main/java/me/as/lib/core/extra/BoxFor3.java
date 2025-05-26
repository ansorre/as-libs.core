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

package me.as.lib.core.extra;


public class BoxFor3<E1, E2, E3>
{

 public E1 element1;
 public E2 element2;
 public E3 element3;


 public BoxFor3()
 {

 }


 public BoxFor3(E1 element1, E2 element2, E3 element3)
 {
  set(element1, element2, element3);
 }

 public BoxFor3 set(E1 element1, E2 element2, E3 element3)
 {
  this.element1=element1;
  this.element2=element2;
  this.element3=element3;
  return this;
 }


}

