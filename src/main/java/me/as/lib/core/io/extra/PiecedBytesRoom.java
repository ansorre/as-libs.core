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

package me.as.lib.core.io.extra;


import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.extra.TimeCounter;
import me.as.lib.core.io.BytesRoom;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.StringExtras;

import static me.as.lib.core.math.RandomExtras.compositeRandom;
import static me.as.lib.core.math.RandomExtras.random;
import static me.as.lib.core.lang.StringExtras.defaultCharsetName;
import static me.as.lib.core.lang.StringExtras.getBytes;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.newAutoString;
import static me.as.lib.core.system.FileSystemExtras.getTemporaryFileName;

public class PiecedBytesRoom
{
 private static final String privSig = "PBR-1.00";
 private static final int minReusableFreeBytes = 100;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private byte signatureBytes[];
 private BytesRoom room;
 private long pieces;
 private int atSize;
 private int frSize;
 private int frLength;

 private long piecesFP;
 private long firstFRFP;
 private long firstATFP;

 private AllocationTable atCursor;
 private FreeRoom frCursor;
 private RoomCursor roomCursor=new RoomCursor();



 public PiecedBytesRoom()
 {
  this(null, new MemBytesRoom());
 }


 public PiecedBytesRoom(String signature, BytesRoom room)
 {
  StringBuilder sb=new StringBuilder();
  if (isNotBlank(signature)) sb.append(signature);
  sb.append(privSig);

  try
  {
   signatureBytes=sb.toString().getBytes(defaultCharsetName);
  } catch (Throwable tr){fireException(tr);}

  this.room=room;
  atSize=100;
  frSize=100;

  if (room.getSize()==0) create();
  startup();
 }


 private void startup()
 {
  room.setCurrentPosition(0);
  byte b[]=new byte[signatureBytes.length];
  room.Read(b);
  if (!ArrayExtras.areEqual(b, signatureBytes))
  {
   throw new me.as.lib.core.io.IOException("The provided BytesRoom is not of the type PiecedBytesRoom");
  }


  try
  {
   atSize=room.readInt();
   frSize=room.readInt();
   piecesFP=room.getCurrentPosition();
   pieces=room.readLong();

   frCursor=new FreeRoom();
   firstFRFP=room.getCurrentPosition();
   frCursor.load();
   frLength=(frCursor.linkers.length*8)+(frCursor.pointers.length*8)+(frCursor.freeBytes.length*4);

   atCursor=new AllocationTable();
   firstATFP=room.getCurrentPosition();
   atCursor.load();
  } catch (Throwable tr){fireException(tr);}
 }


 private void create()
 {
  try
  {
   room.Write(signatureBytes);
   room.writeInt(atSize);
   room.writeInt(frSize);
   room.writeLong(0); // zero pieces!

   frCursor=new FreeRoom();
   frCursor.save();

   atCursor=new AllocationTable();
   atCursor.save();
  } catch (Throwable tr){fireException(tr);}
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private void fireException(Throwable tr)
 {
  throw new me.as.lib.core.io.IOException(tr);
 }


 private void checkIndex(long pieceIndex)
 {
  if (pieceIndex<0 || pieceIndex>=pieces)
  {
   throw new IndexOutOfBoundsException("There are "+pieces+" pieces but was requeted "+pieceIndex);
  }
 }



 private static final int at_1=0;
 /*private static final int at_10=1;
 private static final int at_50=2;
 private static final int at_100=3;
 private static final int at_500=4;
 private static final int at_1000=5;*/
 private static final int _at_size=6;

 private static final long mults[]=new long[]{1, 10, 50, 100, 500, 1000};

 private static final int pPrev=0;
 private static final int pNext=1;
 private static final int _p_size=2;



 void saveArr(long arr[])
 {
  try
  {
   int t, len=arr.length;
   for (t=0;t<len;t++) room.writeLong(arr[t]);
  } catch (Throwable tr){fireException(tr);}
 }


 void loadArr(long arr[])
 {
  try
  {
   int t, len=arr.length;
   for (t=0;t<len;t++) arr[t]=room.readLong();
  } catch (Throwable tr){fireException(tr);}
 }


 void saveArr(int arr[])
 {
  try
  {
   int t, len=arr.length;
   for (t=0;t<len;t++) room.writeInt(arr[t]);
  } catch (Throwable tr){fireException(tr);}
 }


 void loadArr(int arr[])
 {
  try
  {
   int t, len=arr.length;
   for (t=0;t<len;t++) arr[t]=room.readInt();
  } catch (Throwable tr){fireException(tr);}
 }





 class AllocationTable
 {
  int modified=0;
  long filePos=0;
  long firstPieceId=0;

  long nextATs[]=new long[_at_size];
  long prevATs[]=new long[_at_size];
  long table[]=new long[atSize];


  /*
  void deb()
  {
   try
   {
    long fs=room.getSize();
    int t, len=nextATs.length;

    for (t=0;t<len;t++)
    {
     if (nextATs[t]>fs) throw new Throwable("BUG!");
     if (prevATs[t]>fs) throw new Throwable("BUG!");
    }

   }
   catch (Throwable tr)
   {
    systemErrDeepCauseStackTrace(tr);
    tr.printStackTrace();
   }

  }


  void saveNext(){deb();saveArr(nextATs);}
  void savePrev(){deb();saveArr(prevATs);}
  void loadNext(){loadArr(nextATs);deb();}
  void loadPrev(){loadArr(prevATs);deb();}
  */


  void saveNext(){saveArr(nextATs);}
  void savePrev(){saveArr(prevATs);}
  void loadNext(){loadArr(nextATs);}
  void loadPrev(){loadArr(prevATs);}



  void save()
  {
   saveNext();
   savePrev();
   saveArr(table);
  }


  void load()
  {
   filePos=room.getCurrentPosition();
   loadNext();
   loadPrev();
   loadArr(table);
  }


  void checkSave()
  {
   if (modified>0)
   {
    room.setCurrentPosition(filePos);
    saveNext();
    savePrev();

    if (modified>1) saveArr(table);

    modified=0;
   }
  }


  void jumpTo(long atFilePos, long atDist)
  {
   filePos=atFilePos;
   firstPieceId+=atDist;
   room.setCurrentPosition(filePos);
   loadNext();
   loadPrev();
  }




  void storeNewPiece(long firstPieceFilePos)
  {
   table[((int)(pieces-firstPieceId))]=firstPieceFilePos;
   modified=2;
  }


  long getFirstFilePos(long pieceIndex)
  {
   return table[((int)(pieceIndex-firstPieceId))];
  }

  void setFirstFilePos(long pieceIndex, long firstFilePos)
  {
   if (table[((int)(pieceIndex-firstPieceId))]!=firstFilePos)
   {
    table[((int)(pieceIndex-firstPieceId))]=firstFilePos;
    modified=2;
   }
  }


  void reachPiece(long pieceIndex)
  {
   int prevDir=0;
   boolean jumped=false;
   int et, t, len;
   long diff, toFind;
   boolean mustChange=true;
   if (pieceIndex>pieces) pieceIndex=pieces;


   while (mustChange)
   {
    mustChange=(pieceIndex<firstPieceId || pieceIndex>firstPieceId+(table.length-1));

    if (mustChange)
    {
     checkSave();

     if (pieceIndex>firstPieceId+(table.length-1)) // andare avanti
     {
      if (nextATs[at_1]==0) // non c'� altre tabelle, ne va creata una nuova
      {
       AllocationTable tmpAt=new AllocationTable();
       AllocationTable at=new AllocationTable();

       at.filePos=room.getSize();

       nextATs[at_1]=at.filePos;modified++;checkSave(); // salviamo il prossimo dentro di me

       at.prevATs[at_1]=filePos; // io sono il suo precedente

       len=prevATs.length;
       for (t=1;t<len;t++)
       {
        if (prevATs[t]!=0)
        {
         room.setCurrentPosition(prevATs[t]);
         tmpAt.loadNext();
         tmpAt.loadPrev();

         at.prevATs[t]=tmpAt.nextATs[at_1];

         room.setCurrentPosition(at.prevATs[t]);
         tmpAt.filePos=at.prevATs[t];
         tmpAt.loadNext();
         tmpAt.loadPrev();

         tmpAt.nextATs[t]=at.filePos;
         tmpAt.modified++;
         tmpAt.checkSave();
        }
        else
        {
         toFind=firstPieceId+atSize-(mults[t]*atSize);
         if (toFind==0)
         {
          at.prevATs[t]=firstATFP;

          room.setCurrentPosition(at.prevATs[t]);
          tmpAt.filePos=at.prevATs[t];
          tmpAt.loadNext();
          tmpAt.loadPrev();

          tmpAt.nextATs[t]=at.filePos;
          tmpAt.modified++;
          tmpAt.checkSave();
         }
        }
       }

       room.setCurrentPosition(at.filePos);
       at.save();
       modified=0;
       filePos=at.filePos;
       firstPieceId+=atSize;
       nextATs=at.nextATs;
       prevATs=at.prevATs;
       table=at.table;
       at=null; // mi sono transmutato nella nuova tabella!
      }
      else // c'� altre tabelle avanti! andare avanti
      {
       diff=(pieceIndex-firstPieceId)/atSize;
       len=mults.length;

       for (t=1;t<len;t++)
       {
        if (mults[t]>diff)
        {
         if (nextATs[t]!=0 && (prevDir==0 || prevDir==1) && diff>5 && mults[t]-diff<diff-mults[t-1])
         {
          jumpTo(nextATs[t], mults[t]*atSize);
         }
         else
         {
          et=t-1;
          while (nextATs[et]==0) et--;
          jumpTo(nextATs[et], mults[et]*atSize);
         }

         jumped=true;
         break;
        }
       }

       prevDir=1;
      }
     }
     else // andare indietro
     {
      diff=(firstPieceId-pieceIndex)/atSize;
      len=mults.length;

      for (t=1;t<len;t++)
      {
       if (mults[t]>diff)
       {
        if (prevATs[t]!=0 && (prevDir==0 || prevDir==-1) && diff>5 && mults[t]-diff<diff-mults[t-1])
        {
         jumpTo(prevATs[t], -mults[t]*atSize);
        }
        else
        {
         et=t-1;
         while (prevATs[et]==0) et--;
         jumpTo(prevATs[et], -mults[et]*atSize);
        }

        jumped=true;
        break;
       }
      }

      prevDir=-1;
     }
    }
   }

   if (jumped)
   {
    room.setCurrentPosition(filePos);
    load();
   }

  }


 }



 class FreeRoom
 {
  int modified=0;
  long filePos=0;

  long linkers[]=new long[_p_size];
  long pointers[]=new long[frSize];
  int freeBytes[]=new int[frSize];


  void save()
  {
   saveArr(linkers);
   saveArr(pointers);
   saveArr(freeBytes);
  }

  void load()
  {
   filePos=room.getCurrentPosition();
   loadArr(linkers);
   loadArr(pointers);
   loadArr(freeBytes);
  }


  void checkSave()
  {
   if (modified>0)
   {
    room.setCurrentPosition(filePos);
    saveArr(linkers);

    if (modified>1)
    {
     saveArr(pointers);
     saveArr(freeBytes);
    }

    modified=0;
   }
  }




  int unused()
  {
   int res=0, t, len=pointers.length;

   for (t=0;t<len;t++)
   {
    if (pointers[t]==0) res++;
   }

   return res;
  }


  void merge(FreeRoom fr, int linkIdx, int todo)
  {
   /*if (to do>1)
   {
    System.out.println("   ");
   }             */


   linkers[linkIdx]=fr.linkers[linkIdx];
   if (linkers[linkIdx]!=0)
   {
    long link=linkers[linkIdx];
    FreeRoom tmpFr=new FreeRoom();
    room.setCurrentPosition(link);
    loadArr(tmpFr.linkers);
    tmpFr.linkers[((linkIdx==pNext)?pPrev:pNext)]=filePos;
    room.setCurrentPosition(link);
    saveArr(tmpFr.linkers);
   }

   modified=2;
   int t, o=0;
   int made=0;
   int len=pointers.length;

   for (t=0;t<len && made<todo;t++)
   {
    if (pointers[t]==0)
    {
     if (made==0)
     {
      pointers[t]=fr.filePos;
      freeBytes[t]=frLength;
      made++;
     }
     else
     {
      while (pointers[t]==0)
      {
       if (fr.pointers[o]!=0)
       {
        pointers[t]=fr.pointers[o];
        freeBytes[t]=fr.freeBytes[o];
        made++;
       }

       o++;
      }
     }
    }
   }
  }




  void prune()
  {
   boolean tryNext=(linkers[pNext]!=0);
   boolean tryPrev=(linkers[pPrev]!=0 && linkers[pPrev]!=firstFRFP);

   if (tryNext || tryPrev)
   {
    int used, unused=unused();

    if (unused>0)
    {
     FreeRoom fr=new FreeRoom();

     if (tryNext)
     {
      room.setCurrentPosition(linkers[pNext]);
      fr.load();
      used=frSize-fr.unused();

      if (unused>=used+1) merge(fr, pNext, used+1);
     }


     if (tryPrev)
     {
      unused=unused();

      if (unused>0)
      {
       room.setCurrentPosition(linkers[pPrev]);
       fr.load();
       used=frSize-fr.unused();

       if (unused>=used+1) merge(fr, pPrev, used+1);
      }
     }
    }
   }

   //throw new StillUnimplemented();

  }



  boolean innerFreeRoom(long filePoz, int freeBytez)
  {
   boolean res=false;
   int t, len=pointers.length;

   for (t=0;t<len && !res;t++)
   {
    if (pointers[t]==0)
    {
     pointers[t]=filePoz;
     freeBytes[t]=freeBytez;
     modified=2;
     res=true;
    }
   }

   return res;
  }



  void freeRoom(long filePoz, int freeBytez)
  {
   if (freeBytez<minReusableFreeBytes) return;

   boolean freed=innerFreeRoom(filePoz, freeBytez);

   if (!freed)
   {
    long startPos=filePos;
    checkSave();

    boolean moved=false;

    // provo avanti
    while (!freed && linkers[pNext]!=0)
    {
     room.setCurrentPosition(linkers[pNext]);
     load();
     freed=innerFreeRoom(filePoz, freeBytez);
     moved=true;
    }

    if (!freed && moved)
    {
     room.setCurrentPosition(startPos);
     load();
    }

    // provo indietro
    while (!freed && linkers[pPrev]!=0)
    {
     room.setCurrentPosition(linkers[pPrev]);
     load();
     freed=innerFreeRoom(filePoz, freeBytez);
    }
   }

   if (!freed) // non c'� spazio, dobbiamo creare una nuova tabella di spazi vuoti
   {
    FreeRoom fr=new FreeRoom();
    fr.filePos=room.getSize();

    fr.linkers[pNext]=linkers[pNext];
    linkers[pNext]=fr.filePos;
    modified++;
    checkSave();

    fr.linkers[pPrev]=filePos;
    fr.pointers[0]=filePoz;
    fr.freeBytes[0]=freeBytez;
    fr.modified=2;
    fr.checkSave();

    modified=0;
    filePos=fr.filePos;
    linkers=fr.linkers;
    pointers=fr.pointers;
    freeBytes=fr.freeBytes;

    if (fr.linkers[pNext]!=0)
    {
     long link=fr.linkers[pNext];
     fr=new FreeRoom();
     room.setCurrentPosition(link);
     loadArr(fr.linkers);
     fr.linkers[pPrev]=filePos;
     room.setCurrentPosition(link);
     saveArr(fr.linkers);
    }

    fr=null; // mi sono transmutato nella nuova tabella!
   }
  }


  void getRoom(int neededBytes)
  {
   getRoom(neededBytes, true);
  }




  void getRoom(int neededBytes, boolean nesting)
  {
   roomCursor.filePos=0;
   roomCursor.freeBytes=0;

   int remainings, t, len=pointers.length;

   for (t=0;t<len;t++)
   {
    if (pointers[t]!=0 && freeBytes[t]!=0)
    {
     if (neededBytes>=freeBytes[t])
     {
      roomCursor.filePos=pointers[t];
      roomCursor.freeBytes=freeBytes[t];

      pointers[t]=0;
      freeBytes[t]=0;
      modified=2;
      prune();
     }
     else // neededBytes<freeBytes[t]
     {
      remainings=freeBytes[t]-neededBytes;

      roomCursor.filePos=pointers[t];
      roomCursor.freeBytes=neededBytes;

      if (remainings>=minReusableFreeBytes)
      {
       pointers[t]+=neededBytes;
       freeBytes[t]=remainings;
       modified=2;
      }
      else // si spreca spazio, ok, ma altrimenti la frammentazione diverrebbe troppo esasperata!
      {
       pointers[t]=0;
       freeBytes[t]=0;
       modified=2;
       prune();
      }
     }

     return;
    }
   }

   if (!nesting) return;
   // se arrivo qui non c'� spazi liberi in questo trunc!
   boolean createNewSpace=false;

   if (linkers[pNext]==0 && linkers[pPrev]==0)
   {
    createNewSpace=true;
   }
   else
   {
    long startPos=filePos;
    checkSave();

    boolean moved=false;

    // provo avanti
    while (roomCursor.filePos==0 && linkers[pNext]!=0)
    {
     room.setCurrentPosition(linkers[pNext]);
     load();
     getRoom(neededBytes, false);
     moved=true;
    }

    if (roomCursor.filePos==0 && moved)
    {
     room.setCurrentPosition(startPos);
     load();
    }

    // provo indietro
    while (roomCursor.filePos==0 && linkers[pPrev]!=0)
    {
     room.setCurrentPosition(linkers[pPrev]);
     load();
     getRoom(neededBytes, false);
    }

    createNewSpace=(roomCursor.filePos==0);
   }

   if (createNewSpace)
   {
    // non ci sono altri trunc di spazi liberi, dobbiamo per forza allargarci!
    roomCursor.filePos=room.getSize();
    roomCursor.freeBytes=neededBytes;
   }
  }

 };


 class RoomCursor
 {
  long filePos;
  int freeBytes;
 };



 private byte[] loadBytes(long firstFilePos)
 {
  byte res[]=null;
  int len=0, round=0, begin=0;
  long cursor=firstFilePos;

  while (cursor!=0)
  {
   room.setCurrentPosition(cursor);

   if (round==0)
   {
    len=room.ReadInt();
    res=new byte[len];
   }

   len=room.ReadInt();
   cursor=room.ReadLong();

   room.Read(res, begin, len);
   begin+=len;

   round++;
  }

  return res;
 }


 private void freeBytes(long firstFilePos)
 {
  int len=0, round=0, extra=0;
  long oc, cursor=firstFilePos;

  while (cursor!=0)
  {
   extra=4; // current portion len
   extra+=8; // next portion file pos

   room.setCurrentPosition(cursor);

   if (round==0)
   {
    try{room.skipBytes(4);}catch (Throwable tr){fireException(tr);}
    extra+=4; // total len
   }

   len=room.ReadInt()+extra;
   oc=cursor;
   cursor=room.ReadLong();
   frCursor.freeRoom(oc, len);

   round++;
  }
 }




 private void overwriteBytes(long firstFilePos, byte bytes[])
 {
  int len=0, round=0, begin=0;
  long cursor=firstFilePos;

  while (cursor!=0)
  {
   room.setCurrentPosition(cursor);

   if (round==0) try{room.skipBytes(4);}catch (Throwable tr){fireException(tr);}

   len=room.ReadInt();
   cursor=room.ReadLong();

   room.Write(bytes, begin, len);
   begin+=len;

   round++;
  }
 }




 private long saveBytes(byte bytes[])
 {
  long res=0;
  long csiFP=0;
  long nfpFP=0;
  int consumed, avail, needed, begin=0;
  int len=ArrayExtras.length(bytes);

  while (len>0)
  {
   needed=len;
   if (res==0) needed+=4; // total len
   needed+=4; // current portion len
   needed+=8; // next portion file pos

   frCursor.getRoom(needed);

   if (res!=0)
   {
    room.setCurrentPosition(nfpFP);
    room.WriteLong(roomCursor.filePos); // qui aggiusto la prossima porzione nella porzione precedente!
   }

   room.setCurrentPosition(roomCursor.filePos);
   if (res==0) room.WriteInt(len); // lunghezza totale

   csiFP=room.getCurrentPosition();
   try{room.skipBytes(4);}catch (Throwable tr){fireException(tr);} // dopo ci metto la dimensione della portione corrente

   nfpFP=room.getCurrentPosition();
   room.WriteLong(0); // file pos della prossima porzione, per ora lo metto a zero, ma dopo lo aggiusto se serve

   consumed=(int)(room.getCurrentPosition()-roomCursor.filePos);
   avail=roomCursor.freeBytes-consumed;
   if (avail>len) avail=len;

   room.Write(bytes, begin, avail);

   room.setCurrentPosition(csiFP); // ora ci metto la dimensione della portione corrente
   room.WriteInt(avail);

   len-=avail;
   begin+=avail;

   if (res==0) res=roomCursor.filePos;
  }

  return res;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public synchronized long piecesCount()
 {
  return pieces;
 }


 public synchronized byte[] getPiece(long pieceIndex)
 {
  byte res[]=null;
  checkIndex(pieceIndex);
  atCursor.reachPiece(pieceIndex);
  long ffp=atCursor.getFirstFilePos(pieceIndex);
  if (ffp!=0) res=loadBytes(ffp);
  return res;
 }


 public synchronized int getPieceSize(long pieceIndex)
 {
  int res=0;
  checkIndex(pieceIndex);
  atCursor.reachPiece(pieceIndex);
  long ffp=atCursor.getFirstFilePos(pieceIndex);

  if (ffp!=0)
  {
   room.setCurrentPosition(ffp);
   res=room.ReadInt();
  }

  return res;
 }


 public synchronized long addPiece(byte bytes[])
 {
  atCursor.reachPiece(Long.MAX_VALUE);
  long firstFP;

  if (ArrayExtras.length(bytes)>0)
  {
   firstFP=saveBytes(bytes);
  } else firstFP=0;

  atCursor.storeNewPiece(firstFP);
  pieces++;
  room.setCurrentPosition(piecesFP);

  try
  {
   room.writeLong(pieces);
  } catch (Throwable tr){fireException(tr);}

  return (pieces-1);
 }



 public synchronized void setPiece(long pieceIndex, byte bytes[])
 {
  checkIndex(pieceIndex);
  int oldps=getPieceSize(pieceIndex);
  int len=ArrayExtras.length(bytes);
  if (oldps==len)
  {
   if (len>0)
   {
    long ffp=atCursor.getFirstFilePos(pieceIndex);
    overwriteBytes(ffp, bytes);
   }
  }
  else
  {
   long ffp=atCursor.getFirstFilePos(pieceIndex);
   if (ffp!=0) freeBytes(ffp);

   long newFFP=0;
   if (len>0) newFFP=saveBytes(bytes);
   atCursor.setFirstFilePos(pieceIndex, newFFP);
  }
 }




 public synchronized void flush()
 {
  atCursor.checkSave();
  frCursor.checkSave();
  room.flush();
 }







 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 // ALL TESTS SUCCESFUL ! ! !

 public static void main(String args[])
 {
  for (;;)
  {
   test();
   ThreadExtras.sleep(250);
   System.out.println("\n\n");
  }

 }


 public static void test()
 {
  String ts[]=new String[]
  {
   "Madonna  ha presentato al festival il suo primo film dietro la macchina da presa, \"Filth and wisdom\": \"Vi svelo il mio spirito zingaro\"",
   "Secondo il sito del quotidiano Usa il magnate australiano potrebbe acquistare il 20% del motore di ricerca su cui Microsoft ha fatto un'offerta da 44,6 miliardi di dollari",
   "Sei arresti: per tutti loro l'accusa � di associazione a delinquere finalizzata alla produzione e al traffico di stupefacenti, aggravata dal metodo mafioso",
   "La Consulta giudica illegittime le imposte sulle seconde case per chi non risiede nell'isola. Sulla normativa sulle gabelle per yacht e aerei privati decider� la Corte di Giustizie Ue.\n"+"Il governatore Soru replica: �negata l'autonomia impositiva�"
  };

  int t, ti, len=ts.length;
  for (t=0;t<len;t++)
  {
   for (int i=0;i<3;i++) ts[t]+=ts[t];


//   ts[t]=ts[t]+","+ts[t]+","+ts[t]+","+ts[t]+","+ts[t]+","+ts[t]+","+ts[t]+","+ts[t];
//   ts[t]=ts[t]+","+ts[t]+","+ts[t];
  }



  TimeCounter tc=TimeCounter.start();

  //PiecedBytesRoom pbr=new PiecedBytesRoom();

  //
  String tfn=getTemporaryFileName();

  BufferedFileBytesRoom fbr=new BufferedFileBytesRoom(tfn);

//  FileBytesRoom fbr=new FileBytesRoom(tfn, false);
  fbr.open("rw");
  PiecedBytesRoom pbr=new PiecedBytesRoom(null, fbr);
  //


  len=500;

  for (t=0;t<len;t++)
  {
   ti=compositeRandom(10, 10, 10, 10);
   pbr.addPiece(getBytes(ts[ti]));
  }

  tc.stop();System.out.println(""+len+" scritture: "+tc.getElapsedString());

  int llen=250;
  tc=TimeCounter.start();

  String s;

  for (t=0;t<llen;t++)
  {
   s=newAutoString(pbr.getPiece((long)random(len)));
   if (StringExtras.select(ts, s)<0) System.out.println("--------> ERROR!");

   /*
   s=StringExtras.newString(pbr.getPiece((long)MathExtras.random(len)));
   System.out.println("read:\n"+s+"\n");

   if (StringExtras.select(ts, s)>=0) System.out.println("--------> SUCCESS!");
   else System.out.println("--------> ERROR!");
   */
  }

  tc.stop();System.out.println(""+llen+" letture: "+tc.getElapsedString());
  tc=TimeCounter.start();
  int olen=5000;


  for (t=0;t<olen;t++)
  {
   ti=compositeRandom(10, 10, 10, 10);
   pbr.addPiece(getBytes(ts[ti]));
  }

  tc.stop();System.out.println(""+olen+" scritture: "+tc.getElapsedString());

  for (int l=0;l<10;l++)
  {
  tc=TimeCounter.start();
  int rlen=250;


  for (t=0;t<rlen;t++)
  {
   if (random(100)>80) // qualcuno ogni tanto lo svuotiamo!
   {
    pbr.setPiece((long)random(len), null);

    int er, erlen=(int)random(10)+2;
    for (er=0;er<erlen;er++)
    {
     if (random(100)>30)
     {
      pbr.setPiece((long)random(len), null); // pi� cancellazioni consecutive
     }
    }

   }
   else
   {
    ti=compositeRandom(10, 10, 10, 10);
    pbr.setPiece((long)random(len), getBytes(ts[ti]));
   }
  }

  tc.stop();System.out.println(""+rlen+" RI-scritture: "+tc.getElapsedString());
  llen=250;
  tc=TimeCounter.start();
   byte bys[];

  for (t=0;t<llen;t++)
  {
   bys=pbr.getPiece((long)random(len));

   if (ArrayExtras.length(bys)>0)
   {
    s=newAutoString(bys);
    if (StringExtras.select(ts, s)<0) System.out.println("--------> ERROR!");
   }

   /*
   s=StringExtras.newString(pbr.getPiece((long)MathExtras.random(len)));
   System.out.println("read:\n"+s+"\n");

   if (StringExtras.select(ts, s)>=0) System.out.println("--------> SUCCESS!");
   else System.out.println("--------> ERROR!");
   */
  }

  tc.stop();System.out.println(""+llen+" letture: "+tc.getElapsedString());
  }


  System.out.println("pbr.piecesCount: "+pbr.piecesCount());
  System.out.println("pbr.room.getSize: "+pbr.room.getSize());
 }



}
