package id.doran.jete_tws_sdk
import android.content.Context
import com.bluetrum.abmate.BuildConfig
import com.bluetrum.abmate.viewmodels.DefaultDeviceCommManager
import com.bluetrum.abmate.viewmodels.DeviceRepository
import com.bluetrum.abmate.viewmodels.ScannerRepository
import com.bluetrum.devicemanager.DeviceManagerApi
import com.bluetrum.devicemanager.models.ABDevice
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import timber.log.Timber
import timber.log.Timber.DebugTree

/** JeteTwsSdkPlugin */
class JeteTwsSdkPlugin: FlutterPlugin, MethodCallHandler{
  private lateinit var channel : MethodChannel
  private lateinit var mContext : Context
  private lateinit var mDefaultDeviceCommManager: DefaultDeviceCommManager
  private lateinit var mDeviceManagerApi: DeviceManagerApi
  private lateinit var mScannerRepository: ScannerRepository
  private lateinit var mDeviceRepository: DeviceRepository


  private fun initScanner() {
    // Observe scannerViewModel livedata
    mDeviceRepository.deviceConnectionState.observeForever { state ->
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

    mDeviceRepository.popupDevice.observeForever { device ->
      // handle popup device
      Timber.d("$device")
    }

    mDeviceRepository.activeDevice.observeForever { device ->
      // handle active device
      Timber.d("$device")
    }

    mDeviceRepository.devicePower.observeForever { power ->
      // handle power
      Timber.d("$power")
    }

    // other observers

    mDeviceRepository.scanningState.observeForever { scanning ->
      // handle scanning state
      Timber.d("$scanning")
    }

    mScannerRepository.scannerResults.observeForever{ liveData ->
      Timber.d("${liveData.devices}")
    }
  }

  private fun startScan() {
    mDeviceRepository.startScan()
  }

  private fun stopScan() {
    mDeviceRepository.stopScan()
  }

  private fun bondDevice() {
    val device: ABDevice? = mDeviceRepository.activeDevice.value
    mDeviceRepository.bondDevice(device)
  }


  private fun disconnect() {
    mDeviceRepository.disconnect()
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "jete_tws_sdk")
    channel.setMethodCallHandler(this)
    mContext = flutterPluginBinding.applicationContext
    mDeviceManagerApi = DeviceManagerApi(mContext)
    mDefaultDeviceCommManager = DefaultDeviceCommManager()
    mScannerRepository = ScannerRepository(mContext,mDeviceManagerApi)
    mDeviceRepository = DeviceRepository(mContext,mDeviceManagerApi,mDefaultDeviceCommManager)
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
}
