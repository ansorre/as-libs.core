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


import me.as.lib.core.math.MathExtras;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static me.as.lib.core.lang.StringExtras.*;
import static me.as.lib.core.lang.TextCommentsPurger.Event.Type.*;
import static me.as.lib.core.lang.TextCommentsPurger.Event.getPreviousEventPosition;


/**
 *
 * Please note: methods in this class are usable by invoking the correspondent methods in StringExtras
 *
 */
class TextCommentsPurger
{
 static class Event
 {
  enum Type
  {
   singleLineCommentStart,
   multiLineCommentStart

  }

  Type type;
  int position;

  Event(Type type, int position)
  {
   this.type=type;
   this.position=position;
  }


  static int getPreviousEventPosition(Stack<Event> events, Type type)
  {
   int res=-1;
   int toPop=0;
   int t, len=events.size();

   for (t=len-1;t>=0;t--)
   {
    toPop++;
    Event event=events.get(t);
    if (event.type==type)
    {
     while (toPop>0)
     {
      toPop--;
      events.pop();
     }

     return event.position;
    }
   }

   return res;
  }

 }


 static class Strippable
 {
  int start;
  int end;

  Strippable(int start, int end)
  {
   this.start=start;
   this.end=end;
  }

 }



 /**
  *
  * This handles all the cases, even when comments are in strings, or when comments have no spaces around them....
  * it also handles nested comments
  *
  * @param source
  * @param singleLineComment
  * @param multiLineCommentBegin
  * @param multiLineCommentEnd
  * @param stringsDelimiters
  * @return
  */
 static String purgeComments(String source, String singleLineComment, String multiLineCommentBegin, String multiLineCommentEnd, String stringsDelimiters, Character _stringsDelimitersEscapeChar)
 {
  String res=source;
  char stringsDelimitersEscapeChar=(_stringsDelimitersEscapeChar!=null ? _stringsDelimitersEscapeChar : 0);
  int singleLineCommentLen=length(singleLineComment);
  int multiLineCommentBeginLen=length(multiLineCommentBegin);
  boolean hSingleLine=(singleLineCommentLen>0 && source.contains(singleLineComment));
  boolean hMultiLine=(multiLineCommentBeginLen>0 && source.contains(multiLineCommentBegin));

  if (isNotBlank(source) && (hSingleLine || hMultiLine)) // these are not exhaustive checks, just the quickest ones to handle the easy cases
  {
   int multiLineCommentEndLen=length(multiLineCommentEnd);
   int stringsDelimitersLen=length(stringsDelimiters);

   List<Strippable> strippables=new ArrayList<>();
   Stack<Event> events=new Stack<>();
   int lookALen=(int)MathExtras.max(length(singleLineComment), length(multiLineCommentBegin), length(multiLineCommentEnd));
   String lookA="";
   char inStringChar=0;
   int inComments=0;
   int inSingleLineComment=0;
   char ch;
   int t, len=length(source);

   for (t=0;t<len;t++)
   {
    ch=source.charAt(t);

    if (inComments==0)
    {
     if (stringsDelimitersLen>0 && stringsDelimiters.indexOf(ch) >= 0)
     {
      if (inStringChar==0)
      {
       inStringChar=ch;
      }
      else
      {
       if (inStringChar==ch && (lookA.length()==0 || lookA.charAt(lookA.length()-1)!=stringsDelimitersEscapeChar))
        inStringChar=0;
      }
     }
    }


    if (t>=lookALen)
     lookA=lookA.substring(1)+ch;
    else
     lookA+=ch;

    //int g=3;  /*g+=1;*/ g--;

    manyIfs:
    {
     // start of single-line comment
     if (inStringChar==0 && singleLineCommentLen>0 && lookA.endsWith(singleLineComment))
     {
      if (inComments==0)
      {
       inComments++;
       inSingleLineComment++;
       events.push(new Event(singleLineCommentStart, t-(singleLineCommentLen-1)));
      }

      break manyIfs;
     }

     // start of multi-line comment
     if (inStringChar==0 && multiLineCommentBeginLen>0 && lookA.endsWith(multiLineCommentBegin))
     {
      if (inSingleLineComment==0)
      {
       inComments++;

       if (inComments==1)
        events.push(new Event(multiLineCommentStart, t-(multiLineCommentBeginLen-1)));
      }

      break manyIfs;
     }

     // end of multi-line comment
     if (inStringChar==0 && multiLineCommentEndLen>0 && lookA.endsWith(multiLineCommentEnd))
     {
      if (inSingleLineComment==0)
      {
       inComments--;
       if (inComments==0)
        strippables.add(new Strippable(getPreviousEventPosition(events, multiLineCommentStart), t+1));
      }

      break manyIfs;
     }

     // new line
     if (lookA.endsWith("\n"))
     {
      if (inSingleLineComment>0)
      {
       inSingleLineComment--;
       inComments--;
       strippables.add(new Strippable(getPreviousEventPosition(events, singleLineCommentStart), t));
      }
     }
    }
   }

   if ((len=strippables.size())>0)
   {
    int prevPos=0;
    StringBuilder sb=new StringBuilder();

    for (t=0;t<len;t++)
    {
     Strippable strippable=strippables.get(t);

     if (prevPos<strippable.start)
     {
      if (prevPos>0)
      {
       int sbLen=sb.length();
       if (sbLen>0 && isNotBlankNl(sb.substring(sbLen-1, sbLen)))
        sb.append(" ");
      }

      sb.append(source, prevPos, strippable.start);
      prevPos=strippable.end;
     }
     else
     {
      if (prevPos==0)
       prevPos=strippable.end;
     }
    }

    if (prevPos<source.length())
     sb.append(source, prevPos, source.length());

    res=sb.toString();
   }
  }

  return res;
 }


/*
 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static void main(String args[])
 {
  String test=FileSystemExtras.loadTextFromFile("C:\\Disks\\E\\development.2020\\code\\sources\\me.as\\libs\\core\\java\\src\\me\\as\\lib\\core\\lang\\StringExtras.java");
  logOut.println(purgeCommentsFromCLikeLanguageSource(test));
 }
*/

}
