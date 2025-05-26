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

package me.as.lib.core.math;


import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.extra.Box;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.Types;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.lang.StringExtras.trim;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.math.RandomExtras.random;


public class MathExtras
{

 public static final double oneThousand=1000.0;
 public static final double oneMillion=oneThousand*1000.0;
 public static final double oneBillion=oneMillion*1000.0;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static final double DOUBLE_MIN_VALUE=Integer.MIN_VALUE;
 public static final double DOUBLE_MAX_VALUE=Integer.MAX_VALUE;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static final double def_ponderalLengthSimilarityWeight  = 1.0;
 private static final double def_ponderalHeightSimilarityWeight  = 1.0;
 private static final double def_ponderalPatternSimilarityWeight = 3.0;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 /**
  *
  *
  * @param v1
  * @param v2
  * @param decimals   please, this number must be max 18!
  * @return      returns true if v1 is equal to v2 up to the 'decimals' decimal
  */
 public static boolean areEqual(double v1, double v2, int decimals)
 {
  if (decimals>18) throw new IllegalArgumentException("please, decimalDigits digits must be max 18!");
  long mul=(long)Math.pow(10, decimals);

  double mv1=Math.round(v1*mul);
  double mv2=Math.round(v2*mul);

  return (mv1==mv2);
 }




 private static DecimalFormat digiCounter=null;

 /**
  *
  * @param v
  * @return how many decimals the number v has
  */
 public static int decimalDigits(double v)
 {
  int res=0;

  synchronized (MathExtras.class)
  {
   if (digiCounter==null)
   {
    digiCounter=new DecimalFormat();
    digiCounter.setMaximumIntegerDigits(Integer.MAX_VALUE);
    digiCounter.setMaximumFractionDigits(Integer.MAX_VALUE);
    digiCounter.setGroupingUsed(false);
    digiCounter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
   }

   String sv=digiCounter.format(v);
   int io=sv.indexOf(".");

   if (io>=0)
    res=sv.length()-io-1;
  }

  return res;
 }



/*
 // success
 public static void main(String args[])
 {
  double v=1.12345;
  System.out.println(""+v+" --> "+decimalDigits(v));

  v=1.12;
  System.out.println(""+v+" --> "+decimalDigits(v));

  v=1.1;
  System.out.println(""+v+" --> "+decimalDigits(v));

  v=1;
  System.out.println(""+v+" --> "+decimalDigits(v));

  v=123.1234567890;
  System.out.println(""+v+" --> "+decimalDigits(v));

  v=123.123456789;
  System.out.println(""+v+" --> "+decimalDigits(v));


 }
*/





 /**
  *
  * @param v
  * @param decimals   please, this number must be max 18!
  * @return           returns the v value truncated to the 'decimals' decimal
  */
 public static double truncate(double v, int decimals)
 {
  if (decimals>18) throw new IllegalArgumentException("please, number of decimalDigits must be max 18!");
  long mul=(long)Math.pow(10, decimals);
  double mv1=Math.round(v*mul);
  mv1/=mul;
  return mv1;
 }



 public static double[] truncate(double v[], int decimals)
 {
  int t, len=ArrayExtras.length(v);
  for (t=0;t<len;t++) v[t]=truncate(v[t], decimals);
  return v;
 }




 public static double truncateToTick(double v, double tickSize)
 {
  int hm=(int)(v/tickSize);
  double v1=(hm-1)*tickSize;
  double v2=(hm)*tickSize;
  double v3=(hm+1)*tickSize;
  double res=nearest(v, v1, v2, v3);
  return res;
 }




 public static double nearest(double v, double... candidates)
 {
  int idx=-1;
  double td, diff=Integer.MAX_VALUE;
  int t, len=ArrayExtras.length(candidates);

  for (t=0;t<len;t++)
  {
   td=Math.abs(v-candidates[t]);

   if (diff>td)
   {
    diff=td;
    idx=t;
   }
  }

  return candidates[idx];
 }






 public static double signum(double v)
 {
  return ((v<=0)?-1:1);
 }


 public static double signumOrZero(double v)
 {
  if (v<0) return -1;
  if (v>0) return 1;
  return 0;
 }



 public static double signedSqrt(double v)
 {
  double mul=((v<0)?-1:1);
  return Math.sqrt(Math.abs(v))*mul;
//  return v;
 }




 public static double randomlyPerturbate(double value, double maxValue, double minValue,
                                         double perturbatefrequencyPercent, double maxPerturbationPercent)
 {
  double res=value;

  if (random(100)<=perturbatefrequencyPercent)
  {
   double diff=getPercentOf(random(maxPerturbationPercent), maxValue-minValue);
   if (random(100)>=50) diff*=-1;
   res+=diff;
   if (res>maxValue) res=maxValue;
   if (res<minValue) res=minValue;
  }

  return res;
 }




 public static double[] toDoublePrimitiveArray(Object values[])
 {
  int t, len=ArrayExtras.length(values);
  double res[]=new double[len];
  for (t=0;t<len;t++) res[t]=toDouble(values[t]);
  return res;
 }


 public static double getSimilarityPercent(double serie1[], double serie2[])
 {
  return getSimilarityPercent(serie1, serie2,
                              def_ponderalLengthSimilarityWeight,
                              def_ponderalHeightSimilarityWeight,
                              def_ponderalPatternSimilarityWeight);
 }





 public static double getSimilarityPercent(double serie1[], double serie2[],
                                           double ponderalLengthSimilarityWeight,
                                           double ponderalHeightSimilarityWeight,
                                           double ponderalPatternSimilarityWeight)
 {
  double res;

  try
  {
   double ponderalLengthSimilarity=0;
   double ponderalHeightSimilarity=0;
   double ponderalPatternSimilarity=0;

   // determining length similarity
   double max=Math.max(serie1.length, serie2.length);
   double min=Math.min(serie1.length, serie2.length);

   ponderalLengthSimilarity=expressedAsPercentOf(min, max);

   // determining height similarity

   double ps1[]=toPercents(serie1);
   double h1=getHeight(ps1);

   double ps2[]=toPercents(serie2);
   double h2=getHeight(ps2);

   max=Math.max(h1, h2);
   min=Math.min(h1, h2);

   ponderalHeightSimilarity=expressedAsPercentOf(min, max);

   // determining pattern similarity

    // adjust heights
   if (h1>h2) ps2=enlargeHeight(ps2, h1);
   else ps1=enlargeHeight(ps1, h2);


    // adjust widths
   if (serie1.length==Math.max(serie1.length, serie2.length)) ps2=enlargeWidth(ps2, serie1.length);
   else ps1=enlargeWidth(ps1, serie2.length);

   ponderalPatternSimilarity=computePatternsSimilarity(ps1, ps2);

   res=((ponderalLengthSimilarity*ponderalLengthSimilarityWeight)+
        (ponderalHeightSimilarity*ponderalHeightSimilarityWeight)+
        (ponderalPatternSimilarity*ponderalPatternSimilarityWeight))
       /
       (ponderalLengthSimilarityWeight+ponderalHeightSimilarityWeight+ponderalPatternSimilarityWeight);
  }
  catch (Throwable tr)
  {
   tr.printStackTrace();
   res=0;
  }

//  System.out.println("res: "+res);

  return res;
 }














 /**
  *
  * IMPORTANT: only for internal use, because:
  *
  *     1) serie1 and serie2 must be of the same length
  *
  *     2) serie1 and serie2 must have the same vertical amplitude (same height)
  *
  *
  * @param serie1
  * @param serie2
  * @return
  */
 private static double computePatternsSimilarity(double serie1[], double serie2[])
 {
  double res=0;

  int t, len=serie1.length;
  double serie1R[]=new double[len];
  double tr, v1, v2, delta, s2H=getHeight(serie1);

  serie1R[0]=0;

  for (t=1;t<len;t++)
  {
   serie1R[t]=serie1[t]-(serie2[t]-serie2[0]);
  }

  for (t=1;t<len;t++)
  {
   delta=serie1R[t-1]-s2H;
   v1=serie1R[t]-delta;
   v2=serie1R[t-1]-delta;

   tr=100-expressedAsPercentOf(Math.abs(v1-v2), v2);
   res+=tr;
  }

  res/=(double)(len-1);

//  System.out.println("pres: "+res);
  return res;
 }













 /**
  *
  * IMPORTANT: only for internal use, because:
  *
  *     1) serie1 and serie2 must be of the same length
  *
  *     2) serie1 and serie2 must have the same vertical amplitude (same height)
  *
  *
  * @param serie1
  * @param serie2
  * @return
  */
 private static double _computePatternsSimilarity(double serie1[], double serie2[])
 {
  double res=0;

  int c1, c2, t, m, len=serie1.length, len_2=len/2;
  double height=getHeight(serie1), h_100=height/100.0, h_2=height/2.0;
  double h, minD, maxD, v1, v2, ma, tmpRes, matches[]=new double[len];

  for (t=0;t<len;t++)
  {
   for (h=0;h<height;h+=h_100)
   {
    minD=Integer.MAX_VALUE;
    maxD=Integer.MIN_VALUE;

    // determining min and max distances
    for (c1=0;c1<len;c1++)
    {
     c2=c1-len_2+t;

     if (c2>=0 && c2<len)
     {
      v1=serie1[c1]+h_2-h;
      v2=serie2[c2];

      ma=Math.abs(v1-v2);

      if (minD>ma) minD=ma;
      if (maxD<ma) maxD=ma;
     }
    }

    maxD-=minD;

    // resetting matches
    for (m=0;m<len;m++) matches[m]=0;

    // computing differences in percentuals
    for (c1=0;c1<len;c1++)
    {
     c2=c1-len_2+t;

     if (c2>=0 && c2<len)
     {
      v1=serie1[c1]+h_2-h;
      v2=serie2[c2];

      ma=Math.abs(v1-v2)-minD;
      matches[c1]=100.0-expressedAsPercentOf(ma, maxD);
     }
    }

    tmpRes=getAverage(matches);
//    System.out.println("tmpRes: "+tmpRes);
    if (res<tmpRes) res=tmpRes;
   }
  }

//  System.out.println("pres: "+res);
  return res;
 }


 private static double[] enlargeHeight(double serie[], double newHeight)
 {
  int t, len=serie.length;
  double res[]=new double[len];
  double eh=getHeight(serie);

  for (t=0;t<len;t++)
  {
   res[t]=(serie[t]/eh)*newHeight;
  }

  return res;
 }


 private static double[] enlargeWidth(double serie[], int newWidth)
 {
  double res[]=serie;

  if (serie.length!=newWidth)
  {
   throw new StillUnimplemented();
  }

  return res;
 }





 public static double getWeightedAverage(double price1, double price2, double weight1, double weight2)
 {
  return ((price1*weight1)+(price2*weight2))/(weight1+weight2);
 }



 public static double getWeightedAverage(double[]... pricesWeights)
 {
  double values=0, sum=0;
  int t, len=ArrayExtras.length(pricesWeights);

  for (t=0;t<len;t++)
  {
   values+=pricesWeights[t][0]*pricesWeights[t][1];
   sum+=pricesWeights[t][1];
  }

  return values/sum;
 }









 public static double getAverage(double serie[])
 {
  return getUnbalancedAverage(serie, 0, true);
 }


 public static double getUnbalancedAverage(double serie[], int rounds, boolean highest)
 {
  int t, len=ArrayExtras.length(serie), rou=rounds+1;
  double sum, count, prevAverage=((highest)?Integer.MIN_VALUE:Integer.MAX_VALUE);
  boolean onCondition;

  if (len>0)
  {
   do
   {
    sum=0;
    count=0;

    for (t=0;t<len;t++)
    {
     onCondition=((highest)?(serie[t]>=prevAverage):(serie[t]<=prevAverage));

     if (onCondition)
     {
      sum+=serie[t];
      count++;
     }
    }

    prevAverage=sum/count;
    rou--;
   } while (rou>0);
  } else prevAverage=Double.NaN;

  return prevAverage;
 }




 private static double[] toPercents(double serie[])
 {
  int t, len=serie.length;
  double res[]=new double[len];

  res[0]=0;

  for (t=1;t<len;t++)
  {
   res[t]=expressedAsPercentOf(serie[t], serie[0])-100.0;
  }

  return res;
 }

 private static double getHeight(double serie[])
 {
  int t, len=serie.length;
  double min=Integer.MAX_VALUE;
  double max=Integer.MIN_VALUE;

  for (t=0;t<len;t++)
  {
   if (min>serie[t]) min=serie[t];
   if (max<serie[t]) max=serie[t];
  }

  return max-min;
 }


 /**
  * reduce v to the range min~max periodically restarting from min if v>max
  *
  * @param v  the value
  * @param min the range min value
  * @param max  the range max value
  * @return as explained
  */
 public static double reduce(double v, double min, double max)
 {
  if (v<min || v>max)
  {
   v=min+Math.abs(v%(max-min));
  }

  return v;
 }




/*

 // recuce test: well it works, but reduce never returns 1 (except when the input is exactly 1!
 public static void main(String args[])
 {
  int t;
  double v=-4;

  for (t=0;t<100;t++)
  {
   String f=MathExtras.truncate(v, 1)+";"+MathExtras.truncate(MathExtras.reduce(v, 0, 1), 1);
   f=replace(f, ".", ",");
   System.out.println(f);

//   System.out.println("v: "+MathExtras.truncate(v, 1)+" --> "+MathExtras.truncate(MathExtras.reduce(v, 0, 1), 1));

   v=MathExtras.truncate(v+0.1, 12);
  }
 }


  */





 public static double clamp(double v, double min, double max)
 {
  if (v>max) return max;
  if (v<min) return min;
  return v;
 }




 public static double toRange(double v, double srcRangeMin, double srcRangeMax)
 {
  return toRange(v, srcRangeMin, srcRangeMax, 0.0, 1.0);
 }


 public static double toRange(double v, double srcRangeMin, double srcRangeMax, double destRangeMin, double destRangeMax)
 {
  double srcMax=Math.max(srcRangeMin, srcRangeMax);
  double srcMin=Math.min(srcRangeMin, srcRangeMax);
  double srcRange=srcMax-srcMin;

  double destMax=Math.max(destRangeMin, destRangeMax);
  double destMin=Math.min(destRangeMin, destRangeMax);
  double destRange=destMax-destMin;

  double res=destMin+((v-srcMin)/srcRange)*destRange;
  return res;
 }


 public static double[] toRange(double v[], double srcRangeMin, double srcRangeMax)
 {
  int t, len=ArrayExtras.length(v);
  for (t=0;t<len;t++) v[t]=toRange(v[t], srcRangeMin, srcRangeMax, 0.0, 1.0);
  return v;
 }


 public static double[] toRange(double v[], double srcRangeMin, double srcRangeMax, double destRangeMin, double destRangeMax)
 {
  int t, len=ArrayExtras.length(v);
  for (t=0;t<len;t++) v[t]=toRange(v[t], srcRangeMin, srcRangeMax, destRangeMin, destRangeMax);
  return v;
 }


 public static double normalizeScalar(double value, double max)
 {
  return value/max;
 }


 public static double[] normalize(double[] serie)
 {
  return normalize(serie, Double.NaN, Double.NaN);
 }

 public static double[] normalize(double[] serie, double _maxY, double _minY)
 {
  return normalize(serie, _maxY, _minY, true, false);
 }


 public static double[] normalize(double[] serie, boolean force, boolean allocateNewIfChanged)
 {
  return normalize(serie, Double.NaN, Double.NaN, force, allocateNewIfChanged);
 }

 public static double[] normalize(double[] serie, double _maxY, double _minY, boolean force, boolean allocateNewIfChanged)
 {
  double res[]=serie;
  int t, len=ArrayExtras.length(serie);

  if (len>0)
  {
   double minY=Long.MAX_VALUE;
   double maxY=Long.MIN_VALUE;

   for (t=0;t<len;t++)
   {
    if (minY>serie[t]) minY=serie[t];
    if (maxY<serie[t]) maxY=serie[t];
   }

   if (!Double.isNaN(_maxY) && maxY<_maxY) maxY=_maxY;
   if (!Double.isNaN(_minY) && minY>_minY) minY=_minY;

   if (force || minY<0.0 || maxY>1.0)
   {
    if (allocateNewIfChanged) res=(double[])ArrayExtras.clone(serie);

    for (t=0;t<len;t++) res[t]=toRange(res[t], minY, maxY, 0.0, 1.0);
   }
  }

  return res;
 }




/*
 // test:
 // public static double[] normalize(double[] serie)
 // test result: SUCCESS
 public static void main(String args[])
 {
  double[] serie=new double[]{-10.0, -9.8, -9.6, -9.4, -9.2, -9.0, -8.8, -8.6, -8.4, -8.2, -8.0};

  System.out.println("original: ");
  StringExtras.systemOut(serie);

  System.out.println("normalized: ");
  StringExtras.systemOut(normalize(serie));
 }
*/








 public static boolean areDifferentMoreThanPercent(double v1, double v2, double percent)
 {
  return (Math.abs(expressedAsPercentOf(Math.abs(v1-v2), Math.max(v1, v2)))>percent);
 }


 public static boolean areDifferentLessThanPercent(double v1, double v2, double percent)
 {
  return (Math.abs(expressedAsPercentOf(Math.abs(v1-v2), Math.max(v1, v2)))<percent);
 }



 public static boolean areDifferentMoreThan(double v1, double v2, double maxDifference)
 {
  return (Math.abs(v1-v2)>maxDifference);
 }


 public static boolean areDifferentLessThan(double v1, double v2, double maxDifference)
 {
  return (Math.abs(v1-v2)<maxDifference);
 }







 /**
  *
  * @param perc
  * @param ofWhat
  * @return
  *
  *     returns the perc% of ofWhat.
  *     Ex.: getPercentOf(30, 10) retuns 3 (that in fact is the 30% of 10!)
  */
 public static double getPercentOf(double perc, double ofWhat)
 {
  return (ofWhat/100.0)*perc;
 }




 public static double getProportional(double relativePart, double absolutePart, double absoluteTotal)
 {
  return (absolutePart/relativePart)*absoluteTotal;
 }



 /**
  *
  * @param value
  * @param total
  * @return
  *
  *
  *     returns the percentual of value on total.
  *     Ex.:  expressedAsPercentOf(1, 10)  returns 10;
  *
  *
  */
 public static double expressedAsPercentOf(double value, double total)
 {
  if (total==0) return 100.0;
  if (value==total) return 100.0;
  return value/(total/100.0);
 }


 /**
  *
  * @param value1
  * @param value2
  * @return
  *
  *      returns the percent of the bigger of the two values relative to the smaller  one
  *      Ex.:    getBiggestPercentOnSmallest(10, 11) returns 110
  *              getBiggestPercentOnSmallest(11, 10) returns 110
  *
  */
 public static double getBiggerPercentOfSmaller(double value1, double value2)
 {
  double max=Math.max(value1, value2);
  double min=Math.min(value1, value2);
  return expressedAsPercentOf(max, min);
 }


 /**
  *
  * @param value1
  * @param value2
  * @return
  *
  *      returns the percent of the smaller of the two values relative to the bigger one
  *      Ex.:    getSmallerPercentOfBigger(9, 10) returns 90
  *              getSmallerPercentOfBigger(10, 9) returns 90
  *
  */
 public static double getSmallerPercentOfBigger(double value1, double value2)
 {
  double max=Math.max(value1, value2);
  double min=Math.min(value1, value2);
  return expressedAsPercentOf(min, max);
 }




 /**
  *
  * @param mayBeBigger
  * @param mayBeSmaller
  * @param byPercent
  * @return
  *
  *     returns true only if mayBeBigger is at least byPercent% bigger than mayBeSmaller
  */
 public static boolean isBiggerByPercent(double mayBeBigger, double mayBeSmaller, double byPercent)
 {
  boolean res=false;

  if (mayBeBigger>mayBeSmaller)
  {
   res=(expressedAsPercentOf(mayBeBigger-mayBeSmaller, mayBeSmaller)>=byPercent);
  }

  return res;
 }



 /**
  *
  * @param mayBeSmaller
  * @param mayBeBigger
  * @param byPercent
  * @return
  *
  *     returns true only if mayBeSmaller is at least byPercent% smaller than mayBeBigger
  */
 public static boolean isSmallerByPercent(double mayBeSmaller, double mayBeBigger, double byPercent)
 {
  boolean res=false;

  if (mayBeSmaller<mayBeBigger)
  {
   res=(expressedAsPercentOf(mayBeBigger-mayBeSmaller, mayBeSmaller)>=byPercent);
  }

  return res;
 }




/*
 // non ha nessun senso, non funziona per niente, non ricordo manco perch� l'avevo scritta
 public static double getPercentSimilarity(double v1, double v2, double range)
 {
  double res=((v1==v2)?100.0:0);

  if (res==0)
  {
   res=MathExtras.expressedAsPercentOf(v1, v2);

   if (MathExtras.isOutside(res, -range, range)) res=0;
   else
   {
    res=100.0-expressedAsPercentOf(res-100.0, range);
   }
  }

  return res;
 }
*/









 public static int indexOfFirstBiggerOrEqual(double values[], double toLookForBigger)
 {
  int t, len=values.length;

  for (t=0;t<len;t++)
  {
   if (values[t]>=toLookForBigger) return t;
  }

  return -1;
 }

 public static int indexOfFirstSmallerOrEqual(double values[], double toLookForSmaller)
 {
  int t, len=values.length;

  for (t=0;t<len;t++)
  {
   if (values[t]<=toLookForSmaller) return t;
  }

  return -1;
 }


 public static double max(double v[])
 {
  double res=v[0];
  int t, len=ArrayExtras.length(v);

  for (t=0;t<len;t++)
  {
   res=Math.max(res, v[t]);
  }

  return res;
 }


 public static double max(double v1, double v2, double... v)
 {
  double res=Math.max(v1, v2);
  int t, len=ArrayExtras.length(v);

  for (t=0;t<len;t++)
  {
   res=Math.max(res, v[t]);
  }

  return res;
 }


 public static double min(double v[])
 {
  double res=v[0];
  int t, len=ArrayExtras.length(v);

  for (t=0;t<len;t++)
  {
   res=Math.min(res, v[t]);
  }

  return res;
 }


 public static double min(double v1, double v2, double... v)
 {
  double res=Math.min(v1, v2);
  int t, len=ArrayExtras.length(v);

  for (t=0;t<len;t++)
  {
   res=Math.min(res, v[t]);
  }

  return res;
 }


 public static double extreme(boolean max, double v1, double v2, double... v)
 {
  if (max) return max(v1, v2, v);
  else return min(v1, v2, v);
 }





 public static boolean isOdd(int value)
 {
  return !isEven(value);
 }


 public static boolean isEven(int value)
 {
//  return (((value/2)*2)==value);
  return ((value%2)==0);
 }




 // TESTED: SUCCESS!
 public static boolean isWayBigger(double bigger, double than, double ofAtLeastThisPercOfThan)
 {
  return bigger>than && bigger>=than+((than/100.0)*ofAtLeastThisPercOfThan);
//  return (bigger>than && (expressedAsPercentOf(bigger, than)-100.0)>=ofAtLeastThisPercOfThan);
 }



 // TESTED: SUCCESS!
 public static boolean isWaySmaller(double smaller, double than, double ofAtLeastThisPercOfThan)
 {
  return smaller<than && smaller<=than-((than/100.0)*ofAtLeastThisPercOfThan);
//  return (smaller<than && (100.0-expressedAsPercentOf(smaller, than))>=ofAtLeastThisPerc);
 }



 public static boolean areAlmostEqual(double value, double equalTo, double percentTollerance)
 {
  double perc=expressedAsPercentOf(value, equalTo);
  return isBetween(perc, 100.0-percentTollerance, 100.0+percentTollerance);
 }



 public static boolean isBetween(double value, double limit1, double limit2)
 {
  return (value>=Math.min(limit1, limit2) && value<=Math.max(limit1, limit2));
 }


 public static boolean isOutside(double value, double lowerLimit, double higherLimit)
 {
  return !isBetween(value, lowerLimit, higherLimit);
 }


 public static boolean isItOrItsModulo(double value, double it)
 {
  return (value==it || (value % it)==0);
 }





 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 private static final String toDouble_err = "Could not convert to double the value passed to MathExtras.toDouble(...) which class is '";


 public static Object cast(double value, Class castClass)
 {
  return fromDouble(value, castClass);
 }

 public static Object fromDouble(double value, Class castClass)
 {
  if (castClass==Double.TYPE || castClass==Double.class) return value;

  Object res;
  int idx=ArrayExtras.indexOf(Types.primitivesAndAlmostClasses, 0, castClass);

  switch (idx)
  {
   /* Byte.TYPE,      Byte.class      */ case  0:case  1:res=(byte)value;break;
   /* Short.TYPE,     Short.class     */ case  2:case  3:res=(short)value;break;
   /* Integer.TYPE,   Integer.class   */ case  4:case  5:res=(int)value;break;
   /* Long.TYPE,      Long.class      */ case  6:case  7:res=(long)value;break;
   /* Character.TYPE, Character.class */ case  8:case  9:res=(char)value;break;
   /* Float.TYPE,     Float.class     */ case 10:case 11:res=(float)value;break;
   /* Double.TYPE,    Double.class    */ case 12:case 13:res=value;break;
   /* String.class                    */ case 16        :res=String.valueOf(value);break;
   default:
   {
    throw new RuntimeException("Cannot cast a double to class '"+castClass.getName()+"'");
   }
  }

  return res;
 }



 public static boolean isFiniteDouble(double value)
 {
  return (!Double.isNaN(value) && !Double.isInfinite(value));
 }



 public static double toDouble(Object value)
 {
  double res;

  if (value instanceof Double) res=(Double)value;
  else
  {
   switch (ArrayExtras.select(Types.classes2, value.getClass()))
   {
    case 1 /* Byte.class      */:res=(Byte)value;break;
    case 2 /* Short.class     */:res=(Short)value;break;
    case 3 /* Integer.class   */:res=(Integer)value;break;
    case 4 /* Long.class      */:res=(Long)value;break;
    case 5 /* Character.class */:res=(Character)value;break;
    case 6 /* Float.class     */:res=(Float)value;break;
    case 7 /* Double.class    */:res=(Double)value;break;
    case 0 /* null            */:
    case 9 /* String.class    */:
    case 8 /* Boolean.class   */:
    default:
    {
     if (value instanceof String)
     {
      try
      {
       res=Double.parseDouble(trim(replace(value.toString(), ",", null)));
      }
      catch (Throwable tr2)
      {
       throw new RuntimeException(toDouble_err+value.getClass().getName()+"'", tr2);
      }
     }
     else
     {
      throw new RuntimeException(toDouble_err+value.getClass().getName()+"'");
     }

    }
   }
  }

  return res;
 }




 public static int toInt(Object value)
 {
  return (int)toDouble(value);
 }





 /**
  *
  * "index-th" root of argument. Example: root(x,6) sixth root of x, root[tan(x),4] fourth root of the tangent of x.
  *
  * @param arg
  * @param index
  * @return
  */
 public static double root(double arg, double index)
 {
  if (index==2.0) return Math.sqrt(arg); // because maybe it is hardware accelerated!
  if (index==3.0) return Math.cbrt(arg); // because maybe it is hardware accelerated!
  return Math.pow(arg, 1.0/index); // surely NOT hardware accelerated!
 }


 //  round that returns a double!
 public static double round(double x)
 {
  return Math.round(x);
 }



 //  Cotangent
 public static double cot(double x)
 {
  return 1.0/Math.tan(x);
 }

 //  Secant of argument, equiv. to 1/cos(arg).
 public static double sec(double x)
 {
  return 1.0/Math.cos(x);
 }

 //  Cosecant, equiv. to 1/sin(arg).
 public static double csc(double x)
 {
  return 1.0/Math.sin(x);
 }


 // Logarithm base 'base' of a
 public static double logn(double a, double base)
 {
  return Math.log(a)/Math.log(base);
 }

 // Logarithm base 2 of a
 public static double log2(double a)
 {
  return logn(a, 2.0);
 }





 public static double logScaledDistance(double center, double x)
 {
  return logScaledDistance(10, 10, center, x);
 }


 public static double logScaledDistance(double mult, double center, double x)
 {
  return logScaledDistance(10, mult, center, x);
 }


 public static double logScaledDistance(double base, double mult, double center, double x)
 {
  if (x==center) return 0;
  double v, s;

  if (x>center)
  {
   v=x-center+1;
   s=mult;
  }
  else
  {
   v=-x+center+1;
   s=-mult;
  }

  if (base==Math.E) return s*Math.log(v);
  if (base==10) return s*Math.log10(v);
  return s*logn(v, base);
 }




/*
 // logScaledDistance tests: SUCCESS!
 public static void main(String args[])
 {
  int t, len=200;
  double x;

  for (t=0;t<len;t++)
  {
   x=((double)t)*0.1;
//   System.out.println(String.valueOf(x)+" ---> "+logScaledDistance(2, 2, 10, x));
   System.out.println(String.valueOf(x)+" ---> "+logScaledDistance(10, x));
  }
 }
*/





 public static long factorial(int value)
 {
  if (value<0) throw new IllegalArgumentException("please, factorial is defined only for non-negative integers!");
  if (value==0) return 1;
  long res=value;

  while (value>1)
  {
   value--;
   res*=value;
  }

  return res;
 }




 public static BigInteger bigFactorial(int value)
 {
  if (value<0) throw new IllegalArgumentException("please, factorial is defined only for non-negative integers!");
  if (value==0) return BigInteger.valueOf(1);
  BigInteger v=BigInteger.valueOf(value);
  BigInteger res=BigInteger.valueOf(value);

  while (v.compareTo(BigInteger.ONE)>0)
  {
   v=v.subtract(BigInteger.ONE);
   res=res.multiply(v);
  }

  return res;
 }



/*
 dunno if this is the right way to do it!!!

 public static long getBinomialCoefficient(int n, int k)
 {
  return factorial(n)/(factorial(k)*factorial(n-k));
 }
*/






 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // calcolo combinatorio




 /*  // permutation tests: SUCCESS!
 public static void main(String args[])
 {
  int def_testIndex=4;

  ArrayList<Object[]> res=getAllPermutations(0, 1, 2, 3, 4);//, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
  Object set[];
  int i, ilen, t, len=res.size();

  int perlen=getAllPermutationsCount(0, 1, 2, 3, 4);


  for (t=0;t<len;t++)
  {
   set=res.get(t);
   ilen=ArrayExtras.length(set);

   if (t==def_testIndex)
   {
    for (i=0;i<ilen;i++)
    {
     if (i>0) System.out.print(", ");
     System.out.print(set[i].toString());
    }

    System.out.println("   ");
   }

  }

  set=getPermutationByIndex(def_testIndex, 0, 1, 2, 3, 4);
  ilen=ArrayExtras.length(set);
  for (i=0;i<ilen;i++)
  {
   if (i>0) System.out.print(", ");
   System.out.print(set[i].toString());
  }


  System.out.println("number of permutations: "+len);
 }
 */


 /* // SUCCESS!
 public static void main(String args[])
 {
  long def_testIndex=12548799;
  Object elements[]=new Object[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
  long perlen=getAllPermutationsCount(elements);
  System.out.println("number of permutations: "+perlen);
  System.out.print("permutations number : "+def_testIndex+"  ----> ");
  Object set[]=getPermutationByIndex(def_testIndex, elements);
  int i, ilen=ArrayExtras.length(set);
  for (i=0;i<ilen;i++)
  {
   if (i>0) System.out.print(", ");
   System.out.print(set[i].toString());
  }

 }*/


 /* // SUCCESS!
 public static void main(String args[])
 {
  BigInteger def_testIndex=BigInteger.valueOf(1254879559);
  Object elements[]=new Object[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
  BigInteger perlen=getBigPermutationsCount(elements);  //def_testIndex=perlen.subtract(BigInteger.ONE).divide(BigInteger.valueOf(3));
  System.out.println("number of permutations: "+perlen.toString());
  System.out.print("permutations number : "+def_testIndex.toString()+"  ----> ");
  Object set[]=getBigPermutationByIndex(def_testIndex, elements);
  int i, ilen=ArrayExtras.length(set);
  for (i=0;i<ilen;i++)
  {
   if (i>0) System.out.print(", ");
   System.out.print(set[i].toString());
  }

 }//*/





 public static ArrayList<Object[]> getAllPermutations(Object... elements)
 {
  ArrayList<Object[]> res=new ArrayList<Object[]>();

  int len=ArrayExtras.length(elements);

  if (len>0)
  {
   int t, all=(int)getAllPermutationsCount(elements);

   for (t=0;t<all;t++)
   {
    res.add(getPermutationByIndex(t, elements));
   }
  }

  return res;
 }



 public static long getAllPermutationsCount(Object... elements)
 {
  long res=0;
  int len=ArrayExtras.length(elements);

  if (len>0)
  {
   res=factorial(len);
  }

  return res;
 }



 public static Object[] getPermutationByIndex(long permutationIndex, Object... elements)
 {
  Object swap, res[]=(Object[])ArrayExtras.clone(elements);
  long j, len=ArrayExtras.length(res);
  long idx=(getAllPermutationsCount(elements)-1)-permutationIndex;

  for (j=2;j<=len;j++)
  {
   idx=idx/(j-1);  // integer division cuts off the remainder
   swap=res[((int)(j-1))];
   res[((int)(j-1))]=res[((int)(idx % j))];
   res[((int)(idx % j))]=swap;
  }

  return res;
 }





 public static BigInteger getBigPermutationsCount(Object... elements)
 {
  BigInteger res=BigInteger.ZERO;
  int len=ArrayExtras.length(elements);

  if (len>0)
  {
   res=bigFactorial(len);
  }

  return res;
 }




 public static Object[] getBigPermutationByIndex(BigInteger permutationIndex, Object... elements)
 {
  Object swap, res[]=(Object[])ArrayExtras.clone(elements);
  long j, len=ArrayExtras.length(res);
  int i1, i2;
  BigInteger idx=getBigPermutationsCount(elements).subtract(BigInteger.ONE).subtract(permutationIndex);

  for (j=2;j<=len;j++)
  {
   idx=idx.divide(BigInteger.valueOf(j-1)); // integer division cuts off the remainder
   i1=(int)(j-1);
   i2=idx.mod(BigInteger.valueOf(j)).intValue();
   swap=res[i1];
   res[i1]=res[i2];
   res[i2]=swap;
  }

  return res;
 }

















 /**
  *   Restituisce una lista di array di tutte le combinazioni possibili di elements,
  *   senza duplicati, senza ordine, senza dimensione fissa di ogni combinazione.
  *   Per esempio data questa lista di elementi:
  *
  *            1, 2, 3
  *
  *   il risultato sarà:
  *
  *       1
  *       2
  *       3
  *       1, 2
  *       1, 3
  *       2, 3
  *       1, 2, 3
  *
  * @param elements
  * @return
  */
 public static ArrayList<Object[]> getAllCombinationsG(Object... elements)
 {
  ArrayList<Object[]> res=new ArrayList<Object[]>();

  int len=ArrayExtras.length(elements);

  if (len>0)
  {
   addAllCombinations(elements, 0, len-1, res);
  }

  return res;
 }




 private static void addAllCombinations(Object elements[], int firstIndex, int lastIndex, ArrayList<Object[]> res)
 {
  int t;
  ArrayList<Object[]> tmp;
  Object arr[];

  for (t=firstIndex;t<=lastIndex;t++)
  {
   res.add(new Object[]{elements[t]});
  }

  for (t=firstIndex;t<lastIndex;t++)
  {
   addAllCombinations(elements, t+1, lastIndex, tmp=new ArrayList<Object[]>());

   for (Object[] oa : tmp)
   {
    arr=new Object[oa.length+1];
    System.arraycopy(oa, 0, arr, 1, oa.length);
    arr[0]=elements[t];
    res.add(arr);
   }
  }
 }



 /**
  *
  *  Stesso di sopra solo che restituisce solo le combinazioni di combinationLength elementi
  *
  * @param combinationLength
  * @param elements
  * @return
  */

 public static ArrayList<Object[]> getAllCombinations(int combinationLength, boolean alsoAllPermutation, Object... elements)
 {
  ArrayList<Object[]> res=new ArrayList<Object[]>();
  ArrayList<Object[]> tmp=getAllCombinationsG(elements);

  for (Object[] ao : tmp)
  {
   if (ao.length==combinationLength)
   {
    res.add(ao);
   }
  }

  if (alsoAllPermutation && combinationLength>1 && res.size()>0)
  {
   ArrayList<Object[]> newRes=new ArrayList<Object[]>();

   for (Object ao[] : res)
   {
    tmp=getAllPermutations(ao);
    newRes.addAll(tmp);
   }

   res=newRes;
  }

  return res;
 }





 private static void _i_getAllCombinationsRO(List<Object[]> res, int combinationLength, int pos, Object... elements)
 {
  Object comb[];
  int t, elen=ArrayExtras.length(elements);

  if (pos==0)
  {
   for (t=0;t<elen;t++)
   {
    comb=new Object[combinationLength];
    comb[pos]=elements[t];
    res.add(comb);
   }
  }
  else
  {
   _i_getAllCombinationsRO(res, combinationLength, pos-1, elements);

   int r, ressize=res.size();

   for (t=0;t<elen;t++)
   {
    for (r=0;r<ressize;r++)
    {
     comb=res.get(r);

     if (t==0)
     {
      comb[pos]=elements[t];
     }
     else
     {
      comb=(Object[])ArrayExtras.clone(comb);
      comb[pos]=elements[t];
      res.add(comb);
     }
    }
   }
  }
 }


 /**
  * Returns all combinations of the elements whose lenght is combinationLength, has repetitions, has order relevance
  * So for example:
  * List&lt;Object[]&gt; bits=getAllCombinationsRO(elen, 0, 1);
  * gives all the 256 combations you would expect for the o and 1 values in a sequance of 8 bits!
  *
  * N.B.: order relevance means the 01 is different from 10 so both are returned not only one of them!!!
  *
  * @param combinationLength  fixed length for all the returned combinations
  * @param elements           the elements to combine!
  * @return                   the list of all the possibile combinations with repetitions and order relevance
  */
 public static List<Object[]> getAllCombinationsRO(int combinationLength, Object... elements)
 {
  int n=ArrayExtras.length(elements);
  int size=(int)Math.pow(n, combinationLength);
  List<Object[]> res=new ArrayList<Object[]>(size);

  _i_getAllCombinationsRO(res, combinationLength, combinationLength-1, elements);

  return res;
 }


/*
 // test for getAllCombinationsRO
 // SUCCESS!
 public static void main(String args[])
 {
  int elen=8;
  List<Object[]> bits=getAllCombinationsRO(elen, 0, 1);
  Object arr[];
  int p, t, len=bits.size();

  for (t=0;t<len;t++)
  {
   arr=bits.get(t);
   System.out.print(t+": ");

   for (p=0;p<elen;p++)
   {
    if (p>0) System.out.print(" ,");
    System.out.print(arr[p]);
   }

   System.out.println(" ");
  }

 }
*/





 public static double[] getFunctionYValues(int resultLenght, Function f, double startX, double endX, Object... constants)
 {
  return _i_getFunctionYValues(resultLenght, f, startX, endX, null, constants);
 }


 public static double[][] getFunctionXYValues(int resultLenght, Function f, double startX, double endX, Object... constants)
 {
  double res[][]=new double[2][];
  Box<double[]> x=new Box<>();
  double y[]=_i_getFunctionYValues(resultLenght, f, startX, endX, x, constants);

  res[0]=x.element;
  res[1]=y;

  return res;
 }



 public static double[] _i_getFunctionYValues(int resultLenght, Function f, double startX, double endX, Box<double[]> x, Object... constants)
 {
  if (resultLenght<2) resultLenght=2;
  int clen=ArrayExtras.length(constants);
  Object inputs[]=new Object[clen+1];
  for (int t=0;t<clen;t++) inputs[t+1]=constants[t];

  double _x[]=((x!=null)?new double[resultLenght]:null);
  double res[]=new double[resultLenght];

  inputs[0]=startX;
  res[0]=f.f(inputs);             if (x!=null) _x[0]=startX;

  inputs[0]=endX;
  res[resultLenght-1]=f.f(inputs);  if (x!=null) _x[resultLenght-1]=endX;

  if (resultLenght>2)
  {
   double step=(endX-startX)/((double)(resultLenght-1));
   double dx=step;
   double cx;

   for (int t=1;t<=resultLenght-2;t++)
   {
    cx=startX+dx;

    inputs[0]=cx;
    res[t]=f.f(inputs);               if (x!=null) _x[t]=cx;
    dx+=step;
   }
  }

  if (x!=null) x.element=_x;

  return res;
 }




 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // Uniqueness

 /**
  * Computes a sort of universally unique positive int identifier (UUID).
  * It is not really universally unique, but has high probability to be. Anyway the last word is from isUnique Predicate.
  * Use this method to generate unique int IDs for records in databases, but test it's real unicity before using the results.
  *
  *
  * @return a sort of universally unique positive int identifier (UUID)
  */
 public static int getUUintID(Predicate<Integer> isUnique)
 {
  String uniqueID;
  int res;

  do
  {
   uniqueID = UUID.randomUUID().toString();
   res=Math.abs(uniqueID.hashCode());
   if (res>100_000) res-=RandomExtras.intRandom(1, 99_000);

   while (!isUnique.test(res))
   {
//    logOut.println(res);

    if (res==0)
     break;

    res=res/RandomExtras.intRandom(2, 99);
   }

   if (res!=0)
    return res;

  } while (true);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 /*
 public static void main(String args[])
 {
  ArrayList<String> al=new ArrayList<String>();

  int t, len=12;
  for (t=0;t<len;t++)
  {
   al.add(new String(""+((char)('A'+t))));
  }

  String l[]=al.toArray(new String[al.size()]);

  ArrayList<Object[]> c=getAllCombinations(2, false, l);
  //ArrayList<Object[]> c=getAllCombinations(l);
  len=c.size();
  System.out.println(""+len+" combinations");
  if (len>3000) return;

  for (Object[] oa : c)
  {
   len=ArrayExtras.length(oa);

   for (t=0;t<len;t++)
   {
    if (t>0) System.out.print(", ");
    System.out.print(oa[t].toString());
   }

   System.out.println();
  }
 }
 */




/*
 public static void main(String args[])
 {
*/

  // test average

//  System.out.println(getAverage(new double[]{2, 4, 6}));


/*
  // random test

  int t, len=3000;

  for (t=0;t<len;t++)
  {
   System.out.println((int)random(2));
  }

*/



/*
  // test combinatorio
  int t, len;

//  ArrayList<Object[]> test=getAllCombinations(3, (Integer)0, 64, 128, 192, 255);
//  ArrayList<Object[]> test=getAllPermutations(1, 2, 3);
  ArrayList<Object[]> test=getAllCombinations(3, true, 0, 64, 128, 192, 255);


  for (Object[] oa : test)
  {
   len=ArrayExtras.length(oa);

   for (t=0;t<len;t++)
   {
    if (t>0) System.out.print(", ");
    System.out.print(((Integer)oa[t]).intValue());
   }

   System.out.println();
  }
*/


  /*
  // test pari dispari
  int t, len=50;

  for (t=0;t<len;t++)
  {
   System.out.println(""+t+" is "+((isEven(t))?" even":" odd"));
  }
  */


 /*
  // test somiglianza di serie (non funziona tanto bene!)

  getSimilarityPercent(//new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                       new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                       new double[]{1, 2, 3, 4, 3, 2, 1, 1, 9, 10});
 */



/*


 }
*/




 /*
 public static void main(String args[])
 {
  int irnds[]=new int[]{intRandom(100)-50, intRandom(100)-50, intRandom(100)-50};
  double drnds[]=new double[]{random(100), random(100), random(100)};
  int t, len=21470000;

  int ires=0;
  double dres=0;

  TimeCounter tc=TimeCounter.start();

  for (t=0;t<len;t++)
  {
   ires+=irnds[0];;//ires+=irnds[1];ires+=irnds[2];
   if (irnds[1]<10) ires*=irnds[2];
   if (irnds[1]<20) ires/=irnds[2];
   if (irnds[1]<30) ires*=irnds[2];
   if (irnds[1]<40) ires/=irnds[2];
   if (irnds[1]<50) ires*=irnds[2];

   / *
   dres+=drnds[0];
   dres*=Functions.cached_bipolar_sigmoid.f(drnds[1]);
   dres/=Functions.cached_hyptan.f(drnds[2]);
   * /

   //dres*=drnds[1];dres+=drnds[2];
  }

  tc.stop();System.out.println("elapesed: "+tc.getElapsedString());
  System.out.println(ValuesFormatter.f(ires));
  System.out.println(ValuesFormatter.f(dres));
 }
 */





 /*
 // DOES NOT WORK AT ALL!!!
 public static double speedSqrt(double v)
 {
  double res=v/2;
  double maxError=0.01;
  double error=1;
  double sup=res, inf=0;

  int rounds=0;

  while (error>maxError)
  {
   rounds++;


   error=res*res-v;

   if (error>0)
   {
    sup=res;
    res=(sup-inf)/2+inf;
   }
   else
   {
    error*=-1;

    inf=res;
    res=(sup-inf)/2+inf;
   }
  }

  return res;
 }


 public static void main(String args[])
 {
  int r, t, len=300000, reps=10;
  double v[]=new double[len];
  double res[]=new double[len];

  for (t=0;t<len;t++)
  {
   v[t]=9+RandomExtras.random(10000);
  }

  // to let it compile
  for (t=0;t<Math.min(1000, len);t++) res[t]=Math.sqrt(v[t]);
  for (t=0;t<Math.min(1000, len);t++) res[t]=speedSqrt(v[t]);

  TimeCounter tc;


  tc=TimeCounter.start();
  for (r=0;r<reps;r++){for (t=0;t<len;t++) res[t]=Math.sqrt(v[t]);}
  System.out.println("Math.sqrt: "+tc.stopAndGetElapsedString());
  System.out.flush();

  tc=TimeCounter.start();
  for (r=0;r<reps;r++){for (t=0;t<len;t++) res[t]=speedSqrt(v[t]);}
  System.out.println("speedSqrt: "+tc.stopAndGetElapsedString());



 }

*/


}




