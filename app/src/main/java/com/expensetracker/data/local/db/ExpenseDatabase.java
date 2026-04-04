package com.expensetracker.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.expensetracker.data.local.dao.ExpenseDao;
import com.expensetracker.data.local.entity.CategoryEntity;
import com.expensetracker.data.local.entity.ExpenseEntity;

@Database(entities = {ExpenseEntity.class, CategoryEntity.class}, version = 1, exportSchema = false)
public abstract class ExpenseDatabase extends RoomDatabase {

    private static volatile ExpenseDatabase INSTANCE;
    private static final String DB_NAME = "expense_tracker_db";

    public abstract ExpenseDao expenseDao();

    public static ExpenseDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ExpenseDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ExpenseDatabase.class,
                            DB_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
