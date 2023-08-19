package id.doran.jete_tws_sdk
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluetrum.abmate.BuildConfig
import com.bluetrum.abmate.utils.Utils
import com.bluetrum.abmate.viewmodels.DefaultDeviceCommManager
import com.bluetrum.abmate.viewmodels.DeviceRepository
import com.bluetrum.abmate.viewmodels.ScannerRepository
import com.bluetrum.devicemanager.DeviceManagerApi
import com.bluetrum.devicemanager.models.ABDevice
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import timber.log.Timber.DebugTree


/** JeteTwsSdkPlugin */
@SuppressLint("LogNotTimber")
class JeteTwsSdkPlugin: FlutterPlugin, MethodCallHandler, ActivityAware{
  private lateinit var channel : MethodChannel
  private lateinit var mContext : Context
  private lateinit var mActivity: Activity
  private lateinit var mDefaultDeviceCommManager: DefaultDeviceCommManager
  private lateinit var mDeviceManagerApi: DeviceManagerApi
  private lateinit var mScannerRepository: ScannerRepository
  private lateinit var mDeviceRepository: DeviceRepository
  private val PERMISSIONS_REQUEST_CODE = 999

  private var scannerResultChannel: EventChannel? = null
  private var scannerResultSink : EventChannel.EventSink? = null
  private val scannerResultHandler = object : EventChannel.StreamHandler {
      override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
        scannerResultSink = eventSink
      }
      override fun onCancel(o: Any?) {}
    }

  private var scannerStateChannel: EventChannel? = null
  private var scannerStateSink : EventChannel.EventSink? = null
  private val scannerStateHandler = object : EventChannel.StreamHandler {
      override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
        scannerStateSink = eventSink
      }
      override fun onCancel(o: Any?) {}
    }

  private var deviceConnectionStateChannel: EventChannel? = null
  private var deviceConnectionStateSink : EventChannel.EventSink? = null
  private val deviceConnectionStateHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      deviceConnectionStateSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private var popupDeviceChannel: EventChannel? = null
  private var popupDeviceSink : EventChannel.EventSink? = null
  private val popupDeviceHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      popupDeviceSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private var activeDeviceChannel: EventChannel? = null
  private var activeDeviceSink : EventChannel.EventSink? = null
  private val activeDeviceHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      activeDeviceSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private var devicePowerChannel: EventChannel? = null
  private var devicePowerSink : EventChannel.EventSink? = null
  private val devicePowerHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      devicePowerSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private var scanningStateChannel: EventChannel? = null
  private var scanningStateSink : EventChannel.EventSink? = null
  private val scanningStateHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      scanningStateSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private var deviceFirmwareVersionChannel: EventChannel? = null
  private var deviceFirmwareVersionSink : EventChannel.EventSink? = null
  private val deviceFirmwareVersionHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      deviceFirmwareVersionSink = eventSink
    }
    override fun onCancel(o: Any?) {}
  }

  private fun initScanner() {
    mScannerRepository.scannerResults.observeForever{ liveData ->
      liveData.devices.forEach { value ->
        Log.d("scannerResults", value.toString())
      }
    }

    mScannerRepository.scannerState.observeForever{ liveData ->
      Log.d("scannerState", liveData.toString())
    }
  }

  private fun initDevice() {
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

    mDeviceRepository.deviceFirmwareVersion.observeForever { value ->
      Log.d("deviceFirmwareVersion","$value")
    }


  }

  private fun startScan() {
    Log.d("startScan","startScan")
    mScannerRepository.startScan()
  }

  private fun stopScan() {
    Log.d("stopScan","stopScan")
    mScannerRepository.stopScan()
  }

  private fun bondDevice() {
    val device: ABDevice? = mDeviceRepository.activeDevice.value
    Log.d("bondDevice","$device")
    mDeviceRepository.bondDevice(device)
  }


  private fun disconnect() {
    Log.d("disconnect","disconnect")
    mDeviceRepository.disconnect()
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "jete_tws_sdk")
    channel.setMethodCallHandler(this)
    mContext = flutterPluginBinding.applicationContext

    scannerResultChannel = EventChannel(flutterPluginBinding.binaryMessenger, "scannerResult")
    scannerResultChannel!!.setStreamHandler(scannerResultHandler)

    scannerStateChannel = EventChannel(flutterPluginBinding.binaryMessenger, "scannerState")
    scannerStateChannel!!.setStreamHandler(scannerStateHandler)

    deviceConnectionStateChannel = EventChannel(flutterPluginBinding.binaryMessenger, "deviceConnectionState")
    deviceConnectionStateChannel!!.setStreamHandler(deviceConnectionStateHandler)

    popupDeviceChannel = EventChannel(flutterPluginBinding.binaryMessenger, "popupDevice")
    popupDeviceChannel!!.setStreamHandler(popupDeviceHandler)

    activeDeviceChannel = EventChannel(flutterPluginBinding.binaryMessenger, "activeDevice")
    activeDeviceChannel!!.setStreamHandler(activeDeviceHandler)

    devicePowerChannel = EventChannel(flutterPluginBinding.binaryMessenger, "devicePower")
    devicePowerChannel!!.setStreamHandler(devicePowerHandler)

    scanningStateChannel = EventChannel(flutterPluginBinding.binaryMessenger, "scanningState")
    scanningStateChannel!!.setStreamHandler(scanningStateHandler)

    deviceFirmwareVersionChannel = EventChannel(flutterPluginBinding.binaryMessenger, "deviceFirmwareVersion")
    deviceFirmwareVersionChannel!!.setStreamHandler(deviceFirmwareVersionHandler)


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
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }
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
    initDevice()
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onDetachedFromActivity() {

  }
}

