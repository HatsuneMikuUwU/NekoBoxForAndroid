package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.LocaleListCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.preference.EditTextPreferenceModifiers
import io.nekohasekai.sagernet.ktx.FixedLinearLayoutManager
import io.nekohasekai.sagernet.ktx.needReload
import io.nekohasekai.sagernet.ktx.needRestart
import io.nekohasekai.sagernet.ktx.remove
import io.nekohasekai.sagernet.utils.Theme
import moe.matsuri.nb4a.ui.ColorPickerPreference
import moe.matsuri.nb4a.ui.EditConfigPreference
import moe.matsuri.nb4a.ui.LongClickListPreference
import moe.matsuri.nb4a.ui.MTUPreference
import moe.matsuri.nb4a.ui.SimpleMenuPreference
import java.util.Locale

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var isProxyApps: SwitchPreference
    private lateinit var globalCustomConfig: EditConfigPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.layoutManager = FixedLinearLayoutManager(listView)
    }

    private val reloadListener = Preference.OnPreferenceChangeListener { _, _ ->
        needReload()
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStore.configurationStore
        DataStore.initGlobal()
        addPreferencesFromResource(R.xml.global_preferences)

        val appTheme = findPreference<ColorPickerPreference>(Key.APP_THEME)!!
        appTheme.setOnPreferenceChangeListener { _, newTheme ->
            if (DataStore.serviceState.started) {
                SagerNet.reloadService()
            }
            val theme = Theme.getTheme(newTheme as Int)
            requireActivity().apply {
                setTheme(theme)
                ActivityCompat.recreate(this)
            }
            true
        }

        val nightTheme = findPreference<SimpleMenuPreference>(Key.NIGHT_THEME)!!
        nightTheme.setOnPreferenceChangeListener { _, newTheme ->
            Theme.currentNightMode = (newTheme as String).toInt()
            Theme.applyNightTheme()
            true
        }

        // Language switcher feature
        fun getLanguageDisplayName(code: String): String {
            return when (code) {
                "" -> getString(R.string.language_system_default)
                "en-US" -> getString(R.string.language_en_display_name)
                "zh-Hans" -> getString(R.string.language_zh_Hans_display_name)
                else -> Locale.forLanguageTag(code).displayName
            }
        }

        val appLanguage = findPreference<SimpleMenuPreference>(Key.APP_LANGUAGE)!!
        val localeTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "" }
        val currentLocale = if (localeTag == "in") "id" else localeTag
        appLanguage.summary = getLanguageDisplayName(currentLocale)
        appLanguage.value = if (currentLocale in resources.getStringArray(R.array.language_value)) currentLocale else ""
        appLanguage.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newValue as String))
            true
        }

        val mixedPort = findPreference<EditTextPreference>(Key.MIXED_PORT)!!
        mixedPort.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)

        if (Build.VERSION.SDK_INT < 28) {
            findPreference<Preference>(Key.METERED_NETWORK)?.remove()
        }

        isProxyApps = findPreference(Key.PROXY_APPS)!!
        isProxyApps.setOnPreferenceChangeListener { _, newValue ->
            startActivity(Intent(activity, AppManagerActivity::class.java))
            if (newValue as Boolean) DataStore.dirty = true
            newValue
        }

        val profileTrafficStatistics = findPreference<SwitchPreference>(Key.PROFILE_TRAFFIC_STATISTICS)!!
        val speedInterval = findPreference<SimpleMenuPreference>(Key.SPEED_INTERVAL)!!
        profileTrafficStatistics.isEnabled = speedInterval.value != "0"
        speedInterval.setOnPreferenceChangeListener { _, newValue ->
            profileTrafficStatistics.isEnabled = newValue.toString() != "0"
            needReload()
            true
        }

        val enableClashAPI = findPreference<SwitchPreference>(Key.ENABLE_CLASH_API)!!
        enableClashAPI.setOnPreferenceChangeListener { _, newValue ->
            (activity as? MainActivity)?.refreshNavMenu(newValue as Boolean)
            needReload()
            true
        }
        
        val serviceMode = findPreference<Preference>(Key.SERVICE_MODE)!!
        serviceMode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
            if (DataStore.serviceState.started) SagerNet.stopService()
            true
        }

        globalCustomConfig = findPreference(Key.GLOBAL_CUSTOM_CONFIG)!!
        globalCustomConfig.useConfigStore(Key.GLOBAL_CUSTOM_CONFIG)

        findPreference<LongClickListPreference>(Key.LOG_LEVEL)!!.let { logLevel ->
            logLevel.dialogLayoutResource = R.layout.layout_loglevel_help
            logLevel.setOnPreferenceChangeListener { _, _ ->
                needRestart()
                true
            }
            logLevel.setOnLongClickListener {
                context?.let { ctx ->
                    val view = EditText(ctx).apply {
                        inputType = EditorInfo.TYPE_CLASS_NUMBER
                        val size = DataStore.logBufSize.takeIf { it > 0 } ?: 50
                        setText(size.toString())
                    }
                    MaterialAlertDialogBuilder(requireContext()).setTitle("Log buffer size (kb)")
                        .setView(view)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            DataStore.logBufSize = view.text.toString().toIntOrNull() ?: 50
                            if (DataStore.logBufSize <= 0) DataStore.logBufSize = 50
                            needRestart()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                true
            }
        }

        // Assign reload listeners
        mixedPort.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.APPEND_HTTP_PROXY)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.SHOW_DIRECT_SPEED)!!.onPreferenceChangeListener = reloadListener
        findPreference<Preference>(Key.TRAFFIC_SNIFFING)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.BYPASS_LAN)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.BYPASS_LAN_IN_CORE)!!.onPreferenceChangeListener = reloadListener
        findPreference<MTUPreference>(Key.MTU)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.ENABLE_FAKEDNS)!!.onPreferenceChangeListener = reloadListener
        findPreference<EditTextPreference>(Key.REMOTE_DNS)!!.onPreferenceChangeListener = reloadListener
        findPreference<EditTextPreference>(Key.DIRECT_DNS)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.ENABLE_DNS_ROUTING)!!.onPreferenceChangeListener = reloadListener
        findPreference<Preference>(Key.IPV6_MODE)!!.onPreferenceChangeListener = reloadListener
        findPreference<Preference>(Key.ALLOW_ACCESS)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.RESOLVE_DESTINATION)!!.onPreferenceChangeListener = reloadListener
        findPreference<SimpleMenuPreference>(Key.TUN_IMPLEMENTATION)!!.onPreferenceChangeListener = reloadListener
        findPreference<SwitchPreference>(Key.ACQUIRE_WAKE_LOCK)!!.onPreferenceChangeListener = reloadListener
        globalCustomConfig.onPreferenceChangeListener = reloadListener
    }

    override fun onResume() {
        super.onResume()
        if (::isProxyApps.isInitialized) {
            isProxyApps.isChecked = DataStore.proxyApps
        }
        if (::globalCustomConfig.isInitialized) {
            globalCustomConfig.notifyChanged()
        }
    }
}

