part of jete_tws_sdk;

class RemoteEqSetting {
  final int? mode;
  final List<int>? gains;

  RemoteEqSetting({this.mode, this.gains});

  Map<String, dynamic> toJson() {
    return {
      'mode': mode,
      'gains': gains,
    };
  }

  factory RemoteEqSetting.fromJson(Map<String, dynamic> json) {
    return RemoteEqSetting(
      mode: json['mode'],
      gains: json['gains'],
    );
  }
}
