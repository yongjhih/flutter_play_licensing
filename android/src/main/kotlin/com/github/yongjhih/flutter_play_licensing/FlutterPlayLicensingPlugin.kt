package com.github.yongjhih.flutter_play_licensing

import android.content.Context
import android.provider.Settings
import androidx.annotation.NonNull
import com.google.android.vending.licensing.*
import com.google.android.vending.licensing.Policy.NOT_LICENSED
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

object PlayLicensingConfig {
  val salt: ByteArray = byteArrayOf(
          -46, 65, 30, -128, -103, -57, 74, -64, 51, 88,
          -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
  )

  // in base64, BASE64_PUBLIC_KEY
  val publicKey: String = ""
}

/** FlutterPlayLicensingPlugin
 *
 * ref. https://developer.android.com/google/play/licensing/client-side-verification
 */
public class FlutterPlayLicensingPlugin(private val registrar: Registrar? = null) : FlutterPlugin, MethodCallHandler {

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val channel = MethodChannel(flutterPluginBinding.binaryMessenger, "play_licensing")
    channel.setMethodCallHandler(FlutterPlayLicensingPlugin())
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "play_licensing")
      channel.setMethodCallHandler(FlutterPlayLicensingPlugin(registrar))
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "check" -> {
        check(call, result)
      }
      "isAllowed" -> {
        isAllowed(call, result)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun Context.checker(salt: ByteArray = PlayLicensingConfig.salt,
                         publicKey: String = PlayLicensingConfig.publicKey): LicenseChecker {
    return LicenseChecker(
            this,
            ServerManagedPolicy(
                    this,
                    AESObfuscator(
                            salt,
                            packageName,
                            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    )
            ),
            publicKey
    )
  }

  private fun isAllowed(@NonNull call: MethodCall, @NonNull result: Result) {
    registrar?.context()?.let { context ->
      val checker = context.checker()
      checker.checkAccess(
              onAllow = { _ ->
                result.success(true)
              },
              onDontAllow = { _ ->
                result.success(false)
              },
              onApplicationError = { errorCode ->
                result.errors(errorCode.toString(), details = errorCode)
              }
      )
    } ?: result.notImplemented()
  }

  private fun check(@NonNull call: MethodCall, @NonNull result: Result) {
      registrar?.context()?.let { context ->
        val checker = context.checker()
        checker.checkAccess(
                onAllow = { reason ->
                  result.success(reason)
                },
                onDontAllow = { reason ->
                  result.errors(reason.toString(), details = reason)
                },
                onApplicationError = { errorCode ->
                  result.errors(errorCode.toString(), details = errorCode)
                }
        )
      } ?: result.notImplemented()
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }
}

fun LicenseChecker.checkAccess(onAllow: (Int) -> Unit = { _ -> },
                               onDontAllow: (Int) -> Unit = { _ -> },
                               onApplicationError: (Int) -> Unit = { _ -> }) {
  checkAccess(object : LicenseCheckerCallback {
    override fun allow(reason: Int) {
      onAllow(reason)
    }

    override fun dontAllow(reason: Int) {
      onDontAllow(reason)
    }

    override fun applicationError(errorCode: Int) {
      onApplicationError(errorCode)
    }
  })
}

fun <T> MethodCall.argumentOrNull(key: String): T? = try { argument(key) } catch (e: Throwable) { null }
fun <T> MethodCall.argumentsOrNull(): T? = arguments() as? T?
//fun <T> MethodCall.argument(key: String): T? = try { argument(key) } catch (e: Throwable) { null }
//fun <T> MethodCall.arguments(): T? = arguments() as? T?
//fun Result.success(result: Any? = null): Unit = success(result)
fun Result.success(): Unit = success(null) // avoid shadow
fun Result.errors(code: String, message: String? = null, details: Any? = null): Unit = error(code, message, details)
fun Result.error(e: Throwable): Unit = errors(e.cause.toString(), e.message, e.stackTrace)

val Any.TAG: String
  get() {
    val tag = javaClass.simpleName
    val max = 23
    return if (tag.length <= max) tag else tag.substring(0, max)
  }

/// ref. https://gist.github.com/fabiomsr/845664a9c7e92bafb6fb0ca70d4e44fd#gistcomment-2836766
val ByteArray.toHex inline get() = joinToString(separator = "") {
  String.format("%02x",(it.toInt() and 0xFF))
}

val String.toHexByteArray inline get(): ByteArray? = try {
  chunked(2).map {
    it.toUpperCase().toInt(16).toByte()
  }.toByteArray()
} catch (e: Throwable) { null }
