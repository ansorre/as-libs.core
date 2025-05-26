/*SOURCE_CODE_COMMENT_HEADER*/
//@author Antonio Sorrentini
//@owner antonio

package me.as.lib.core.lang;


import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;


public class SmartRuntimeException extends RuntimeException
{

 public SmartRuntimeException()
 {
  super();
  showCause();
 }

 public SmartRuntimeException(String msg)
 {
  super(msg);
  showCause();
 }

 public SmartRuntimeException(String msg, Throwable tr)
 {
  super(msg, tr);
  showCause();
 }

 public SmartRuntimeException(Throwable tr)
 {
  super(tr);
  showCause();
 }



 private void showCause()
 {
  String msg=getMessage();
  if (StringExtras.isNotBlank(msg))
   System.err.println(msg);

  Throwable tr=getCause();
  if (tr!=null)
   systemErrDeepCauseStackTrace(tr);
 }


}
