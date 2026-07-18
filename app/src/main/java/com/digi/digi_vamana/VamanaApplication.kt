package com.digi.digi_vamana

import android.app.Application
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog

class VamanaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        VamanaActivityLog.init(this)
    }
}
