package me.csystems.gategarage.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import me.csystems.palandgate.BuildConfig
import me.csystems.palandgate.R

class MainActivity : AppCompatActivity() {

    private val viewModel: GateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val btnOpen  = findViewById<Button>(R.id.btnOpenGate)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val progress = findViewById<ProgressBar>(R.id.progressBar)

        val density = resources.displayMetrics.density
        swipeRefresh.setProgressViewOffset(false, 0, (120 * density).toInt())
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            swipeRefresh.isRefreshing = false
        }

        btnOpen.setOnClickListener { viewModel.openGate() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val loading = state is GateUiState.Loading
                    btnOpen.isEnabled   = !loading
                    progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE

                    when (state) {
                        is GateUiState.Idle    -> tvStatus.text = ""
                        is GateUiState.Loading -> {
                            tvStatus.text = state.message
                            tvStatus.setTextColor(getColor(android.R.color.white))
                        }
                        is GateUiState.Success -> {
                            tvStatus.text = getString(R.string.status_success)
                            tvStatus.setTextColor(getColor(R.color.success_green))
                            btnOpen.postDelayed({ viewModel.resetState() }, 3000)
                        }
                        is GateUiState.Error   -> {
                            tvStatus.text = state.message
                            tvStatus.setTextColor(getColor(R.color.error_red))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(
                        "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})\n" +
                        "Built on ${BuildConfig.BUILD_DATE}\n\n" +
                        "github.com/k0smo3/palandgate"
                    )
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
