package id.doran.jete_tws_sdk
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluetrum.abmate.utils.Utils
import com.bluetrum.abmate.viewmodels.DefaultDeviceCommManager
import com.bluetrum.abmate.viewmodels.DeviceRepository
import com.bluetrum.abmate.viewmodels.ScannerRepository
import com.bluetrum.devicemanager.DeviceManagerApi
import com.bluetrum.devicemanager.models.ABDevice
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import pub.devrel.easypermissions.EasyPermissions
import java.util.Arrays


/** JeteTwsSdkPlugin */
class JeteTwsSdkPlugin: FlutterPlugin, MethodCallHandler,ActivityAware{
  private lateinit var channel : MethodChannel
  private lateinit var mContext : Context
  private lateinit var mActivity: Activity
  private lateinit var mDefaultDeviceCommManager: DefaultDeviceCommManager
  private lateinit var mDeviceManagerApi: DeviceManagerApi
  private lateinit var mScannerRepository: ScannerRepository
  private lateinit var mDeviceRepository: DeviceRepository
  private val PERMISSIONS_REQUEST_CODE = 999

  @SuppressLint("LogNotTimber")
  private fun initScanner() {
    // Observe scannerViewModel livedata
    mDeviceRepository.deviceConnectionState.observeForever { state ->
      Log.d("deviceConnectionState","$state")
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
      Log.d("popupDevice","$device")
    }

    mDeviceRepository.activeDevice.observeForever { device ->
      // handle active device
      Log.d("activeDevice","$device")
    }

    mDeviceRepository.devicePower.observeForever { power ->
      // handle power
      Log.d("devicePower","$power")
    }

    // other observers

    mDeviceRepository.scanningState.observeForever { scanning ->
      // handle scanning state
      Log.d("scanningState","$scanning")
    }

    mScannerRepository.scannerResults.observeForever{ liveData ->
      Log.d("scannerResults","${liveData.devices}")
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
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "jete_tws_sdk")
    channel.setMethodCallHandler(this)
    mContext = flutterPluginBinding.applicationContext
//    mDeviceManagerApi = DeviceManagerApi(mContext)
//    mDefaultDeviceCommManager = DefaultDeviceCommManager()
//    mScannerRepository = ScannerRepository(mContext,mDeviceManagerApi)
//    mDeviceRepository = DeviceRepository(mContext,mDeviceManagerApi,mDefaultDeviceCommManager)
//    initScanner()
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
    val rationale = arrayOf("Playing music need read external storage permission.")
    val permissions =mutableListOf(
      Manifest.permission.READ_EXTERNAL_STORAGE
    ).apply{
      if (Utils.isAndroid12OrAbove()) {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        rationale[0] += "\n"
        rationale[0] += "Bluetooth scanning and connecting need bluetooth device permission."
      } else {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        rationale[0] += "\n"
        rationale[0] += "Bluetooth scanning and connecting need bluetooth device permission."
      }
    }
    // Request permissions
    EasyPermissions.requestPermissions(
      mActivity,
      rationale[0],
      PERMISSIONS_REQUEST_CODE,
      permissions.toTypedArray().toString()
    )
    mDeviceManagerApi = DeviceManagerApi(mActivity)
    mDefaultDeviceCommManager = DefaultDeviceCommManager()
    mScannerRepository = ScannerRepository(mActivity,mDeviceManagerApi)
    mDeviceRepository = DeviceRepository(mActivity,mDeviceManagerApi,mDefaultDeviceCommManager)
    initScanner()
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onDetachedFromActivity() {

  }
}

