// ignore_for_file: constant_identifier_names
library jete_tws_sdk;

part 'device_power.dart';
part 'remote_eq_setting.dart';

class DeviceInfo {
  final DevicePower? devicePower;
  final int? deviceFirmwareVersion;
  final String? deviceName;
  final RemoteEqSetting? deviceEqSetting;
  final Map<String, int>? deviceKeySettings;
  final int? deviceVolume;
  final bool? devicePlayState;
  final int? deviceWorkMode;
  final bool? deviceInEarStatus;
  final int? deviceLanguageSetting;
  final bool? deviceAutoAnswer;
  final int? deviceAncMode;
  final bool? deviceIsTws;
  final bool? deviceTwsConnected;
  final bool? deviceLedSwitch;
  final List<int>? deviceFwChecksum;
  final int? deviceAncGain;
  final int? deviceTransparencyGain;
  final int? deviceAncGainNum;
  final int? deviceTransparencyGainNum;
  final List<RemoteEqSetting>? deviceRemoteEqSettings;
  final bool? deviceLeftIsMainSide;
  final int? deviceProductColor;
  final bool? deviceSoundEffect3d;
  final int? deviceCapacities;
  final int? deviceMaxPacketSize;

  DeviceInfo({
    this.devicePower,
    this.deviceFirmwareVersion,
    this.deviceName,
    this.deviceEqSetting,
    this.deviceKeySettings,
    this.deviceVolume,
    this.devicePlayState,
    this.deviceWorkMode,
    this.deviceInEarStatus,
    this.deviceLanguageSetting,
    this.deviceAutoAnswer,
    this.deviceAncMode,
    this.deviceIsTws,
    this.deviceTwsConnected,
    this.deviceLedSwitch,
    this.deviceFwChecksum,
    this.deviceAncGain,
    this.deviceTransparencyGain,
    this.deviceAncGainNum,
    this.deviceTransparencyGainNum,
    this.deviceRemoteEqSettings,
    this.deviceLeftIsMainSide,
    this.deviceProductColor,
    this.deviceSoundEffect3d,
    this.deviceCapacities,
    this.deviceMaxPacketSize,
  });

  Map<String, dynamic> toJson() {
    return {
      'devicePower': devicePower?.toJson(),
      'deviceFirmwareVersion': deviceFirmwareVersion,
      'deviceName': deviceName,
      'deviceEqSetting': deviceEqSetting?.toJson(),
      'deviceKeySettings': deviceKeySettings,
      'deviceVolume': deviceVolume,
      'DevicePlayState': devicePlayState,
      'DeviceWorkMode': deviceWorkMode,
      'deviceInEarStatus': deviceInEarStatus,
      'deviceLanguageSetting': deviceLanguageSetting,
      'deviceAutoAnswer': deviceAutoAnswer,
      'deviceAncMode': deviceAncMode,
      'deviceIsTws': deviceIsTws,
      'deviceTwsConnected': deviceTwsConnected,
      'deviceLedSwitch': deviceLedSwitch,
      'deviceFwChecksum': deviceFwChecksum,
      'deviceAncGain': deviceAncGain,
      'deviceTransparencyGain': deviceTransparencyGain,
      'deviceAncGainNum': deviceAncGainNum,
      'deviceTransparencyGainNum': deviceTransparencyGainNum,
      'deviceRemoteEqSettings':
          deviceRemoteEqSettings?.map((e) => e.toJson()).toList(),
      'deviceLeftIsMainSide': deviceLeftIsMainSide,
      'deviceProductColor': deviceProductColor,
      'deviceSoundEffect3d': deviceSoundEffect3d,
      'deviceCapacities': deviceCapacities,
      'deviceMaxPacketSize': deviceMaxPacketSize,
    };
  }

  factory DeviceInfo.fromJson(Map<String, dynamic> json) {
    return DeviceInfo(
      devicePower: json['devicePower'] == null
          ? null
          : DevicePower.fromJson(json['devicePower']),
      deviceFirmwareVersion: json['deviceFirmwareVersion'],
      deviceName: json['deviceName'],
      deviceEqSetting: json['deviceEqSetting'] == null
          ? null
          : RemoteEqSetting.fromJson(json['deviceEqSetting']),
      deviceKeySettings: Map<String, int>.from(json['deviceKeySettings'] ?? {}),
      deviceVolume: json['deviceVolume'],
      devicePlayState: json['DevicePlayState'],
      deviceWorkMode: json['DeviceWorkMode'],
      deviceInEarStatus: json['deviceInEarStatus'],
      deviceLanguageSetting: json['deviceLanguageSetting'],
      deviceAutoAnswer: json['deviceAutoAnswer'],
      deviceAncMode: json['deviceAncMode'],
      deviceIsTws: json['deviceIsTws'],
      deviceTwsConnected: json['deviceTwsConnected'],
      deviceLedSwitch: json['deviceLedSwitch'],
      deviceFwChecksum: List<int>.from(json['deviceFwChecksum'] ?? []),
      deviceAncGain: json['deviceAncGain'],
      deviceTransparencyGain: json['deviceTransparencyGain'],
      deviceAncGainNum: json['deviceAncGainNum'],
      deviceTransparencyGainNum: json['deviceTransparencyGainNum'],
      deviceRemoteEqSettings: json['deviceRemoteEqSettings'] == null
          ? null
          : List<RemoteEqSetting>.from(json['deviceRemoteEqSettings']
                  ?.map((x) => RemoteEqSetting.fromJson(x)) ??
              []),
      deviceLeftIsMainSide: json['deviceLeftIsMainSide'],
      deviceProductColor: json['deviceProductColor'],
      deviceSoundEffect3d: json['deviceSoundEffect3d'],
      deviceCapacities: json['deviceCapacities'],
      deviceMaxPacketSize: json['deviceMaxPacketSize'],
    );
  }
}
