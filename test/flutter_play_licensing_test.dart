import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_play_licensing/flutter_play_licensing.dart';

void main() {
  const MethodChannel channel = MethodChannel('play_licensing');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return false;
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('isAllowed', () async {
    expect(await PlayLicensing.isAllowed(), false);
  });
}
