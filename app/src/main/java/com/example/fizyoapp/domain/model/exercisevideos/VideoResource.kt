package com.example.fizyoapp.domain.model.exercisevideos

/**
 * VideoResource: Egzersiz videolarının kaynak bilgilerini temsil eden domain sınıfı
 *
 * Bu sınıf:
 * 1. Video dosyasının raw klasöründeki adını tutar
 * 2. Video için açıklama metnini tutar
 * 3. ViewModel'den domain mantığını ayırmaya yardımcı olur
 *
 * @property name: Video dosyasının raw klasöründeki adı (uzantısız)
 * @property description: Video için açıklama metni
 */
data class VideoResource(
    val name: String,
    val description: String
)