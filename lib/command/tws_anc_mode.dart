// ignore_for_file: constant_identifier_names

part of '../jete_tws_sdk.dart';

abstract class TwsAncMode {
  static const int ANC_MODE_OFF = 0x00;
  static const int ANC_MODE_ON = 0x01;
  static const int ANC_MODE_TRANSPARENCY = 0x02;

  static const Map<int, String> intToNameMap = {
    ANC_MODE_OFF: 'ANC_MODE_OFF',
    ANC_MODE_ON: 'ANC_MODE_ON',
    ANC_MODE_TRANSPARENCY: 'ANC_MODE_TRANSPARENCY',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
