// ignore_for_file: constant_identifier_names

part of 'jete_tws_sdk.dart';

abstract class TwsWorkMode {
  static const int WORK_MODE_NORMAL = 0x00;
  static const int WORK_MODE_GAMING = 0x01;

  static const Map<int, String> intToNameMap = {
    WORK_MODE_NORMAL: 'WORK_MODE_NORMAL',
    WORK_MODE_GAMING: 'WORK_MODE_GAMING',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
