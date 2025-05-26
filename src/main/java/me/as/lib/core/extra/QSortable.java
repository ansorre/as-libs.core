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


public interface QSortable
{
 void setMid(int mididx, Object params);

 // must return:
 // <0 if elem1<mid
 // 0 if elem1==mid
 // >0 if elem1>mid
 int compareToMid(int elem1, Object params);

 boolean swap(int elem1, int elem2, Object params);
}
