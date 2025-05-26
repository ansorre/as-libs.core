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

package me.as.lib.core.lang;


public interface Types
{
 int _none      =  0;
 int _byte      =  1;
 int _short     =  2;
 int _int       =  3;
 int _long      =  4;
 int _char      =  5;
 int _float     =  6;
 int _double    =  7;
 int _boolean   =  8;
 int _string    =  9;
 int _bytes     = 10;
 int _shorts    = 11;
 int _ints      = 12;
 int _longs     = 13;
 int _chars     = 14;
 int _floats    = 15;
 int _doubles   = 16;
 int _booleans  = 17;
 int _strings   = 18;

 int _None      =  0;
 int _Byte      =  1;
 int _Short     =  2;
 int _Integer   =  3;
 int _Long      =  4;
 int _Character =  5;
 int _Float     =  6;
 int _Double    =  7;
 int _Boolean   =  8;
 int _String    =  9;



 String javaPrimitivesNames[]=new String[]
 {
  "byte",
  "short",
  "int",
  "long",
  "char",
  "float",
  "double",
  "boolean",
  "java.lang.String"
 };

 String jvmPrimitiveTypeNames[]=new String[]
 {
  /* byte    */ "B",
  /* short   */ "S",
  /* int     */ "I",
  /* long    */ "J",
  /* char    */ "C",
  /* float   */ "F",
  /* double  */ "D",
  /* boolean */ "Z",
  /* Object  */ "L"
 };


 String typesNames[]=new String[]
 {
  "none",
  "byte",
  "short",
  "int",
  "long",
  "char",
  "float",
  "double",
  "boolean",
  "string",
  "bytes",
  "shorts",
  "ints",
  "longs",
  "chars",
  "floats",
  "doubles",
  "booleans",
  "strings"
 };


 Class classes[]=new Class[]
 {
  null,
  Byte.TYPE,
  Short.TYPE,
  Integer.TYPE,
  Long.TYPE,
  Character.TYPE,
  Float.TYPE,
  Double.TYPE,
  Boolean.TYPE,
  String.class,
  byte[].class,
  short[].class,
  int[].class,
  long[].class,
  char[].class,
  float[].class,
  double[].class,
  boolean[].class,
  String[].class
 };


 Class classes2[]=new Class[]
 {
  null,
  Byte.class,
  Short.class,
  Integer.class,
  Long.class,
  Character.class,
  Float.class,
  Double.class,
  Boolean.class,
  String.class,
 };




 Class primitivesAndAlmostClasses[]=new Class[]
 {
  Byte.TYPE,      Byte.class,
  Short.TYPE,     Short.class,
  Integer.TYPE,   Integer.class,
  Long.TYPE,      Long.class,
  Character.TYPE, Character.class,
  Float.TYPE,     Float.class,
  Double.TYPE,    Double.class,
  Boolean.TYPE,   Boolean.class,
  String.class
 };

 static boolean isPrimitiveOrAlmost(Class clazz)
 {
  return (ArrayExtras.indexOf(primitivesAndAlmostClasses, 0, clazz)>=0);
 }



 /*
  These two below are to be used like this:

  res=Types.classesClasses[StringExtras.select(Types.classesNames, _theName_)];

 */



 String classesNames[]=new String[]
 {
  "Object", "java.lang.Object", "L",
  "java.lang.String",
  "byte", "B",
  "short", "S",
  "int", "I",
  "long", "J",
  "char", "C",
  "float", "F",
  "double", "D",
  "boolean", "Z",
  "java.lang.String",
  "java.lang.Byte",
  "java.lang.Short",
  "java.lang.Integer",
  "java.lang.Long",
  "java.lang.Character",
  "java.lang.Float",
  "java.lang.Double",
  "java.lang.Boolean",
 };


 Class classesClasses[]=new Class[]
 {
  Object.class, Object.class, Object.class,
  String.class,
  byte.class, byte.class,
  short.class, short.class,
  int.class, int.class,
  long.class, long.class,
  char.class, char.class,
  float.class, float.class,
  double.class, double.class,
  boolean.class, boolean.class,
  Byte.class,
  Short.class,
  Integer.class,
  Long.class,
  Character.class,
  Float.class,
  Double.class,
  Boolean.class,
 };




}