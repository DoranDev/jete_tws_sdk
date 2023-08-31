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
import com.bluetrum.devicemanager.cmd.Request
import com.bluetrum.devicemanager.cmd.request.AncGainRequest
import com.bluetrum.devicemanager.cmd.request.AncModeRequest
import com.bluetrum.devicemanager.cmd.request.AutoAnswerRequest
import com.bluetrum.devicemanager.cmd.request.AutoShutdownRequest
import com.bluetrum.devicemanager.cmd.request.BluetoothNameRequest
import com.bluetrum.devicemanager.cmd.request.ClearPairRecordRequest
import com.bluetrum.devicemanager.cmd.request.DeviceInfoRequest
import com.bluetrum.devicemanager.cmd.request.EqRequest
import com.bluetrum.devicemanager.cmd.request.FactoryResetRequest
import com.bluetrum.devicemanager.cmd.request.FindDeviceRequest
import com.bluetrum.devicemanager.cmd.request.InEarDetectRequest
import com.bluetrum.devicemanager.cmd.request.KeyRequest
import com.bluetrum.devicemanager.cmd.request.LanguageRequest
import com.bluetrum.devicemanager.cmd.request.LedSwitchRequest
import com.bluetrum.devicemanager.cmd.request.MusicControlRequest
import com.bluetrum.devicemanager.cmd.request.SoundEffect3dRequest
import com.bluetrum.devicemanager.cmd.request.TransparencyGainRequest
import com.bluetrum.devicemanager.cmd.request.WorkModeRequest
import com.bluetrum.devicemanager.models.ABDevice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
class JeteTwsSdkPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var mContext : Context
  private lateinit var mActivity: Activity
  private lateinit var mDefaultDeviceCommManager: DefaultDeviceCommManager
  private lateinit var mDeviceManagerApi: DeviceManagerApi
  private lateinit var mScannerRepository: ScannerRepository
  private lateinit var mDeviceRepository: DeviceRepository

  private val permissionRequestCode = 999

  private var scannerResultChannel: EventChannel? = null
  private var scannerResultSink : EventChannel.EventSink? = null
  private val scannerResultHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      Timber.tag("scannerResultSink").d("onListen")
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

  private var deviceInfoChannel: EventChannel? = null
  private var deviceInfoSink : EventChannel.EventSink? = null
  private val deviceInfoHandler = object : EventChannel.StreamHandler {
    override fun onListen(arg: Any?, eventSink: EventChannel.EventSink?) {
      deviceInfoSink = eventSink
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

  private val mDevices = mutableListOf<Map<String, Any>>()
  private val mABDevices  = mutableListOf<ABDevice>()

  private fun deviceMap(value:ABDevice):MutableMap<String, Any> {
    val item: MutableMap<String, Any> = HashMap()
    item["deviceName"] = value.bleName
    item["deviceMacAddress"] = value.address
    item["deviceBleAddress"] = value.bleAddress
    item["rssi"] = value.rssi
    return item
  }

  private fun initScanner() {
    mScannerRepository.scannerResults.observeForever{ data ->
      data.devices.forEach { value ->
        val item: MutableMap<String, Any> = deviceMap(value)

        val existingIndex =  mDevices.indexOfFirst { (it as? Map<String, Any>)?.get("deviceMacAddress") == item["deviceMacAddress"] }

        if(existingIndex != -1){
          mDevices[existingIndex] = item
        }else{
          mABDevices.add(value)
          mDevices.add(item)
        }

        mDevices.sortWith(compareByDescending { (it as? Map<String, Any>)?.get("rssi") as? Int ?: -100 })
      }
      mDeviceRepository
      scannerResultSink?.success(mDevices)
    }

    mScannerRepository.scannerState.observeForever{ liveData ->
      Log.d("scannerState", liveData.toString())
      scannerStateSink?.success(gson.toJson(liveData))
    }
  }

  private fun initDevice() {
    // Observe scannerViewModel livedata
    mDeviceRepository.deviceConnectionState.observeForever { state ->
      Log.d("deviceConnectionState","$state")
//      val DEVICE_CONNECTION_STATE_IDLE = 0
//      val DEVICE_CONNECTION_STATE_PAIRING = 1
//      val DEVICE_CONNECTION_STATE_PAIRED = 2
//      val DEVICE_CONNECTION_STATE_CONNECTING = 3
//      val DEVICE_CONNECTION_STATE_CONNECTED = 4
//      val DEVICE_CONNECTION_STATE_AUTHENTICATING = 5
//      val DEVICE_CONNECTION_STATE_AUTHENTICATED = 6
      deviceConnectionStateSink?.success(state)
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
      val item: MutableMap<String, Any> = deviceMap(device)
      popupDeviceSink?.success(item)
    }

    mDeviceRepository.activeDevice.observeForever { device ->
      // handle active device
      Log.d("activeDevice","$device")
      val item: MutableMap<String, Any> = deviceMap(device)
      activeDeviceSink?.success(item)
    }

    mDeviceRepository.scanningState.observeForever { scanning ->
      // handle scanning state
      Log.d("scanningState","$scanning")
      scanningStateSink?.success(scanning)
    }
  }

  private fun startScan() {
    Log.d("startScan","startScan")
    mDevices.clear()
    mScannerRepository.startScan()
  }

  private fun stopScan() {
    Log.d("stopScan","stopScan")
    mScannerRepository.stopScan()
  }
  private var gson = Gson()
  private fun bondDevice(device:String) {
    Log.d("abDevice", device)
    val type = object : TypeToken<HashMap<String, Any>>() {}.type
    val abDevice: ABDevice? = deviceFromFlutter(device = gson.fromJson(device,type), devices = mABDevices)
    if(abDevice!=null){
      mDeviceRepository.bondDevice(abDevice)
    }
  }


  private fun disconnect() {
    Log.d("disconnect","disconnect")
    mDeviceRepository.disconnect()
  }

  private fun deviceInfo() {
    Log.d("sendRequest","deviceInfo")
    mDeviceRepository.deviceCommManager.sendRequest(DeviceInfoRequest.defaultInfoRequest())
    Thread.sleep(500)
    getLiveData()
  }

  private fun getLiveData() {
    val deviceInfo = DeviceInfoModel(
      devicePower = mDeviceRepository.devicePower.value,
      deviceName = mDeviceRepository.deviceName.value,
      deviceFirmwareVersion = mDeviceRepository.deviceFirmwareVersion.value,
      deviceEqSetting = mDeviceRepository.deviceEqSetting.value,
      deviceKeySettings = mDeviceRepository.deviceKeySettings.value,
      deviceVolume = mDeviceRepository.deviceVolume.value,
      DevicePlayState = mDeviceRepository.devicePlayState.value,
      DeviceWorkMode = mDeviceRepository.deviceWorkMode.value,
      deviceInEarStatus = mDeviceRepository.deviceInEarStatus.value,
      deviceLanguageSetting = mDeviceRepository.deviceLanguageSetting.value,
      deviceAutoAnswer = mDeviceRepository.deviceAutoAnswer.value,
      deviceAncMode = mDeviceRepository.deviceAncMode.value,
      deviceIsTws = mDeviceRepository.deviceIsTws.value,
      deviceTwsConnected = mDeviceRepository.deviceTwsConnected.value,
      deviceLedSwitch = mDeviceRepository.deviceLedSwitch.value,
      deviceFwChecksum = mDeviceRepository.deviceFwChecksum.value,
      deviceAncGain = mDeviceRepository.deviceAncGain.value,
      deviceTransparencyGain = mDeviceRepository.deviceTransparencyGain.value,
      deviceAncGainNum = mDeviceRepository.deviceAncGainNum.value,
      deviceTransparencyGainNum = mDeviceRepository.deviceTransparencyGainNum.value,
      deviceRemoteEqSettings = mDeviceRepository.deviceRemoteEqSettings.value,
      deviceLeftIsMainSide = mDeviceRepository.deviceLeftIsMainSide.value,
      deviceProductColor = mDeviceRepository.deviceProductColor.value,
      deviceSoundEffect3d = mDeviceRepository.deviceSoundEffect3d.value,
      deviceCapacities = mDeviceRepository.deviceCapacities.value,
      deviceMaxPacketSize = mDeviceRepository.deviceMaxPacketSize.value
    )
    Log.d("getLiveData",gson.toJson(deviceInfo))
    deviceInfoSink?.success(gson.toJson(deviceInfo))
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

    deviceInfoChannel = EventChannel(flutterPluginBinding.binaryMessenger, "deviceInfo")
    deviceInfoChannel!!.setStreamHandler(deviceInfoHandler)

    scanningStateChannel = EventChannel(flutterPluginBinding.binaryMessenger, "scanningState")
    scanningStateChannel!!.setStreamHandler(scanningStateHandler)

  }

  override fun onMethodCall( call: MethodCall,  result: Result) {
    when(call.method){
      "startScan"  -> startScan()
      "stopScan"  -> stopScan()
      "bondDevice"  -> {
        val bmac: String? = call.argument<String>("bmac")
        if (bmac != null) {
          bondDevice(bmac)
        }
      }
      "disconnect"  -> disconnect()
      "deviceInfo"  -> deviceInfo()
      "sendRequest" ->{
        val strRequest: String? = call.argument<String>("strRequest")
        val gain: Byte? = call.argument<Int>("gain")?.toByte()
        val mode: Byte? = call.argument<Int>("mode")?.toByte()
        val enable: Boolean? = call.argument<Boolean>("enable")
        val bluetoothName: String? = call.argument<String>("bluetoothName")
        val setting: Byte? = call.argument<Int>("setting")?.toByte()
        val eqmode: Byte? = call.argument<Int>("eqmode")?.toByte()
        val language: Byte? = call.argument<Int>("language")?.toByte()
        val eqgain: Byte? = call.argument<Int>("eqgain")?.toByte()
        val isCustom: Boolean? = call.argument<Boolean>("isCustom")
        val keyType: Byte? = call.argument<Int>("keyType")?.toByte()
        val keyFunction: Byte? = call.argument<Int>("keyFunction")?.toByte()
        val controlType: Byte? = call.argument<Int>("controlType")?.toByte()
        var request: Request? = null
        when (strRequest){
          "AncGainRequest" -> {
            request = gain?.let { AncGainRequest(it) }
          }
          "AncModeRequest" -> {
            request = mode?.let { AncModeRequest(it) }
          }
          "AutoAnswerRequest" -> {
            request = enable?.let { AutoAnswerRequest(it) }
          }
          "AutoShutdownRequest" -> {
            request = setting?.let { AutoShutdownRequest(it) }
          }
          "BluetoothNameRequest" -> {
            request = BluetoothNameRequest(bluetoothName)
          }
          "ClearPairRecordRequest" -> {
            request = ClearPairRecordRequest()
          }
          "EqRequest" -> {
            request = eqmode?.let {
              if(isCustom==true) {
                EqRequest.CustomEqRequest(it, eqgain!!)
              }else{
                EqRequest.PresetEqRequest(it, eqgain!!)
              }
            }
          }
          "FactoryResetRequest" -> {
            request = FactoryResetRequest()
          }
          "FindDeviceRequest" -> {
            request = enable?.let { FindDeviceRequest(it) }
          }
          "InEarDetectRequest" -> {
            request = enable?.let { InEarDetectRequest(it) }
          }
          "KeyRequest" -> {
            request = keyType?.let {
              if (keyFunction != null) {
                KeyRequest(it,keyFunction)
              }else{
                null
              }
            }
          }
          "LanguageRequest" -> {
            request = language?.let { LanguageRequest(it) }
          }
          "LedSwitchRequest" -> {
            request = enable?.let { LedSwitchRequest(it) }
          }
          "MusicControlRequest" -> {
            request = controlType?.let { MusicControlRequest(it) }
          }
          "SoundEffect3dRequest" -> {
            request = enable?.let { SoundEffect3dRequest(it) }
          }
          "TransparencyGainRequest" -> {
            request = gain?.let {TransparencyGainRequest(it) }
          }
          "WorkModeRequest" -> {
            request = mode?.let { WorkModeRequest(it) }
          }
        }
        if (request != null) {
          mDeviceRepository.deviceCommManager.sendRequest(request)
          Log.d("sendRequest",request.toString())
        }
      }
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
      permissionRequestCode,
      permissions.toTypedArray().toString()
    )
    mDeviceManagerApi = DeviceManagerApi(mActivity)
    mDefaultDeviceCommManager = DefaultDeviceCommManager()
    mScannerRepository = ScannerRepository(mActivity,mDeviceManagerApi)
    mDeviceRepository = DeviceRepository(mActivity,mDeviceManagerApi,mDefaultDeviceCommManager)
    mDeviceRepository.registerBroadcastReceivers()
    initScanner()
    initDevice()
  }

  override fun onDetachedFromActivityForConfigChanges() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

  }

  override fun onDetachedFromActivity() {

  }

  private fun deviceFromFlutter(device: HashMap<String, Any?>, devices : List<ABDevice>):ABDevice? {
    if (devices.isEmpty()) {
      Timber.tag("deviceFromFlutter").d("devices list is empty")
      return null
    }

    devices.forEach{
      Timber.tag("deviceFromFlutter").d(it.bleAddress)
      if(it.bleAddress == device["deviceBleAddress"] ){
        return it
      }
    }
    return null
  }
}
