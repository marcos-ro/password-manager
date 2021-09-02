package com.github.passwordmanager.models.crypto.default

import java.util.Base64
import java.security.{KeyPair, KeyPairGenerator, PrivateKey, PublicKey}
import javax.crypto.Cipher
import java.io.{
  FileInputStream,
  FileOutputStream,
  ObjectOutputStream,
  ObjectInputStream
}
import scala.util.Random
import scala.util.control.NonFatal
import com.github.passwordmanager.models.crypto.Crypto

private object AES {
  private val random = new Random()

  /** A random password generator
    * @param size Size of password
    * @param password The generate password with tail recurtion
    */
  def randomPassword(size: Int, password: String = ""): String =
    if (size <= 0)
      password
    else {
      val character = random.between(33, 125).toChar
      randomPassword(size - 1, password.appended(character))
    }

  /** A generate pair keys and write the result in <file>.jkey
    * @param path The path to generate key pairs
    * @return  Either[Crypto.CryptoError, Unit]
    */
  def generateKeyPair(path: String): Either[Crypto.CryptoError, Unit] = {
    var objectOutputStream: ObjectOutputStream = null
    try {
      // generate key pair
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(4096)
      val keyPair = keyPairGenerator.generateKeyPair()

      // write key pair
      objectOutputStream = new ObjectOutputStream(new FileOutputStream(path))
      objectOutputStream.writeObject(keyPair)
      objectOutputStream.close()
      Right((): Unit)
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    } finally {
      if (objectOutputStream != null)
        objectOutputStream.close()
    }
  }

  /** A key pair loader
    * @param path  The paht to read <file>.jkey
    * @return Either[Crypto.CryptoError, KeyPair]
    */
  def loadKeyPairs(path: String): Either[Crypto.CryptoError, KeyPair] = {
    var objectInputStream: ObjectInputStream = null
    try {
      objectInputStream = new ObjectInputStream(new FileInputStream(path))
      val keyPair = objectInputStream.readObject().asInstanceOf[KeyPair]
      Right(keyPair)
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    } finally {
      if (objectInputStream != null)
        objectInputStream.close()
    }
  }

  /** A password encrypter
    * @param key The public key generate and writed in <file>.jkey
    * @param password The plain password
    * @return Either[Crypto.CryptoError, String] when String is a encrypted password and encoded in base64
    */
  def encrypt(
      key: PublicKey,
      password: String
  ): Either[Crypto.CryptoError, String] =
    try {
      val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING")
      cipher.init(Cipher.ENCRYPT_MODE, key)
      val bytes = cipher.doFinal(password.getBytes("UTF-8"))
      val encryptedPassword = Base64.getEncoder().encodeToString(bytes)
      Right(encryptedPassword)
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    }

  /**  A password decrypter
    * @param key The private key generate and writed in <file>.jkey
    * @param password The encrypted password and encoded in base64
    * @return Either[Crypto.CryptoError, String] when String is a plain password
    */
  def decrypt(
      key: PrivateKey,
      password: String
  ): Either[Crypto.CryptoError, String] =
    try {
      val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING")
      val bytes = Base64.getDecoder().decode(password)
      cipher.init(Cipher.DECRYPT_MODE, key)
      val decryptedPassword = cipher.doFinal(bytes).map(_.toChar).mkString
      Right(decryptedPassword)
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    }
}
