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


import me.as.lib.core.lang.ArrayExtras;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.math.MathExtras.decimalDigits;

//import org.uncommons.maths.random.*;


public class RandomExtras
{

 public static Random newRandomNumberGenerator()
 {
//  return new CellularAutomatonRNG(ByteExtras.intToBytes((new Random()).nextInt()));
  return new Random();
 }


 private static Random randomNumberGenerator=newRandomNumberGenerator();


 /**
  *
  * @param howMany - max 10,000
  * @param minValue - min -1,000,000,000
  * @param maxValue - max 1,000,000,000
  * @return
  */
 public static int[] getRandomNumbersFromRandomOrg(int howMany, int minValue, int maxValue)
 {
  int res[];

  if (howMany<1) throw new RuntimeException("'howMany' parameter is invalid (must be bigger than zero)");
  if (minValue<-1000000000) throw new RuntimeException("'minValue' is too small (min allowed is -1,000,000,000)");
  if (maxValue>1000000000) throw new RuntimeException("'maxValue' is too biig (max allowed is 1,000,000,000)");

  //HttpURLConnection conne=new HttpURLConnection();

  try
  {
   URL url=new URL("http://www.random.org/cgi-bin/randnum?num="+howMany+"&min="+minValue+"&max="+maxValue+"&col=1");
   HttpURLConnection conne=(HttpURLConnection)url.openConnection();
   conne.setDoInput(true);
   conne.setAllowUserInteraction(false);
   conne.setDoOutput(false);
   conne.setUseCaches(false);
   conne.connect();

   InputStream       is  = conne.getInputStream();
   InputStreamReader isr = new InputStreamReader(is);
   BufferedReader    br  = new BufferedReader(isr);

   String line;
   int c=0;
   res=new int[howMany];

   do
   {
    line=br.readLine();

    try
    {
     res[c]=Integer.parseInt(line.trim());
     c++;
    } catch (Throwable ignore){}

   } while(line!=null);

   br.close();
   isr.close();
   is.close();
   conne.disconnect();

  }
  catch (Throwable tr)
  {
   throw new RuntimeException("Errors occurred during the excution of getRandomNumbersFromRandomOrg", tr);
  }

  return res;
 }



 public static Random getRandomNumberGenerator()
 {
  return randomNumberGenerator;
 }


 public static double random()
 {
  return random(randomNumberGenerator, 1.0);
 }

 public static double random(double range)
 {
  return random(randomNumberGenerator, range);
 }

 public static double random(Random rnd, double range)
 {
  return rnd.nextDouble()*range;
 }




/*
 public static double random(Random rnd, double range)
 {
  double res=0;

  try
  {
   res=randomNumberGenerator.nextDouble()*range;
  }
  catch (Throwable tr)
  {
   boolean showException=false;

   synchronized (MathExtras.class)
   {
    if (randomNumberGenerator==null)
    {
     randomNumberGenerator=new Random();
    } else showException=true;
   }

   if (showException)
   {
    systemErrDeepCauseStackTrace(tr);
    tr.printStackTrace();
   } else res=random(range);
  }

  return res;
 }
*/


 public static <O> O randomSelect(O... objs)
 {
  O res=null;
  int len=ArrayExtras.length(objs);

  if (len==1) res=objs[0];
  else
  {
   if (len>1) res=objs[((int)random(len))];
  }

  return res;
 }


 public static double random(double rangeMin, double rangeMax)
 {
  return random(randomNumberGenerator, rangeMin, rangeMax);
 }


 public static double random(Random rnd, double rangeMin, double rangeMax)
 {
  double min=Math.min(rangeMin, rangeMax);
  double max=Math.max(rangeMin, rangeMax);
  return random(rnd, max-min)+min;
 }


 public static double random(double rangeMin, double rangeMax, double step)
 {
  return random(randomNumberGenerator, rangeMin, rangeMax, step);
 }


 public static double random(Random rnd, double rangeMin, double rangeMax, double step)
 {
  if (step<=0)
   throw new IllegalArgumentException("Invalid step value: "+step);

  double min=Math.min(rangeMin, rangeMax);
  double max=Math.max(rangeMin, rangeMax);

  int numSteps=(int)((max-min)/step)+1;
  int randomStep=(int)(rnd.nextDouble()*numSteps);

  return MathExtras.truncate(min+randomStep*step, decimalDigits(step));
 }



 public static long longRandom()
 {
  return getRandomNumberGenerator().nextLong();
 }


 /**
  *        For example intRandom(3) can return one of these:
  *               0, 1, 2, 3
  * @param maxValue
  * @return
  */
 public static long longRandom(long maxValue)
 {
  return (long)Math.floor((random((maxValue+1)*1000)/1000));
 }


 public static long longRandom(long minValue, long maxValue)
 {
  return longRandom(maxValue-minValue)+minValue;
 }


 public static int intRandom()
 {
  return getRandomNumberGenerator().nextInt();
 }


 /**
  *        For example intRandom(3) can return one of these:
  *               0, 1, 2, 3
  * @param maxValue
  * @return
  */
 public static int intRandom(int maxValue)
 {
  return getRandomNumberGenerator().nextInt(maxValue+1);
//  return (int)Math.floor((random((maxValue+1)*1000)/1000));
 }

 public static int intRandom(Random rnd, int minValue, int maxValue)
 {
  return rnd.nextInt((maxValue-minValue)+1)+minValue;
//  return ((int)Math.floor((random(rnd, (maxValue-minValue+1)*1000)/1000)))+minValue;
 }


 /**
  * @return  a random value between minValue and maxValue INCLUDING both of them!
  */
 public static int intRandom(int minValue, int maxValue)
 {
  return intRandom(maxValue-minValue)+minValue;
 }

/*
 public static void main(String args[])
 {
  int rMin=0;
  int rMax=128;
  boolean minFound=false;
  boolean maxFound=false;

  for (;;)
  {
   int val=intRandom(rMin, rMax);
   if (val==rMin) minFound=true;
   if (val==rMax) maxFound=true;

   if (val<rMin || val>rMax)
   {
    System.out.println("????? "+val);
   }

   if (minFound && maxFound)
   {
    System.out.println("All found!");
    break;
   }
  }
 }
*/


 public static double gaussianRandom(double rangeMin, double rangeMax, double gcr[])
 {
  int idx=compositeRandom(gcr);
  double sign=((idx>gcr.length/2)?1:-1);
  double res=random(10-gcr[idx]);
  double half=(rangeMax-rangeMin)/2.0;
  res=rangeMin+half+sign*((res/10.0)*half);
  return res;

  /*
  double res=random(10-gcr[compositeRandom(gcr)]);
  double half=(rangeMax-rangeMin)/2.0;
  res=rangeMin+half+signum(random(-1, 1))*((res/10.0)*half);
  return res;
  */
 }



 /**
  * So this method works this way: given some ranges a random number is generated and the index this number lies in is returned.
  * Example! With:
  * compositeRandom(10, 10, 10)
  * a random number is generated in the range 0 ~ 30:
  * if it is > 20 then 2 is returned,
  * if it is > 10 then 1 is returned,
  * else 0 is returned
  *
  * @param ranges
  * @return
  *
  *
  */
 public static int compositeRandom(double... ranges)
 {
  return compositeRandom(randomNumberGenerator, ranges);
 }



 public static int compositeRandom(Random rnd, double... ranges)
 {
  int t, len=ranges.length;
  double all=0;

  for (t=0;t<len;t++) all+=ranges[t];
  return compositeRandom(rnd, all, ranges);
 }


 private static int compositeRandom(Random rnd, double cachedAll, double... ranges)
 {
  int res=-1;
  int t, len=ranges.length;
  double rres=rnd.nextDouble()*cachedAll;

  for (t=len-1;t>=0 && res==-1;t--)
  {
   cachedAll-=ranges[t];
   if (rres>cachedAll) res=t;
  }

  return res;
 }










 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 static class SplineCompositeCache
 {
//  double ranges[];
  double sums[];
  double cachedAll;

 }





 /*

 // RANDOM GENERATORS TEST!

 public static void main(String args[])
 {
  final int w=160;
  final int h=120;
  final Color grays[]=new Color[256];
  int t;

  for (t=0;t<grays.length;t++) grays[t]=new Color(t, t, t);

  JFrame f=new JFrame();
  Container c=f.getContentPane();
  c.setLayout(new BorderLayout());
  c.add(new JComponent()
  {
   protected void paintComponent(Graphics g)
   {
    Dimension d=getSize();
    double dwp=((double)d.width)/((double)w);
    double dhp=((double)d.height)/((double)h);
    int wp=(int)(dwp);while (wp<dwp) wp++;
    int hp=(int)(dhp);while (hp<dhp) hp++;
    int x, y;

    for (x=0;x<w;x++)
    {
     for (y=0;y<h;y++)
     {
      g.setColor(grays[((int)random(255))]);
      g.fillRect((int)(x*dwp), (int)(y*dhp), wp, hp);
     }
    }

    repaint();
   }

  }, BorderLayout.CENTER);

  f.setBounds(10, 10, 320, 240);
  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  f.setVisible(true);
 }
 */


 /*
 public static void main(String args[])
 {
  String objs[]=new String[]{"A", "B", "C", "D", "E", "F", "G"};
  int counts[]=new int[objs.length];

  int t, len=100;
  for (t=0;t<len;t++) counts[ArrayExtras.select(objs, randomSelect(objs))]++;

  len=objs.length;
  for (t=0;t<len;t++) System.out.println(objs[t]+": "+counts[t]);
 }
 */


 /*

   // intRandom SUCCESS!

  public static void main(String args[])
  {
   int t, len=10;

   for (t=0;t<len;t++)
   {
    System.out.println("intRandom(3): "+intRandom(3));
   }
  }
 */





 /*
public static void main(String args[])
{
TimeCounter tc=TimeCounter.start();
int t, len=750000;
double f=0;

for (t=0;t<len;t++)
{
f+=gaussianRandom(-0.5, 0.5);
//f+=random(-0.5, 0.5);
}

System.out.println(f);
tc.stop();
System.out.println(tc.getElapsedString());
}
 */




 /*
public static void main(String args[])
{
int t, len=100;

for (t=0;t<len;t++)
{
System.out.println(StringExtras.formatDouble(gaussianRandom(-2.5, 2.5), 5, 5, Pasquale.ITALIAN));
}
}            */






 //
 /* TEST: SUCCESS
  //funziona perfettamente, magari solo un po' 'distributionModifier' sembra avere poca escursione, ma il suo lavoro lo fa

  public static void main(String args[])
  {
   double gcr[]=createGaussianCompositeRandomRanges(30, 0);
   //double gcr[]=createGaussianCompositeRandomRanges(30, 1);
   //double gcr[]=createGaussianCompositeRandomRanges(30, -1);
   //double gcr[]=createGaussianCompositeRandomRanges(30, 0.02);
   //double gcr[]=createGaussianCompositeRandomRanges(30, -0.02);

   int t, len=ArrayExtras.length(gcr);

   for (t=0;t<len;t++)
   {
    System.out.println(StringExtras.formatDouble(gcr[t], Pasquale.ITALIAN));
   }
  }

 //
 */


 /*
  // test of compositeRandom: SUCCESS
  public static void main(String args[])
  {
   int t, len=100;
   for (t=0;t<len;t++)
   {
    System.out.println(MathExtras.compositeRandom(100, 10, 10));
   }
  }
 */


}
