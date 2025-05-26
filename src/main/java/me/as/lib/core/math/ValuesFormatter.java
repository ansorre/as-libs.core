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

package me.as.lib.core.math;


import me.as.lib.core.lang.StringExtras;

import java.util.Locale;

public class ValuesFormatter
{


 public static String f(double value)
 {
  return StringExtras.formatDouble(value, 2, 2, Locale.US);
 }

 public static String f(double value, boolean asInteger)
 {
  if (asInteger) return f((int)value);
  else return f(value);
 }



 public static String f1(double value)
 {
  return StringExtras.formatDouble(value, 1, 1, Locale.US);
 }


 public static String f2(double value)
 {
  return StringExtras.formatDouble(value, 2, 2, Locale.US);
 }


 public static String f4(double value)
 {
  return StringExtras.formatDouble(value, 4, 4, Locale.US);
 }


 public static String f5(double value)
 {
  return StringExtras.formatDouble(value, 5, 5, Locale.US);
 }


 public static String f6(double value)
 {
  return StringExtras.formatDouble(value, 6, 6, Locale.US);
 }

 public static String f7(double value)
 {
  return StringExtras.formatDouble(value, 7, 7, Locale.US);
 }


 public static String f10(double value)
 {
  return StringExtras.formatDouble(value, 10, 10, Locale.US);
 }


 public static String f(int value)
 {
  return f((long)value);
 }


 public static String f(long value)
 {
  String res;
  boolean minusThanZero=(value<0);
  if (minusThanZero) value*=-1;
  res=String.valueOf(StringExtras.equallySeparate(String.valueOf(value), 3, ","));
  if (minusThanZero) res="-"+res;
  return res;
 }



}
