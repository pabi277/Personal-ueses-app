package io.github.pabi277.dayledger

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BlockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("package") ?: ""
        setContent {
            io.github.pabi277.dayledger.ui.theme.DayLedgerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BlockScreen(
                        packageName = pkg,
                        onExtend = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val db = DayLedgerDatabase.getInstance(this@BlockScreenActivity)
                                db.limitEventDao().insert(
                                    io.github.pabi277.dayledger.data.LimitEventEntity(
                                        dateKey = LocalDate.now().toString(),
                                        packageName = pkg,
                                        limitMin = 0,
                                        reachedAt = System.currentTimeMillis(),
                                        action = "block",
                                        blockDurationMin = 10,
                                        overrideType = "extend"
                                    )
                                )
                            }
                            finish()
                        },
                        onUnlock = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val db = DayLedgerDatabase.getInstance(this@BlockScreenActivity)
                                db.limitEventDao().insert(
                                    io.github.pabi277.dayledger.data.LimitEventEntity(
                                        dateKey = LocalDate.now().toString(),
                                        packageName = pkg,
                                        limitMin = 0,
                                        reachedAt = System.currentTimeMillis(),
                                        action = "block",
                                        overrideType = "pin"
                                    )
                                )
                            }
                            finish()
                        },
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun BlockScreen(packageName: String, onExtend: () -> Unit, onUnlock: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Time's up for $packageName", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("You've reached your limit. Take a breath.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExtend) { Text("Extend 10 min (once)") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose) { Text("Skip to next block") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onUnlock) { Text("Unlock anyway") }
    }
}
