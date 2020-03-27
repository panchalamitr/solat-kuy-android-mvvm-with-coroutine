package com.programmergabut.solatkuy.ui.fragmentmain.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.data.model.ModelPrayer
import com.programmergabut.solatkuy.data.model.dao.MsApi1
import com.programmergabut.solatkuy.data.model.dao.PrayerLocal
import com.programmergabut.solatkuy.data.model.prayerJson.Timings
import com.programmergabut.solatkuy.ui.fragmentmain.viewmodel.FragmentMainViewModel
import com.programmergabut.solatkuy.ui.main.view.MainActivity
import com.programmergabut.solatkuy.util.Broadcaster
import com.programmergabut.solatkuy.util.EnumStatus
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.layout_prayer_time.*
import kotlinx.android.synthetic.main.layout_widget.*
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.math.abs

class FragmentMain : Fragment() {

    private lateinit var fragmentMainViewModel: FragmentMainViewModel
    private var tempTimings: Timings? = null
    private var tempMsApi1: MsApi1? = null
    private var mCityName: String? = null
    private var mListOfListModelPrayer: MutableList<MutableList<ModelPrayer>?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentMainViewModel = ViewModelProviders.of(this).get(FragmentMainViewModel::class.java)

        subscribeObserversAPI()
        subscribeObserversDB()
    }

    override fun onStart() {
        super.onStart()

        loadTempData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cbClickListener()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_main, container, false)


    /* Subscribe live data */
    private fun subscribeObserversDB() {

        val currDate = LocalDate()

        fragmentMainViewModel.prayerLocal.observe(this, androidx.lifecycle.Observer {
            bindCheckBox(it)
            mListOfListModelPrayer = modelPrayerFactory(it)
            updateAlarmManager()
        })

        fragmentMainViewModel.msApi1Local.observe(this, androidx.lifecycle.Observer {

            if(it == null )
                return@Observer

            /* save temp data */
            tempMsApi1 = it

            bindWidgetLocation(it)

            /* fetching Prayer API */
            fetchPrayerApi(it.latitude,it.longitude,"8", currDate.monthOfYear.toString(),currDate.year.toString())
        })
    }

    private fun subscribeObserversAPI() {

        val sdf = SimpleDateFormat("dd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        fragmentMainViewModel.prayerApi.observe(this, androidx.lifecycle.Observer { it ->
            when(it.Status){
                EnumStatus.SUCCESS -> {
                    it.data.let {
                        val timings = it?.data?.find { obj -> obj.date.gregorian.day == currentDate.toString() }?.timings

                        /* save temp data */
                        tempTimings = timings

                        bindPrayerText(timings)
                        bindWidget(timings)
                    }}
                EnumStatus.LOADING -> bindPrayerText(null)
                EnumStatus.ERROR -> Toast.makeText(context,it.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })

    }


    /* fetch API data */
    private fun fetchPrayerApi(latitude: String, longitude: String, method: String, month: String, year: String) {

        Toasty.info(context!!, "fetching data..", Toast.LENGTH_SHORT).show()

        fragmentMainViewModel.fetchPrayerApi(latitude,longitude,method, month,year)
    }


    /* Database Transaction */
    private fun insertDb(prayer:String, isNotified:Boolean){

        if(isNotified)
            Toasty.success(context!!, "$prayer will be notified every day", Toast.LENGTH_SHORT).show()
        else
            Toasty.warning(context!!, "$prayer will not be notified anymore", Toast.LENGTH_SHORT).show()

        fragmentMainViewModel.update(PrayerLocal(prayer, isNotified))
    }


    /* init first load data */
    private fun modelPrayerFactory(it: List<PrayerLocal>): MutableList<MutableList<ModelPrayer>?>? {

        val listOfListModelPrayer = mutableListOf<MutableList<ModelPrayer>?>()
        val notifiedList = mutableListOf<ModelPrayer>()
        val nonNotifiedList = mutableListOf<ModelPrayer>()
        var modelPrayer: ModelPrayer? = null

        if(tempTimings == null)
            return null

        it.forEach con@{ p ->

            when (p.prayerName) {
                getString(R.string.fajr) -> modelPrayer = ModelPrayer(p.prayerID, p.prayerName, p.isNotified, tempTimings?.fajr!!)
                getString(R.string.dhuhr) -> modelPrayer = ModelPrayer(p.prayerID, p.prayerName, p.isNotified, tempTimings?.dhuhr!!)
                getString(R.string.asr) -> modelPrayer = ModelPrayer(p.prayerID, p.prayerName, p.isNotified, tempTimings?.asr!!)
                getString(R.string.maghrib) -> modelPrayer = ModelPrayer(p.prayerID, p.prayerName, p.isNotified, tempTimings?.maghrib!!)
                getString(R.string.isha) -> modelPrayer = ModelPrayer(p.prayerID, p.prayerName, p.isNotified, tempTimings?.isha!!)
            }

            if(p.isNotified)
                notifiedList.add(modelPrayer!!)
            else
                nonNotifiedList.add(modelPrayer!!)
        }

        listOfListModelPrayer.add(notifiedList)
        listOfListModelPrayer.add(nonNotifiedList)

        return listOfListModelPrayer
    }

    private fun loadTempData() {
        if(tempTimings != null){
            bindPrayerText(tempTimings)
            bindWidget(tempTimings)
        }
        if(tempMsApi1 != null)
            bindWidgetLocation(tempMsApi1!!)
    }

    private fun cbClickListener() {

        cb_fajr.setOnClickListener {
            if(cb_fajr.isChecked)
                insertDb(getString(R.string.fajr), true)
            else
                insertDb(getString(R.string.fajr), false)
        }

        cb_dhuhr.setOnClickListener {
            if(cb_dhuhr.isChecked)
                insertDb(getString(R.string.dhuhr), true)
            else
                insertDb(getString(R.string.dhuhr), false)
        }

        cb_asr.setOnClickListener {
            if(cb_asr.isChecked)
                insertDb(getString(R.string.asr), true)
            else
                insertDb(getString(R.string.asr), false)
        }

        cb_maghrib.setOnClickListener {
            if(cb_maghrib.isChecked)
                insertDb(getString(R.string.maghrib), true)
            else
                insertDb(getString(R.string.maghrib), false)
        }

        cb_isha.setOnClickListener {
            if(cb_isha.isChecked)
                insertDb(getString(R.string.isha), true)
            else
                insertDb(getString(R.string.isha), false)
        }

    }

    private fun bindCheckBox(list: List<PrayerLocal>) {
        list.forEach {
            when {
                it.prayerName.trim() == getString(R.string.fajr) && it.isNotified -> cb_fajr.isChecked = true
                it.prayerName.trim() == getString(R.string.dhuhr) && it.isNotified -> cb_dhuhr.isChecked = true
                it.prayerName.trim() == getString(R.string.asr) && it.isNotified -> cb_asr.isChecked = true
                it.prayerName.trim() == getString(R.string.maghrib) && it.isNotified -> cb_maghrib.isChecked = true
                it.prayerName.trim() == getString(R.string.isha) && it.isNotified -> cb_isha.isChecked = true
            }}
    }


    /* Widget */
    private fun bindWidgetLocation(it: MsApi1) {
        val geoCoder = Geocoder(context!!, Locale.getDefault())
        try {
            val addresses: List<Address> = geoCoder.getFromLocation(it.latitude.toDouble(),it.longitude.toDouble(), 1)
            mCityName = addresses[0].subAdminArea
        }
        catch (ex: Exception){
            mCityName = "-"
        }

        tv_view_latitude.text = it.latitude + " °S"
        tv_view_longitude.text = it.longitude + " °E"
        tv_view_city.text = mCityName
    }

    private fun bindPrayerText(timings: Timings?) {

        if(timings == null){
            tv_fajr_time.text = getString(R.string.loading)
            tv_dhuhr_time.text = getString(R.string.loading)
            tv_asr_time.text = getString(R.string.loading)
            tv_maghrib_time.text = getString(R.string.loading)
            tv_isha_time.text = getString(R.string.loading)
        }
        else{
            tv_fajr_time.text = timings.fajr
            tv_dhuhr_time.text = timings.dhuhr
            tv_asr_time.text = timings.asr
            tv_maghrib_time.text = timings.maghrib
            tv_isha_time.text = timings.isha
        }
    }

    private fun bindWidget(timings: Timings?) {

        if(timings == null)
            return

        val selPrayer = selectPrayer(timings)
        selectWidgetTitle(selPrayer)
        selectWidgetPic(selPrayer)
        selectNextPrayerTime(selPrayer, timings)
    }

    private fun selectNextPrayerTime(selPrayer: Int, timings: Timings) {

        val sdfPrayer = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val localTime = LocalTime.now().toString()
        val nowTime = DateTime(sdfPrayer.parse(localTime))
        var period: Period? = null

        when(selPrayer){
            -1 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.dhuhr.split(" ")[0].trim() + ":00")))
            1 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.sunrise.split(" ")[0].trim()+ ":00")))
            2 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.asr.split(" ")[0].trim()+ ":00")))
            3 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.maghrib.split(" ")[0].trim()+ ":00")))
            4 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.isha.split(" ")[0].trim()+ ":00")))
            5 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.fajr.split(" ")[0].trim()+ ":00")).plusDays(1))
            6 -> period = Period(nowTime, DateTime(sdfPrayer.parse(timings.fajr.split(" ")[0].trim()+ ":00")))
        }

        if(period == null)
            return

        CoroutineScope(Dispatchers.IO).launch{
            coroutineTimer(period.hours, period.minutes, 60 - nowTime.secondOfMinute)
        }

    }

    private fun selectWidgetTitle(selPrayer: Int) {

        when(selPrayer){
            -1 -> tv_widget_prayer_name.text = "Next prayer is Dhuhr"
            1 -> tv_widget_prayer_name.text = getString(R.string.fajr)
            2 -> tv_widget_prayer_name.text = getString(R.string.dhuhr)
            3 -> tv_widget_prayer_name.text = getString(R.string.asr)
            4 -> tv_widget_prayer_name.text = getString(R.string.maghrib)
            5 -> tv_widget_prayer_name.text = getString(R.string.isha)
            6 -> tv_widget_prayer_name.text = getString(R.string.isha)
        }
    }

    private fun selectWidgetPic(selPrayer: Int) {
        var widgetDrawable: Drawable? = null

        when(selPrayer){
            -1 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.sunrise)
            1 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.fajr)
            2 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.dhuhr)
            3 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.asr)
            4 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.maghrib)
            5 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.isha)
            6 -> widgetDrawable = getDrawable(context?.applicationContext!!,R.drawable.isha)
        }

        if(widgetDrawable != null)
            Glide.with(this).load(widgetDrawable).centerCrop().into(iv_prayer_widget)
    }

    private fun selectPrayer(timings: Timings): Int {
        var prayer: Int = -1

        //prayer time
        val sdfPrayer = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fajrTime = DateTime(sdfPrayer.parse(timings.fajr.split(" ")[0].trim()))
        val dhuhrTime =  DateTime(sdfPrayer.parse(timings.dhuhr.split(" ")[0].trim()))
        val asrTime =  DateTime(sdfPrayer.parse(timings.asr.split(" ")[0].trim()))
        val maghribTime =  DateTime(sdfPrayer.parse(timings.maghrib.split(" ")[0].trim()))
        val ishaTime =  DateTime(sdfPrayer.parse(timings.isha.split(" ")[0].trim()))

        //sunrise & next fajr time
        val sunriseTime =  DateTime(sdfPrayer.parse(timings.sunrise.split(" ")[0].trim()))
        val nextfajrTime = DateTime(sdfPrayer.parse(timings.fajr.split(" ")[0].trim())).plusDays(1)
        val zerozeroTime = DateTime(sdfPrayer.parse("00:00"))

        val nowTime = DateTime(sdfPrayer.parse(LocalTime.now().toString()))

        if(nowTime.isAfter(zerozeroTime) && nowTime.isBefore(fajrTime)) //--> isha time
            prayer = 6

        if(fajrTime.isBefore(nowTime) && nowTime.isBefore(sunriseTime)) //--> fajr time
            prayer = 1
        else if(dhuhrTime.isBefore(nowTime) && nowTime.isBefore(asrTime)) //--> dhuhr time
            prayer = 2
        else if(asrTime.isBefore(nowTime) && nowTime.isBefore(maghribTime)) //--> asr time
            prayer = 3
        else if(maghribTime.isBefore(nowTime) && nowTime.isBefore(ishaTime)) //--> maghrib time
            prayer = 4
        else if(ishaTime.isBefore(nowTime) && nowTime.isBefore(nextfajrTime)) //--> isha time
            prayer = 5

        return prayer
    }


    /* Coroutine Timer */
    private suspend fun coroutineTimer(hour: Int, minute: Int, second: Int){
        
        var tempHour = abs(hour)
        var tempMinute = abs(minute)
        var tempSecond = abs(second)

        var onOffCountDown = true
        var isMinuteZero = false

        while (onOffCountDown){
            delay(1000)
            tempSecond--

            if(tempSecond == 0){
                tempSecond = 60

                if(tempMinute != 0)
                    tempMinute -= 1

                if(tempMinute == 0)
                    isMinuteZero = true

            }

            if(tempMinute == 0){

                if(!isMinuteZero)
                    tempMinute = 60

                if(tempHour != 0)
                    tempHour -= 1
            }

            /* fetching Prayer API */
            if(tempHour == 0 && tempMinute == 0 && tempSecond == 1){
                withContext(Dispatchers.Main){
                    fetchPrayerApi(tempMsApi1?.latitude!!,tempMsApi1?.longitude!!, "8", tempMsApi1!!.month, tempMsApi1!!.year)
                }
            }

            withContext(Dispatchers.Main){
                if(tv_widget_prayer_countdown != null)
                    tv_widget_prayer_countdown.text = "$tempHour : $tempMinute : $tempSecond remaining"
                else
                    onOffCountDown = false
            }

        }
    }

    /* Alarm Manager & Notification */
    private fun updateAlarmManager(){
        mListOfListModelPrayer?.let { listPrayer ->
            setNotification(listPrayer[0]!! /* sel list */ ,listPrayer[1]!! /* non list */)
            Toasty.info(context!!, "alarm manager updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setNotification(selList: MutableList<ModelPrayer>, nonSelList: MutableList<ModelPrayer>) {

        val intent = Intent(activity, Broadcaster::class.java)
        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        selList.forEach{

            val hour = it.prayerTime.split(":")[0].trim()
            val minute = it.prayerTime.split(":")[1].split(" ")[0].trim()

            val c = Calendar.getInstance()
            c.set(Calendar.HOUR_OF_DAY, hour.toInt())
            c.set(Calendar.MINUTE, minute.toInt())
            c.set(Calendar.SECOND, 0)

            intent.putExtra("prayer_id", it.prayerID)
            intent.putExtra("prayer_name", it.prayerName)
            intent.putExtra("prayer_time", it.prayerTime)
            intent.putExtra("prayer_city", mCityName)

            val pendingIntent = PendingIntent.getBroadcast(context, it.prayerID, intent, 0)

            if(c.before(Calendar.getInstance()))
                c.add(Calendar.DATE, 1)

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, 60000, pendingIntent)
        }

        nonSelList.forEach{

            val pendingIntent = PendingIntent.getBroadcast(context, it.prayerID, intent, 0)

            alarmManager.cancel(pendingIntent)
        }

    }




}
