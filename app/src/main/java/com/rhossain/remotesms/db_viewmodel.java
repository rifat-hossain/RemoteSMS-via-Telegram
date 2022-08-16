package com.rhossain.remotesms;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.util.List;

public class db_viewmodel {
    private dbDao dao;
    private dbMain db;
    public db_viewmodel(@NonNull Context context) {
        db = dbMain.getDB(context);
        dao = db.dao();
    }

    public void insertsms(Queue sms){
        new InsertAsyncTask(dao).execute(sms);
    }
    public void insertReceiver(db_receiver receiver){
        new InsertAsyncReceiver(dao).execute(receiver);
    }
    public void insert_settings(db_settings settings){
        new InsertAsyncSettings(dao).execute(settings);
    }
    public List<Queue> getQueues(){
        return dao.getQueues();
    }
    public List<db_receiver> getReceivers(){
        return dao.getReceivers();
    }
    public db_settings load_settings(){
        return dao.load_settings();
    }
    public void delete(Queue sms){
        dao.delete(sms);
    }
    private class InsertAsyncTask extends AsyncTask<Queue, Void, Void> {
        dbDao mDAO;
        public InsertAsyncTask(dbDao dao) {
            this.mDAO = dao;
        }

        @Override
        protected Void doInBackground(Queue... queues) {
            mDAO.insert(queues[0]);
            return null;
        }
    }
    private class InsertAsyncReceiver extends AsyncTask<db_receiver, Void, Void> {
        dbDao mDAO;
        public InsertAsyncReceiver(dbDao dao) {
            this.mDAO = dao;
        }

        @Override
        protected Void doInBackground(db_receiver... receivers) {
            mDAO.insert_receiver(receivers[0]);
            return null;
        }
    }
    private class InsertAsyncSettings extends AsyncTask<db_settings, Void, Void>{
        dbDao mDao;
        public InsertAsyncSettings(dbDao dao){
            this.mDao = dao;
        }
        @Override
        protected Void doInBackground(db_settings... db_settings) {
            mDao.insert_set(db_settings[0]);
            return null;
        }
    }
}
