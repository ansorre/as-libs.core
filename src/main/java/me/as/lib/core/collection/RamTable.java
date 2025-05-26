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

package me.as.lib.core.collection;


import me.as.lib.core.extra.QSortable;
import me.as.lib.core.extra.QuickSort;
import me.as.lib.core.lang.StringExtras;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.math.MathExtras;
import me.as.lib.core.lang.ObjectExtras;
import me.as.lib.core.log.Logable;
import me.as.lib.core.log.LogableHandler;

import static me.as.lib.core.lang.StringExtras.considerableFalse;
import static me.as.lib.core.lang.StringExtras.considerableTrue;
import static me.as.lib.core.lang.ArrayExtras.changeArrayType;
import static me.as.lib.core.lang.StringExtras.doTheyMatch;
import static me.as.lib.core.lang.StringExtras.grantLength;
import static me.as.lib.core.lang.StringExtras.hasChars;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.putInSystemClipboard;
import static me.as.lib.core.lang.StringExtras.replace;

class OneRow
{
 static final int dis=5;
 Object cels[]=null;
 int curCols=0;

 public OneRow()
 {

 }

 public OneRow(int forcedcolscount)
 {
  forceColsCount(forcedcolscount);
 }


 public void setObject(int col, Object value)
 {
  if (value==null && col>=curCols) return;
  if (cels==null) resizeCels(((col>dis)?col+dis:dis));
  if (col>=cels.length) resizeCels(col+dis);
  cels[col]=value;
  if (col>=curCols) curCols=col+1;
 }

 private void resizeCels(int newsize)
 {
  int t;
  Object tmp[]=new Object[newsize];
  //for (t=0;t<newsize;t++) tmp[t]=null;
  if (cels!=null) System.arraycopy(cels, 0, tmp, 0, ((cels.length<tmp.length)?cels.length:tmp.length));
  cels=tmp;
 }


 public void forceColsCount(int cols)
 {
  /*
  if (cols>curCols)
  {
   //resizeCels(cols+dis);
   if (cels!=null)
   {
    if (cels.length>cols+dis) resizeCels(cols+dis);
   }
  }
  */
  if ((cels!=null && cels.length<cols) || cels==null) resizeCels(cols+dis);
  curCols=cols;
 }


 public void insertCols(int colsInsertPoint, int numOfColsToBeInserted)
 {
  int _cols=curCols;
  forceColsCount(_cols+numOfColsToBeInserted);
  System.arraycopy(cels, colsInsertPoint, cels, colsInsertPoint+numOfColsToBeInserted, _cols-colsInsertPoint);

  int c;
  for (c=colsInsertPoint;c<colsInsertPoint+numOfColsToBeInserted;c++)
  {
   cels[c]=null;
  }
 }

 public void cutCols(int colIdx, int numOfColsToCutOut)
 {
  if (cels!=null)
  {
   if (colIdx<curCols)
   {
    if (numOfColsToCutOut+colIdx>curCols)
    {
     numOfColsToCutOut=curCols-colIdx;
    }

    if (colIdx==curCols-1)
    {
     cels[colIdx]=null;
     curCols--;
    }
    else
    {
     System.arraycopy(cels, colIdx+numOfColsToCutOut, cels, colIdx, cels.length-(colIdx+numOfColsToCutOut));
     curCols-=numOfColsToCutOut;
     for (int t=curCols;t<cels.length;t++) cels[t]=null;
    }
   }
  }
 }


 public Object getObject(int col)
 {
  if (col>=0 && col<curCols) return cels[col];
  else return null;
 }

}




class sortParams
{
 int sortType; // 0 = ByInt   1 = ByDouble   2 = ByFloat   3 = ByString
 int sortColNo;
 boolean DESC;

 Comparable cmidValue;
 int    imidValue;
 double dmidValue;
 float  fmidValue;
 String smidValue;
 long   lmidValue;

 public sortParams(int sortType, int sortColNo, boolean DESC)
 {
  this.sortType=sortType;
  this.sortColNo=sortColNo;
  this.DESC=DESC;
 }

}



public class RamTable implements QSortable
{
 public static final RamTable emptyRamTable=new RamTable();

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public final static int SORT_BY_INT_ASC      =  1;
 public final static int SORT_BY_INT_DESC     =  2;

 public final static int SORT_BY_DOUBLE_ASC   =  3;
 public final static int SORT_BY_DOUBLE_DESC  =  4;

 public final static int SORT_BY_FLOAT_ASC    =  5;
 public final static int SORT_BY_FLOAT_DESC   =  6;

 public final static int SORT_BY_STRING_ASC   =  7;
 public final static int SORT_BY_STRING_DESC  =  8;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private int dis;
 private boolean superSpeed;
 private int curCols=0;
 private int curRows=0;

 private OneRow rows[]=null;



 public RamTable()
 {
  this(50, true);
 }

 public RamTable(int dis)
 {
  this(dis, true);
 }

 public RamTable(int dis, boolean ss)
 {
  setDefaultRowIncrement(dis);
  setSuperSpeed(ss);
 }


 public void setDefaultRowIncrement(int dis)
 {
  this.dis=dis;
 }

 public void setSuperSpeed(boolean ss)
 {
  this.superSpeed=ss;
 }


 public synchronized void setMid(int mididx, Object params)
 {
  int sortColNo=((sortParams)params).sortColNo;

  switch (((sortParams)params).sortType)
  {
   case -1:((sortParams)params).cmidValue=(Comparable)getObject(sortColNo, mididx);break;
   case 0:((sortParams)params).imidValue=getInt(sortColNo, mididx);break;
   case 1:((sortParams)params).dmidValue=getDouble(sortColNo, mididx);break;
   case 2:((sortParams)params).fmidValue=getFloat(sortColNo, mididx);break;
   case 3:((sortParams)params).smidValue=getString(sortColNo, mididx);break;
   case 4:((sortParams)params).lmidValue=getLong(sortColNo, mididx);break;
  }
 }


 // must return:
 // <0 if elem1<mid
 // 0 if elem1==mid
 // >0 if elem1>mid
 public synchronized int compareToMid(int elem1, Object params)
 {
  int res=0;
  int sortColNo=((sortParams)params).sortColNo;

  switch (((sortParams)params).sortType)
  {
   case -1:
        {
         Comparable e1=(Comparable)getObject(sortColNo, elem1);
         Comparable e2=((sortParams)params).cmidValue;
         res=e1.compareTo(e2);
        } break;

   case 0:
        {
         int e1=getInt(sortColNo, elem1);
         int e2=((sortParams)params).imidValue;
         res=((e1<e2)?-1:((e1>e2)?1:0));
        } break;

   case 1:
        {
         double e1=getDouble(sortColNo, elem1);
         double e2=((sortParams)params).dmidValue;
         res=((e1<e2)?-1:((e1>e2)?1:0));
        } break;

   case 2:
        {
         float e1=getFloat(sortColNo, elem1);
         float e2=((sortParams)params).fmidValue;
         res=((e1<e2)?-1:((e1>e2)?1:0));
        } break;

   case 3:
        {
         String e1=getString(sortColNo, elem1);
         String e2=((sortParams)params).smidValue;

         if (e1==null && e2==null) res=0;
         else
         {
          if (e1==null) res=-1;
          else
          {
           if (e2==null) res=1;
           else res=e1.compareTo(e2);
          }
         }
        } break;

   case 4:
    {
     long e1=getLong(sortColNo, elem1);
     long e2=((sortParams)params).lmidValue;
     res=((e1<e2)?-1:((e1>e2)?1:0));
    } break;
  }

  if (((sortParams)params).DESC) res*=-1;

  return res;
 }


 public synchronized boolean swap(int elem1, int elem2, Object params)
 {
  swapRows(elem1, elem2);
  return true;
 }


 public synchronized void swapRows(int row1, int row2)
 {
  int max=((row1>row2)?row1:row2);

  if (max>=curRows)
  {
   // giusto per far creare la righa
   setObject(0, max, 1);
   setObject(0, max, null);
  }

  if (row1!=row2)
  {
   OneRow r1=rows[row1];
   rows[row1]=rows[row2];
   rows[row2]=r1;
  }
 }


 // for sortTypes use the "SORT_BY_xxxx" flags
 public synchronized void sortMoreColumns(final int cols[], final int sortTypes[])
 {
  if (curRows>0 && cols!=null && sortTypes!=null && cols.length==sortTypes.length && cols.length>0)
    new QuickSort(
       new QSortable()
       {
        int nuc=cols.length;
        int ints[]=new int[nuc];
        double doubles[]=new double[nuc];
        float floats[]=new float[nuc];
        String strings[]=new String[nuc];

        public void setMid(int mididx, Object params)
        {
         int c;

         for (c=0;c<nuc;c++)
         {
          switch (sortTypes[c])
          {
           case SORT_BY_INT_ASC:
           case SORT_BY_INT_DESC:
                {
                 ints[c]=getInt(cols[c], mididx);
                } break;

           case SORT_BY_DOUBLE_ASC:
           case SORT_BY_DOUBLE_DESC:
                {
                 doubles[c]=getDouble(cols[c], mididx);
                } break;

           case SORT_BY_FLOAT_ASC:
           case SORT_BY_FLOAT_DESC:
                {
                 floats[c]=getFloat(cols[c], mididx);
                } break;

           case SORT_BY_STRING_ASC:
           case SORT_BY_STRING_DESC:
                {
                 strings[c]=getString(cols[c], mididx);
                } break;
          }
         }
        }

        // must return:
        // <0 if elem1<mid
        // 0 if elem1==mid
        // >0 if elem1>mid
        public int compareToMid(int elem1, Object params)
        {
         int res=0;
         int c;

         for (c=0;c<nuc && res==0;c++)
         {
          switch (sortTypes[c])
          {
           case SORT_BY_INT_ASC:
           case SORT_BY_INT_DESC:
                {
                 int e1=getInt(cols[c], elem1);
                 int e2=ints[c];
                 res=((e1<e2)?-1:((e1>e2)?1:0));
                 if (sortTypes[c]==SORT_BY_INT_DESC) res*=-1;
                } break;

           case SORT_BY_DOUBLE_ASC:
           case SORT_BY_DOUBLE_DESC:
                {
                 double e1=getDouble(cols[c], elem1);
                 double e2=doubles[c];
                 res=((e1<e2)?-1:((e1>e2)?1:0));
                 if (sortTypes[c]==SORT_BY_DOUBLE_DESC) res*=-1;
                } break;

           case SORT_BY_FLOAT_ASC:
           case SORT_BY_FLOAT_DESC:
                {
                 float e1=getFloat(cols[c], elem1);
                 float e2=floats[c];
                 res=((e1<e2)?-1:((e1>e2)?1:0));
                 if (sortTypes[c]==SORT_BY_FLOAT_DESC) res*=-1;
                } break;

           case SORT_BY_STRING_ASC:
           case SORT_BY_STRING_DESC:
                {
                 String e1=getString(cols[c], elem1);
                 String e2=strings[c];

                 if (e1==null && e2==null) res=0;
                 else
                 {
                  if (e1==null) res=-1;
                  else
                  {
                   if (e2==null) res=1;
                   else res=e1.compareTo(e2);
                  }
                 }

                 if (sortTypes[c]==SORT_BY_STRING_DESC) res*=-1;
                } break;
          }
         }

         return res;
        }

        public boolean swap(int elem1, int elem2, Object params)
        {
         swapRows(elem1, elem2);
         return true;
        }
       }, 0, curRows-1, null);
 }



 public synchronized void sortByComparable(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(-1, col, false));
 }


 public synchronized void sortByInt(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(0, col, false));
 }

 public synchronized void sortByDouble(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(1, col, false));
 }

 public synchronized void sortByFloat(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(2, col, false));
 }

 public synchronized void sortByString(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(3, col, false));
 }

 public synchronized void sortByLong(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(4, col, false));
 }



 public synchronized void sortByComparableDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(-1, col, true));
 }


 public synchronized void sortByIntDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(0, col, true));
 }

 public synchronized void sortByDoubleDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(1, col, true));
 }

 public synchronized void sortByFloatDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(2, col, true));
 }

 public synchronized void sortByStringDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(3, col, true));
 }

 public synchronized void sortByLongDESC(int col)
 {
  if (curRows>0) new QuickSort(this, 0, curRows-1, new sortParams(4, col, true));
 }




 public synchronized void clear()
 {
  curCols=0;
  curRows=0;
  rows=null;
 }

 public boolean isEmpty()
 {
  return isClear();
 }

 public synchronized boolean isClear()
 {
  return (curCols==0 || curRows==0);
 }



 public boolean equals(Object obj)
 {
  boolean res=false;

  if (obj!=null)
  {
   if (obj instanceof RamTable)
   {
    RamTable ct=(RamTable)obj;
    int c, cc=ct.getColsCount();
    int r, rc=ct.getRowsCount();

    if (cc==getColsCount() &&
        rc==getRowsCount())
    {
     res=true;

     for (c=0;c<cc && res;c++)
     {
      for (r=0;r<rc && res;r++)
      {
       res=ObjectExtras.areEqual(ct.getObject(c, r), getObject(c, r));
      }
     }
    }
   }
  }

  return res;
 }




 public boolean columnsAreEqual(int col, RamTable comparable)
 {
  int r, len=getRowsCount();
  boolean res=(len==comparable.getRowsCount());

  if (res && len>0)
  {
   for (r=0;r<len && res;r++)
   {
    res=ObjectExtras.areEqual(getObject(col, r), comparable.getObject(col, r));
   }
  }

  return res;
 }


 public boolean rowsAreEqual(int row, RamTable comparable)
 {
  int c, len=getColsCount();
  boolean res=(len==comparable.getColsCount());

  if (res && len>0)
  {
   for (c=0;c<len && res;c++)
   {
    res=ObjectExtras.areEqual(getObject(c, row), comparable.getObject(c, row));
   }
  }

  return res;
 }












 public synchronized void reverseRows()
 {
  if (rows!=null && curRows>0)
  {
   int t, len=ArrayExtras.length(rows);
   OneRow tmp[]=new OneRow[len];

   for (t=0;t<curRows;t++)
   {
    tmp[t]=rows[curRows-1-t];
   }

   rows=tmp;
  }
 }




 public synchronized void compact()
 {
  forceColsAndRows(curCols, curRows);
 }



 public synchronized void forceColsAndRows(int cols, int rows)
 {
  forceRowsCount(rows);
  forceColsCount(cols);
 }

 public synchronized void forceColsCount(int cols)
 {
  /*
  if (curCols>cols)
  {
   int t;
   for (t=0;t<curRows;t++)
   {
    if (rows[t]!=null) rows[t].forceColsCount(cols);
   }
  }
  */

  int t;
  for (t=0;t<curRows;t++)
  {
   if (rows[t]!=null) rows[t].forceColsCount(cols);
   else rows[t]=new OneRow(cols);
  }

  curCols=cols;
 }

 public synchronized void forceRowsCount(int rows)
 {
  //if (curRows>rows) resizeRows(rows+dis);
  resizeRows(rows+dis);
  curRows=rows;
 }

 public synchronized int getColsCount()
 {
  return curCols;
 }

 public synchronized int getRowsCount()
 {
  return curRows;
 }



 public synchronized void setContent(int howManyColumns, Object... data)
 {
  int c=0, r=0;
  int t, len=ArrayExtras.length(data);

  for (t=0;t<len;t++)
  {
   setObject(c, r, data[t]);
   c++;
   if (c==howManyColumns)
   {
    c=0;
    r++;
   }
  }
 }












 public synchronized void fillColumn(int col, Object o, int startRow, int endRow)
 {
  for (int r=startRow;r<=endRow;r++)
  {
   setObject(col, r, o);
  }
 }


 public synchronized void fillRow(int row, Object o, int startCol, int endCol)
 {
  for (int c=startCol;c<=endCol;c++)
  {
   setObject(c, row, o);
  }
 }

 public synchronized void setCol(int col, Object o[])
 {
  setCol(col, o, 0);
 }

 public synchronized void setCol(int col, Object o[], int startRow)
 {
  int t, len=ArrayExtras.length(o);

  for (t=0;t<len;t++)
  {
   setObject(col, t+startRow, o[t]);
  }
 }

 public synchronized void setRow(int row, Object o[])
 {
  setRow(row, o, 0);
 }

 public synchronized void setRow(int row, Object o[], int startCol)
 {
  int t, len=ArrayExtras.length(o);

  for (t=0;t<len;t++)
  {
   setObject(t+startCol, row, o[t]);
  }
 }


 public synchronized void setString(int col, int row, String value)
 {
  setObject(col, row, value);
 }

 public synchronized void setLong(int col, int row, long value)
 {
  setObject(col, row, value);
 }

 public synchronized void setInt(int col, int row, int value)
 {
  setObject(col, row, value);
 }


 public synchronized void setBoolean(int col, int row, boolean value)
 {
  setObject(col, row, value);
 }


 public synchronized void setDouble(int col, int row, double value)
 {
  setObject(col, row, value);
 }

 public synchronized void setFloat(int col, int row, float value)
 {
  setObject(col, row, value);
 }

 private synchronized void resizeRows(int newsize)
 {
  int t;
  OneRow tmp[]=new OneRow[newsize];
  //for (t=0;t<newsize;t++) tmp[t]=null;
  if (rows!=null && curRows>0) System.arraycopy(rows, 0, tmp, 0, ((rows.length<tmp.length)?rows.length:tmp.length));
  rows=tmp;
 }

 /*
 public synchronized void setObject(int col, int row, Object value)
 {
  if (value==null && (col>=curCols || row>=curRows)) return;
  if (rows==null) resizeRows(((row>dis)?row+dis:dis));
  if (row>=rows.length) resizeRows(row+dis);
  if (rows[row]==null) rows[row]=new OneRow();
  rows[row].setObject(col, value);

  if (col>=curCols) curCols=col+1;
  if (row>=curRows) curRows=row+1;
 }
 */

 private void _slow_setObject(int col, int row, Object value)
 {
  System.out.println("_slow_");

  if (value==null && (col>=curCols || row>=curRows)) return;
  if (rows==null) resizeRows(((row>dis)?row+dis:dis));
  if (row>=rows.length) resizeRows(row+dis);
  if (rows[row]==null) rows[row]=new OneRow();
  rows[row].setObject(col, value);

  if (col>=curCols) curCols=col+1;
  if (row>=curRows) curRows=row+1;
 }

 public synchronized void setObject(int col, int row, Object value)
 {
  try
  {
   OneRow r=rows[row];
   r.cels[col]=value;
   if (col>=r.curCols) r.curCols=col+1;
   if (col>=curCols) curCols=col+1;
   if (row>=curRows) curRows=row+1;
  }
  catch (Throwable tr)
  {
   if (col<0) throw new ArrayIndexOutOfBoundsException("Invalid column index: "+col);
   if (row<0) throw new ArrayIndexOutOfBoundsException("Invalid row index: "+row);

   if (superSpeed)
   {
    int r_curRows=curRows;
    int r_curCols=curCols;

    int maxc=((curCols>col)?curCols:col);
    int maxr=((curRows>row)?curRows:row);

    forceColsAndRows(maxc+OneRow.dis, maxr+dis);

    curRows=r_curRows;
    curCols=r_curCols;
    setObject(col, row, value);
   } else _slow_setObject(col, row, value);
  }
 }


 public synchronized void setEmpty(int col, int row)
 {
  setObject(col, row, null);
 }


 public synchronized String[] getStringsFromRow(int row)
 {
  return getStringsFromRow(row, 0, curCols);
 }

 public synchronized String[] getStringsFromRow(int row, int firstCol, int cols)
 {
  return (String[])changeArrayType(getObjectsFromRow(row, firstCol, cols), "java.lang.String");
 }


 public synchronized String[] getStringsFromColumn(int col)
 {
  return getStringsFromColumn(col, 0, curRows);
 }

 public synchronized String[] getStringsFromColumn(int col, int firstRow, int rows)
 {
  return (String[])changeArrayType(getObjectsFromColumn(col, firstRow, rows), "java.lang.String");
 }



 public synchronized Object[] getObjectsFromRow(int row)
 {
  return getObjectsFromRow(row, 0, curCols);
 }

 public synchronized Object[] getObjectsFromRow(int row, int firstCol, int cols)
 {
  Object res[]=null;

  if (!isEmpty() && firstCol<curCols)
  {
   if (curCols-cols<firstCol) cols=curCols-firstCol;
   res=new Object[cols];

   int t;
   for (t=0;t<cols;t++)
   {
    res[t]=getObject(t+firstCol, row);
   }
  }

  return res;
 }


 public synchronized Object[] getObjectsFromColumn(int col)
 {
  return getObjectsFromColumn(col, 0, curRows);
 }

 public synchronized Object[] getObjectsFromColumn(int col, int firstRow, int rows)
 {
  Object res[]=null;

  if (!isEmpty() && firstRow<curRows)
  {
   if (curRows-rows<firstRow) rows=curRows-firstRow;
   res=new Object[rows];

   int t;
   for (t=0;t<rows;t++)
   {
    res[t]=getObject(col, t+firstRow);
   }
  }

  return res;
 }




 public synchronized Object getObjectsFromRow(int row, String arrayElementType)
 {
  return changeArrayType(getObjectsFromRow(row), arrayElementType);
 }

 public synchronized Object getObjectsFromRow(int row, int firstCol, int cols, String arrayElementType)
 {
  return changeArrayType(getObjectsFromRow(row, firstCol, cols), arrayElementType);
 }


 public synchronized Object getObjectsFromColumn(int col, String arrayElementType)
 {
  return changeArrayType(getObjectsFromColumn(col), arrayElementType);
 }

 public synchronized Object getObjectsFromColumn(int col, int firstRow, int rows, String arrayElementType)
 {
  return changeArrayType(getObjectsFromColumn(col, firstRow, rows), arrayElementType);
 }







 public synchronized String getString(int col, int row)
 {
  String res=null;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   res=ores.toString();
  }

  return res;
 }

 public synchronized long getLong(int col, int row)
 {
  long res=0;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   if (ores instanceof Float) res=((Float)ores).longValue();
   if (ores instanceof Double) res=((Double)ores).longValue();
   if (ores instanceof Long) res=((Long)ores).longValue();
   if (ores instanceof Integer) res=((Integer)ores).longValue();
   //if (ores instanceof String) res=(new Long((String)ores)).longValue();
   if (ores instanceof String) res=Long.parseLong((String)ores);
  }

  return res;
 }


 public synchronized int getInt(int col, int row)
 {
  int res=0;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   if (ores instanceof Float) res=((Float)ores).intValue();
   if (ores instanceof Double) res=((Double)ores).intValue();
   if (ores instanceof Long) res=((Long)ores).intValue();
   if (ores instanceof Integer) res=((Integer)ores).intValue();
   //if (ores instanceof String) res=(new Integer((String)ores)).intValue();
   if (ores instanceof String) res=Integer.parseInt((String)ores);
  }

  return res;
 }


 public synchronized boolean getBoolean(int col, int row)
 {
  boolean res=false;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   if (ores instanceof Boolean)
   {
    Boolean casted=(Boolean)ores;
    res=casted.booleanValue();
   }
   else
   {

    try
    {
     res=(MathExtras.toDouble(ores)!=0);
    }
    catch (Throwable tr)
    {
     try
     {
      res=(StringExtras.select(considerableTrue, ores.toString())>=0);
     }
     catch (Throwable tr2)
     {

      if (StringExtras.select(considerableFalse, ores.toString())>=0) res=false;

     }
    }
   }
  }

  return res;
 }








 public synchronized double getDouble(int col, int row)
 {
  double res=0;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   if (ores instanceof Float) res=((Float)ores).doubleValue();
   if (ores instanceof Double) res=((Double)ores).doubleValue();
   if (ores instanceof Long) res=((Long)ores).doubleValue();
   if (ores instanceof Integer) res=((Integer)ores).doubleValue();
   //if (ores instanceof String) res=(new Double((String)ores)).doubleValue();
   if (ores instanceof String) res=Double.parseDouble((String)ores);
  }

  return res;
 }

 public synchronized float getFloat(int col, int row)
 {
  float res=0;
  Object ores=getObject(col, row);

  if (ores!=null)
  {
   if (ores instanceof Float) res=((Float)ores).floatValue();
   if (ores instanceof Double) res=((Double)ores).floatValue();
   if (ores instanceof Long) res=((Long)ores).floatValue();
   if (ores instanceof Integer) res=((Integer)ores).floatValue();
   //if (ores instanceof String) res=(new Float((String)ores)).floatValue();
   if (ores instanceof String) res=Float.parseFloat((String)ores);
  }

  return res;
 }

 public synchronized Object getObject(int col, int row)
 {
  if (row>=0 && row<curRows)
  {
   return ((rows[row]!=null)?rows[row].getObject(col):null);
  } else return null;
 }


 public synchronized boolean isEmpty(int col, int row)
 {
  return (getObject(col, row)==null);
 }


 public synchronized int findInCol(int col, String value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (StringExtras.areEqual(getString(col, t), value)) res=t;
   }
  }

  return res;
 }

 public synchronized int findInCol(int col, int value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (getInt(col, t)==value) res=t;
   }
  }

  return res;
 }




 public synchronized int findInCol(int col, double value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (getDouble(col, t)==value) res=t;
   }
  }

  return res;
 }


 public synchronized int findInCol(int col, boolean value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (getBoolean(col, t)==value) res=t;
   }
  }

  return res;
 }



 public synchronized int findInCol(int col, float value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (getFloat(col, t)==value) res=t;
   }
  }

  return res;
 }


 public synchronized int findInCol(int col, Object value, int startRow)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    //    if (getObject(col, t).equals(value)) res=t;
    if (ObjectExtras.areEqual(getObject(col, t), value)) res=t;
   }
  }

  return res;
 }









 public synchronized int findInRow(int row, String value, int startCol)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (getString(t, row).equals(value)) res=t;
   }
  }

  return res;
 }

 public synchronized int findInRow(int row, int value, int startCol)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (getInt(t, row)==value) res=t;
   }
  }

  return res;
 }

 public synchronized int findInRow(int row, double value, int startCol)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (getDouble(t, row)==value) res=t;
   }
  }

  return res;
 }

 public synchronized int findInRow(int row, float value, int startCol)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (getFloat(t, row)==value) res=t;
   }
  }

  return res;
 }


 public synchronized int findInRow(int row, Object value, int startCol)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (getObject(t, row).equals(value)) res=t;
   }
  }

  return res;
 }




 public synchronized int findMatchInCol(int col, int startRow, String match_mask, boolean caseSensitive)
 {
  int res=-1;

  if (startRow<curRows)
  {
   int t;

   for (t=startRow;t<curRows && res==-1;t++)
   {
    if (doTheyMatch(getString(col, t), match_mask, caseSensitive)) res=t;
   }
  }

  return res;
 }



 public synchronized int findMatchInRow(int row, int startCol, String match_mask, boolean caseSensitive)
 {
  int res=-1;

  if (startCol<curCols)
  {
   int t;

   for (t=startCol;t<curCols && res==-1;t++)
   {
    if (doTheyMatch(getString(t, row), match_mask, caseSensitive)) res=t;
   }
  }

  return res;
 }






 public synchronized void append(RamTable rt)
 {
  int rows=((rt!=null)?rt.curRows:0);

  if (rows>0)
  {
   int c, cols=rt.curCols;
   int r, dr=curRows;

   for (r=0;r<rows;r++)
   {
    for (c=0;c<cols;c++) setObject(c, dr, rt.getObject(c, r));
    dr++;
   }
  }
 }






 public synchronized void appendRow(RamTable rt, int rowNo)
 {
  if (rt!=null)
  {
   if (rt.getRowsCount()>rowNo)
   {
    int c, ccount=rt.getColsCount();

    if (ccount>0)
    {
     int rcount=getRowsCount();

     for (c=0;c<ccount;c++)
     {
      setObject(c, rcount, rt.getObject(c, rowNo));
     }
    }
   }
  }
 }

 public synchronized boolean testLIKEOnCell(int colNo, int rowNo, String sub, boolean CaseSensitive)
 {
  boolean res=false;
  String cells=null;

  try
  {
   cells=getString(colNo, rowNo);
  } catch (Exception e){cells=null;}

  if (sub==null && cells==null) res=true;
  else
  {
   if (sub==null || cells==null) res=false;
   else
   {
    if (CaseSensitive)
    {
     cells=cells.toUpperCase();
     sub=sub.toUpperCase();
    }

    res=(cells.indexOf(sub)!=-1);
   }
  }

  return res;
 }

 // CaseSensitive=false
 public synchronized RamTable getLIKERows(int colNo, String sub)
 {
  return getLIKERows(colNo, sub, false);
 }

 public synchronized RamTable getLIKERows(int colNo, String sub, boolean CaseSensitive)
 {
  RamTable res=null;

  if (colNo>0 && colNo<getColsCount())
  {
   int r, count=getRowsCount();

   if (count>0)
   {
    for (r=0;r<count;r++)
    {
     if (testLIKEOnCell(colNo, r, sub, CaseSensitive))
     {
      if (res==null) res=new RamTable();
      res.appendRow(this, r);
     }
    }
   }
  }

  return res;
 }




 public synchronized void insertRows(int rowsInsertPoint, int numOfRowsToBeInserted)
 {
  int _rows=getRowsCount();
  int _cols=getColsCount();

  if (_cols>0 && numOfRowsToBeInserted>0)
  {
   if (rowsInsertPoint>=_rows) forceRowsCount(_rows+numOfRowsToBeInserted);
   else
   {
    if (rowsInsertPoint<_rows && rowsInsertPoint>=0)
    {
     forceRowsCount(_rows+numOfRowsToBeInserted);
     System.arraycopy(rows, rowsInsertPoint, rows, rowsInsertPoint+numOfRowsToBeInserted, _rows-rowsInsertPoint);

     int r, c;

     for (r=rowsInsertPoint;r<rowsInsertPoint+numOfRowsToBeInserted;r++)
     {
      rows[r]=null;
     }
    }
   }
  }
 }

 public synchronized void copyRows(RamTable sourceRt, int sourceStartRow, int destStartRow, int numOfRowsToCopy)
 {
  int colsCount=sourceRt.getColsCount();

  if (colsCount>0 && numOfRowsToCopy>0)
  {
   int c, sr=sourceStartRow;
   int dr=destStartRow;
   int count=0;

   for (count=0;count<numOfRowsToCopy;count++)
   {
    for (c=0;c<colsCount;c++)
    {
     setObject(c, dr, sourceRt.getObject(c, sr));
    }

    sr++;
    dr++;
   }
  }

 }




 public synchronized void cutRows(int rowIdx, int numOfRowsToCutOut)
 {
  if (rowIdx<curRows)
  {
   if (numOfRowsToCutOut+rowIdx>curRows || numOfRowsToCutOut>curRows)
   {
    numOfRowsToCutOut=curRows-rowIdx;
   }

   if (rowIdx==curRows-1)
   {
    curRows--;
   }
   else
   {
    System.arraycopy(rows, rowIdx+numOfRowsToCutOut, rows, rowIdx, rows.length-(rowIdx+numOfRowsToCutOut));
    curRows-=numOfRowsToCutOut;
   }
  }
 }




 public synchronized void insertCols(int colsInsertPoint, int numOfColsToBeInserted)
 {
  int _rows=getRowsCount();
  int _cols=getColsCount();

  if (_cols>0 && numOfColsToBeInserted>0)
  {
   if (colsInsertPoint>=_cols) forceColsCount(_cols+numOfColsToBeInserted);
   else
   {
    if (colsInsertPoint<_cols && colsInsertPoint>=0)
    {
     int r, c;
     for (r=0;r<_rows;r++)
     {
      rows[r].insertCols(colsInsertPoint, numOfColsToBeInserted);
     }

     curCols=_cols+numOfColsToBeInserted;
    }
   }
  }
 }


 public synchronized void cutCols(int colIdx, int numOfColsToCutOut)
 {
  int _rows=getRowsCount();
  int _cols=getColsCount();

  if (_rows>0 && _cols>colIdx && colIdx>=0 && numOfColsToCutOut>0)
  {
   // we really need this line of code even if it seems redundant.
   // Infact if one passes numOfColsToCutOut=Integer.MAX_VALUE... you know!
   if (numOfColsToCutOut>curCols) numOfColsToCutOut=curCols;

   if (numOfColsToCutOut+colIdx>curCols)
   {
    numOfColsToCutOut=curCols-colIdx;
   }

   int r;

   for (r=_rows-1;r>=0;r--)
   {
    if (rows[r]!=null)
    {
     rows[r].cutCols(colIdx, numOfColsToCutOut);
    }
   }

//   forceColsCount(curCols-numOfColsToCutOut);
   curCols-=numOfColsToCutOut;
  }
 }




 public synchronized String[] columnToStrings(int colIdx)
 {
  String res[]=null;

  if (!isEmpty())
  {
   int r, rows=getRowsCount();
   res=new String[rows];

   for (r=0;r<rows;r++) res[r]=getString(colIdx, r);
  }

  return res;
 }


 public synchronized String[][] toStrings(boolean firstAreColumnsIndex)
 {
  String res[][]=null;

  if (curCols>0 && curRows>0)
  {
   if (firstAreColumnsIndex)
   {
    res=new String[curCols][curRows];
    int c, r;
    for (c=0;c<curCols;c++)
    {
     for (r=0;r<curRows;r++) res[c][r]=getString(c, r);
    }
   }
   else
   {
    res=new String[curRows][curCols];
    int c, r;
    for (c=0;c<curCols;c++)
    {
     for (r=0;r<curRows;r++) res[r][c]=getString(c, r);
    }
   }
  }

  return res;
 }


 public synchronized String[][] toStrings()
 {
  return toStrings(true);
 }



 public synchronized Object[] columnToObjects(int colIdx)
 {
  Object res[]=null;

  if (!isEmpty())
  {
   int r, rows=getRowsCount();
   res=new Object[rows];

   for (r=0;r<rows;r++) res[r]=getObject(colIdx, r);
  }

  return res;
 }


 public synchronized Object[][] toObjects(boolean firstAreColumnsIndex)
 {
  Object res[][]=null;

  if (curCols>0 && curRows>0)
  {
   if (firstAreColumnsIndex)
   {
    res=new Object[curCols][curRows];
    int c, r;
    for (c=0;c<curCols;c++)
    {
     for (r=0;r<curRows;r++) res[c][r]=getObject(c, r);
    }
   }
   else
   {
    res=new Object[curRows][curCols];
    int c, r;
    for (c=0;c<curCols;c++)
    {
     for (r=0;r<curRows;r++) res[r][c]=getObject(c, r);
    }
   }
  }

  return res;
 }

 public synchronized Object[][] toObjects()
 {
  return toObjects(true);
 }




 public synchronized Object clone()
 {
  RamTable res=new RamTable();

  int _rows=getRowsCount();
  int _cols=getColsCount();

  if (_rows>0 && _cols>0)
  {
   res.rows=new OneRow[_rows+dis];
   int r, c, cc;

   for (r=0;r<_rows;r++)
   {
    if (rows[r]!=null)
    {
     res.rows[r]=new OneRow();

     if (rows[r].cels!=null)
     {
      cc=rows[r].curCols;

      if (cc>0)
      {
       res.rows[r].cels=new Object[cc+OneRow.dis];
       res.rows[r].curCols=cc;

       System.arraycopy(rows[r].cels, 0, res.rows[r].cels, 0, cc);

       /*
       for (c=0;c<cc;c++)
       {
        res.rows[r].cels[c]=rows[r].cels[c];
       } */
      }
     }
    } else res.rows[r]=null;
   }

   res.curCols=curCols;
   res.curRows=curRows;
  }


  return res;
 }





 public void copyToSystemClipboard()
 {
  int rows=getRowsCount();
  int cols=getColsCount();
  int r, c;
  String tmpStr;
  StringBuilder sb=new StringBuilder();

  for (r=0;r<rows;r++)
  {
   for (c=0;c<cols;c++)
   {
    tmpStr=getString(c, r);
    if (isNotBlank(tmpStr)) sb.append(tmpStr);
    if (c<cols-1) sb.append("\t");
   }

   if (r<rows-1) sb.append("\n");
  }

  final String testo=sb.toString();
  putInSystemClipboard(testo);
 }




 public String toAutoFormattedString(String columnSeparator, String eol)
 {
  StringBuilder sb=new StringBuilder();
  int cols=getColsCount();
  int rows=getRowsCount();

  if (rows>0 && cols>0)
  {
   String tmpStr;
   int c, r;
   int colSizes[]=new int[cols];

   for (c=0;c<cols;c++)
   {
    colSizes[c]=0;

    for (r=0;r<rows;r++)
    {
     tmpStr=getString(c, r);

     if (hasChars(tmpStr))
     {
      tmpStr=replace(tmpStr, "\n", "\\n");
      tmpStr=replace(tmpStr, "\r", "\\r");
      if (tmpStr.length()>colSizes[c]) colSizes[c]=tmpStr.length();
     }
    }

//    if (colSizes[c]>40) colSizes[c]=40;
//    if (colSizes[c]<3) colSizes[c]=3;
   }


   for (r=0;r<rows;r++)
   {
    for (c=0;c<cols;c++)
    {
     tmpStr=getString(c, r);

     if (hasChars(tmpStr))
     {
      tmpStr=replace(tmpStr, "\n", "\\n");
      tmpStr=replace(tmpStr, "\r", "\\r");

/*
      if (tmpStr.length()>colSizes[c-1])
      {
       tmpStr=tmpStr.substring(0, colSizes[c-1]-3);
       tmpStr+="...";
      }
*/
     }

     tmpStr=grantLength(tmpStr, colSizes[c], ' ', true);
     if (c>0) sb.append(columnSeparator);
     sb.append(tmpStr);
    }

    sb.append(eol);
   }
  }

  return sb.toString();
 }





 protected String getLogableColumnLabel(int col)
 {
  return String.valueOf(col);
 }

 protected String getLogableRowLabel(int row)
 {
  return String.valueOf(row);
 }




 public void debugTable()
 {
  debugTable(null, null, "NO TITLE", Integer.MAX_VALUE);
 }

 public void debugTable(int limit)
 {
  debugTable(null, null, "NO TITLE", limit);
 }

 public void debugTable(String title)
 {
  debugTable(null, null, title, Integer.MAX_VALUE);
 }


 public void debugTable(Logable log, String traceLevels, String title)
 {
  debugTable(log, traceLevels, title, Integer.MAX_VALUE);
 }

 public void debugTable(Logable log, String traceLevels, String title, int limit)
 {
  if (log==null) log=new LogableHandler(System.out);
  if (!isNotBlank(traceLevels)) traceLevels="*";

  log.println(traceLevels, "");
  log.println(traceLevels, "------- debugTable --- begin -------------------------------");
  log.println(traceLevels, ((title!=null) ? ((title.length()>0) ? title:"NO TITLE"):"NO TITLE"));
  log.println(traceLevels, "------------------------------------------------------------");

  int cols=getColsCount();
  int rows=getRowsCount();
  if (rows>limit) rows=limit;

  if (rows==0 || cols==0)
  {
   log.println(traceLevels, "The table is empty ! ! !");
  }
  else
  {
   String tmpStr;
   int rowlength=0;
   int c, r;
   int colSizes[]=new int[cols];

   log.println(traceLevels, "Columns: "+cols+", Rows: "+rows);
   log.println(traceLevels, "------------------------------------------------------------");
   log.println(traceLevels, "");

   for (c=0;c<cols;c++)
   {
    colSizes[c]=0;

    tmpStr=getLogableColumnLabel(c);
    if (hasChars(tmpStr))
    {
     if (tmpStr.length()>colSizes[c]) colSizes[c]=tmpStr.length();
    }

    for (r=0;r<rows;r++)
    {
     tmpStr=getString(c, r);
     if (hasChars(tmpStr))
     {
      if (tmpStr.length()>colSizes[c]) colSizes[c]=tmpStr.length();
     }
    }

    if (colSizes[c]>40) colSizes[c]=40;
    if (colSizes[c]<3) colSizes[c]=3;
   }

   for (c=0;c<cols;c++)
   {
    rowlength+=colSizes[c]+4;
   }

   rowlength+=10;

   for (r=0;r<=rows;r++)
   {
    for (c=0;c<=cols;c++)
    {
     if (c==0)
     {
      if (r>0)
      {
       tmpStr=grantLength(getLogableRowLabel(r-1), 4, ' ', true);
       log.print(traceLevels, " ");
       log.print(traceLevels, tmpStr);
       log.print(traceLevels, " | ");
      } else log.print(traceLevels, "      | ");
     }
     else
     {
      if (r==0)
      {
       //tmpStr=StringExtras.grantLength(""+(c-1), colSizes[c-1], ' ', true);
       tmpStr=grantLength(getLogableColumnLabel(c-1), colSizes[c-1], ' ', true);
       log.print(traceLevels, " ");
       log.print(traceLevels, tmpStr);
       log.print(traceLevels, " | ");
      }
      else
      {
       tmpStr=getString(c-1, r-1);

       if (hasChars(tmpStr))
       {
        tmpStr=replace(tmpStr, "\n", "\\n");
        tmpStr=replace(tmpStr, "\r", "\\r");

        if (tmpStr.length()>colSizes[c-1])
        {
         tmpStr=tmpStr.substring(0, colSizes[c-1]-3);
         tmpStr+="...";
        }
       }

       tmpStr=grantLength(tmpStr, colSizes[c-1], ' ', false);

       log.print(traceLevels, " ");
       log.print(traceLevels, tmpStr);
       log.print(traceLevels, " | ");
      }
     }
    }

    log.println(traceLevels, "");

    for (c=0;c<rowlength;c++)
    {
     log.print(traceLevels, "-");
    }

    log.println(traceLevels, "");
   }
  }

  log.println(traceLevels, "");
  log.println(traceLevels, "------- debugTable --- end   -------------------------------");
  log.println(traceLevels, "");
  log.println(traceLevels, "");
 }



}








