// ignore_for_file: constant_identifier_names

part of 'jete_tws_sdk.dart';

abstract class TwsMusicControl {
  static const int CONTROL_TYPE_VOLUME = 0x01;
  static const int CONTROL_TYPE_PLAY = 0x02;
  static const int CONTROL_TYPE_PAUSE = 0x03;
  static const int CONTROL_TYPE_PREVIOUS = 0x04;
  static const int CONTROL_TYPE_NEXT = 0x05;

  static const Map<int, String> intToNameMap = {
    CONTROL_TYPE_VOLUME: 'CONTROL_TYPE_VOLUME',
    CONTROL_TYPE_PLAY: 'CONTROL_TYPE_PLAY',
    CONTROL_TYPE_PAUSE: 'CONTROL_TYPE_PAUSE',
    CONTROL_TYPE_PREVIOUS: 'CONTROL_TYPE_PREVIOUS',
    CONTROL_TYPE_NEXT: 'CONTROL_TYPE_NEXT',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
