package com.programmergabut.solatkuy.data

import androidx.lifecycle.LiveData
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.data.local.localentity.MsSetting
import com.programmergabut.solatkuy.data.local.localentity.NotifiedPrayer
import com.programmergabut.solatkuy.data.remote.remoteentity.compassJson.CompassResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.prayerJson.PrayerResponse
import com.programmergabut.solatkuy.util.Resource
import kotlinx.coroutines.Deferred

interface PrayerRepository {
    suspend fun updatePrayerIsNotified(prayerName: String, isNotified: Boolean)
    fun updatePrayerTime(prayerName: String, prayerTime: String)
    fun getMsApi1(): LiveData<MsApi1>
    suspend fun updateMsApi1(msApi1: MsApi1)
    fun getMsSetting(): LiveData<MsSetting>
    suspend fun updateIsUsingDBQuotes(isUsingDBQuotes: Boolean)
    suspend fun updateMsApi1MonthAndYear(api1ID: Int, month: String, year:String)
    suspend fun updateIsHasOpenApp(isHasOpen: Boolean)
    suspend fun fetchCompass(msApi1: MsApi1): Deferred<CompassResponse>
    suspend fun fetchPrayerApi(msApi1: MsApi1): Deferred<PrayerResponse>
    suspend fun syncNotifiedPrayerTesting(): List<NotifiedPrayer>
    fun getListNotifiedPrayerSync(): List<NotifiedPrayer>
}