package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "fizyolist")
data class FizyoterapistlerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unvan:String,
    val name:String,
    val surname:String,
    val fizyoImages:String
)
