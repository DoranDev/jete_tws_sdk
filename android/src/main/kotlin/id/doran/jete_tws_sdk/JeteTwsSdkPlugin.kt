package id.doran.jete_tws_sdk
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.bluetrum.abmate.viewmodels.DeviceRepository
import com.bluetrum.abmate.viewmodels.ScannerViewModel
import com.bluetrum.devicemanager.models.ABDevice
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import timber.log.Timber

/** JeteTwsSdkPlugin */
class JeteTwsSdkPlugin: FlutterPlugin, MethodCallHandler, ActivityAware{
  private lateinit var channel : MethodChannel
  private lateinit var mContext : Context
  private lateinit var mActivity: Context


  // Singleton instance of your ViewModel
  private lateinit var scannerViewModel: ScannerViewModel


  private fun initScanner() {
    // Observe scannerViewModel livedata
    scannerViewModel.deviceConnectionState.observeForever { state ->
      Timber.d("$state")
      when (state) {
        DeviceRepository.DEVICE_CONNECTION_STATE_IDLE -> {
          // handle idle state
        }
        DeviceRepository.DEVICE_CONNECTION_STATE_PAIRING -> {
          // handle pairing state
        }
        // other states
      }
    }

    scannerViewModel.popupDevice.observeForever { device ->
      // handle popup device
      Timber.d("$device")
    }

    scannerViewModel.activeDevice.observeForever { device ->
      // handle active device
      Timber.d("$device")
    }

    scannerViewModel.devicePower.observeForever { power ->
      // handle power
      Timber.d("$power")
    }

    // other observers

    scannerViewModel.deviceRepository.scanningState.observeForever { scanning ->
      // handle scanning state
      Timber.d("$scanning")
    }

    scannerViewModel.scannerRepository.scannerResults.observeForever{ liveData ->
      Timber.d("${liveData.devices}")
    }
  }

  private fun startScan() {
    scannerViewModel.deviceRepository?.startScan()
  }

  private fun stopScan() {
    scannerViewModel.deviceRepository?.stopScan()
  }

  private fun bondDevice() {
    val device: ABDevice? = scannerViewModel.activeDevice.value
    scannerViewModel.deviceRepository?.bondDevice(device)
  }


  private fun disconnect() {
    scannerViewModel.deviceRepository?.disconnect()
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "jete_tws_sdk")
    channel.setMethodCallHandler(this)
    mContext = flutterPluginBinding.applicationContext
    initScanner()
  }

  override fun onMethodCall( call: MethodCall,  result: Result) {
    when(call.method){
      "startScan"  -> startScan()
      "stopScan"  -> stopScan()
      "bondDevice"  -> bondDevice()
      "disconnect"  -> disconnect()
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine( binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    mActivity = binding.activity
    scannerViewModel = ViewModelProvider(mActivity as FlutterFragmentActivity).get(ScannerViewModel::class.java)
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onDetachedFromActivity() {

  }
}
