package com.idemia.biosmart.scenes.settings

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import com.idemia.biosmart.R
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : PreferenceActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val root = findViewById<View>(android.R.id.list).parent.parent.parent as LinearLayout
        val bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false) as Toolbar
        root.addView(bar, 0) // insert at top
        bar.setNavigationOnClickListener{
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || FaceAndFingersPreferenceFragment::class.java.name == fragmentName
                //|| NotificationPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.idemia_key_lkms_url)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.idemia_key_middleware_ip_address)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.idemia_key_middleware_name)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.idemia_key_middleware_port)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_SERVICE_PROVIDER_SERVER_URL)))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows face and fingers configuration. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class FaceAndFingersPreferenceFragment: PreferenceFragment(){
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_face_and_fingers)
            setHasOptionsMenu(true)

            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_CAPTURE_TIMEOUT)))
            // bindPreferenceBoolean(findPreference(getString(R.string.IDEMIA_KEY_USE_CAMERA_REAR)))
            bindPreferenceBoolean(findPreference(getString(R.string.IDEMIA_KEY_USE_OVERLAY)))
            bindPreferenceBoolean(findPreference(getString(R.string.IDEMIA_KEY_USE_TORCH)))
            bindPreferenceBoolean(findPreference(getString(R.string.IDEMIA_KEY_DO_NOT_SHOW_FINGERS_TUTORIAL)))
            bindPreferenceBoolean(findPreference(getString(R.string.IDEMIA_KEY_DO_NOT_SHOW_FACE_TUTORIAL)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_FACE_CAPTURE_MODE)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_FINGERS_CAPTURE_MODE)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_TIME_BEFORE_START_CAPTURE)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.IDEMIA_KEY_CHALLENGE_INTER_DELAY)))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }


    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                    if (index >= 0)
                        listPreference.entries[index]
                    else
                        null
                )

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("")

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue)
                    )

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if(stringValue == "true"){
                    preference.summary = "Yes"
                }else if (stringValue == "false"){
                    preference.summary = "No"
                }else{
                    preference.summary = stringValue
                }
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }

        private fun bindPreferenceBoolean(preference: Preference){
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getBoolean(preference.key, false)
            )
        }
    }
}
