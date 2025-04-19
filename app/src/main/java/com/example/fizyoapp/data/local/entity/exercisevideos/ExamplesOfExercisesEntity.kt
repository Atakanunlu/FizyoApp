package com.example.fizyoapp.data.local.entity.exercisevideos

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "videos")
data class ExamplesOfExercisesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val description: String,
    val category:  String
)