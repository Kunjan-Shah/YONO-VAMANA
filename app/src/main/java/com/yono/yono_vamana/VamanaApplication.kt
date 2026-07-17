package com.yono.yono_vamana

import android.app.Application
import com.yono.yono_vamana.vamana.intelligence.VamanaActivityLog

class VamanaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        VamanaActivityLog.init(this)
    }
}
