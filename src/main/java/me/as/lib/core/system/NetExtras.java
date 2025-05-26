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


import java.net.ServerSocket;


public class NetExtras
{

 public static int findSocketFreePort()
 {
  ServerSocket socket=null;

  try
  {
   socket=new ServerSocket(0);
   socket.setReuseAddress(true);

   return socket.getLocalPort();
  }
  catch (Throwable tr)
  {
   return -1;
  }
  finally
  {
   if (socket!=null)
    try{ socket.close(); }catch (Throwable ignore){}
  }
 }


}
