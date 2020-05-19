package com.programmergabut.solatkuy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.programmergabut.solatkuy.data.ContextProviders
import com.programmergabut.solatkuy.data.local.dao.MsApi1Dao
import com.programmergabut.solatkuy.data.local.dao.MsSettingDao
import com.programmergabut.solatkuy.data.local.dao.NotifiedPrayerDao
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.data.local.localentity.MsSetting
import com.programmergabut.solatkuy.data.local.localentity.NotifiedPrayer
import com.programmergabut.solatkuy.util.EnumConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/*
 * Created by Katili Jiwo Adi Wiyono on 25/03/20.
 */

@Database(version = 5, entities = [NotifiedPrayer::class, MsApi1::class, MsSetting::class])
abstract class SolatKuyRoom: RoomDatabase() {

    abstract fun notifiedPrayerDao(): NotifiedPrayerDao
    abstract fun msApi1Dao(): MsApi1Dao
    abstract fun msSettingDao(): MsSettingDao


    companion object{
        @Volatile
        private var INSTANCE: SolatKuyRoom? = null
        private const val dbName = "solatkuydb"

        fun getDataBase(context: Context, contextProviders: ContextProviders): SolatKuyRoom {
            val tempInstance =
                INSTANCE

            if(tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, SolatKuyRoom::class.java,
                    dbName
                )
                    //.fallbackToDestructiveMigration()
                    /*.addMigrations(object :Migration(1,2){
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE MsApi1 (`api1ID` INTEGER, "
                                    + "`latitude` TEXT, "
                                    + "`longitude` TEXT, "
                                    + "`method` TEXT, "
                                    + "`month` TEXT"
                                    + "`year` TEXT"
                                    + "PRIMARY KEY(`id`))")

                            //var api1ID: Int, val latitude: String, val longitude: String, val method: String, val month: String, val year: String
                        }

                  })*/
                    .addCallback(
                        NotifiedPrayerCallBack(
                            contextProviders
                        )
                    )
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        fun getDataBase(context: Context): SolatKuyRoom {
            val tempInstance =
                INSTANCE

            if(tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                    SolatKuyRoom::class.java,
                    dbName
                )
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        private class NotifiedPrayerCallBack(private val contextProviders: ContextProviders): RoomDatabase.Callback(){

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                INSTANCE.let {
                    GlobalScope.launch(contextProviders.IO) {
                        it?.let {
                            populateMsSetting(it.msSettingDao())
                            populateNotifiedPrayer(it.notifiedPrayerDao())
                            populateMsApi1(it.msApi1Dao())
                        }
                    }
                }
            }

            suspend fun populateMsSetting(msSettingDao: MsSettingDao){
                msSettingDao.deleteAll()

                msSettingDao.insertMsSetting(
                    MsSetting(
                        1,
                        false
                    )
                )
            }

            suspend fun populateMsApi1(msApi1Dao: MsApi1Dao) {
                msApi1Dao.deleteAll()

                msApi1Dao.insertMsApi1(
                    MsApi1(
                        1,
                        "0",
                        "0",
                        "3",
                        "3",
                        "2020"
                    )
                )
            }

            suspend fun populateNotifiedPrayer(notifiedPrayerDao: NotifiedPrayerDao){
                notifiedPrayerDao.deleteAll()

                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.fajr,
                        true,
                        "00:00"
                    )
                )
                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.dhuhr,
                        true,
                        "00:00"
                    )
                )
                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.asr,
                        true,
                        "00:00"
                    )
                )
                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.maghrib,
                        true,
                        "00:00"
                    )
                )
                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.isha,
                        true,
                        "00:00"
                    )
                )
                notifiedPrayerDao.insertNotifiedPrayer(
                    NotifiedPrayer(
                        EnumConfig.sunrise,
                        true,
                        "00:00"
                    )
                )
            }
        }


    }

}