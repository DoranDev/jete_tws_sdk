part of jete_tws_sdk;

class DevicePower {
  final DeviceComponentPower? leftSidePower;
  final DeviceComponentPower? rightSidePower;
  final DeviceComponentPower? casePower;

  DevicePower({
    this.leftSidePower,
    this.rightSidePower,
    this.casePower,
  });

  Map<String, dynamic> toJson() {
    return {
      'leftSidePower': leftSidePower?.toJson(),
      'rightSidePower': rightSidePower?.toJson(),
      'casePower': casePower?.toJson(),
    };
  }

  factory DevicePower.fromJson(Map<String, dynamic> json) {
    return DevicePower(
      leftSidePower: DeviceComponentPower.fromJson(json['leftSidePower']),
      rightSidePower: DeviceComponentPower.fromJson(json['rightSidePower']),
      casePower: DeviceComponentPower.fromJson(json['casePower']),
    );
  }
}

class DeviceComponentPower {
  final int? powerLevel;
  final bool? isCharging;

  DeviceComponentPower({
    this.powerLevel,
    this.isCharging,
  });

  Map<String, dynamic> toJson() {
    return {
      'powerLevel': powerLevel,
      'isCharging': isCharging,
    };
  }

  factory DeviceComponentPower.fromJson(Map<String, dynamic> json) {
    return DeviceComponentPower(
      powerLevel: json['powerLevel'],
      isCharging: json['isCharging'],
    );
  }
}
