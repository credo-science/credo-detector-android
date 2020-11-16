package science.credo.mobiledetector2.database

//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import science.credo.mobiledetector.detector.old.OldHit
//
//@Database(entities = [OldHit::class], version = 1, exportSchema = false)
//public abstract class CredoRoomDatabase : RoomDatabase() {
//
//    abstract fun wordDao(): HitDao
//
//    companion object {
//        // Singleton prevents multiple instances of database opening at the
//        // same time.
//        @Volatile
//        private var INSTANCE: CredoRoomDatabase? = null
//
//        fun getDatabase(context: Context): CredoRoomDatabase {
//            val tempInstance = INSTANCE
//            if (tempInstance != null) {
//                return tempInstance
//            }
//            synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    CredoRoomDatabase::class.java,
//                    "credo_database"
//                ).build()
//                INSTANCE = instance
//                return instance
//            }
//        }
//    }
//}