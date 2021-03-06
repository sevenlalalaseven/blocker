package com.merxury.blocker.ui.settings

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceManager
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AlertDialog
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.MenuItem
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.elvishew.xlog.XLog
import com.merxury.blocker.R
import com.merxury.blocker.util.ToastUtil
import com.merxury.blocker.work.ScheduledWork
import com.merxury.libkit.utils.FileUtils
import java.util.concurrent.TimeUnit

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class PreferenceFragment : PreferenceFragmentCompat(), SettingsContract.SettingsView, Preference.OnPreferenceClickListener {
    private val logger = XLog.tag("PreferenceFragment")
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var prefs: SharedPreferences
    private lateinit var presenter: SettingsPresenter

    private lateinit var controllerTypePreference: Preference
    private lateinit var rulePathPreference: Preference
    private lateinit var exportRulePreference: Preference
    private lateinit var importRulePreference: Preference
    private lateinit var ifwRulePathPreference: Preference
    private lateinit var exportIfwRulePreference: Preference
    private lateinit var importIfwRulePreference: Preference
    private lateinit var resetIfwPreference: Preference
    private lateinit var importMatRulesPreference: Preference
    private lateinit var autoBlockPreference: CheckBoxPreference
    private lateinit var forceDozePreference: CheckBoxPreference
    private lateinit var aboutPreference: Preference

    private val matRulePathRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        findPreference()
        initPreference()
        initListener()
        initPresenter()
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    private fun findPreference() {
        controllerTypePreference = findPreference(getString(R.string.key_pref_controller_type))
        rulePathPreference = findPreference(getString(R.string.key_pref_rule_path))
        exportRulePreference = findPreference(getString(R.string.key_pref_export_rules))
        importRulePreference = findPreference(getString(R.string.key_pref_import_rules))
        ifwRulePathPreference = findPreference(getString(R.string.key_pref_ifw_rule_path))
        importIfwRulePreference = findPreference(getString(R.string.key_pref_import_ifw_rules))
        exportIfwRulePreference = findPreference(getString(R.string.key_pref_export_ifw_rules))
        resetIfwPreference = findPreference(getString(R.string.key_pref_reset_ifw_rules))
        importMatRulesPreference = findPreference(getString(R.string.key_pref_import_mat_rules))
        autoBlockPreference = findPreference(getString(R.string.key_pref_auto_block)) as CheckBoxPreference
        forceDozePreference = findPreference(getString(R.string.key_pref_force_doze)) as CheckBoxPreference
        aboutPreference = findPreference(getString(R.string.key_pref_about))
    }

    private fun initPreference() {
        controllerTypePreference.setDefaultValue(getString(R.string.key_pref_controller_type_default_value))
        rulePathPreference.setDefaultValue(getString(R.string.key_pref_rule_path_default_value))
        bindPreferenceSummaryToValue(rulePathPreference)
        ifwRulePathPreference.setDefaultValue(getString(R.string.key_pref_ifw_rule_path_default_value))
        bindPreferenceSummaryToValue(ifwRulePathPreference)
    }

    private fun initPresenter() {
        presenter = SettingsPresenter(context!!, this)
    }

    private fun initListener() {
        listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            // TODO add later
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        exportRulePreference.onPreferenceClickListener = this
        importRulePreference.onPreferenceClickListener = this
        exportIfwRulePreference.onPreferenceClickListener = this
        importIfwRulePreference.onPreferenceClickListener = this
        importMatRulesPreference.onPreferenceClickListener = this
        resetIfwPreference.onPreferenceClickListener = this
        autoBlockPreference.onPreferenceClickListener = this
        forceDozePreference.onPreferenceClickListener = this
        aboutPreference.onPreferenceClickListener = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showExportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int) {

    }

    override fun showImportResult(isSucceed: Boolean, successfulCount: Int, failedCount: Int) {

    }

    override fun showResetResult(isSucceed: Boolean) {

    }

    override fun showMessage(res: Int) {
        ToastUtil.showToast(res, Toast.LENGTH_SHORT)
    }

    override fun showDialog(title: String, message: String, action: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.ok) { _, _ -> action() }
                    .create()
                    .show()
        }
    }

    override fun showDialog(title: String, message: String, file: String?, action: (file: String?) -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.ok) { _, _ -> action(file) }
                    .create()
                    .show()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        if (preference == null) {
            return false
        }
        logger.d("onPreferenceClick: ${preference.key}")
        when (preference) {
            exportRulePreference -> showDialog(getString(R.string.warning), getString(R.string.export_all_rules_warning_message), presenter::exportAllRules)
            importRulePreference -> showDialog(getString(R.string.warning), getString(R.string.import_all_rules_warning_message), presenter::importAllRules)
            exportIfwRulePreference -> showDialog(getString(R.string.warning), getString(R.string.export_all_ifw_rules_warning_message), presenter::exportAllIfwRules)
            importIfwRulePreference -> showDialog(getString(R.string.warning), getString(R.string.import_all_ifw_rules_warning_message), presenter::importAllIfwRules)
            importMatRulesPreference -> selectMatFile()
            resetIfwPreference -> showDialog(getString(R.string.warning), getString(R.string.reset_ifw_warning_message), presenter::resetIFW)
            autoBlockPreference, forceDozePreference -> initAutoBlockAndDoze()
            aboutPreference -> showAbout()
            else -> return false
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            matRulePathRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val filePath = FileUtils.getUriPath(context!!, data?.data)
                    showDialog(getString(R.string.warning), getString(R.string.import_all_rules_warning_message), filePath, presenter::importMatRules)
                }
            }
        }
    }

    private fun selectMatFile() {
        val pm = context?.packageManager ?: return
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        if (intent.resolveActivity(pm) != null) {
            startActivityForResult(intent, matRulePathRequestCode)
        } else {
            ToastUtil.showToast(getString(R.string.file_manager_required))
        }
    }

    private fun showAbout() {
        CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(ABOUT_URL))
    }

    private fun initAutoBlockAndDoze() {
        if (!autoBlockPreference.isChecked && !forceDozePreference.isChecked) {
            logger.d("Canceling scheduled work.")
            WorkManager.getInstance().cancelAllWork()
        } else {
            warnExperimentalFeature()
            val scheduleWork = PeriodicWorkRequest.Builder(ScheduledWork::class.java,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS).build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(SCHEDULED_WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, scheduleWork)
            logger.d("Scheduled work activated")
        }
    }

    private fun warnExperimentalFeature() {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.experimental_features_warning)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }

    }

    companion object {
        private const val ABOUT_URL = "https://github.com/lihenggui/blocker"
        private const val SCHEDULED_WORK_TAG = "BlockerScheduledWork"

        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else {
                preference.summary = stringValue
            }
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}