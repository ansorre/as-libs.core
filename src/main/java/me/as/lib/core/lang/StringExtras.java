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


import me.as.lib.core.collection.RamTable;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.extra.QSortable;
import me.as.lib.core.extra.QuickSort;
import me.as.lib.core.extra.QuickSortExtras;
import me.as.lib.core.extra.TimeCounter;
import me.as.lib.core.math.MathExtras;
import me.as.lib.core.math.RandomExtras;

import javax.script.ScriptEngine;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static me.as.lib.core.extra.JavaScriptExtras.newJavaScriptEngine;
import static me.as.lib.core.lang.ArrayExtras.isArray;
import static me.as.lib.core.lang.ArrayExtras.isArrayOfPrimitive;
import static me.as.lib.core.lang.ByteExtras.copyInNew;
import static me.as.lib.core.math.RandomExtras.intRandom;


// `This string uses backticks.`;
public class StringExtras
{
 // singleton
 private StringExtras(){}

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static final String LINE_SEPARATOR=System.getProperty("line.separator");

 public static final char noBreakSpace=160;
 public static final String trimmable=(" \t"+noBreakSpace);
 public static final String trimmable_and_nl=(trimmable+"\n\r");

 public static final String utf8Charset           = "UTF-8";
 public static final String defaultCharsetName    = utf8Charset;
 public static final String defaultNetworkNewLine = "\r\n";

 public static final String considerableTrue[]=new String[]{"true", "yes", "1", "on"};
 public static final String considerableFalse[]=new String[]{"false", "no", "0", "off", "null", "void"};

 public static final String regexSpecialChars="\\*.?[]()':+|^$@%!{},";

 public static final String emailRegex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private static final String _spaces_="                        ";

 public static String getSpaces(int howMany)
 {
  int sLen=_spaces_.length();
  if (howMany<sLen) return _spaces_.substring(0, howMany);
  StringBuilder sb=new StringBuilder();

  while (howMany>0)
  {
   int min=Math.min(sLen, howMany);
   sb.append(_spaces_, 0, min);

   howMany-=min;
  }

  return sb.toString();
 }

/*

 public static void main(String args[])
 {
  String spac=getSpaces(0);
  System.out.println("->"+spac+"<-");

  spac=getSpaces(1);
  System.out.println("->"+spac+"<- "+spac.length());

  spac=getSpaces(8);
  System.out.println("->"+spac+"<- "+spac.length());

  spac=getSpaces(48);
  System.out.println("->"+spac+"<- "+spac.length());
 }

*/

 /**
  * Returns 0 if the sequence is null otherwise the sequence length
  *
  * @param sequence the sequence to check
  * @return the sequence length or zero if null
  */
 public static int length(CharSequence sequence)
 {
  return (sequence==null) ? 0 : sequence.length();
 }


 public static <S extends CharSequence> int maxLength(S sequences[])
 {
  int res=0;
  int t, len=ArrayExtras.length(sequences);

  for (t=0;t<len;t++)
  {
   int sLen=length(sequences[t]);
   if (res<sLen)
    res=sLen;
  }

  return res;
 }





 public static byte[] getBytes(String txt)
 {
  return getBytes(txt, defaultCharsetName);
 }



 public static byte[] getBytes(String txt, String charsetName)
 {
  byte res[];

  try
  {
   if (hasChars(txt)) res=txt.getBytes(charsetName);
   else res=null;
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return res;
 }



 public static boolean isOneOf(String source, String... oneOfs)
 {
  int t, len=ArrayExtras.length(oneOfs);

  for (t=0;t<len;t++)
   if (areEqual(source, oneOfs[t]))
    return true;

  return false;
 }


 public static int getCharCount(String str, char ch)
 {
  int res=0;

  if (hasChars(str))
  {
   int t, len=str.length();
   for (t=0;t<len;t++)
   {
    if (str.charAt(t)==ch) res++;
   }
  }

  return res;
 }



 /**
  * Checks if sequence is not null and has more than 0 chars
  *
  * @param sequence the sequence to check
  * @return returns false either if str is null or if (str.length()==0)
  */
 public static boolean hasChars(CharSequence sequence)
 {
  return (sequence!=null && sequence.length()>0);
 }

 public static boolean haveChars(CharSequence... sequence)
 {
  int t, len=ArrayExtras.length(sequence);

  for (t=0;t<len;t++)
  {
   if (sequence[t]==null || sequence[t].length()==0) return false;
  }

  return true;
 }

 /**
  * Checks if sequence is not null and has more than 0 non blank chars
  *
  * @param sequence the sequence to check
  * @return returns false either if str is null or if (str.length()==0)
  */
 public static boolean isNotBlank(CharSequence sequence)
 {
  if (hasChars(sequence))
   return hasChars(betterTrim(sequence));

  return false;
 }

 public static boolean isNotBlankNl(CharSequence sequence)
 {
  if (hasChars(sequence))
   return hasChars(betterTrimNl(sequence));

  return false;
 }


 public static boolean areNotBlank(CharSequence... sequence)
 {
  int t, len=ArrayExtras.length(sequence);

  for (t=0;t<len;t++)
  {
   if (!isNotBlank(sequence[t])) return false;
  }

  return true;
 }

 public static boolean areNotBlankNl(CharSequence... sequence)
 {
  int t, len=ArrayExtras.length(sequence);

  for (t=0;t<len;t++)
  {
   if (!isNotBlankNl(sequence[t])) return false;
  }

  return true;
 }


 public static String notBlankOrNull(String str)
 {
  return (isNotBlank(str) ? str : null);
 }





 /**
  * Checks if sequence is not null and has more than 0 non blank chars
  *
  * @param sequence the sequence to check
  * @return returns false either if str is null or if (str.length()==0)
  */
 public static boolean isBlank(CharSequence sequence)
 {
  return !isNotBlank(sequence);
 }

 public static boolean areBlank(CharSequence... sequence)
 {
  return !areNotBlank(sequence);
 }



 public static boolean containsOnlyThoseChars(String str, String allowedChars)
 {
  boolean res=true;

  if (hasChars(str) && hasChars(allowedChars))
  {
   int t, len=str.length();

   for (t=0;t<len && res;t++)
   {
    res=(allowedChars.indexOf(str.charAt(t))>=0);
   }
  }

  return res;
 }


 // tested with success
 public static boolean doesNotContainThoseChars(String str, String charsToBeAvoided)
 {
  boolean res=true;

  if (hasChars(str) && hasChars(charsToBeAvoided))
  {
   int t, len=str.length();

   res=false;

   for (t=0;t<len && !res;t++)
   {
    res=(charsToBeAvoided.indexOf(str.charAt(t))>=0);
   }

   res=!res;
  }

  return res;
 }



 public static boolean containsAtLeastOneOfThoseChars(String str, String allowedChars)
 {
  boolean res=false;

  if (hasChars(str) && hasChars(allowedChars))
  {
   int t, len=str.length();

   for (t=0;t<len && !res;t++)
   {
    res=(allowedChars.indexOf(str.charAt(t))>=0);
   }
  }

  return res;
 }



 public static boolean containsOnlyLettersOrThoseChars(String str, String allowedChars)
 {
  boolean res=true;

  if (hasChars(str) && hasChars(allowedChars))
  {
   char ch;
   int t, len=str.length();

   for (t=0;t<len && res;t++)
   {
    ch=str.charAt(t);
    res=(Character.isLetter(ch) || (allowedChars.indexOf(ch)>=0));
   }
  }

  return res;
 }




 public static int getOccurrencesCount(String str, String occurring)
 {
  int res=0;

  if (hasChars(str) && hasChars(occurring))
  {
   int c=0, len=length(str);

   while (c>=0 && c<len)
   {
    c=indexOf(str, occurring, c);
    if (c>=0)
    {
     res++;
     c++;
    }
   }
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Select


 // flags for select(String str[], String compare, int flag)
 public static final int SELECT_NONE             = 0x0000;
 public static final int SELECT_CASE_INSENSITIVE = 0x0001;
 public static final int SELECT_AUTOTRIM         = 0x0010;


 public static int select(String str[], String compare)
 {
  return select(str, compare, SELECT_NONE, 0);
 }


 // returns the index of the string 'compare' in the array 'str[]'
 // if 'compare' is not present in the array 'str[]' returns -1;
 public static int select(String str[], String compare, int flag)
 {
  return select(str, compare, flag, 0);
 }


 // returns the index of the string 'compare' in the array 'str[]'
 // if 'compare' is not present in the array 'str[]' returns -1;
 public static int select(String str[], String compare, int flag, int startIndex)
 {
  int res=-1;
  String tmpStr1, tmpStr2;

  if (compare==null) { return res; }

  try
  {
   int t, len=str.length;

   if (len>0)
   {
    tmpStr2=compare;

    if ((flag&SELECT_AUTOTRIM)!=0) { tmpStr2=betterTrim(tmpStr2); }
    if ((flag&SELECT_CASE_INSENSITIVE)!=0) { tmpStr2=tmpStr2.toUpperCase(); }

    for (t=startIndex;t<len && res==-1;t++)
    {
     try
     {
      tmpStr1=str[t];

      if ((flag&SELECT_AUTOTRIM)!=0) { tmpStr1=betterTrim(tmpStr1); }
      if ((flag&SELECT_CASE_INSENSITIVE)!=0) { tmpStr1=tmpStr1.toUpperCase(); }

      if (tmpStr1.equals(tmpStr2))
      {
       res=t;
      }
     } catch (Throwable ignore) {}
    }
   }
  } catch (Throwable ignore) {}

  return res;
 }

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Trim

 public static final String blankChars=(" \t"+noBreakSpace);
 public static final String blankChars_and_nl=(blankChars+"\n\r");

 public static String trim(String str)
 {
  return ((str!=null)?betterTrim(str):null);
 }


 public static String trim(String str, boolean alsoPurgeNewLines)
 {
  String res;

  if (alsoPurgeNewLines)
  {
   res=str;

   if (res!=null)
   {
    res=replace(res, "\n", " ");
    res=replace(res, "\r", " ");
    res=betterTrim(res);
   }
  } else res=trim(str);

  return res;
 }


 public static String[] trim(String str[])
 {
  return trim(str, false);
 }



 public static String[] trim(String str[], boolean alsoPurgeNewLines)
 {
  if (str!=null)
  {
   int t, len=str.length;

   if (len>0)
   {
    for (t=0;t<len;t++)
    {
     str[t]=trim(str[t], alsoPurgeNewLines);
    }
   }
  }

  return str;
 }

 public static <L extends List<String>> L trim(L str)
 {
  return trim(str, false);
 }

 public static <L extends List<String>> L trim(L str, boolean alsoPurgeNewLines)
 {
  if (str!=null)
  {
   int t, len=str.size();

   if (len>0)
   {
    for (t=0;t<len;t++)
    {
     str.set(t, trim(str.get(t), alsoPurgeNewLines));
    }
   }
  }

  return str;
 }



 public static String betterTrim(CharSequence sequence, String whatToTrim)
 {
  String str=sequence!=null ? sequence.toString() : null;

  if (hasChars(str))
  {
   while (str.length()>0 && whatToTrim.indexOf(str.charAt(0))>=0) str=str.substring(1);
   int idx=str.length()-1;
   while (idx>=0 && whatToTrim.indexOf(str.charAt(idx))>=0) {str=str.substring(0, idx);idx=str.length()-1;}
  }

  return str;
 }

 public static String betterTrimNl(CharSequence sequence)
 {
  return betterTrim(sequence, blankChars_and_nl);
 }

 public static String[] betterTrimNl(String str[])
 {
  return betterTrim(str, blankChars_and_nl);
 }

 public static String betterTrim(CharSequence sequence)
 {
  return betterTrim(sequence, blankChars);
 }


 public static String[] betterTrim(String str[])
 {
  int t, len=ArrayExtras.length(str);
  for (t=0;t<len;t++) str[t]=betterTrim(str[t]);
  return str;
 }


 public static String[] betterTrim(String str[], String whatToTrim)
 {
  int t, len=ArrayExtras.length(str);
  for (t=0;t<len;t++) str[t]=betterTrim(str[t], whatToTrim);
  return str;
 }



 public static String purgeAllNewLines(String txt)
 {
  String res=replace(txt, "\r\n", " ");
  res=replace(res, "\n\r", " ");

  int t, len=blankChars_and_nl.length();

  for (t=0;t<len;t++)
  {
   res=replace(res, ""+blankChars_and_nl.charAt(t), " ");
  }

  return res;
 }

 public static String purgeAllNewLinesAndAllWhiteSpaces(String txt)
 {
  String res=purgeAllNewLines(txt);
  int t, len=blankChars_and_nl.length();

  for (t=0;t<len;t++)
  {
   res=replace(res, ""+blankChars_and_nl.charAt(t), null);
  }

  return res;
 }








 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // transforms

 public static String toUpperCase(String str)
 {
  return ((str!=null)?str.toUpperCase():null);
 }

 public static String toLowerCase(String str)
 {
  return ((str!=null)?str.toLowerCase():null);
 }


 public static String[] toUpperCase(String strs[])
 {
  if (ArrayExtras.length(strs)>0)
  {
   int t, len=ArrayExtras.length(strs);

   for (t=0;t<len;t++)
    strs[t]=toUpperCase(strs[t]);
  }

  return strs;
 }


 public static String[] toLowerCase(String strs[])
 {
  if (ArrayExtras.length(strs)>0)
  {
   int t, len=ArrayExtras.length(strs);

   for (t=0;t<len;t++)
    strs[t]=toLowerCase(strs[t]);
  }

  return strs;
 }


 public static String capitalizeFirstLetter(String str)
 {
  if (length(str)>1)
  {
   return str.substring(0, 1).toUpperCase()+str.substring(1);
  }

  return toUpperCase(str);
 }



 public static String reverse(String text)
 {
  String res=text;

  if (hasChars(text))
  {
   StringBuilder sb=new StringBuilder();
   int t, len=text.length();

   for (t=len-1;t>=0;t--)
   {
    sb.append(text.charAt(t));
   }

   res=sb.toString();
  }

  return res;
 }



 public static String getMD5(String text)
 {
  String res;

  try
  {
   res=ByteExtras.getMD5(text.getBytes(defaultCharsetName));
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Equal

 // this returns true only if:
 // 1) both are null
 // 2) both are not null but are empty
 // 3) one is null and the other is not null but is empty
 // 4) both are not null neither empty and are equal
 public static boolean areEqual(String str1, String str2)
 {
  return areEqual(str1, str2, true);
 }


 public static boolean areEqual(String str1, String str2, boolean caseSensitive)
 {
  boolean res=false;

  if (str1==null && str2==null) res=true;
  else
  {
   if (!caseSensitive)
   {
    if (str1!=null) str1=str1.toUpperCase();
    if (str2!=null) str2=str2.toUpperCase();
   }

   if (str1!=null && str2!=null) res=str2.equals(str1);
   else
   {
    if (str1==null && str2.length()==0) res=true;
    else
    {
     if (str2==null && str1.length()==0) res=true;
    }
   }
  }

  return res;
 }



 public static boolean areEqual(String str1[], String str2[])
 {
  return areEqual(str1, str2, true);
 }

 public static boolean areEqual(String str1[], String str2[], boolean caseSensitive)
 {
  boolean res=false;
  int t, len=ArrayExtras.length(str1);

  if (ArrayExtras.length(str2)==len)
  {
   res=true;

   for (t=0;t<len && res;t++)
   {
    res=areEqual(str1[t], str2[t], caseSensitive);
   }
  }

  return res;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // IndexOf


 public static int lastIndexOf(String test, String match_mask, char charRep, boolean caseSensitive)
 {
  int res=-1;
  BoxFor2 to=_prepare_IndexOf_(test, match_mask, 0, caseSensitive, false);

  if (to!=null)
  {
   test=(String)to.element1;
   match_mask=(String)to.element2;

   if (charRep==0 || match_mask.indexOf(charRep)<0) res=test.lastIndexOf(match_mask);
   else
   {
    int mlen=match_mask.length();
    int tlen=test.length();

    if (containsOnlyThoseChars(match_mask, ""+charRep)) res=tlen-mlen;
    else
    {
     int max=tlen-mlen;
     char mch;
     char tch;
     int matching=0;
     int currInit=max;
     int pos=currInit;
     int mp=0;

     do
     {
      tch=test.charAt(pos);
      mch=match_mask.charAt(mp);

      if (mch!=tch && mch!=charRep)
      {
       matching=0;
       currInit--;
       pos=currInit;
       mp=0;
      }
      else
      {
       pos++;
       mp++;
       matching++;
      }
     } while (pos>=0 && matching!=mlen);

     if (matching==mlen)
     {
      res=currInit;
     }
    }
   }
  }

  return res;
 }


 public static int fullIndexOf(String test, String match_mask, boolean caseSensitive)
 {
  return indexOfShortest(test, match_mask, 0, caseSensitive);
 }

 public static int indexOfShortest(String test, String match_mask, int startIndex, boolean caseSensitive)
 {
  int vals[]=_indexOfShortest(test, match_mask, startIndex, caseSensitive);
  return vals[0];
 }

 private static int[] _indexOfShortest(String test, String match_mask, int startIndex, boolean caseSensitive)
 {
  int res=-1;
  int length=0;
  char jolly='*';

  if (hasChars(test) && hasChars(match_mask))
  {
   if (match_mask.startsWith(String.valueOf(jolly)))
   {
    if (doTheyMatch(test, match_mask, caseSensitive)) res=0;
   }
   else
   {
    String jollys[]=unmerge(match_mask, jolly);
    int len=ArrayExtras.length(jollys);

    if (len==1)
    {
     res=indexOf(test, match_mask, startIndex, caseSensitive);
    }
    else
    {
     int i=0, lastPos=0, pos=startIndex;

     do
     {
      pos=indexOf(test, jollys[i], pos, caseSensitive);

      if (pos>=0)
      {
       lastPos=pos;
       if (i==0) res=pos;
       i++;
      } else res=-1;

     } while (pos>=0 && i<len);

     if (res>=0)
     {
      length=lastPos-res;
      int tmp[]=_indexOfShortest(test, match_mask, res+1, caseSensitive);
      if (tmp[0]>=0 && tmp[1]<length)
      {
       res=tmp[0];
       length=tmp[1];
      }
     }
    }
   }
  }

  return new int[]{res, length};
 }




 public static int indexOf(String test, String match_mask, boolean caseSensitive)
 {
  return indexOf(test, match_mask, 0, caseSensitive);
 }

 public static int indexOf(String test, String match_mask, int startIndex, boolean caseSensitive)
 {
  int res=-1;
  char charRep='?';
  BoxFor2<String, String> b2=_prepare_IndexOf_(test, match_mask, startIndex, caseSensitive, false);

  if (b2!=null)
  {
   test=b2.element1;
   match_mask=b2.element2;

   if (match_mask.indexOf(charRep)<0) res=test.indexOf(match_mask, startIndex);
   else
   {
    if (containsOnlyThoseChars(match_mask, ""+charRep)) res=startIndex;
    else
    {
     int mlen=match_mask.length();
     int tlen=test.length();
     int max=tlen-mlen;
     char mch;
     char tch;
     int matching=0;
     int currInit=startIndex;
     int pos=currInit;
     int mp=0;

     do
     {
      tch=test.charAt(pos);
      mch=match_mask.charAt(mp);

      if (mch!=tch && mch!=charRep)
      {
       matching=0;
       currInit++;
       pos=currInit;
       mp=0;
      }
      else
      {
       pos++;
       mp++;
       matching++;
      }
     } while (currInit<=max && matching!=mlen);

     if (matching==mlen)
     {
      res=currInit;
     }
    }
   }
  }

  return res;
 }


 public static int indexOf(String str, int ch)
 {
  int res=-1;
  if (str!=null) res=str.indexOf(ch);
  return res;
 }

 public static int indexOf(String str, int ch, int fromIndex)
 {
  int res=-1;
  if (str!=null) res=str.indexOf(ch, fromIndex);
  return res;
 }

 public static boolean contains(String str, String test)
 {
  return (indexOf(str, test)>=0);
 }

 public static int indexOf(String str, String test)
 {
  int res=-1;
  if (str!=null && test!=null) res=str.indexOf(test);
  return res;
 }

 public static int indexOf(String str, String test, int fromIndex)
 {
  int res=-1;
  if (str!=null && test!=null) res=str.indexOf(test, fromIndex);
  return res;
 }


 private static BoxFor2<String, String> _prepare_IndexOf_(String test, String match_mask, int startIndex, boolean caseSensitive, boolean ignoreLengths)
 {
  BoxFor2 to=null;

  if (hasChars(test) && hasChars(match_mask) && startIndex>=0)
  {
   int tlen=test.length();
   int mlen=match_mask.length();

   if (ignoreLengths || mlen<=tlen)
   {
    if (ignoreLengths || startIndex<=tlen-mlen)
    {
     if (!caseSensitive)
     {
      test=test.toUpperCase();
      match_mask=match_mask.toUpperCase();
     }

     to=new BoxFor2(test, match_mask);
    }
   }
  }

  return to;
 }

 public static int indexOfFirstAllowedChar(String str, String allowedChars)
 {
  return indexOfFirstAllowedChar(str, allowedChars, 0);
 }

 public static int indexOfFirstAllowedChar(String str, String allowedChars, int startIndex)
 {
  int res=-1;
  int len=length(str);

  if (startIndex<len && hasChars(allowedChars))
  {
   for (int t=startIndex;t<len && res==-1;t++)
   {
    if (allowedChars.indexOf(str.charAt(t))>=0) res=t;
   }
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Split/Merge/Unmerge


 public static String enclose(String str, String initStr, String endStr)
 {
  if (initStr==null && endStr==null) return str;
  else
  {
   StringBuilder sb=new StringBuilder();

   if (hasChars(initStr)) sb.append(initStr);
   if (hasChars(str)) sb.append(str);
   if (hasChars(endStr)) sb.append(endStr);

   return sb.toString();
  }
 }



 /**
  *
  * Executes 'enclose' for each string in the array then returns the modified array
  *
  * @param str
  * @param initStr
  * @param endStr
  * @return
  */
 public static String[] enclose(String str[], String initStr, String endStr)
 {
  int t, len;

  if (str!=null && (len=str.length)>0)
   for (t=0;t<len;t++)
    str[t]=enclose(str[t], initStr, endStr);

  return str;
 }



 public static String mergeEnclosing(String str[], String initStr, String endStr)
 {
  return mergeEnclosing(str, 0, ArrayExtras.length(str), initStr, endStr);
 }

 public static String mergeEnclosing(String str[], int off, int len, String initStr, String endStr)
 {
  if (len>0)
   return merge(enclose((String[])ArrayExtras.clone(str, off, len), initStr, endStr));
  return null;
 }


 public static String merge(String... strs)
 {
  String res=null;

  if (ArrayExtras.length(strs)>0)
  {
   StringBuilder sb=new StringBuilder();
   for (String s : strs) sb.append(s);
   res=sb.toString();
  }

  return res;
 }


 public static String[] unmerge(String str, char separator)
 {
  ArrayList<String> al=new ArrayList<>();
  unmerge(str, separator, al, null);
  return al.toArray(new String[al.size()]);
 }


 public static String[] unmerge(String str, char separator, ArrayList<String> myList)
 {
  unmerge(str, separator, myList, null);
  return myList.toArray(new String[myList.size()]);
 }


 public static String[] unmerge(String str, char separator, String escapeString)
 {
  ArrayList<String> al=new ArrayList<>();
  unmerge(str, separator, al, escapeString);
  return al.toArray(new String[al.size()]);
 }



 public static ArrayList<String> unmerge(String str, char separator, ArrayList<String> myList, String escapeString)
 {
  if (hasChars(str))
  {
   int esLen=length(escapeString);
   int ei, si=0, len=str.length();

   do
   {
    ei=str.indexOf(separator, si);

    if (ei>=0)
    {
     if (esLen>0 && str.indexOf(escapeString, si)==ei) si=ei+esLen;
     else
     {
      if (ei==si)
      {
       if (ei>0) myList.add("");
      } else myList.add(str.substring(si, ei));
      si=ei+1;
     }
    }
    else
    {
     if (si<len) myList.add(str.substring(si, len));
    }
   } while (ei>=0);
  }
  else
  {
   if (str!=null)
   {
    myList.add(str);
   }
  }

  return myList;
 }



 // Ex. 1 : splitLast("1-2-3-4", '-') will return {"1-2-3", "4"}
 // Ex. 2 : splitLast("4", '-') will return {null, "4"}
 // Ex. 3 : splitLast(null, '-') will return {null, null}
 public static String[] splitLast(String txt, char ch, String myArray[])
 {
  if (hasChars(txt))
  {
   int lio=txt.lastIndexOf(ch);

   if (lio>=0)
   {
    myArray[0]=txt.substring(0, lio);
    myArray[1]=txt.substring(lio+1);
   }
   else
   {
    myArray[0]=null;
    myArray[1]=txt;
   }
  }
  else
  {
   myArray[0]=null;
   myArray[1]=null;
  }

  return myArray;
 }


 public static String[] splitLast(String txt, char ch)
 {
  return splitLast(txt, ch, new String[2]);
 }





 /**
  *
  * The strings returned in the array are extracted from 'str' in such a way that merging
  * all the returned strings returns the original 'str' here passed but all the strings
  * returned by cut are never longer than maxLen characters.
  *
  * @param str
  * @param maxLen
  * @return an array of strings.
  */
 public static String[] cut(String str, int maxLen)
 {
  return cutOnOccurrences(str, maxLen, null);
 }

 private static boolean canCut(int pos, char ch, int maxLen, String occurences)
 {
  boolean res=(pos>=maxLen);

  if (res && occurences!=null)
  {
   res=(occurences.indexOf(ch)>=0);
  }

  return res;
 }


 // same as cut but tries to cut 'str' at the nearest occurence of
 // one of the characters in 'occurences' to the maxLen character
 // of each obtained string
 public static String[] cutOnOccurrences(String str, int maxLen, String occurences)
 {
  String res[]=null;
  boolean returnIt=false;

  if (maxLen>0)
  {
   if (str!=null)
   {
    int t, len=str.length();
    char ch;

    if (len>0)
    {
     ArrayList<String> v=new ArrayList<>();
     StringBuilder strBuff=new StringBuilder();
     int yetAppended=0;

     for (t=0;t<len;t++)
     {
      ch=str.charAt(t);
      strBuff.append(ch);
      yetAppended++;

      if (canCut(yetAppended, ch, maxLen, occurences))
      {
       v.add(strBuff.toString());
       strBuff=new StringBuilder();
       yetAppended=0;
      }
     }

     if (yetAppended>0)
     {
      v.add(strBuff.toString());
      strBuff=new StringBuilder();
      yetAppended=0;
     }

     res=ArrayExtras.toArrayOfStrings(v);

    } else returnIt=true;
   }
  } else returnIt=true;

  if (returnIt)
  {
   res=new String[1];
   res[0]=str;
  }

  return res;
 }











 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Replace




 /**
  * Replaces the first substring of this string that matches the given <a
  * href="../extra/regex/Pattern.html#sum">regular expression</a> with the
  * given replacement.
  *
  * <p> An invocation of this method of the form
  * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
  * yields exactly the same result as the expression
  *
  * <blockquote>
  * <code>
  * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#compile()}(<i>regex</i>).
  * {@link java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).
  * {@link java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
  * </code>
  * </blockquote>
  *
  *<p>
  * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
  * replacement string may cause the results to be different than if it were
  * being treated as a literal replacement string; see
  * {@link java.util.regex.Matcher#replaceFirst}.
  * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
  * meaning of these characters, if desired.
  *
  * @param   regex
  *          the regular expression to which this string is to be matched
  * @param   replacement
  *          the string to be substituted for the first match
  *
  * @return  The resulting {@code String}
  *
  * @throws PatternSyntaxException
  *          if the regular expression's syntax is invalid
  *
  * @see java.util.regex.Pattern
  *
  * @since 1.4
  */
 public static String replaceFirst(String string, String regex, String replacement)
 {
  if (hasChars(string))
     return string.replaceFirst(regex,  replacement);

  return string;
 }

 /**
  * Replaces each substring of this string that matches the given <a
  * href="../extra/regex/Pattern.html#sum">regular expression</a> with the
  * given replacement.
  *
  * <p> An invocation of this method of the form
  * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
  * yields exactly the same result as the expression
  *
  * <blockquote>
  * <code>
  * {@link java.util.regex.Pattern}.
  * {@link java.util.regex.Pattern#compile()}(<i>regex</i>).
  * {@link java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).
  * {@link java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
  * </code>
  * </blockquote>
  *
  *<p>
  * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
  * replacement string may cause the results to be different than if it were
  * being treated as a literal replacement string; see
  * {@link java.util.regex.Matcher#replaceAll Matcher.replaceAll}.
  * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
  * meaning of these characters, if desired.
  *
  * @param   regex
  *          the regular expression to which this string is to be matched
  * @param   replacement
  *          the string to be substituted for each match
  *
  * @return  The resulting {@code String}
  *
  * @throws  PatternSyntaxException
  *          if the regular expression's syntax is invalid
  *
  * @see java.util.regex.Pattern
  *
  * @since 1.4
  */
 public static String replaceAll(String string, String regex, String replacement)
 {
  if (hasChars(string))
     return string.replaceAll(regex,  replacement);

  return string;
 }


 public static String[] replaceAll(String string[], String regex, String replacement)
 {
  int t, len=ArrayExtras.length(string);

  if (len>0)
  {
   for (t=0;t<len;t++)
    string[t]=replaceAll(string[t], regex, replacement);
  }

  return string;
 }


 /**
  * Replaces each substring of this string that matches the literal target
  * sequence with the specified literal replacement sequence. The
  * replacement proceeds from the beginning of the string to the end, for
  * example, replacing "aa" with "b" in the string "aaa" will result in
  * "ba" rather than "ab".
  *
  * @param  target The sequence of char values to be replaced
  * @param  replacement The replacement sequence of char values
  * @return  The resulting string
  * @since 1.5
  */
 public static String replace(String string, CharSequence target, CharSequence replacement)
 {
  return replace(string, target, replacement, false);
 }


 public static String[] replace(String strings[], CharSequence target, CharSequence replacement)
 {
  return replace(strings, target, replacement, false);
 }

 public static String[] replace(String strings[], CharSequence target, CharSequence replacement, boolean onlyOneRound)
 {
  int t, len=ArrayExtras.length(strings);

  for (t=0;t<len;t++)
   strings[t]=replace(strings[t], target, replacement, onlyOneRound);

  return strings;
 }


 public static String replace(String string, CharSequence target, CharSequence replacement, boolean onlyOneRound)
 {
  if (hasChars(string))
  {
   string=string.replace(target, stringOrEmpty(replacement));

   if (!onlyOneRound)
   {
    String st=target.toString();

    while (string.contains(st))
     string=string.replace(target, replacement);
   }
  }

  return string;
 }



 public static String replace(String str, String currentStrs[], String newStrs[])
 {
  return replace(str, currentStrs, newStrs, false);
 }


 public static String[] replace(String strings[], String currentStrs[], String newStrs[])
 {
  return replace(strings, currentStrs, newStrs, false);
 }


 public static String[] replace(String strings[], String currentStrs[], String newStrs[], boolean onlyOneRound)
 {
  int t, len=ArrayExtras.length(strings);

  for (t=0;t<len;t++)
   strings[t]=replace(strings[t], currentStrs, newStrs, onlyOneRound);

  return strings;
 }


 public static String replace(String str, String currentStrs[], String newStrs[], boolean onlyOneRound)
 {
  int t, len=currentStrs.length;

  for (t=0;t<len;t++)
   str=replace(str, currentStrs[t], newStrs[t], onlyOneRound);

  return str;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Numbers formatting

 private static transient final String numberDictionary[]={"", "K", "M", "G", "T", "P", "E", "Z", "Y"};
 private static transient final String bytesDictionary[]={"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};


 public static String formatBytes(double bytes)
 {
  return formatBigNumber(bytes, 1024, 0, bytesDictionary, " ");
 }

 public static String formatBigNumber(double bigNumber)
 {
  return formatBigNumber(bigNumber, 1000, 1, numberDictionary, "");
 }


 public static String formatBigNumber(double bigNumber, double demultiplier, int digits, String amountsDctionary[], String inside)
 {
  int index;

  for (index=0;index<amountsDctionary.length;index++)
  {
   if (bigNumber<demultiplier) break;
   bigNumber=bigNumber/demultiplier;
  }

  if (isBlank(inside)) inside="";
  String res=String.format(Locale.US, "%."+digits+"f", bigNumber);

  if (res.endsWith(".0"))
   res=res.substring(0, res.length()-2);

  res+=inside+amountsDctionary[index];

  return res;
 }




 public static String toIntWithSeparators(int value)
 {
  return formatDouble(value, 0, 0, Locale.ITALIAN, true, true);
 }


 public static String formatLong(long value)
 {
  return formatDouble(value, 0, 0, Locale.getDefault(), true, true);
 }


 public static String formatDouble(double value)
 {
  return formatDouble(value, 1, null);
 }

 public static String formatDouble(double value, Locale loc)
 {
  return formatDouble(value, 1, loc);
 }


 public static String formatDouble(double value, int numberOfDecimals)
 {
  return formatDouble(value, numberOfDecimals, null);
 }


 public static String formatDouble(double value, int numberOfDecimals, Locale loc)
 {
  return formatDouble(value, numberOfDecimals, -1, loc);
 }


 public static String formatDouble(double value, int minNumberOfDecimals, int maxNumberOfDecimals)
 {
  return formatDouble(value, minNumberOfDecimals, maxNumberOfDecimals, null);
 }

 public static String formatDouble(double value, int minNumberOfDecimals, int maxNumberOfDecimals, Locale loc)
 {
  return formatDouble(value, minNumberOfDecimals, maxNumberOfDecimals, loc, true, true);
 }


 public static String formatDouble(double value, int minNumberOfDecimals, int maxNumberOfDecimals, Locale loc, boolean grouping)
 {
  return formatDouble(value, minNumberOfDecimals, maxNumberOfDecimals, loc, grouping, true);
 }

 public static String formatDouble(double value, int minNumberOfDecimals, int maxNumberOfDecimals, Locale loc, boolean grouping, boolean returnPlainZeroWhenIsZero)
 {
  NumberFormat f;

  if (returnPlainZeroWhenIsZero && value==0) return "0";

  if (loc!=null) f=NumberFormat.getInstance(loc);
  else f=NumberFormat.getInstance();

  if (f instanceof DecimalFormat)
  {
   f.setGroupingUsed(grouping);

   if (minNumberOfDecimals>0) ((DecimalFormat)f).setDecimalSeparatorAlwaysShown(true);

   if (minNumberOfDecimals>=0) f.setMinimumFractionDigits(minNumberOfDecimals);
   else f.setMinimumFractionDigits(0);

   if (maxNumberOfDecimals>=0) f.setMaximumFractionDigits(maxNumberOfDecimals);
   else f.setMaximumFractionDigits(Integer.MAX_VALUE);
  }

  return f.format(value);
 }


 public static String speedFormatPrice(double amount, String currencySymbol)
 {
  return currencySymbol+" "+formatDouble(amount, 2, 2, Locale.US, true, true);
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Date/Calendar

 public static final String me_as_DateFormat="EEEEEEEEEEEEEEEEEEE d MMMMMMMMMMMMMMMMM yyyy, H:mm.ss";
 public static final String fileSystemCompatibleDateTimeFormat="yyyy_MM_dd-HH_mm_ss";
 public static final String speedDateFormat="HH.mm.ss dd/MMM/yyyy";
 public static final String speedDateFormatForFileNames="dd-MMM-yyyy-HH.mm.ss";
 public static final SimpleDateFormat c2s2cDF=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSSS");

 public static final SimpleDateFormat hmDF=new SimpleDateFormat("HH:mm");
 public static final SimpleDateFormat moreSimpleDF=new SimpleDateFormat("dd/MMM/yyyy HH:mm");
 public static final SimpleDateFormat simpleDF=new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
 public static final SimpleDateFormat dmyDF=new SimpleDateFormat("dd_MM_yyyy");
 public static final SimpleDateFormat dmyDF2=new SimpleDateFormat("dd/MM/yyyy");
 public static final SimpleDateFormat dmyDF3=new SimpleDateFormat("EEEE dd/MM/yyyy");
 public static final SimpleDateFormat dmyDF4=new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm");
 public static final SimpleDateFormat dmyDF5=new SimpleDateFormat("dd/MM/yyyy HH:mm");
 public static final SimpleDateFormat dmyDF6=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 public static final SimpleDateFormat RSS_pubDate=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
 public static final SimpleDateFormat httpTimeFormat=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);


 public static String messageNow(String message)
 {
  return "["+calendar2String(CalendarExtras.now(), simpleDF)+"] "+message;
 }

 public static String getLocalizedMonthName(int month)
 {
  return getLocalizedMonthName(month, Locale.getDefault());
 }


 public static String getLocalizedMonthName(int month, String language)
 {
  return getLocalizedMonthName(month, new Locale(language));
 }



 public static String getLocalizedMonthName(int month, Locale locale)
 {
  Calendar monday=
   CalendarExtras.setHourMinutesSecondsAndMillis(CalendarExtras.newDay(2011, 0, 14), 1, 0, 0, 0);

  //  System.out.println(speedDateToString(monday));

  monday.add(Calendar.MONTH, month);

  SimpleDateFormat dowDF=new SimpleDateFormat("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM", locale);
  String res=dowDF.format(monday.getTime());

  return res;
 }




 public static String getLocalizedDayOfWeek(int dayOfWeek)
 {
  return getLocalizedDayOfWeek(dayOfWeek, Locale.getDefault());
 }


 public static String getLocalizedDayOfWeek(int dayOfWeek, String language)
 {
  return getLocalizedDayOfWeek(dayOfWeek, new Locale(language));
 }



 /**
  *
  *
  * @param dayOfWeek  0 = monday
  * @param locale
  * @return
  */
 public static String getLocalizedDayOfWeek(int dayOfWeek, Locale locale)
 {
  Calendar monday=
   CalendarExtras.setHourMinutesSecondsAndMillis(CalendarExtras.newDay(2011, 2, 14), 1, 0, 0, 0);

//  System.out.println(speedDateToString(monday));

  monday.add(Calendar.DAY_OF_YEAR, dayOfWeek);

  SimpleDateFormat dowDF=new SimpleDateFormat("EEEEEEEEEEEEEEEEEEEEEEEEEEEEE", locale);
  String res=dowDF.format(monday.getTime());

  return res;
 }



 public static String speedDateToString(Calendar d, String format)
 {
  return speedDateToString(d.getTime(), format);
 }

 public static String speedDateToString(Calendar d)
 {
  return speedDateToString(d.getTime());
 }

 public static Calendar string2Calendar(String time, SimpleDateFormat sdf)
 {
  return string2Calendar(time, sdf, false);
 }


 public static Calendar string2Calendar(String time, SimpleDateFormat sdf, boolean prevDayOnNoExists)
 {
  Calendar res=null;

  if (sdf!=null && isNotBlank(time))
  {
   synchronized (sdf)
   {
    try
    {
     res=Calendar.getInstance();
     res.setTime(sdf.parse(time));

     if (prevDayOnNoExists)
     {
      String verif=calendar2String(res, sdf);
      if (!areEqual(verif, time))
      {
       res.add(Calendar.DAY_OF_YEAR, -1);
      }
     }
    }
    catch (Throwable tr)
    {
     res=null;
    }
   }
  }

  return res;
 }

 public static Calendar string2Calendar(String time)
 {
  return string2Calendar(time, c2s2cDF);
 }


 public static String calendar2String(Calendar time, String dateFormat)
 {
  return calendar2String(time, new SimpleDateFormat(dateFormat));
 }


 public static String calendar2String(Calendar time, SimpleDateFormat sdf)
 {
  String res=null;

  if (time!=null && sdf!=null)
  {
   synchronized (sdf)
   {
    synchronized (time)
    {
     res=sdf.format(time.getTime());
    }
   }
  }

  return res;
 }


 public static String calendar2String(Calendar time)
 {
  return calendar2String(time, c2s2cDF);
 }








 public static String speedDateToString(Date dt, String format)
 {
  SimpleDateFormat formatter=new SimpleDateFormat(format, Locale.ENGLISH);
  return formatter.format(dt);
 }


 public static String speedDateToString(Date dt)
 {
  return speedDateToString(dt, speedDateFormat);
 }


 public static String speedNowToString(String format)
 {
  return speedDateToString(Calendar.getInstance().getTime(), format);
 }


 public static String speedNowToString()
 {
  return speedDateToString(Calendar.getInstance().getTime());
 }


/*
 public static void main(String args[])
 {
  Date d=new Date();
  String sss=speedDateToString(d, me_as_DateFormat);
  logOut.println(speedStringToDate(sss, me_as_DateFormat));
 }
*/

 public static Date speedStringToDate(String dt)
 {
  Date res=null;

  try
  {
   SimpleDateFormat formatter=new SimpleDateFormat(speedDateFormat);
   res=formatter.parse(dt);
  } catch (Throwable ignore){}

  return res;
 }


 public static Date speedStringToDate(String dt, String format)
 {
  Date res=null;

  try
  {
   SimpleDateFormat formatter=new SimpleDateFormat(format, Locale.ENGLISH);
   res=formatter.parse(dt);
  }
  catch (Throwable ignore){}

  return res;
 }


 public static String getRelevantLiteralTimeAmount(long millis)
 {
  return getRelevantLiteralTimeAmount(millis, 3);
 }

 public static String getRelevantLiteralTimeAmount(long millis, int maxElems)
 {
  int sm[]=TimeCounter.splitMillis(millis);
  int years=sm[0];
  int months=sm[1];
  int weeks=sm[2];
  int days=sm[3];
  int hours=sm[4];
  int minutes=sm[5];
  int seconds=sm[6];
  int milliseconds=sm[7];

  StringBuilder sb=new StringBuilder();
  int added=0;

  if ((added>0 && added<maxElems) || (added==0 &&years>0)) {added++;sb.append(years);sb.append("y ");}
  if ((added>0 && added<maxElems) || (added==0 &&months>0)) {added++;sb.append(months);sb.append("Mo ");}
  if ((added>0 && added<maxElems) || (added==0 &&weeks>0)) {added++;sb.append(weeks);sb.append("w ");}
  if ((added>0 && added<maxElems) || (added==0 &&days>0)) {added++;sb.append(days);sb.append("d ");}
  if ((added>0 && added<maxElems) || (added==0 &&hours>0)) {added++;sb.append(hours);sb.append("h ");}
  if ((added>0 && added<maxElems) || (added==0 &&minutes>0)) {added++;sb.append(minutes);sb.append("m ");}
  if ((added>0 && added<maxElems) || (added==0 &&seconds>0)) {added++;sb.append(seconds);sb.append("s ");}
  if (added<maxElems) {sb.append(milliseconds);sb.append("ms ");}

  return sb.toString();
 }


 public static String getLiteralTimeAmount(long millis, boolean asShortestAsPossible)
 {
  int sm[]=TimeCounter.splitMillis(millis);
  int years=sm[0];
  int months=sm[1];
  int weeks=sm[2];
  int days=sm[3];
  int hours=sm[4];
  int minutes=sm[5];
  int seconds=sm[6];
  int milliseconds=sm[7];

  StringBuilder sb=new StringBuilder();
  boolean started=false;

  if (years>0 || !asShortestAsPossible) {started=true;sb.append(years);sb.append("y ");}
  if (started || months>0) {started=true;sb.append(months);sb.append("Mo ");}
  if (started || weeks>0) {started=true;sb.append(weeks);sb.append("w ");}
  if (started || days>0) {started=true;sb.append(days);sb.append("d ");}
  if (started || hours>0) {started=true;sb.append(hours);sb.append("h ");}
  if (started || minutes>0) {started=true;sb.append(minutes);sb.append("m ");}
  if (started || seconds>0) {sb.append(seconds);sb.append("s ");}

  sb.append(milliseconds);sb.append("ms");

  return sb.toString();
 }


 public static String getLiteralTimeAmount(long millis)
 {
  return getLiteralTimeAmount(millis, true);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Generate random and unique strings


 public static final String quickNotLettersNotDigits=" “”’!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~ ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿×".intern();
 public static final String unambiguousChars="2367abcdefhkpqrstwxyzACEFGHJKLPRSTWXYZ";
 public static final String hexChars="0123456789abcdef".intern();
 public static final String digits="0123456789".intern();
 public static final String lowerLetters="abcdefghijklmnopqrstuvwxyz".intern();
 public static final String lowerLettersDigits=(lowerLetters+digits).intern();
 public static final String upperLetters="ABCDEFGHIJKLMNOPQRSTUVWXYZ".intern();
 public static final String letters=(upperLetters+lowerLetters).intern();
 public static final String lettersDigits=(letters+digits).intern();
 public static final String otherCharsOkForPasswords="!#%&()\\/*+-.:;<=>?@[]{},".intern();
 public static final String lettersDigitsUnderscore=(lettersDigits+"_").intern();

 public static final String charsOkForPasswords=(lettersDigitsUnderscore+otherCharsOkForPasswords).intern();
 public static final String fileSystemSecureSymbols=(digits+lowerLetters).intern();

 public static final String okCharsForIdentifiers;
 static
 {
  int t, len=257;
  char ch;
  StringBuilder sb=new StringBuilder();

  for (t=0;t<len;t++)
  {
   ch=(char)t;
   if (Character.isUnicodeIdentifierPart(ch))
   {
    String tmp=String.valueOf(ch);
    String deac=deAccentize(tmp);

    if (lettersDigitsUnderscore.indexOf(ch)>=0 ||
        (length(deac)==1 &&
         lettersDigitsUnderscore.indexOf(deac.charAt(0))>=0)) sb.append(ch);
   }
  }

  okCharsForIdentifiers=sb.toString();
 }


 /**
  *
  * Seems speed enough: less than 3 seconds for 4 milion calls!!!
  *
  */
 public static String getQuickUniqueKey(Object... keys)
 {
  StringBuilder sb=new StringBuilder();
  int t, len=keys.length;

  for (t=0;t<len;t++)
  {
   if (keys[t]!=null)
   {
    if (keys[t] instanceof Calendar)
    {
     sb.append("millis=");
     sb.append(String.valueOf(((Calendar)keys[t]).getTimeInMillis()));
    }
    else
    {
     if (isArray(keys[t]))
     {
      sb.append("[").append(getQuickUniqueKey((Object[])keys[t])).append("]");
     } else sb.append(keys[t].toString());
    }

    sb.append(":");
   } else sb.append("!:");
  }

  return sb.toString();
 }

 public static String getQuickHashedUniqueKey(Object... keys)
 {
  String res=getQuickUniqueKey(keys);
  int hc=res.hashCode();

  if (hc<0)
   res="n"+((-1)*hc);
  else
   res="p"+hc;

  return res;
 }



 public static String generateQuickMaybeUniqueString(int length, Map<String, ?> avoidThese)
 {
  String res;

  do
  {
   res=generateQuickMaybeUniqueString(length);
   if (avoidThese.get(res)==null) break;
  } while (true);

  return res;
 }

 public static String generateQuickMaybeUniqueString(int length)
 {
  return interweave(String.valueOf(System.currentTimeMillis()), generateRandomString(length)).substring(0, length);
 }




 public static String generateRandomString(int length)
 {
  StringBuilder res=new StringBuilder();
  int lduLen=lettersDigits.length()-1;

  do
  {
   int idx=intRandom(0, lduLen);
   res.append(lettersDigits.charAt(idx));

  } while (res.length()<length);

  return res.toString();
 }






 public static String generateRandomPassword(int len)
 {
  return generateRandomPassword(len, false);
 }

 public static String generateRandomPassword(int len, boolean easy)
 {
  return generateRandomPassword(len, (easy ? unambiguousChars : charsOkForPasswords));
 }


 public static String generateRandomPassword(int len, String allowedChars)
 {
  StringBuilder sb=new StringBuilder();
  Random rnd=RandomExtras.newRandomNumberGenerator();
  int allowedCharsLen=allowedChars.length();

  for (int t=0;t<len;t++)
  {
   sb.append(allowedChars.charAt(rnd.nextInt(allowedCharsLen)));
  }

  return sb.toString();
 }





 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Matching with both Glob and RegEx


 /**
  * Converts a standard POSIX Shell globbing pattern into a regular expression
  * pattern. The result can be used with the standard {@link java.util.regex} API to
  * recognize strings which match the glob pattern.
  *
  * See also, the POSIX Shell language:
  * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
  *
  * @param pattern A glob pattern.
  * @return A regex pattern to recognize the given glob pattern.
  */
 public static String globToRegex(String pattern)
 {
  StringBuilder sb=new StringBuilder(pattern.length());
  int inGroup=0;
  int inClass=0;
  int firstIndexInClass=-1;
  char[] arr=pattern.toCharArray();
  for (int i=0;i<arr.length;i++)
  {
   char ch=arr[i];
   switch (ch)
   {
    case '\\':
     if (++i >= arr.length)
     {
      sb.append("\\\\");
     }
     else
     {
      char next=arr[i];
      switch (next)
      {
       case ',':
        // escape not needed
        break;
       case 'Q':
       case 'E':
        // extra escape needed
//        sb.append('\\');
       default:
        sb.append("\\\\");
      }

      if (regexSpecialChars.contains(""+next))
      {
       i--;
      }
      else
       sb.append(next);
     }
     break;
    case '*':
     if (inClass==0)
      sb.append(".*");
     else
      sb.append('*');
     break;
    case '?':
     if (inClass==0)
      sb.append('.');
     else
      sb.append('?');
     break;
    case '[':
     inClass++;
     firstIndexInClass=i+1;
     sb.append('[');
     break;
    case ']':
     inClass--;
     sb.append(']');
     break;
    case '.':
    case '(':
    case ')':
    case '+':
    case '|':
    case '^':
    case '$':
    case '@':
    case '%':
     if (inClass==0 || (firstIndexInClass==i && ch=='^'))
     { sb.append('\\'); }
     sb.append(ch);
     break;
    case '!':
     if (firstIndexInClass==i)
      sb.append('^');
     else
      sb.append('!');
     break;
    case '{':
     inGroup++;
     sb.append('(');
     break;
    case '}':
     inGroup--;
     sb.append(')');
     break;
    case ',':
     if (inGroup>0)
      sb.append('|');
     else
      sb.append(',');
     break;
    default:
     sb.append(ch);
   }
  }

  return sb.toString();
 }


/*
 public static void main(String args[])
 {
  String baudo="c:\\disk\\E\\pippo\\Q\\baudo";
  String reg=globToRegex("c:\\disk\\E\\pippo\\Q*");

  boolean res=baudo.matches(reg);

  logOut.println(res);
 }
*/







 /**
  * doTheyMatch:
  * doTheyMatch compares 'test' with 'match_mask' and returns true if they
  * matches.
  * 'match_mask' should be a Glob Pattern
  *
  *
  */
 public static boolean doTheyMatch(String test, String match_mask, boolean caseSensitive)
 {
  boolean res=false;
  char jolly='*';
  char charRep='?';

  if (!hasChars(match_mask)) return true;

  if (areEqual(match_mask, String.valueOf(jolly))) return true;

  if (containsOnlyThoseChars(match_mask, String.valueOf(charRep)))
  {
   return (length(test)==length(match_mask));
  }

  String regexPattern=globToRegex(match_mask);

  if (areEqual(regexPattern, match_mask))
  {
   if (!caseSensitive)
   {
    test=toLowerCase(test);
    match_mask=toLowerCase(match_mask);
   }

   return areEqual(test, match_mask);
  }
  else
   regexPattern="^"+regexPattern+"$";

  int flags=Pattern.MULTILINE;

  if (!caseSensitive)
   flags|=Pattern.CASE_INSENSITIVE;

  return doTheyRegexMatch(test, regexPattern, flags);
 }



 public static boolean doTheyMatch(String test, String match_mask)
 {
  return doTheyMatch(test, match_mask, true);
 }



 /**
  * WARNING: passing a null (or empty) array for match_masks this method returns 'true'
  */
 public static boolean doTheyMatch(String test, String match_masks[], boolean caseSensitive)
 {
  if (ArrayExtras.length(match_masks)==0) return true;
  return (getWhichMaskMatches(test, match_masks, caseSensitive)>=0);
 }


 public static int getWhichMaskMatches(String test, String match_masks[], boolean caseSensitive)
 {
  int t, len=ArrayExtras.length(match_masks);
  int res=-1;

  for (t=0;t<len && res==-1;t++)
  {
   if (doTheyMatch(test, match_masks[t], caseSensitive))
   {
    res=t;
   }
  }

  return res;
 }



 public static boolean doTheyRegexMatch(String stringTolookInside, String regexPattern)
 {
  return doTheyRegexMatch(stringTolookInside, regexPattern, Pattern.MULTILINE);
 }

 public static boolean doTheyRegexMatch(String stringTolookInside, String regexPattern, int regexFlags)
 {
  return (getFirstRegexMatch(stringTolookInside, regexPattern, regexFlags)!=null);
 }

 public static String getFirstRegexMatch(String stringTolookInside, String regexPattern)
 {
  return getFirstRegexMatch(stringTolookInside, regexPattern, Pattern.MULTILINE);
 }

 public static String getFirstRegexMatch(String stringTolookInside, String regexPattern, int regexFlags)
 {
  String res=null;
  Pattern r=Pattern.compile(regexPattern, regexFlags);

  // Now create matcher object.
  Matcher m=r.matcher(stringTolookInside);

  if (m.find())
  {
   res=m.group(0);
  }

  return res;
 }

 public static StringMatch getFirstRegexMatchEx(String stringTolookInside, String regexPattern)
 {
  StringMatch res=null;
  Pattern r=Pattern.compile(regexPattern, Pattern.MULTILINE);

  // Now create matcher object.
  Matcher m=r.matcher(stringTolookInside);

  if (m.find())
  {
   res=new StringMatch();
   res.source=stringTolookInside;
   res.matchStart=m.start(0);
   res.matchEnd=m.end(0);
   res.match=stringTolookInside.substring(res.matchStart, res.matchEnd);
  }

  return res;
 }


 public static List<String> getRegexMatches(String stringTolookInside, String regexPattern)
 {
  List<String> res=new ArrayList<>();
  Pattern r=Pattern.compile(regexPattern, Pattern.MULTILINE);

  // Now create matcher object.
  Matcher m=r.matcher(stringTolookInside);

  while (m.find())
  {
   res.add(stringTolookInside.substring(m.start(), m.end()));
  }

  return res;
 }


 public static List<StringMatch> getRegexMatchesEx(String stringTolookInside, String regexPattern)
 {
  return getRegexMatchesEx(stringTolookInside, regexPattern, null);
 }

 public static List<StringMatch> getRegexMatchesEx(String stringTolookInside, String regexPattern, List<StringMatch> addToThis)
 {
  List<StringMatch> res=((addToThis!=null) ? addToThis:new ArrayList<>());
  Pattern r=Pattern.compile(regexPattern, Pattern.MULTILINE);

  // Now create matcher object.
  Matcher m=r.matcher(stringTolookInside);

  while (m.find())
  {
   StringMatch sm=new StringMatch();
   sm.source=stringTolookInside;
   sm.matchStart=m.start();
   sm.matchEnd=m.end();
   sm.match=stringTolookInside.substring(sm.matchStart, sm.matchEnd);
   res.add(sm);
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Clipboard

 public static String getFromSystemClipboard()
 {
  String res;

  try
  {
   Clipboard clipb=Toolkit.getDefaultToolkit().getSystemClipboard();
   Transferable tr=clipb.getContents(null);
   res=tr.getTransferData(DataFlavor.stringFlavor).toString();
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
   res=null;
  }

  return res;
 }


 public static void putInSystemClipboard(final String testo)
 {
  Clipboard clipb=Toolkit.getDefaultToolkit().getSystemClipboard();

  clipb.setContents
  (
   new Transferable()
   {
    DataFlavor df[]=new DataFlavor[]{DataFlavor.stringFlavor};

    public DataFlavor[] getTransferDataFlavors()
    {
     return df;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
     return flavor.equals(DataFlavor.stringFlavor);
    }

    public Object getTransferData(DataFlavor flavor)
    {
     return testo;
    }
   },
   new ClipboardOwner(){public void lostOwnership(Clipboard clipboard, Transferable contents){}}
  );
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // checking


 public static boolean endsWith(String test, String ends)
 {
  return (hasChars(test) && hasChars(ends) && test.endsWith(ends));
 }


 public static boolean endsWith(String test, String match_mask, char charRep, boolean caseSensitive)
 {
  boolean res;

  try
  {
   res=(test.length()-match_mask.length()==lastIndexOf(test, match_mask, charRep, caseSensitive));
  } catch (Throwable tr){res=false;}

  return res;
 }

 public static boolean startsWith(String test, String starts)
 {
  return (hasChars(test) && hasChars(starts) && test.startsWith(starts));
 }

 public static boolean startsWith(String test, String match_mask, char charRep, boolean caseSensitive)
 {
  return (indexOf(test, match_mask, charRep, caseSensitive)==0);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // normalizing


 public static Integer integerOrNull(CharSequence str)
 {
  Integer res;

  try
  {
   res=Integer.parseInt(str.toString());
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }



 public static String nonBlankOrNull(CharSequence s)
 {
  if (isNotBlank(s)) return s.toString();
  else return null;
 }

 public static String stringOrEmpty(CharSequence s)
 {
  if (hasChars(s)) return s.toString();
  else return "";
 }

 public static String stringOrThrow(CharSequence s)
 {
  if (hasChars(s)) return s.toString();
  throw new RuntimeException();
 }


 public static String nonBlankOrThat(CharSequence s, CharSequence that)
 {
  if (isNotBlank(s)) return s.toString();
  else return (that==null ? null : that.toString());
 }

 public static String stringOrThat(CharSequence s, CharSequence that)
 {
  if (hasChars(s)) return s.toString();
  else return (that==null ? null : that.toString());
 }

 public static String stringOrNull(CharSequence s)
 {
  return stringOrThat(s, null);
 }




 public static String[] stringsOrThose(String s[], String those[])
 {
  int t, len=ArrayExtras.length(s);

  for (t=0;t<len;t++)
  {
   if (hasChars(s[t])) return s;
  }

  return those;
 }


 public static String[] stringsOrNull(String s[])
 {
  return stringsOrThose(s, null);
 }



 public static int getNumberOfCharsOfSet(String str, String setOfChars)
 {
  int res=0;
  int t, len=length(str);

  for (t=0;t<len;t++)
  {
   if (setOfChars.indexOf(str.charAt(t))>=0) res++;
  }

  return res;
 }



 public static boolean isValidPassword(String password)
 {
  return isValidPassword(password, 5,
   new BoxFor2<>(1, digits), // at least one number
   new BoxFor2<>(1, letters) // at least one letter
  );
 }


 public static boolean isValidPassword(String password,
                                       int minAllowedLength,
                                       BoxFor2<Integer, String>... atLeastChars)
 {
  boolean res=(length(password)>=minAllowedLength);

  if (res)
  {
   int t, len=ArrayExtras.length(atLeastChars);

   for (t=0;t<len && res;t++)
   {
    res=(getNumberOfCharsOfSet(password, atLeastChars[t].element2)>=atLeastChars[t].element1);
   }
  }

  return res;
 }





 public static boolean isValidEmailAddress(String senderEmail)
 {
  boolean res=isNotBlank(senderEmail);

  if (res)
  {
   res=(getCharCount(senderEmail, '@')==1);
   if (res) res=senderEmail.matches(emailRegex);
   if (res) res=senderEmail.length()>4; // minimal email! At least 5 chars: a@b.c
  }

  return res;
 }






 public static boolean isValidNameAndSurname(String value)
 {
  value=betterTrim(value);

  if (stringOrEmpty(betterTrim(value)).length()<5)
  {
   return false;
  }
  else
  {
   String s[]=unmerge(value, ' ');

   if (ArrayExtras.length(s)<2)
   {
    return false;
   }
   else
   {
    try
    {
     if (!Character.isLetter(s[0].charAt(0))) return false;
     if (!Character.isLetter(s[1].charAt(0))) return false;
     return true;
    }
    catch (Throwable tr)
    {
     return false;
    }
   }
  }
 }




 public static boolean isValidPhoneNumber(String number, int minSize)
 {
  number=betterTrim(number);
  boolean res=(length(number)>=minSize);

  if (res)
  {
   res=containsOnlyThoseChars(number, digits+"(). +");
  }

  return res;
 }








 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Conversions


 private static final String _illegal_conv_arg="%f cuold not convert to '%t' the supplied 'str' (%s)";
 private static final String _illegal_conv_arg_phs[]=new String[]{"%f", "%t", "%s"};
 private static final String _bad_type_="StringExtras.$ called with invalid value type";



 public static Integer toIntOrNull(String str)
 {
  Integer res;

  try
  {
   res=Integer.parseInt(betterTrim(str));
  }
  catch (Throwable tr)
  {
   res=null;
  }

  return res;
 }



 public static Integer extractInt(String str)
 {
  String s[]=str.split(" ");
  int t, len=ArrayExtras.length(s);

  for (t=0;t<len;t++)
  {
   try
   {
    return toInt(s[t]);
   }
   catch (Throwable ignore)
   {
   }
  }

  throw new NumberFormatException("cannot extract an int from '"+str+"'");
 }


/*
 public static int extractInt(String str, int defaultValue)
 {
  int res;

  try
  {
   res=extractInt(str);
  }
  catch (Throwable tr)
  {
   res=defaultValue;
  }

  return res;
 }


 public static int extractInt(String str) throws NumberFormatException
 {
  int res;

  try
  {
   res=Integer.parseInt(str);
  }
  catch (Throwable ignore)
  {
   try
   {
    StringBuilder sb=new StringBuilder();
    char ch;
    int t, len=length(str);

    for (t=0;t<len;t++)
    {
     ch=str.charAt(t);
     if (t==0 && ch=='-' || ch=='+') sb.append(ch);
     else
     {
      if (Character.isDigit(ch)) sb.append(ch);
      else break;
     }
    }

    res=Integer.parseInt(sb.toString());
   }
   catch (Throwable tr)
   {
    throw new NumberFormatException("cannot extract an int from '"+str+"'");
   }
  }

  return res;
 }
*/


 public static Object toType(String value, Class<?> type)
 {
  Object res;

  switch (ArrayExtras.indexOf(Types.primitivesAndAlmostClasses, 0, type))
  {
   case  0 /* Byte.TYPE       */:
   case  1 /* Byte.class      */:res=toByte(value);break;
   case  2 /* Short.TYPE      */:
   case  3 /* Short.class     */:res=toShort(value);break;
   case  4 /* Integer.TYPE    */:
   case  5 /* Integer.class   */:res=toInt(value);break;
   case  6 /* Long.TYPE       */:
   case  7 /* Long.class      */:res=toLong(value);break;
   case  8 /* Character.TYPE  */:
   case  9 /* Character.class */:res=toChar(value);break;
   case 10 /* Float.TYPE      */:
   case 11 /* Float.class     */:res=toFloat(value);break;
   case 12 /* Double.TYPE     */:
   case 13 /* Double.class    */:res=toDouble(value);break;
   case 14 /* Boolean.TYPE    */:
   case 15 /* Boolean.class   */:res=toBoolean(value);break;
   case 16 /* String.class    */:res=value;break;


/*
   case 1 / *Byte.TYPE      * /               :res=toByte(value);break;
   case 2 / *Short.TYPE     * /               :res=toShort(value);break;
   case 3 / *Integer.TYPE   * /               :res=toInt(value);break;
   case 4 / *Long.TYPE      * /               :res=toLong(value);break;
   case 5 / *Character.TYPE * /               :res=toChar(value);break;
   case 6 / *Float.TYPE     * /               :res=toFloat(value);break;
   case 7 / *Double.TYPE    * /               :res=toDouble(value);break;
   case 8 / *Boolean.TYPE   * /               :res=toBoolean(value);break;
   case 9 / *String.class   * /               :res=value;break;
*/
   default:
   {
    if (type.isEnum())
    {
     res=Enum.valueOf((Class<Enum>)type, (String)value);
    }
    else
    {
     throw new RuntimeException("Cannot convert!");
    }
   }
  }

  return res;
 }


 public static byte toByte(String str) throws NumberFormatException
 {
  return Byte.parseByte(betterTrim(str));
 }

 public static short toShort(String str) throws NumberFormatException
 {
  return Short.parseShort(betterTrim(str));
 }



 public static int toInt(String str) throws NumberFormatException
 {
  return Integer.parseInt(betterTrim(str));
 }

 public static long toLong(String str) throws NumberFormatException
 {
  return Long.parseLong(betterTrim(str));
 }

 public static char toChar(String str) throws IllegalArgumentException
 {
  int v=toInt(str);

  if (v<((int)Character.MIN_VALUE) || v>((int)Character.MAX_VALUE))
  {
   throw new IllegalArgumentException(
           replace(_illegal_conv_arg, _illegal_conv_arg_phs, new String[]{"toChar(String str)", "char", str}));
  } else return (char)v;
 }

 public static float toFloat(String str) throws NumberFormatException
 {
  return Float.parseFloat(betterTrim(str));
 }

 public static double toDouble(String str) throws NumberFormatException
 {
  return MathExtras.toDouble(betterTrim(str));
  //return Double.parseDouble(str);
 }

 public static boolean toBoolean(String str) throws IllegalArgumentException
 {
  if (isBlank(str)) throw new IllegalArgumentException("argumetn isBlank!");

  boolean res=false;
  str=betterTrim(str);

  if (isNotBlank(str))
  {
   try
   {
    str=str.trim();
    if (select(considerableTrue, str, SELECT_CASE_INSENSITIVE)>=0)
    {
     res=true;
    }
    else
    {
     if (select(considerableFalse, str, SELECT_CASE_INSENSITIVE)>=0)
     {
      res=false;
     }
     else
     {
      res=(toInt(str)!=0);
     }
    }
   }
   catch (Throwable tr)
   {
    throw new IllegalArgumentException(
            replace(_illegal_conv_arg, _illegal_conv_arg_phs, new String[]{"toBoolean(String str)", "boolean", str}));
   }
  }

  return res;
 }




 public static byte[] toBytes(String str[]) throws NumberFormatException
 {
  byte res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new byte[len];
   for (t=0;t<len;t++){res[t]=toByte(str[t]);}
  }

  return res;
 }


 public static short[] toShorts(String str[]) throws NumberFormatException
 {
  short res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new short[len];
   for (t=0;t<len;t++){res[t]=toShort(str[t]);}
  }

  return res;
 }


 public static int[] toInts(String str[]) throws NumberFormatException
 {
  int res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new int[len];
   for (t=0;t<len;t++){res[t]=toInt(str[t]);}
  }

  return res;
 }


 public static long[] toLongs(String str[]) throws NumberFormatException
 {
  long res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new long[len];
   for (t=0;t<len;t++){res[t]=toLong(str[t]);}
  }

  return res;
 }


 public static char[] toChars(String str[]) throws IllegalArgumentException
 {
  char res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new char[len];
   for (t=0;t<len;t++){res[t]=toChar(str[t]);}
  }

  return res;
 }


 public static float[] toFloats(String str[]) throws NumberFormatException
 {
  float res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new float[len];
   for (t=0;t<len;t++){res[t]=toFloat(str[t]);}
  }

  return res;
 }


 public static double[] toDoubles(String str[]) throws NumberFormatException
 {
  double res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new double[len];
   for (t=0;t<len;t++){res[t]=MathExtras.toDouble(str[t]);}
  }

  return res;
 }

 public static double[] toDoubles(ArrayList<String> str) throws NumberFormatException
 {
  double res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new double[len];
   for (t=0;t<len;t++){res[t]=MathExtras.toDouble(str.get(t));}
  }

  return res;
 }



 public static double[] speedToDoubles(ArrayList<String> str) throws NumberFormatException
 {
  double res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new double[len];
   for (t=0;t<len;t++){res[t]=Double.parseDouble(str.get(t));}
  }

  return res;
 }




 public static boolean[] toBooleans(String str[]) throws IllegalArgumentException
 {
  boolean res[]=null;
  int t, len=ArrayExtras.length(str);

  if (len>0)
  {
   res=new boolean[len];
   for (t=0;t<len;t++){res[t]=toBoolean(str[t]);}
  }

  return res;
 }





 public static boolean isByte(String str)
 {
  try
  {
   toByte(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isShort(String str)
 {
  try
  {
   toShort(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isInt(String str)
 {
  try
  {
   toInt(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isLong(String str)
 {
  try
  {
   toLong(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isChar(String str)
 {
  try
  {
   toChar(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isFloat(String str)
 {
  try
  {
   toFloat(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isDouble(String str)
 {
  try
  {
   toDouble(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }


 public static boolean isBoolean(String str)
 {
  try
  {
   toBoolean(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }



 public static boolean areBytes(String str[])
 {
  try
  {
   toBytes(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areShorts(String str[])
 {
  try
  {
   toShorts(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areInts(String str[])
 {
  try
  {
   toInts(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areLongs(String str[])
 {
  try
  {
   toLongs(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areChars(String str[])
 {
  try
  {
   toChars(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areFloats(String str[])
 {
  try
  {
   toFloats(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areDoubles(String str[])
 {
  try
  {
   toDoubles(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }

 public static boolean areBooleans(String str[])
 {
  try
  {
   toBooleans(str);
   return true;
  }
  catch (Throwable tr)
  {
   return false;
  }
 }





 private static String _getStringValue(Object value, int idx)
 {
  String res;

  switch (ArrayExtras.select(Types.classes, value.getClass()))
  {
   case 11 /* byte[].class    */:res=String.valueOf(((byte[])value)[idx]);break;
   case 12 /* short[].class   */:res=String.valueOf(((short[])value)[idx]);break;
   case 13 /* int[].class     */:res=String.valueOf(((int[])value)[idx]);break;
   case 14 /* long[].class    */:res=String.valueOf(((long[])value)[idx]);break;
   case 15 /* char[].class    */:res=String.valueOf(((char[])value)[idx]);break;
   case 16 /* float[].class   */:res=String.valueOf(((float[])value)[idx]);break;
   case 17 /* double[].class  */:res=String.valueOf(((double[])value)[idx]);break;
   case 18 /* boolean[].class */:res=String.valueOf(((boolean[])value)[idx]);break;
   default:throw new RuntimeException(replace(_bad_type_, "$", "_getStringValue"));
  }

  return res;
 }

 public static String primitiveArraytoString(Object value)
 {
  return primitiveArraytoString(value, ",");
 }

 public static String primitiveArraytoString(Object value, String separator)
 {
  String res=null;
  int t, len=ArrayExtras.length(value);

  if (len>0)
  {
   StringBuilder sb=new StringBuilder();

   for (t=0;t<len;t++)
   {
    if (t>0) sb.append(separator);
    sb.append(_getStringValue(value, t));
   }

   res=sb.toString();
  }

  return res;
 }


 public static String toString(Object o)
 {
  String res=null;

  if (o!=null)
  {
   if (isArray(o))
   {
    if (isArrayOfPrimitive(o))
    {
     primitiveArraytoString(o);
    }
    else
    {
     res=toString((Object[])o);
    }
   }
   else
   {
    res=o.toString();
   }
  }

  return res;
 }



 public static String toString(byte values[]){return primitiveArraytoString(values);}
 public static String toString(short values[]){return primitiveArraytoString(values);}
 public static String toString(int values[]){return primitiveArraytoString(values);}
 public static String toString(long values[]){return primitiveArraytoString(values);}
 public static String toString(char values[]){return primitiveArraytoString(values);}
 public static String toString(float values[]){return primitiveArraytoString(values);}
 public static String toString(double values[]){return primitiveArraytoString(values);}
 public static String toString(boolean values[]){return primitiveArraytoString(values);}
 public static String toString(String values[]){return toString(values, ",");}
 public static String toString(Object values[]){return toString(values, ",");}

 public static String toString(byte values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(short values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(int values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(long values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(char values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(float values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(double values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(boolean values[], String separator){return primitiveArraytoString(values, separator);}
 public static String toString(String values[], String separator)
 {
  String res=mergeEnclosing(values, separator, null);
  if (length(res)>separator.length()) res=res.substring(separator.length());
  return res;
 }

 public static String toString(Object values[], String separator)
 {
  return toString(ArrayExtras.toArrayOfStrings(values), separator);
 }



 public static int getFirstEnclosedInteger(String str) throws NumberFormatException
 {
  int res;
  int len=length(str);

  if (len>0)
  {
   try
   {
    res=Integer.parseInt(str);
   }
   catch (Throwable tr)
   {
    StringBuilder sb=new StringBuilder();
    boolean goon=true;
    boolean firstDigitFound=false;
    char ch;
    int t;

    for (t=0;t<len && goon;t++)
    {
     ch=str.charAt(t);

     if (Character.isDigit(ch))
     {
      sb.append(ch);
      firstDigitFound=true;
     }
     else
     {
      if (firstDigitFound) goon=false;
     }
    }

    try
    {
     res=getFirstEnclosedInteger(sb.toString());
    }
    catch (Throwable tr2)
    {
     throw new NumberFormatException("The passed string does not contain any integer");
    }
   }
  } else throw new NumberFormatException("The passed string is null or empty");

  return res;
 }


 /**
  * THIS IS SLOW (I think, I'm not sure, but honestly I think).
  * Use this only when you need json just a very few times to justify the adding of Jackson or Gson.
  *
  * @param jsonString
  * @return
  */
 public static Map<String, Object> parseJson(String jsonString)
 {
  Map map;
  try
  {
   ScriptEngine se=newJavaScriptEngine();
   se.put("theScript", jsonString);
   map=(Map)se.eval("JSON.parse(theScript)");
  }
  catch (Throwable tr)
  {
   System.err.println("OFFENDING JSON: \n"+jsonString+"\n");
   throw new RuntimeException(tr);
  }

  HashMap<String, Object> hmkeysValues=new HashMap<>();

  for (Object k : map.keySet())
  {
   hmkeysValues.put(k.toString(), map.get(k));
  }

  return hmkeysValues;
 }



 public static HashMap<String, String> quickMap(String... keysValues)
 {
  HashMap<String, String> res=new HashMap<>();
  int t, len=ArrayExtras.length(keysValues);

  if (len>0)
  {
   if (MathExtras.isOdd(len)) throw new RuntimeException("Numbner of keys is different from number of values!!!");

   for (t=0;t<len;t+=2)
   {
    res.put(keysValues[t], keysValues[t+1]);
   }
  }

  return res;
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // removing chars

 public static String removeChars(String str, String charsToRemove)
 {
  String res;

  if (hasChars(str) && hasChars(charsToRemove))
  {
   StringBuilder sb=new StringBuilder();
   char ch;
   int t, len=length(str);

   for (t=0;t<len;t++)
   {
    ch=str.charAt(t);
    if (charsToRemove.indexOf(ch)<0) sb.append(ch);
   }

   res=sb.toString();
  } else res=str;

  return res;
 }



 public static String[] removeChars(String str[], String charsToRemove)
 {
  int t, len=ArrayExtras.length(str);
  for (t=0;t<len;t++) str[t]=removeChars(str[t], charsToRemove);
  return str;
 }



 public static String[] removeChars(String str[])
 {
  int t, len=ArrayExtras.length(str);
  for (t=0;t<len;t++) str[t]=removeTrimmables(str[t]);
  return str;
 }


 public static String removeTrimmables(String str)
 {
  return removeChars(str, trimmable);
 }




 public static String removeLastChars(String str, int howManyChars)
 {
  return str.substring(0, str.length()-howManyChars);
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Captcha



 public static boolean isEqualToCaptcha(String str, String captchaString, boolean severeVerify)
 {
  boolean res=false;

  if (severeVerify) res=areEqual(str, captchaString);
  else res=areEqual(removeTrimmables(str), removeTrimmables(captchaString), false);

  return res;
 }





 public static String generateCaptchaString(int len)
 {
  StringBuilder sb=new StringBuilder();
  Random rnd=RandomExtras.newRandomNumberGenerator();
  int length=unambiguousChars.length();

  for (int t=0;t<len;t++)
  {
   sb.append(unambiguousChars.charAt(rnd.nextInt(length)));
  }

  return sb.toString();
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Sort

 // types of sorts for 'public static String[] sort(String items[], int sortType)'
 public static final int GSCAT_BYINT         = 1;
 public static final int GSCAT_BYENCLOSEDINT = 2;
 public static final int GSCAT_BYSTRING      = 3;
 public static final int GSCAT_BYLENGTH      = 4;



 public static String[] sort(String items[])
 {
  return QuickSortExtras.sort(items);
 }



 /**
  *
  * @param items
  * @param sortType this can be:<br>
  * GSCAT_BYINT:<br>
  * Every children has a name parsable as an int. Children are sorted according to these int values<br><br>
  *
  * GSCAT_BYENCLOSEDINT:<br>
  * Every children has a name that include an int. Children are sorted according to these int values.<br>
  * Valid samples are:<br>
  * '[100] my name' -> would be considered as 100 while sorting by int<br>
  * '25 - a name' -> would be considered as 25 while sorting by int<br>
  * '123 321 124' -> would be considered as 123 while sorting by int<br>
  * 'You are 73 years old' -> would be considered as 73 while sorting by int<br>
  * 'His age is 12' -> would be considered as 12 while sorting by int<br>
  * 'We have 12,234 dollars in the cash' -> would be considered as 12 while sorting by int<br><br>
  *
  * GSCAT_BYSTRING:<br>
  * Children are sorted by their string values according to the current charset<br><br>
  *
  * @return the same array of strings but sorted
  */
 public static String[] sort(String items[], int sortType)
 {
  int len=ArrayExtras.length(items);

  if (len>0)
  {
   switch (sortType)
   {
    case GSCAT_BYINT:
    {
     items=QuickSortExtras.sortStrictIntegersInStrings(items);
    } break;

    case GSCAT_BYENCLOSEDINT:
    {
     items=QuickSortExtras.sortIntegersEnclosedInStrings(items);
    } break;

    case GSCAT_BYSTRING:
    {
     items=sort(items);
    } break;

    case GSCAT_BYLENGTH:
    {
     items=sortByLength(items);
    } break;

    default:throw new RuntimeException("StringExtras.sort(...) invoked with invalid sortType value (it was: "+sortType+")");
   }
  }

  return items;
 }




 public static String[] sortByLength(final String s[])
 {
  if (ArrayExtras.length(s)>1)
  {
   new QuickSort(new QSortable()
   {
    String mid;
    int midLen;

    public void setMid(int mididx, Object params)
    {
     mid=s[mididx];
     midLen=length(mid);
    }

    // must return:
    // <0 if elem1<mid
    // 0 if elem1==mid
    // >0 if elem1>mid
    public int compareToMid(int elem1, Object params)
    {
     int e1l=length(s[elem1]);
     if (e1l<midLen) return 1;
     if (e1l>midLen) return -1;
     return 0;
    }


    public boolean swap(int elem1, int elem2, Object params)
    {
     String swap=s[elem1];
     s[elem1]=s[elem2];
     s[elem2]=swap;
     return true;
    }
   }, 0, ArrayExtras.length(s)-1, null);
  }

  return s;
 }







 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Other



 public static String equallySeparate(String str, int sepLen, String separator)
 {
  if (hasChars(str))
  {
   StringBuilder separated;
   int  t1;
   int z;

   z=0;
   t1=str.length()-1;

   while (t1>=0)
   {
    z++;

    if (z==sepLen && t1!=0)
    {
     separated=new StringBuilder();
     separated.append(str, 0, t1);
     separated.append(separator);
     separated.append(str, t1, str.length());
     str=separated.toString();
     z=0;
    }

    t1--;
   }
  }

  return str;
 }



 /**
  * WARNING: null strings are REMOVED !
  */
 public static String[] discardDuplicatedStrings(String str[])
 {
  String res[]=null;

  if (ArrayExtras.length(str)>0)
  {
   ArrayList<String> v=new ArrayList<>();
   int t, len=str.length;

   for (t=0;t<len;t++)
   {
    if (str!=null)
    {
     if (!v.contains(str[t])) v.add(str[t]);
    }
   }

   res=v.toArray(new String[v.size()]);
  }

  return res;
 }


 public static String[] duplicate(String str[])
 {
  String res[]=null;

  if (str!=null)
  {
   int t, len=str.length;
   if (len>0)
   {
    res=new String[len];
    for (t=0;t<len;t++)
    {
     if (str[t]==null) res[t]=null;
     else res[t]=str[t];
    }
   } else res=new String[0];
  }

  return res;
 }



 public static String escapeChar(String str, char charToEscape, char escaperChar)
 {
  if (!hasChars(str)) return str;

  StringBuilder sb=new StringBuilder();
  int prevIdx=-1;
  int idx;

  do
  {
   idx=str.indexOf(charToEscape, prevIdx+1);
   if (prevIdx==-1) prevIdx=0;

   if (idx>=0)
   {
    if (idx>0) sb.append(str, prevIdx, idx);
    sb.append(escaperChar);
    prevIdx=idx;
   } else sb.append(str.substring(prevIdx));

  } while (idx>=0);

  return sb.toString();
 }


 public static String getAStringOfChar(int numOfChars, char theChar)
 {
  if (numOfChars==0) return "";
  StringBuilder sb=new StringBuilder();
  int t=0;

  while (t<numOfChars)
  {
   sb.append(theChar);
   t++;
  }

  return sb.toString();
 }


 public static String interweave(String... strings)
 {
  StringBuilder res=new StringBuilder();
  int t, len=ArrayExtras.length(strings);

  if (len>0)
  {
   boolean somethingAdded;
   int idx=0;

   do
   {
    somethingAdded=false;

    for (t=0;t<len;t++)
    {
     if (strings[t].length()>idx)
     {
      res.append(strings[t].charAt(idx));
      somethingAdded=true;
     }
    }

    idx++;
   } while (somethingAdded);
  }

  return res.toString();
 }


 public static String ellipsizeTooLong(String str, int maxLength)
 {
  if (length(str)>maxLength)
   str=str.substring(0, maxLength)+"...";

  return str;
 }


 public static String translateChars(String txt, String sourceChars, String destChars)
 {
  String res=txt;
  int len=length(txt);

  if (len>0)
  {
   StringBuilder sb=new StringBuilder();
   int t;

   for (t=0;t<len;t++)
   {
    sb.append(destChars.charAt(sourceChars.indexOf(txt.charAt(t))));
   }

   res=sb.toString();
  }

  return res;
 }




 public static String toBase64(String text)
 {
  if (!hasChars(text))
   return text;
  else
  {
   try
   {
    return Base64.getEncoder().encodeToString(text.getBytes(defaultCharsetName));
   }
   catch (Throwable tr)
   {
    throw new RuntimeException(tr);
   }
  }
 }


 public static String fromBase64(String base64)
 {
  return newAutoString(Base64.getDecoder().decode(base64), defaultCharsetName);
 }




 /**
  * Returns a string built with the passed bytes trying to detect the encoding of the bytes...
  *
  * @param b the bytes to be converted in a string
  * @return
  */
 public static String newAutoString(byte b[])
 {
  return newAutoString(b, defaultCharsetName);
 }

 public static String newAutoString(byte b[], int off, int len)
 {
  return newAutoString(copyInNew(b, off, len), defaultCharsetName);
 }

 public static String newAutoString(byte b[], int off, int len, String charsetName)
 {
  return newAutoString(copyInNew(b, off, len), charsetName);
 }

 public static String newAutoString(byte b[], String charsetName)
 {
  String res=null;

  if (b!=null && b.length>0)
  {
   int skipBom=0;
   String enc=null;

   if (b.length>2)
   {
    if (b[0]==-1 &&
        b[1]==-2)
    {
     enc="UTF-16LE";
     skipBom=2;
    }

    if (b[0]==-2 &&
        b[1]==-1 &&
        b[2]==-0)
    {
     enc="UTF-16BE";
     skipBom=2;
    }

    if (b[0]==-17 &&
        b[1]==-69 &&
        b[2]==-65)
    {
     enc="UTF-8";
     skipBom=3;
    }

    if (enc!=null)
    {
     try
     {
      res=new String(b, skipBom, b.length-skipBom, enc);
     }
     catch (Throwable tr)
     {
      throw new RuntimeException(tr);
     }
    }
   }

   if (enc==null)
   {
    try
    {
     res=new String(b, charsetName);
    }
    catch (Throwable tr)
    {
     throw new RuntimeException(tr);
    }
   }
  }

  return res;
 }


 public static String deAccentize(String str)
 {
  if (!isNotBlank(str)) return str;
  String nfdNormalizedString=Normalizer.normalize(str, Normalizer.Form.NFD);
  Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
  return pattern.matcher(nfdNormalizedString).replaceAll("");
 }


 public static String[] toBetterTrimmedNoBlankLines(CharSequence sequence)
 {
  return purgeNullsAndEmpties(betterTrimNl(toLines(sequence.toString())));
 }

 public static String[] toLines(CharSequence sequence)
 {
  String res[]=null;

  if (hasChars(sequence))
  {
   String str=sequence.toString();
   str=replace(str, "\r\n", "\n", false);
   str=replace(str, "\r", " ", false);
   if (hasChars(sequence)) res=unmerge(str, '\n');
  }

  return res;
 }


 public static String[] toTrimmedLines(String str)
 {
  return purgeNullsAndEmpties(betterTrim(toLines(str)));
 }


 // this method tests the length of 'str' and if it has more than 'len' chars truncate it
 // to let it have only 'len'chars.
 // If 'str' has less chars than 'len' than this method adds 'fillerChar' chars
 // to the right (or to the left if 'addFillerToRight'=false) of the string till str'll have
 // exactly 'len' chars
 public static String grantLength(String str, int len, char fillerChar, boolean addFillerToRight)
 {
  String res=str;

  if (!hasChars(res)) res="";

  int clen=res.length();

  if (clen>len) res=res.substring(0, len);
  else
  {
   StringBuilder sb=new StringBuilder();
   int t=0;

   while (t+clen<len) {sb.append(fillerChar);t++;}

   if (addFillerToRight) res+=sb.toString();
   else
   {
    sb.append(res);
    res=sb.toString();
   }
  }

  return res;
 }


 // same as grantLength but it doesn't truncate the string if it is bigger than len
 public static String grantMinLength(String str, int len, char fillerChar, boolean addFillerToRight)
 {
  String res=str;

  if (length(res)<len)
  {
   res=grantLength(str, len, fillerChar, addFillerToRight);
  }

  return res;
 }







 public static int getEndsWithIndex(String str, String probableEndings[])
 {
  return getEorSWithIndex(str, probableEndings, false);
 }

 public static int getStartsWithIndex(String str, String probableStartings[])
 {
  return getEorSWithIndex(str, probableStartings, true);
 }


 private static int getEorSWithIndex(String str, String probables[], boolean starts)
 {
  int res=-1;
  int len=ArrayExtras.length(probables);

  if (len>0 && hasChars(str))
  {
   int t;

   for (t=0;t<len && res==-1;t++)
   {
    if (starts)
    {
     if (str.startsWith(probables[t])) res=t;
    }
    else
    {
     if (str.endsWith(probables[t])) res=t;
    }
   }
  }

  return res;
 }




 public static int selectStartingWith(String str[], String compare)
 {
  int res=-1;

  if (hasChars(compare))
  {
   int t, len=ArrayExtras.length(str);

   for (t=0;t<len && res==-1;t++)
   {
    if (compare.startsWith(str[t])) res=t;
   }
  }

  return res;
 }


 public static int indexInAOfTheFirstOfBInA(String A[], String B[])
 {
  return indexInAOfTheFirstOfBInA(A, B, SELECT_NONE);
 }


 /**
  *
  * @param A
  * @param B
  * @param flag same as select(..., flag);
  * @return
  */
 public static int indexInAOfTheFirstOfBInA(String A[], String B[], int flag)
 {
  int res=-1;
  int fLen=ArrayExtras.length(A);
  int sLen=ArrayExtras.length(B);

  if (fLen>0 && sLen>0)
  {
   int t;
   for (t=0;t<sLen && res==-1;t++)
   {
    res=select(A, B[t], flag);
   }
  }

  return res;
 }



 public static boolean firstContainsAtLeastOneOfSecond(String first[], String second[])
 {
  return firstContainsAtLeastOneOfSecond(first, second, SELECT_NONE);
 }


 /**
  *
  * @param first
  * @param second
  * @param flag same as select(..., flag);
  * @return
  */
 public static boolean firstContainsAtLeastOneOfSecond(String first[], String second[], int flag)
 {
  return (indexInAOfTheFirstOfBInA(first, second, flag)>=0);
 }





 public static int findMatchingString(String str[], String matchMask)
 {
  return findMatchingString(str, matchMask, 0);
 }

 public static int findMatchingString(String str[], String matchMask, int startIdx)
 {
  int res=-1;

  try
  {
   int t=0;

   do
   {
    if (doTheyMatch(str[t].substring(startIdx), matchMask, false))
     res=t;
    else
     t++;

   } while (res==-1);

  } catch (Throwable ignored){}

  return res;
 }




 /**
  * WARNING: this is very simplicistic method, does not make a lot of things:
  * 1) does not evaluate escaped "
  * 2) works only when separator is , or ;
  *
  * @param csvText
  * @return
  */

 public static RamTable quickCsvToRamTable(String csvText, int... toLowerCaseCols)
 {
  return quickCsvToRamTable(csvText, false, toLowerCaseCols);
 }

 public static RamTable quickCsvToRamTable(String csvText, boolean skipFirstRow, int... toLowerCaseCols)
 {
  int comma=StringExtras.getCharCount(csvText, ',');
  int semicolon=StringExtras.getCharCount(csvText, ';');
  String splitChar=((comma>semicolon) ? "," : ";");

  String values[], csvTextLines[]=toTrimmedLines(csvText);
  RamTable res=new RamTable();
  int c, clen, t, len=ArrayExtras.length(csvTextLines);

  for (t=0;t<len;t++)
  {
   values=StringExtras.betterTrimNl(replace(csvTextLines[t].split(splitChar), "\"", null));
   clen=ArrayExtras.length(values);

   for (c=0;c<clen;c++)
   {
    res.setString(c, t, values[c]);
   }
  }

  if (skipFirstRow && res.getRowsCount()>0)
   res.cutRows(0, 1);

  len=ArrayExtras.length(toLowerCaseCols);

  for (t=0;t<len;t++)
  {
   c=toLowerCaseCols[t];
   int r, rows=res.getRowsCount();
   for (r=0;r<rows;r++) res.setString(c, r, res.getString(c, r).toLowerCase());
  }

  return res;
 }









 public static String[] purgeNullsAndEmpties(String strs[])
 {
  return purge(strs, true, true, false);
 }


 public static String[] purgeNullsAndEmptiesAndBlanks(String strs[])
 {
  return purge(strs, true, true, true);
 }


 public static String[] purge(String strs[], boolean nulls, boolean empties, boolean blanks)
 {
  int t, len=ArrayExtras.length(strs);

  if (len>0)
  {
   boolean isNull, isEmpty, isBlank;
   ArrayList l=new ArrayList();

   for (t=0;t<len;t++)
   {
    isNull=(strs[t]==null);
    isEmpty=(!hasChars(strs[t]));
    isBlank=isBlank(strs[t]);

    if ((!isNull || !nulls) &&
        (!isEmpty || !empties) &&
        (!isBlank || !blanks))
     l.add(strs[t]);
   }

   strs=ArrayExtras.toArrayOfStrings(l);
  }

  return strs;
 }

/*
 // purge success tests
 public static void main(String args[])
 {
  String ss[]=new String[]
  {
   "pippo",
   null,
   "",
   "  ",
   "ciro"
  };

  logOut.println(purge(ss, true, false, false));
  logOut.println("---------------------------");
  logOut.println(purge(ss, false, true, false));
  logOut.println("---------------------------");
  logOut.println(purge(ss, false, false, true));
  logOut.println("---------------------------");
  logOut.println(purge(ss, true, true, true));
  logOut.println("---------------------------");
  logOut.println(purge(ss, true, true, false));
 }
*/





 public static String purgeComments(String txt, String singleLineComment, String multiLineCommentBegin, String multiLineCommentEnd, String stringsDelimiters, Character stringsDelimitersEscapeChar)
 {
  return TextCommentsPurger.purgeComments(txt, singleLineComment, multiLineCommentBegin, multiLineCommentEnd, stringsDelimiters, stringsDelimitersEscapeChar);
 }

 public static String purgeCommentsFromCLikeLanguageSource(String cLikeLanguageSource)
 {
  return TextCommentsPurger.purgeComments(cLikeLanguageSource, "//", "/*", "*/", "'\"`", '\\');
 }




 public static String purgeHtmlComments(String htmlCode)
 {
  return purgeHtmlComments(htmlCode, false);
 }


 public static String purgeHtmlComments(String htmlCode, boolean threeMinusVersion)
 {
  String core="\\s*?.*?[\\s\\S]*?";
  String whitespace="[\\r|\\n|\\s]*?";
  String regexPattern=whitespace+((threeMinusVersion) ? "<!---"+core+"--->" : "<!--"+core+"-->")+whitespace;
  List<StringMatch> matches=getRegexMatchesEx(htmlCode, regexPattern);

  for (StringMatch sm : matches)
  {
   htmlCode=replaceAll(htmlCode, sm.match, null);
  }

  return htmlCode;
 }


}
