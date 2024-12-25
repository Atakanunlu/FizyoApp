package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.repository

import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.FizyoterapistDao
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data.FizyoterapistlerEntity
import kotlinx.coroutines.flow.Flow

class FizyoterapistRepository(private val dao: FizyoterapistDao) {
    suspend fun searchByName(name: String): Flow<List<FizyoterapistlerEntity>> = dao.searchByName(name)
    suspend fun getAll()=dao.getAll()

}

//Repository, verilerin kaynakları (örneğin veritabanı veya ağ) ile uygulama arasındaki bağlantıyı sağlar. Verilere erişim kurallarını burada tanımlarsın.
//
//kotlin
//Kodu kopyala