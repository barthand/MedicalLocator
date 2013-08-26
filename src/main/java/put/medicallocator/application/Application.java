package put.medicallocator.application;

import android.database.sqlite.SQLiteDatabase;

public class Application extends android.app.Application {

    private SQLiteDatabase readableDatabase;

    public SQLiteDatabase getReadableDatabase() {
        return readableDatabase;
    }

    public void setReadableDatabase(SQLiteDatabase readableDatabase) {
        this.readableDatabase = readableDatabase;
    }

}
