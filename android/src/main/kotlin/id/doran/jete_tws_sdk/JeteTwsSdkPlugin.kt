package id.doran.jete_tws_sdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluetrum.abmate.BuildConfig
import com.bluetrum.abmate.auth.Authenticator
import com.bluetrum.abmate.utils.Utils
import com.bluetrum.abmate.viewmodels.DefaultDeviceCommManager
import com.bluetrum.abmate.viewmodels.DeviceRepository
import com.bluetrum.abmate.viewmodels.ScannerRepository
import com.bluetrum.devicemanager.DeviceManagerApi
import com.bluetrum.devicemanager.models.ABDevice
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
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
import java.lang.reflect.Type


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

  private val mDevices = mutableListOf<Map<String, Any>>()
  private val mABDevices  = mutableListOf<ABDevice>()

  private fun initScanner() {
    mScannerRepository.scannerResults.observeForever{ data ->
      data.devices.forEach { value ->
        val item: MutableMap<String, Any> = HashMap()
        item["deviceName"] = value.bleName
        item["deviceMacAddress"] = value.address
        item["deviceBleAddress"] = value.bleAddress
        item["rssi"] = value.rssi
       // item["abDevice"] = gson.toJson(value)
       // Timber.tag("scannerResults").d(item.toString())
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
//      Timber.tag("mDevices").d(mDevices.toString())
      //         Handler(Looper.getMainLooper()).post {
      scannerResultSink?.success(mDevices)
//          }
    }

    mScannerRepository.scannerState.observeForever{ liveData ->
      Log.d("scannerState", liveData.toString())
      val item: MutableMap<String, Any> = HashMap()
      item["isEmpty"] = liveData.isEmpty
      item["isBluetoothEnabled"] = liveData.isBluetoothEnabled
      item["isScanning"] = liveData.isScanning
      item["isLocationEnabled"] = liveData.isLocationEnabled
      scannerStateSink?.success(item)
    }
  }

  private fun initDevice() {
    // Observe scannerViewModel livedata
    mDeviceRepository.deviceConnectionState.observeForever { state ->
      Log.d("deviceConnectionState","$state")
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
      val item: MutableMap<String, Any> = HashMap()
      item["deviceName"] = device.bleName
      item["deviceMacAddress"] = device.address
      item["deviceBleAddress"] = device.bleAddress
      item["rssi"] = device.rssi
      popupDeviceSink?.success(item)
    }

    mDeviceRepository.activeDevice.observeForever { device ->
      // handle active device
      Log.d("activeDevice","$device")
      val item: MutableMap<String, Any> = HashMap()
      item["deviceName"] = device.bleName
      item["deviceMacAddress"] = device.address
      item["deviceBleAddress"] = device.bleAddress
      item["rssi"] = device.rssi
      activeDeviceSink?.success(item)
    }

    mDeviceRepository.devicePower.observeForever { power ->
      // handle power
      Log.d("devicePower","$power")
      val item: MutableMap<String, Any?> = HashMap()
      item["casePower"] = power.casePower
      item["leftSidePower"] = power.leftSidePower
      item["rightSidePower"] = power.rightSidePower
      devicePowerSink?.success(item)
    }

    // other observers

    mDeviceRepository.scanningState.observeForever { scanning ->
      // handle scanning state
      Log.d("scanningState","$scanning")
      scanningStateSink?.success(scanning)
    }

    mDeviceRepository.deviceFirmwareVersion.observeForever { value ->
      Log.d("deviceFirmwareVersion","$value")
      deviceFirmwareVersionSink?.success(value)
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
//    val authGson = GsonBuilder()
//      .registerTypeAdapter(Authenticator::class.java, AuthenticatorDeserializer())
//      .create()
//    val device: ABDevice = authGson.fromJson(abDevice, ABEarbuds::class.java)
    Log.d("abDevice","$device")
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
      "bondDevice"  -> {
        val bmac: String? = call.argument<String>("bmac")
        if (bmac != null) {
          bondDevice(bmac)
        }
      }
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
      permissionRequestCode,
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

  private fun deviceFromFlutter(device: HashMap<String, Any?>, devices : List<ABDevice>):ABDevice? {
//    item["deviceMacAddress"] = value.address
//    item["deviceBleAddress"] = value.bleAddress
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


class AuthenticatorDeserializer : JsonDeserializer<Authenticator?> {
  @Throws(JsonParseException::class)
  override fun deserialize(
    json: JsonElement,
    typeOfT: Type,
    context: JsonDeserializationContext
  ): Authenticator? {
    if (json.isJsonNull) {
      // Handle null case if needed
      return null
    }
    val jsonObject = json.asJsonObject
    val implementationClassElement = jsonObject["implementationClass"]
    if (implementationClassElement == null || implementationClassElement.isJsonNull) {
      // Handle missing or null element
      return null
    }
    val implementationClassName = implementationClassElement.asString
    return try {
      val implementationClass = Class.forName(implementationClassName)
      implementationClass.newInstance() as Authenticator
    } catch (e: ClassNotFoundException) {
      throw JsonParseException("Error creating instance of Authenticator implementation", e)
    } catch (e: IllegalAccessException) {
      throw JsonParseException("Error creating instance of Authenticator implementation", e)
    } catch (e: InstantiationException) {
      throw JsonParseException("Error creating instance of Authenticator implementation", e)
    }
  }
}