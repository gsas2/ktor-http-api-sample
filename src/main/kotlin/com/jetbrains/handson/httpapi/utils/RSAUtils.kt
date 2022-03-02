package com.jetbrains.handson.httpapi.utils

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

/**
 * This code is based on the examples provided here:
 * https://www.baeldung.com/java-read-pem-file-keys
 */
@Throws(Exception::class)
fun readPrivateKey(file: String): RSAPrivateKey? {
    val file = File(file)
    val key = String(Files.readAllBytes(file.toPath()), Charset.defaultCharset())
    val privateKeyPEM = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace(System.lineSeparator().toRegex(), "")
        .replace("-----END PRIVATE KEY-----", "")
    val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyPEM))
    return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
}
