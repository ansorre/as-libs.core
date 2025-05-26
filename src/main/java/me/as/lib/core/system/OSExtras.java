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

package me.as.lib.core.system;


import me.as.lib.core.StillUnimplemented;

import javax.swing.filechooser.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

import static me.as.lib.core.lang.ClassExtras.classExists;
import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;
import static me.as.lib.core.lang.ResourceExtras.loadPackagedFile;
import static me.as.lib.core.system.FileSystemExtras.adjustPath;
import static me.as.lib.core.system.FileSystemExtras.deleteFile;
import static me.as.lib.core.system.FileSystemExtras.exists;
import static me.as.lib.core.system.FileSystemExtras.getCanonicalPath;
import static me.as.lib.core.system.FileSystemExtras.getFileSystemCompatibleFileName;
import static me.as.lib.core.system.FileSystemExtras.mergePath;
import static me.as.lib.core.system.FileSystemExtras.mkdirs;
import static me.as.lib.core.system.FileSystemExtras.saveInFile;
import static me.as.lib.core.lang.StringExtras.generateRandomString;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.indexOf;


public class OSExtras
{
 private static String nativeHelper=null;
 private static String temporaryDirectory=null;



 public static String calculateOSDependentApplicationConfigDirectory(String companyName,
                                                                     String applicationName,
                                                                     boolean doLeadDotApplicationName)
 {
  String res;
  if (isSomeMicrosoftWindows()) res=calculateOSDependentApplicationConfigDirectory_for_MSWindows(companyName,
                                                                                                 applicationName,
                                                                                                 doLeadDotApplicationName);
  else
  {
   if (isAppleMacOSX())
   {
    res=calculateOSDependentApplicationConfigDirectory_for_MacOSX(companyName,
                                                                  applicationName,
                                                                  doLeadDotApplicationName);

   } else res=calculateOSDependentApplicationConfigDirectory_for_NoWinNoMac(companyName,
                                                                              applicationName,
                                                                              doLeadDotApplicationName);
  }

  return res;
 }


 public static String calculateOSDependentApplicationConfigDirectory_for_MSWindows(String companyName,
                                                                                   String applicationName,
                                                                                   boolean doLeadDotApplicationName)
 {
  String res=null;
  String tmpStr, appdataDir;

  try
  {
   appdataDir=getCanonicalPath(System.getenv("APPDATA"));
   res=appdataDir+File.separator+companyName+File.separator+
       ((doLeadDotApplicationName)?".":"")+applicationName.trim().toLowerCase();
  }
  catch (Throwable tr)
  {
   appdataDir=null;
  }

  if (appdataDir==null) res=calculateOSDependentApplicationConfigDirectory_for_NoWinNoMac(companyName,
                                                                                            applicationName,
                                                                                            doLeadDotApplicationName);
  else
  {
   String user_home=System.getProperties().getProperty("user.home").trim().toLowerCase();
   tmpStr=appdataDir.trim().toLowerCase();
   if (!tmpStr.startsWith(user_home))
   {
    res=calculateOSDependentApplicationConfigDirectory_for_NoWinNoMac(companyName,
                                                                        applicationName,
                                                                        doLeadDotApplicationName);
   }
  }

  return res;
 }




 public static String calculateOSDependentApplicationConfigDirectory_for_MacOSX(String companyName,
                                                                                    String applicationName,
                                                                                    boolean doLeadDotApplicationName)
 {
  String user_home=System.getProperties().getProperty("user.home");
  return user_home+File.separator+"Library"+File.separator+companyName+File.separator+
         ((doLeadDotApplicationName)?".":"")+applicationName.trim().toLowerCase();
 }


 public static String calculateOSDependentApplicationConfigDirectory_for_NoWinNoMac(String companyName,
                                                                                      String applicationName,
                                                                                      boolean doLeadDotApplicationName)
 {
  String user_home=System.getProperties().getProperty("user.home");
  return user_home+File.separator+companyName+File.separator+
         ((doLeadDotApplicationName)?".":"")+applicationName.trim().toLowerCase();
 }




 public static String calculateOSDependentApplicationsDirectory(String applicationName)
 {
  String res=null;
  boolean _isAppleMacOSX=false;

  if (isSomeMicrosoftWindows())
  {
   try
   {
    File f=new File(System.getenv("ProgramFiles"));
    res=f.getCanonicalPath();
   }
   catch (Throwable tr)
   {
    tr.printStackTrace();
   }
  }
  else
  {
   if (isAppleMacOSX())
   {
    _isAppleMacOSX=true;
    res="/Applications";
   }
   else
   {
    res=System.getProperties().getProperty("user.home");
   }
  }

  if (isNotBlank(res))
  {
   if (_isAppleMacOSX) res=mergePath(res, applicationName+".app");
   else res=mergePath(res, applicationName);
  }

  return res;
 }





 public static File calculateOSDependentUserHomeDirectory()
 {
  return new File(System.getProperties().getProperty("user.home").trim());
 }



 public static File calculateOSDependentUserDocumentDirectory()
 {
  return FileSystemView.getFileSystemView().getDefaultDirectory();
 }


 private static String _lowerdOsName_ = null;


 private synchronized static String lowerdOsName()
 {
  if (_lowerdOsName_==null)
   _lowerdOsName_=System.getProperty("os.name").trim().toLowerCase();

  return _lowerdOsName_;
 }


 public static boolean isSomeMicrosoftWindows()
 {
  return (lowerdOsName().contains("windows"));
 }

 public static boolean isAppleMacOSX()
 {
  return (lowerdOsName().contains("mac os x"));
 }

 public static boolean isSomeLinux()
 {
  return (indexOf(lowerdOsName(), "linux")>=0);
 }


 public static boolean isAppleiOS()
 {
  return classExists("org.robovm.cocoatouch.uikit.UIApplication");
 }


 public static boolean isSomeAndroid()
 {
  return classExists("android.app.Activity");
 }


 public static boolean isSomeUnix()
 {
  boolean res=isAppleMacOSX();

  if (!res) res=isSomeLinux();
  if (!res)
  {
   if (!isSomeMicrosoftWindows()) return true;
  }

  return res;
 }


 public static String grantUserTemporaryDirectory()
 {
  synchronized (OSExtras.class)
  {
   if (temporaryDirectory==null)
   {
    String uahdp=System.getProperties().getProperty("user.home");
    temporaryDirectory=adjustPath(uahdp+File.separator+"temp");
    mkdirs(temporaryDirectory);
   }
  }

  return temporaryDirectory;
 }


 public static String saveInTemporaryFile(byte bytes[], String fileExtension)
 {
  String td=grantUserTemporaryDirectory();
  boolean exists;
  String fileName;

  do
  {
   fileName=adjustPath(td+File.separator+getFileSystemCompatibleFileName(generateRandomString(30)+"."+fileExtension));
   exists=exists(fileName);
  } while (exists);

  saveInFile(fileName, bytes);
  return fileName;
 }



 public static String grantNativeHelper()
 {
  synchronized (OSExtras.class)
  {
   if (nativeHelper==null)
   {
    if (isSomeMicrosoftWindows())
    {
     String td=grantUserTemporaryDirectory();
     nativeHelper=adjustPath(td+File.separator+"winutil.exe");
     deleteFile(nativeHelper);
     saveInFile(nativeHelper, loadPackagedFile("/me/as/lib/core2/resource/native/winutils.exe"));
    }
    else
    {
     throw new StillUnimplemented();
    }
   }
  }

  return nativeHelper;
 }


 public static void openTheFileFolderWithDefaultSystemExplorerAndHighlightTheFileItself(String file)
 {
  if (isSomeMicrosoftWindows())
  {
   try
   {
    Runtime.getRuntime().exec("explorer.exe /select," + file);
   }
   catch (Throwable tr)
   {
    systemErrDeepCauseStackTrace(tr);
   }
  }
  else
  {
   throw new StillUnimplemented();
  }
 }


 public static void openTheFileWithDefaultSystemApplication(String file)
 {
  if (isSomeMicrosoftWindows())
  {
   ExternalProcessSupport.exec(new String[]{"CMD", "/C", file}, new ExternalProcessResults(), false);
  }
  else
  {
   try
   {
    Desktop.getDesktop().open(new File(file));
   }
   catch (Throwable tr)
   {
    systemErrDeepCauseStackTrace(tr);
   }
  }
 }


 public static String readConsoleLine()
 {
  Console console=System.console();
  if (console!=null) return console.readLine();

  Scanner scanner=new Scanner(System.in);
  return scanner.nextLine();
 }


}
