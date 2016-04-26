package com.kelsos.mbrc.annotations

import android.support.annotation.StringDef

object PlayerState {
    const val PLAYING = "playing"
    const val PAUSED = "paused"
    const val STOPPED = "stopped"
    const val UNDEFINED = "undefined"

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(PAUSED, PLAYING, STOPPED, UNDEFINED)
    annotation class State
}
