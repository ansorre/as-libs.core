/*
 * Copyright 2025 Transhumai
 *
 * The license for this file is available into the file
 * LICENSE present in the root directory of this project.
 *
 */

package me.as.lib.core.system;


import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.StringExtras;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FastExternalProcessExtras
{

 public static int runExternalProcess(String... commandLine)
 {
  return runExternalProcess(null, null, commandLine);
 }

 public static int runExternalProcess(BoxFor2<byte[], byte[]> results, String... commandLine)
 {
  return runExternalProcess(null, results, commandLine);
 }

 public static int runExternalProcess(String workingDir, String... commandLine)
 {
  return runExternalProcess(workingDir, null, commandLine);
 }

 public static int runExternalProcess(String workingDir, BoxFor2<byte[], byte[]> results, String... commandLine)
 {
  return runExternalProcess(workingDir, results, false, commandLine);
 }

 public static int runExternalProcess(boolean skipCmdExeOnWindows, String... commandLine)
 {
  return runExternalProcess(null, null, skipCmdExeOnWindows, commandLine);
 }

 public static int runExternalProcess(String workingDir, BoxFor2<byte[], byte[]> results, boolean skipCmdExeOnWindows, String... commandLine)
 {
  Process process;

  try
  {
   ProcessBuilder builder=new ProcessBuilder();

   if (!skipCmdExeOnWindows && OSExtras.isSomeMicrosoftWindows())
   {
    List<String> cmdLine=new ArrayList<>();
    cmdLine.addAll(Arrays.asList("cmd.exe", "/c"));
    cmdLine.addAll(Arrays.asList(commandLine));
    commandLine=cmdLine.toArray(new String[0]);
   }

   if (StringExtras.isBlank(workingDir))
    workingDir=new File(".").getCanonicalPath();

   builder.command(commandLine);
   builder.directory(new File(workingDir)); // working dir

   builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
   builder.redirectErrorStream(true); // Redirects error stream to the output stream

   process=builder.start();

   // Read the output from the process as binary data
   InputStream inputStream=process.getInputStream();
   ByteArrayOutputStream outputStream=(results!=null ? new ByteArrayOutputStream() : null);

   // Read the error output from the process as binary data
   InputStream errorStream=process.getErrorStream();
   ByteArrayOutputStream errorOutputStream=(results!=null ? new ByteArrayOutputStream() : null);

   PrintStream out=(results==null ? System.out : null);
   PrintStream err=(results==null ? System.err : null);

   Thread outputThread=readByThread(inputStream, outputStream, out);
   Thread errorThread=readByThread(errorStream, errorOutputStream, err);

   // Start the threads
   outputThread.start();
   errorThread.start();

   // Wait for the threads to finish
   outputThread.join();
   errorThread.join();

   // Wait for the process to complete
   int exitCode=process.waitFor();

   // You can access the binary output data using outputStream.toByteArray()
   if (results!=null)
   {
    byte processOutput[]=outputStream.toByteArray();
    byte processErrorOutput[]=errorOutputStream.toByteArray();

    results.element1=processOutput;
    results.element2=processErrorOutput;
   }

   return exitCode;
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
  }

  return -1;
 }


 private static Thread readByThread(InputStream inputStream, ByteArrayOutputStream outputStream, PrintStream backupOutput)
 {
  Thread res=new Thread(() ->
  {
   try
   {
    byte[] buffer=new byte[1024];
    int length;
    while ((length=inputStream.read(buffer))!=-1)
    {
     if (outputStream!=null)
      outputStream.write(buffer, 0, length);

     if (backupOutput!=null)
      backupOutput.write(buffer, 0, length);
    }
   }
   catch (IOException e)
   {
    e.printStackTrace();
   }
  });

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static int runExternalJavaProcess(BoxFor2<byte[], byte[]> results, String mainClass, String... programArguments)
 {
  return runExternalJavaProcess(results, null, mainClass, programArguments);
 }

 public static int runExternalJavaProcess(String mainClass, String... programArguments)
 {
  return runExternalJavaProcess(null, null, mainClass, programArguments);
 }

 public static int runExternalJavaProcess(BoxFor2<byte[], byte[]> results, String extraClassPathEntries[], String mainClass, String... programArguments)
 {
  try
  {
   // current working dir
   String cwd=new File(".").getCanonicalFile().getPath();

   String ps=System.getProperty("path.separator");
   String classpath = System.getProperty("java.class.path");
   String[] classpathEntries = classpath.split(ps);
   List<String> newClassPath=new ArrayList<>();

   for (String entry : classpathEntries)
    newClassPath.add(entry);

   int len=ArrayExtras.length(extraClassPathEntries);
   if (len>0)
    Arrays.asList(extraClassPathEntries).forEach(entry -> newClassPath.add(entry));

   List<String> cmdl=new ArrayList<>();
   String javaExe=FileSystemExtras.mergePath(System.getProperty("java.home"), "bin", "java.exe");

   cmdl.add(javaExe);
   cmdl.add("-cp");
   cmdl.add(StringExtras.mergeEnclosing(newClassPath.toArray(new String[]{}), ps, null).substring(1));
   cmdl.add(mainClass);

   len=ArrayExtras.length(programArguments);
   if (len>0)
    Arrays.asList(programArguments).forEach(arg -> cmdl.add(arg));

   return runExternalProcess(cwd, results, true, cmdl.toArray(new String[]{}));
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


}
