package com.kelsos.mbrc.utilities

import android.app.Application
import com.kelsos.mbrc.R
import javax.inject.Inject

class KeyProviderImpl
@Inject
constructor(private val context: Application) : KeyProvider {

  override val hostKey: String
    get() = context.getString(R.string.settings_key_hostname)

  override val portKey: String
    get() = context.getString(R.string.settings_key_port)

  override val reduceVolumeKey: String
    get() = context.getString(R.string.settings_legacy_key_reduce_volume)

  override val notificationKey: String
    get() = context.getString(R.string.settings_key_notification_control)

  override val pluginUpdateCheckKey: String
    get() = context.getString(R.string.settings_key_plugin_check)

  override val lastUpdateKey: String
    get() = context.getString(R.string.settings_key_last_update_check)

  override val searchActionKey: String
    get() = context.getString(R.string.settings_search_default_key)

  override val searchActionValueKey: String
    get() = context.getString(R.string.search_click_default_value)

  override val lastVersionKey: String
    get() = context.getString(R.string.settings_key_last_version_run)
}
