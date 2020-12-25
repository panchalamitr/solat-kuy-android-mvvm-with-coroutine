package com.programmergabut.solatkuy.data

import androidx.lifecycle.LiveData
import com.programmergabut.solatkuy.data.local.localentity.MsFavAyah
import com.programmergabut.solatkuy.data.local.localentity.MsFavSurah
import com.programmergabut.solatkuy.data.remote.remoteentity.quranallsurahJson.AllSurahResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.quranallsurahJson.Data
import com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonAr.ReadSurahArResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonEn.ReadSurahEnResponse
import com.programmergabut.solatkuy.util.Resource
import kotlinx.coroutines.Deferred

interface QuranRepository {
    fun getListFavAyah(): LiveData<List<MsFavAyah>>
    fun getListFavAyahBySurahID(surahID: Int): LiveData<List<MsFavAyah>>
    suspend fun insertFavAyah(msFavAyah: MsFavAyah)
    suspend fun deleteFavAyah(msFavAyah: MsFavAyah)
    fun getListFavSurah(): LiveData<List<MsFavSurah>>
    fun getFavSurahBySurahID(surahID: Int): LiveData<MsFavSurah>
    suspend fun insertFavSurah(msFavSurah: MsFavSurah)
    suspend fun deleteFavSurah(msFavSurah: MsFavSurah)
    suspend fun fetchReadSurahEn(surahID: Int): Deferred<ReadSurahEnResponse>
    suspend fun fetchAllSurah(): Deferred<AllSurahResponse>
    suspend fun fetchReadSurahAr(surahID: Int): Deferred<ReadSurahArResponse>
}