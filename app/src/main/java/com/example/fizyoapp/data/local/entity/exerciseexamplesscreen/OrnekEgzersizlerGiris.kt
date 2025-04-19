package com.example.fizyoapp.data.local.entity.exerciseexamplesscreen

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "exercise_categories")
data class OrnekEgzersizlerGiris(
    @PrimaryKey val id:String,
    @ColumnInfo(name="baslik") val baslik:String?,
    @ColumnInfo(name = "image_resource_id") val imageResourceId: Int
)


