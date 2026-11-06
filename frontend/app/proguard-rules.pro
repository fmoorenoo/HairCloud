# Reglas R8 para el build release (minify + shrink activados).

# Modelos serializados con Gson por reflexión: conservar nombres de campos.
-keep class com.haircloud.data.model.** { *; }
-keep class com.haircloud.data.ApiResponse { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Retrofit: interfaces de servicio y tipos genéricos de suspend functions.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

# Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Eliminar logs de depuración en release.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
