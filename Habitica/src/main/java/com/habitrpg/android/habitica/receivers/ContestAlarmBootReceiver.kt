package com.habitrpg.android.habitica.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitrpg.android.habitica.data.ContestRepository
import com.habitrpg.android.habitica.helpers.contests.ContestAlarmManager
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver that reschedules contest reminders after device reboot.
 * 
 * Android Behavior:
 * - All alarms are cleared on device reboot
 * - Apps must reschedule alarms via BOOT_COMPLETED broadcast
 * - Requires RECEIVE_BOOT_COMPLETED permission
 * 
 * Architecture:
 * - Uses Hilt for dependency injection
 * - Follows TaskAlarmBootReceiver pattern
 * - Lightweight operation (delegates to alarm manager)
 * 
 * Performance:
 * - Runs on background thread
 * - Only reschedules enabled reminders
 * - Batches alarm updates for efficiency
 * 
 * Testing:
 * - Manual: `adb shell am broadcast -a android.intent.action.BOOT_COMPLETED`
 * - Unit test: ContestAlarmBootReceiverTest.kt
 * 
 * Permissions Required:
 * - RECEIVE_BOOT_COMPLETED (in AndroidManifest.xml)
 * - SCHEDULE_EXACT_ALARM (Android 12+)
 * 
 * Reference:
 * - TaskAlarmBootReceiver implementation
 * - Android AlarmManager best practices
 */
@AndroidEntryPoint
class ContestAlarmBootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var contestRepository: ContestRepository
    
    @Inject
    lateinit var contestAlarmManager: ContestAlarmManager
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        
        HLogger.log(LogLevel.INFO, TAG, "Device boot completed - rescheduling contest reminders")
        
        // Use background coroutine to avoid blocking main thread
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            try {
                // Fetch all upcoming contests with reminders enabled
                val contests = contestRepository.getUpcomingContests().firstOrNull() ?: emptyList()
                val enabledReminders = contests.filter { it.isReminderEnabled }
                
                HLogger.log(
                    LogLevel.INFO,
                    TAG,
                    "Found ${enabledReminders.size} contests with reminders to reschedule"
                )
                
                // Reschedule all reminders
                contestAlarmManager.rescheduleAllReminders(enabledReminders)
                
                HLogger.log(
                    LogLevel.INFO,
                    TAG,
                    "Successfully rescheduled contest reminders after boot"
                )
            } catch (e: Exception) {
                HLogger.log(
                    LogLevel.ERROR,
                    TAG,
                    "Failed to reschedule contest reminders after boot",
                    e
                )
            }
        }
    }
    
    companion object {
        private const val TAG = "ContestAlarmBootReceiver"
    }
}
