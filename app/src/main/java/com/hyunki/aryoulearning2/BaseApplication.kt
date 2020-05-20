package com.hyunki.aryoulearning2

import android.app.Application

import com.hyunki.aryoulearning2.di.AppComponent
import com.hyunki.aryoulearning2.di.DaggerAppComponent

class BaseApplication : Application() {
    var appComponent: AppComponent
        internal set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().application(this).build()
    }
}
