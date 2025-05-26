/*
 * Copyright 2023 Antonio Sorrentini
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


import me.as.lib.core.collection.RamTable;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ResourceExtras;
import me.as.lib.core.lang.StringExtras;

import java.util.HashMap;

import static me.as.lib.core.resource.ResourcesPaths.mime_types_txt;


public class MIMEExtras
{
 private final static RamTable extensionsTypes=new RamTable();
 private static HashMap extensions=null;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static String getMimeTypeFromFileName(String fileName)
 {
  String res="application/octet-stream";

  synchronized (extensionsTypes)
  {
   if (extensions==null)
   {
    prepareExtensionsTypes();
   }

   if (StringExtras.isNotBlank(fileName) && fileName.lastIndexOf('.')>=0)
   {
    String exts[]=StringExtras.unmerge(fileName, '.');
    String ext=exts[exts.length-1].trim().toLowerCase();
    String tmpS=(String)extensions.get(ext);

    if (StringExtras.isNotBlank(tmpS)) res=tmpS;
   }
  }

  return res;
 }



 private static void prepareExtensionsTypes()
 {
  try
  {
   String ext_types=StringExtras.newAutoString(ResourceExtras.loadPackagedFile(mime_types_txt));
   ext_types=StringExtras.replaceAll(ext_types, "\r", "\n");
   ext_types=StringExtras.replaceAll(ext_types, "\n\n", "\n");
   String tmpStr, parts[], lines[]=StringExtras.unmerge(ext_types, '\n');
   int t, r, len=ArrayExtras.length(lines);
   extensions=new HashMap();

   for (r=0;r<len;r++)
   {
    parts=StringExtras.unmerge(lines[r], ';');
    tmpStr=parts[0].trim().toLowerCase();
    extensionsTypes.setString(0, r, tmpStr);

    if (parts.length>1)
    {
     parts[1]=parts[1].trim().toLowerCase();
     parts[1]=StringExtras.replaceAll(parts[1], " ", null);
     extensionsTypes.setString(1, r, parts[1]);
     parts=StringExtras.unmerge(parts[1], ',');

     extensionsTypes.setString(0, r, parts[0].trim().toLowerCase());

     for (t=0;t<parts.length;t++)
     {
      extensions.put(parts[t], tmpStr);
     }
    }
   }
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }
 }


}
