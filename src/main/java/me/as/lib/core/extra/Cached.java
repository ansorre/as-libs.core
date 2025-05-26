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

package me.as.lib.core.extra;


public class Cached<E>
{
 private CacheHelper<E> helper;
 private Object params[];
 private long millisBetweenRefreshes;
 private long lastRefreshMillis=0;

 private E payload=null;


 public Cached(CacheHelper<E> helper, long millisBetweenRefreshes, Object... params)
 {
  this.helper=helper;
  this.params=params;
  this.millisBetweenRefreshes=millisBetweenRefreshes;
 }


 public synchronized E get()
 {
  if (payload==null ||
      (System.currentTimeMillis()-lastRefreshMillis)>millisBetweenRefreshes)
  {
   payload=helper.create(params);
   lastRefreshMillis=System.currentTimeMillis();
  }

  return payload;
 }


}
