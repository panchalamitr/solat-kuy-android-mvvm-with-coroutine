package com.programmergabut.solatkuy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.programmergabut.solatkuy.data.PrayerRepository
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.data.local.localentity.MsSetting
import com.programmergabut.solatkuy.data.local.localentity.NotifiedPrayer
import com.programmergabut.solatkuy.data.remote.remoteentity.compassJson.CompassResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.prayerJson.PrayerResponse
import com.programmergabut.solatkuy.ui.DummyRetValue
import com.programmergabut.solatkuy.util.Resource

/*
 * Created by Katili Jiwo Adi Wiyono on 26/03/20.
 */

class FakePrayerRepositoryAndroidTest: PrayerRepository {

    private var msApi11 = DummyRetValue.getMsApi1()
    private val observableMsApi1 = MutableLiveData<Resource<MsApi1>>()
    private fun refreshMsApi1(){
        observableMsApi1.postValue(Resource.success(msApi11))
    }

    private var msSetting = DummyRetValue.getMsSetting()
    private val observableMsSetting = MutableLiveData<Resource<MsSetting>>()
    private fun refreshMsSetting(){
        observableMsSetting.postValue(Resource.success(msSetting))
    }


    /* Room */
    /* NotifiedPrayer */
    override suspend fun updatePrayerIsNotified(prayerName: String, isNotified: Boolean){}

    /* MsApi1 */
    override fun getMsApi1(): LiveData<Resource<MsApi1>> {
        return observableMsApi1
    }
    override suspend fun updateMsApi1(msApi1: MsApi1){
        msApi11 = msApi1
        refreshMsApi1()
    }

    /* MsSetting */
    override fun getMsSetting(): LiveData<Resource<MsSetting>> {
        return observableMsSetting
    }
    override suspend fun updateIsUsingDBQuotes(isUsingDBQuotes: Boolean){
        msSetting = MsSetting(msSetting.no, msSetting.isHasOpenApp, isUsingDBQuotes)
        refreshMsSetting()
    }
    override suspend fun updateMsApi1MonthAndYear(api1ID: Int, month: String, year:String){
        msApi11 = MsApi1(api1ID, msApi11.latitude, msApi11.longitude, msApi11.method, month, year)
        refreshMsSetting()
    }
    override suspend fun updateIsHasOpenApp(isHasOpen: Boolean){
        msSetting = MsSetting(msSetting.no, isHasOpen, msSetting.isUsingDBQuotes)
        refreshMsSetting()
    }

    /*
     * Retrofit
     */
    override suspend fun fetchCompass(msApi1: MsApi1): CompassResponse {
        return DummyRetValue.fetchCompassApi()
    }
    override suspend fun fetchPrayerApi(msApi1: MsApi1): PrayerResponse {
        return DummyRetValue.fetchPrayerApi()
    }
    override suspend fun syncNotifiedPrayer(msApi1: MsApi1): List<NotifiedPrayer> {
        return DummyRetValue.getNotifiedPrayer()
    }
    override suspend fun syncNotifiedPrayerTesting(): List<NotifiedPrayer> {
        return DummyRetValue.getNotifiedPrayer()
    }
}

