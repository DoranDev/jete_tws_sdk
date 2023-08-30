// ignore_for_file: constant_identifier_names

part of 'jete_tws_sdk.dart';

abstract class TwsKey {
  static const int KEY_LEFT_SINGLE_TAP = 0x01;
  static const int KEY_RIGHT_SINGLE_TAP = 0x02;
  static const int KEY_LEFT_DOUBLE_TAP = 0x03;
  static const int KEY_RIGHT_DOUBLE_TAP = 0x04;
  static const int KEY_LEFT_TRIPLE_TAP = 0x05;
  static const int KEY_RIGHT_TRIPLE_TAP = 0x06;
  static const int KEY_LEFT_LONG_PRESS = 0x07;
  static const int KEY_RIGHT_LONG_PRESS = 0x08;

  static const Map<int, String> intToNameMap = {
    KEY_LEFT_SINGLE_TAP: 'KEY_LEFT_SINGLE_TAP',
    KEY_RIGHT_SINGLE_TAP: 'KEY_RIGHT_SINGLE_TAP',
    KEY_LEFT_DOUBLE_TAP: 'KEY_LEFT_DOUBLE_TAP',
    KEY_RIGHT_DOUBLE_TAP: 'KEY_RIGHT_DOUBLE_TAP',
    KEY_LEFT_TRIPLE_TAP: 'KEY_LEFT_TRIPLE_TAP',
    KEY_RIGHT_TRIPLE_TAP: 'KEY_RIGHT_TRIPLE_TAP',
    KEY_LEFT_LONG_PRESS: 'KEY_LEFT_LONG_PRESS',
    KEY_RIGHT_LONG_PRESS: 'KEY_RIGHT_LONG_PRESS',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}

abstract class TwsKeyTouch {
  static const int KEY_TOUCH_SENSITIVE_NORMAL = 0x00;
  static const int KEY_TOUCH_SENSITIVE_LOW = 0x01;
  static const int KEY_TOUCH_SENSITIVE_HIGH = 0x02;

  static const Map<int, String> intToNameMap = {
    KEY_TOUCH_SENSITIVE_NORMAL: 'KEY_TOUCH_SENSITIVE_NORMAL',
    KEY_TOUCH_SENSITIVE_LOW: 'KEY_TOUCH_SENSITIVE_LOW',
    KEY_TOUCH_SENSITIVE_HIGH: 'KEY_TOUCH_SENSITIVE_HIGH',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}

abstract class TwsKeyFunction {
  static const int KEY_FUNCTION_NONE = 0x00;
  static const int KEY_FUNCTION_RECALL = 0x01;
  static const int KEY_FUNCTION_ASSISTANT = 0x02;
  static const int KEY_FUNCTION_PREVIOUS = 0x03;
  static const int KEY_FUNCTION_NEXT = 0x04;
  static const int KEY_FUNCTION_VOLUME_UP = 0x05;
  static const int KEY_FUNCTION_VOLUME_DOWN = 0x06;
  static const int KEY_FUNCTION_PLAY_PAUSE = 0x07;
  static const int KEY_FUNCTION_GAME_MODE = 0x08;
  static const int KEY_FUNCTION_ANC_MODE = 0x09;

  static const Map<int, String> intToNameMap = {
    KEY_FUNCTION_NONE: 'KEY_FUNCTION_NONE',
    KEY_FUNCTION_RECALL: 'KEY_FUNCTION_RECALL',
    KEY_FUNCTION_ASSISTANT: 'KEY_FUNCTION_ASSISTANT',
    KEY_FUNCTION_PREVIOUS: 'KEY_FUNCTION_PREVIOUS',
    KEY_FUNCTION_NEXT: 'KEY_FUNCTION_NEXT',
    KEY_FUNCTION_VOLUME_UP: 'KEY_FUNCTION_VOLUME_UP',
    KEY_FUNCTION_VOLUME_DOWN: 'KEY_FUNCTION_VOLUME_DOWN',
    KEY_FUNCTION_PLAY_PAUSE: 'KEY_FUNCTION_PLAY_PAUSE',
    KEY_FUNCTION_GAME_MODE: 'KEY_FUNCTION_GAME_MODE',
    KEY_FUNCTION_ANC_MODE: 'KEY_FUNCTION_ANC_MODE',
  };

  int getControl();
  int getControlType();
  List<int> getPayload();

  String byteToName(int value) {
    return intToNameMap[value] ?? 'Unknown';
  }
}
