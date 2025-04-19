package com.example.fizyoapp.presentation.user.ornekegzersizler.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class OrnekEgzersizlerGiris(
    @PrimaryKey val uid:Int,
    @ColumnInfo(name="baslik") val baslik:String?,
    @ColumnInfo(name="fotograf_yolu") val fotograf_yolu: String?
)


