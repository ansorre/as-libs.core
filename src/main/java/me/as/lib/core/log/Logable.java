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

package me.as.lib.core.log;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Antonio Sorrentini: Copyright (c) 2002</p>
 *
 * <p>Antonio Sorrentini: </p>
 *
 * WARNING 1:
 * At the moment the design of tracelevels is weird. It is not clear what
 * happens when you set the tracelevels to a log that has attached sub-logs. Do
 * tracelevels have to propagate to sub-logs? When 'print, println,...' are
 * executed tracelevels must be evaluated for all of the sub-logs or only for
 * 'this'?
 * We'd better add a setTraceLevelsPolicy(...) to let user coonfigure all of
 * this behaviours.
 *
 * WARNING 2:
 * All the close() and isClosed() stuff must be reviewed and tested better
 *
 * @author not attributable
 * @version 1.0
 */
public interface Logable extends MinimalLogable
{

 boolean isTraceable(String traceLevels);

 void print(String str);
 void println();
 void println(String str);
 void println(String str[]);
 void println(Object o);
 void printStackTrace(Throwable tr);

 void print(String traceLevels, String str);
 void println(String traceLevels, String str);
 void println(String traceLevels, String str[]);
 void println(String traceLevels, Object o);
 void printStackTrace(String traceLevels, Throwable tr);

 void setEnabled(boolean on);
 boolean getEnabled();

 void setTraceLevels(String traceLevels);
 String getTraceLevels();

 void attachLog(Logable anotherLogInCascade);
 void detachLog(Logable anotherLogInCascade);

 void setRedirectedExclusivelyToAttached(boolean on);
 boolean isRedirectedExclusivelyToAttached();


}