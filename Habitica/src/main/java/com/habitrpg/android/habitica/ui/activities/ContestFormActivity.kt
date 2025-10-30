package com.habitrpg.android.habitica.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityContestFormBinding
import com.habitrpg.android.habitica.models.contests.Contest
import com.habitrpg.android.habitica.ui.viewmodels.ContestsViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Activity for creating and editing contests
 */
@AndroidEntryPoint
class ContestFormActivity : BaseActivity() {

    private lateinit var binding: ActivityContestFormBinding
    private val viewModel: ContestsViewModel by viewModels()
    
    private var contestId: String? = null
    private var contest: Contest? = null
    private var startTime: Date? = null
    private var endTime: Date? = null
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun getLayoutResId(): Int = R.layout.activity_contest_form

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityContestFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        contestId = intent.getStringExtra(CONTEST_ID_KEY)
        
        setupPlatformSpinner()
        setupListeners()
        
        contestId?.let { id ->
            loadContest(id)
        }
    }

    private fun setupPlatformSpinner() {
        val platforms = arrayOf(
            "Codeforces",
            "LeetCode",
            "CodeChef",
            "AtCoder",
            "HackerRank",
            "HackerEarth",
            "TopCoder",
            "Other"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, platforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.platformSpinner.adapter = adapter
    }

    private fun setupListeners() {
        binding.startTimeButton.setOnClickListener {
            showDateTimePicker { date ->
                startTime = date
                binding.startTimeButton.text = dateFormat.format(date)
            }
        }
        
        binding.endTimeButton.setOnClickListener {
            showDateTimePicker { date ->
                endTime = date
                binding.endTimeButton.text = dateFormat.format(date)
            }
        }
        
        binding.saveButton.setOnClickListener {
            saveContest()
        }
    }

    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(calendar.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadContest(id: String) {
        launchCatching {
            viewModel.getContest(id).collect { loadedContest ->
                contest = loadedContest
                populateFields(loadedContest)
            }
        }
    }

    private fun populateFields(contest: Contest) {
        binding.nameEditText.setText(contest.name)
        binding.descriptionEditText.setText(contest.description)
        binding.urlEditText.setText(contest.url)
        
        contest.platform?.let { platform ->
            val adapter = binding.platformSpinner.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(platform)
            if (position >= 0) {
                binding.platformSpinner.setSelection(position)
            }
        }
        
        contest.startTime?.let { start ->
            startTime = start
            binding.startTimeButton.text = dateFormat.format(start)
        }
        
        contest.endTime?.let { end ->
            endTime = end
            binding.endTimeButton.text = dateFormat.format(end)
        }
        
        contest.duration.let { duration ->
            binding.durationEditText.setText(duration.toString())
        }
    }

    private fun saveContest() {
        val name = binding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameEditText.error = "Name is required"
            return
        }
        
        val platform = binding.platformSpinner.selectedItem.toString()
        val description = binding.descriptionEditText.text.toString().trim()
        val url = binding.urlEditText.text.toString().trim()
        val duration = binding.durationEditText.text.toString().toLongOrNull() ?: 0L
        
        val contestToSave = contest ?: Contest()
        contestToSave.name = name
        contestToSave.platform = platform
        contestToSave.description = description
        contestToSave.url = url
        contestToSave.startTime = startTime
        contestToSave.endTime = endTime
        contestToSave.duration = duration
        
        if (contest != null) {
            viewModel.updateContest(contestToSave)
        } else {
            viewModel.createContest(contestToSave)
        }
        
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val CONTEST_ID_KEY = "contest_id"
    }
}
