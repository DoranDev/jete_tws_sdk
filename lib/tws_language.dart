// ignore_for_file: constant_identifier_names

part of 'jete_tws_sdk.dart';

abstract class TwsLanguage {
  static const int LANGUAGE_ENGLISH = 0x00;
  static const int LANGUAGE_CHINESE = 0x01;

  static const Map<int, String> intToNameMap = {
    LANGUAGE_ENGLISH: 'LANGUAGE_ENGLISH',
    LANGUAGE_CHINESE: 'LANGUAGE_CHINESE',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
