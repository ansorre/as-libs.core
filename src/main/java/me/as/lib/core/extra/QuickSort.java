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


public class QuickSort implements QSortable
{
 private QSortable object;
 private Object params;
 private int a[]=null;
 private boolean DESC;
 private int midValue;


 public QuickSort()
 {

 }

 public QuickSort(int a[], int init, int end, boolean DESC)
 {
  this.a=a;
  this.DESC=DESC;
  object=this;
  params=null;

  sort(init, end);
  clear();
 }

 public QuickSort(QSortable object, int init, int end, Object params)
 {
  sort(object, init, end, params);
 }


 public void sort(QSortable object, int init, int end, Object params)
 {
  this.object=object;
  this.params=params;
  sort(init, end);
  clear();
 }


 private void clear()
 {
  object=null;
  params=null;
 }



 public void setMid(int mididx, Object params)
 {
  midValue=a[mididx];
 }


 // must return:
 // <0 if elem1<mid
 // 0 if elem1==mid
 // >0 if elem1>mid
 public int compareToMid(int elem1, Object params)
 {
  int res=0;
  int e1=a[elem1];
  int e2=midValue;
  res=((e1<e2)?-1:((e1>e2)?1:0));

  if (DESC) res*=-1;

  return res;
 }

 public boolean swap(int elem1, int elem2, Object params)
 {
  if (elem1!=elem2)
  {
   int v=a[elem1];
   a[elem1]=a[elem2];
   a[elem2]=v;
  }

  return true;
 }

/*
 public void sort(int left, int right)
 {

    int i, last;

    if (left >= right) { // do nothing if array contains fewer than two
        return; 	     // two elements
    }

    object.swap(left, (left+right) / 2, params);


    last = left;
    for (i = left+1; i <= right; i++)
    {
     object.setMid(left, params);

        if (object.compareToMid(i, params)<0) {
         object.swap(++last, i, params);

        }
    }
    object.swap(left, last, params);

    sort(left, last-1);
    sort(last+1, right);
   }
 */


 public void sort(int lo0, int hi0)
 {
  int lo = lo0;
  int hi = hi0;

  if (hi0>lo0)
  {
   // Arbitrarily establishing partition element in the midpoint of the array.
   object.setMid(((lo0+hi0)/2), params);

   // loop through the array until indices cross
   while (lo<=hi)
   {
    // find the first element that is greater than or equal to
    // the partition element starting from the left Index.

    while (lo<hi0 && object.compareToMid(lo, params)<0) ++lo;

    // find an element that is smaller than or equal to
    // the partition element starting from the right Index.

    while (hi>lo0 && object.compareToMid(hi, params)>0) --hi;

    // if the indexes have not crossed, swap
    if (lo<=hi)
    {
     object.swap(lo, hi, params);
     ++lo;
     --hi;
    }
   }

   // If the right index has not reached the left side of array
   // must now sort the left partition.

   if (lo0<hi) sort(lo0, hi);

   // If the left index has not reached the right side of array
   // must now sort the right partition.

   if (lo<hi0) sort(lo, hi0);
  }
 }


}
