package id.doran.jete_tws_sdk

import com.bluetrum.devicemanager.models.DevicePower
import com.bluetrum.devicemanager.models.RemoteEqSetting


class DeviceInfoModel(
    val devicePower: DevicePower?,
    val deviceFirmwareVersion: Int?,
    val deviceName: String?,
    val deviceEqSetting: RemoteEqSetting?,
    val deviceKeySettings:Map<Int, Int>?,
    val deviceVolume:Byte?,
    val DevicePlayState:Boolean?,
    val DeviceWorkMode:Byte?,
    val deviceInEarStatus:Boolean?,
    val deviceLanguageSetting:Byte?,
    val deviceAutoAnswer:Boolean?,
    val deviceAncMode:Byte?,
    val deviceIsTws:Boolean?,
    val deviceTwsConnected:Boolean?,
    val deviceLedSwitch:Boolean?,
    val deviceFwChecksum:ByteArray?,
    val deviceAncGain:Int?,
    val deviceTransparencyGain:Int?,
    val deviceAncGainNum:Int?,
    val deviceTransparencyGainNum:Int?,
    val deviceRemoteEqSettings:List<RemoteEqSetting>?,
    val deviceLeftIsMainSide:Boolean?,
    val deviceProductColor:Int?,
    val deviceSoundEffect3d:Boolean?,
    val deviceCapacities:Int?,
    val deviceMaxPacketSize:Short?
)
