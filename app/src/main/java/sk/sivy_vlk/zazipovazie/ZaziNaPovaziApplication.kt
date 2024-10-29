package sk.sivy_vlk.zazipovazie

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import sk.sivy_vlk.zazipovazie.di.KMZInputStreamRepositoryModule
import sk.sivy_vlk.zazipovazie.di.appModule

class ZaziNaPovaziApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ZaziNaPovaziApplication)
            modules(appModule, KMZInputStreamRepositoryModule)
        }
        appContext = applicationContext
    }

    companion object {
        /**
         * Returns the application context.
         *
         * @return
         */
        @JvmStatic
        var appContext: Context? = null
            private set
    }

}