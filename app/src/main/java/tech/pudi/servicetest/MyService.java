package tech.pudi.servicetest;

import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class MyService extends Service {
    private MediaPlayer mp;
    private ArrayList<Contact> myContactList= new ArrayList<Contact>();
    public static ArrayList<ContentProviderOperation> operations;
    public static String newRawContactId;
    @NonNull static String contactRawContactId;
    static SQLiteDatabase localphonedb;
    public MyService() {
        myContactList=getContactData();
        localphonedb=openOrCreateDatabase("phone.db", Context.MODE_PRIVATE,null);
        localphonedb.execSQL("CREATE TABLE IF NOT EXISTS cont(name varchar,number int)");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for(Contact contact:myContactList){
            for (String number: contact.getNumbers()){
                if(isNumberAlreadyRegistered(number)){
                    Log.i("number ",number );
                }
                else{
                    registerContact(this,number,contact.getName(),contact.getRawContactIdMap());
                    Log.i("number ",number );
                }

            }
        }
        return START_STICKY;
    }

    public static void registerContact(Context context, String number, String contactName , HashMap<String,String> rawContactIdMap )
    {
        operations=new ArrayList<>();
        operations.add(ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number) // Supply the number to be synced
                .build()
        );
        localphonedb.execSQL("insert into cont('"+contactName+"','"+number+"')");

        ContentProviderResult[] contentProviderResult = new ContentProviderResult[0];
        try {
            contentProviderResult = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        newRawContactId = Long.toString(ContentUris.parseId(contentProviderResult[0].uri));
        contactRawContactId=rawContactIdMap.get(number);
    }
    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation)
    {
        if(isSyncOperation){
            return uri.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,"true").build();
        }
        return uri;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
    }

    private Boolean isNumberAlreadyRegistered(String number){
        Boolean isRegistered=false;
        Cursor rawContactIdCursor = MyService.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID},ContactsContract.CommonDataKinds.Phone.NUMBER+"= ?",new String[]{number},null);
        ArrayList<String> rawContactIdList= new ArrayList<>();
        if(rawContactIdCursor!=null && rawContactIdCursor.moveToFirst()){
            do {
                rawContactIdList.add(rawContactIdCursor.getString(rawContactIdCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)));
            }
            while (rawContactIdCursor.moveToNext());
            rawContactIdCursor.close();
        }

        for (String rawContactId: rawContactIdList){
            Cursor accTypeCursor = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,new String[]{ContactsContract.RawContacts.ACCOUNT_TYPE},ContactsContract.RawContacts._ID+"= ?",new String[]{rawContactId},null);
            ArrayList<String> accTypeList=new ArrayList<String>();
            if(accTypeCursor!=null && accTypeCursor.moveToFirst()){
                do{
                    accTypeList.add(accTypeCursor.getString(accTypeCursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)));
                }while (accTypeCursor.moveToNext());
                accTypeCursor.close();
            }
            if (accTypeList.contains("tech.pudi.contacttest")){
                isRegistered=true;
                break;
            }
        }


        return isRegistered;
    }
    private ArrayList<Contact> getContactData(){
        ArrayList<Contact> contacts= new ArrayList<Contact>();
        Cursor idCursor = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,new String[]{ContactsContract.Contacts._ID},null,null,null);
        if(idCursor != null && idCursor.moveToFirst()) {
            do {
                String contactId = idCursor.getString(idCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                Cursor numberCursor=this.getContentResolver().query(ContactsContract.Data.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},ContactsContract.Data.CONTACT_ID+"? AND"+ContactsContract.Data.MIMETYPE,new String[]{contactId,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},null);
                ArrayList<String> numbers=new ArrayList<>();
                if(numberCursor!=null && numberCursor.moveToNext()) {
                    do {
                        numbers.add(numberCursor.getString(numberCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                    while (numberCursor.moveToNext());
                    numberCursor.close();
                }
                Cursor nameCursor=this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,new String[]{ContactsContract.Contacts.DISPLAY_NAME},ContactsContract.Contacts._ID+"?",new String[]{contactId},null);
                String name="";
                if(nameCursor!= null && nameCursor.moveToFirst()){
                    do {
                        name=nameCursor.getString(nameCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                    while (nameCursor.moveToNext());
                    nameCursor.close();
                }
                HashMap<String,String> rawContactIdMap=new HashMap<String,String>();
                for(String number: numbers) {
                    String rawContactId;
                    Cursor rawContactIdCursor=this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,new String[]{ContactsContract.Data.RAW_CONTACT_ID},ContactsContract.CommonDataKinds.Phone.NUMBER+"= ?", new String[]{number},null);
                    if(rawContactIdCursor!=null && rawContactIdCursor.moveToFirst()) {
                        rawContactId=rawContactIdCursor.getString(rawContactIdCursor.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID));
                        rawContactIdMap.put(number,rawContactId);
                        rawContactIdCursor.close();
                    }
                }

                contacts.add(new Contact(contactId,name,numbers,rawContactIdMap));
            }
            while (idCursor.moveToNext());
        }

        return contacts;
    }
}
