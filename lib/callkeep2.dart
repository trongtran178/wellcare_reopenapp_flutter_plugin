import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Callkeep2 {
  static const MethodChannel _channel = const MethodChannel('callkeep2');

  static Future<void> reopenAppFromBackground({
    @required String packageName,
  }) async {
    await _channel.invokeMethod(
      'reopenAppFromBackground',
      <String, String>{
        'packageName': packageName,
      },
    );
  }
}
