package com.kelsos.mbrc

import android.app.Application
import android.content.Intent
import android.support.multidex.MultiDex
import com.kelsos.mbrc.controller.Controller
import roboguice.RoboGuice
import timber.log.Timber

class RemoteApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    MultiDex.install(this)
    RoboGuice.setupBaseApplicationInjector(this)
    startService(Intent(this, Controller::class.java))

    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
          return "${super.createStackElementTag(element)}:${element.lineNumber} [${Thread.currentThread().name}]"
        }
      })
    }
  }
}