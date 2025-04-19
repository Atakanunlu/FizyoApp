package com.example.fizyoapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.data.local.dao.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.entity.OrnekEgzersizlerGiris


@Database(entities = arrayOf(OrnekEgzersizlerGiris::class), version = 1)
abstract class  OrnekEgzersizlerGirisDatabase:RoomDatabase() {
    abstract fun OrnekEgzersizlerGirisDao(): OrnekEgzersizlerGirisDao


    companion object{

        @Volatile private var instance: OrnekEgzersizlerGirisDatabase?=null
        private val lock=Any()


        operator fun invoke (context: Context)= instance ?: synchronized(lock){
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context:Context)=Room.databaseBuilder(
            context.applicationContext, OrnekEgzersizlerGirisDatabase::class.java,
            "ornekegzersizlergiris"
        ).build()


    }


}
