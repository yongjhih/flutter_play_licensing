import 'dart:async';

import 'package:flutter/services.dart';

class PlayLicensing {
  static const MethodChannel _channel =
      const MethodChannel('play_licensing');

  static Future<int> get check({
    /// In hex
    /// Prefer to initialize PlayLicensing.salt in native
    String salt,
    /// In base64
    String publicKey,
  }) async {
    final int reason = await _channel.invokeMethod('check', {
      'salt': salt,
      'publicKey': publicKey,
    });
    return reason;
  }

  static Future<bool> isAllowed({
    /// In hex
    /// Prefer to initialize PlayLicensing.salt in native
    String salt,
    /// In base64
    String publicKey,
  }) async {
    return await _channel.invokeMethod('isAllowed', {
      'salt': salt,
      'publicKey': publicKey,
    });
  }
}
