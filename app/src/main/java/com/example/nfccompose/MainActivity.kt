package com.example.nfccompose

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.nfccompose.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity, NfcAdapter.ReaderCallback {

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
    }

    private val viewModel: MainViewModel by viewModels<MainViewModel>()

    constructor() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var text by remember {
                mutableStateOf("")
            }
            val activityCompat: FragmentActivity = this
            Column {
                Button(onClick = {
                    onCheckedChanged(true)
                    Coroutines.main(activityCompat) { scope ->
                        scope.launch(block = {
                            viewModel.observeNFCStatus()
                                .collectLatest(action = { status ->
                                    Log.d(TAG, "observeNFCStatus $status")
                                    if (status == NFCStatus.NoOperation) NFCManager.disableReaderMode(
                                        this@MainActivity,
                                        this@MainActivity
                                    )
                                    else if (status == NFCStatus.Tap) NFCManager.enableReaderMode(
                                        this@MainActivity,
                                        this@MainActivity,
                                        this@MainActivity,
                                        viewModel.getNFCFlags(),
                                        viewModel.getExtras()
                                    )
                                })
                        })
                        scope.launch(block = {
                            viewModel.observeToast().collectLatest(action = { message ->
                                Log.d(TAG, "observeToast $message")
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                            })
                        })
                        scope.launch(block = {
                            viewModel.observeTag().collectLatest(action = { tag ->
                                Log.d(TAG, "observeTag $tag")
                                if (tag != null) {
                                    text = tag
                                }
                            })
                        })
                    }

                }) {
                    Text(text = "presionar")
                }
                Text(text = text)
            }

        }
    }

     fun onCheckedChanged(isChecked : Boolean) {
            viewModel.onCheckNFC(isChecked)
    }

    override fun onTagDiscovered(tag : Tag?) {
       viewModel.readTag(tag)
        if (tag != null) {
            Log.i("tag", tag.id.toString())
        }
    }


}






