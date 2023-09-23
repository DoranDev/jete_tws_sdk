// ignore_for_file: constant_identifier_names

part of '../jete_tws_sdk.dart';

abstract class TwsAutoShutdown {
  static const int AUTO_SHUTDOWN_IMMEDIATELY = 0x00;
  static const int AUTO_SHUTDOWN_CANCEL = 0xFF;

  static const Map<int, String> intToNameMap = {
    AUTO_SHUTDOWN_IMMEDIATELY: 'AUTO_SHUTDOWN_IMMEDIATELY',
    AUTO_SHUTDOWN_CANCEL: 'AUTO_SHUTDOWN_CANCEL',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
