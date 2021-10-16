package com.github.marcosro.passwordmanager.services

import com.github.marcosro.passwordmanager.models.crypto.Crypto
import com.github.marcosro.passwordmanager.models.crypto.commands._
import com.github.marcosro.passwordmanager.models.crypto.programs._
import com.github.marcosro.passwordmanager.models.crypto.default.DefaultCrypto

/** A class represent crypto services like:
  * {{{
  * val program = CryptoService.encrypt("random")
  * CryptoServices.interpreter.run(program) // return encrypted password
  * }}}
  */
object CryptoServices {
  private var path: String =
    "/tmp/password-manager.jkey" // default path to <file>.jkey

  /** A set a path to <file>.jkey
    * @param path Path to <file>.jkey
    */
  def setPath(path: String): Unit =
    this.path = path

  /** Gets a path to <file>.jkey
    * @return Gets path to <file>.jkey
    */
  def getPath: String =
    path

  /** Gets if exists default path
    * @return Gets boolean value to represent if path exists or not
    */
  def existsPath: Boolean =
    (path.nonEmpty) && (new java.io.File(path).exists())

  /** Gets a crypto interpreter for processing crypto programs
    * @return Gets crypto interpreter for processing crypto programs
    */
  def interpreter: Crypto =
    new DefaultCrypto

  def point[T](t: => T): Program[T] =
    Return(t)

  /** Gets a random password program with `size` specified
    * @param size The password's size
    * @return Gets a random password program with `size` specified
    */
  def randomPassword(size: Int): Program[String] =
    Execute(RandomPassword(size))

  /** Gets generate key pair program
    * @return Gets a genearte key pair program
    */
  def generateKeyPair: Program[Unit] =
    Execute(GenerateKeyPair(path))

  /** Gets encryptation program for `password`
    * @param password The password to encrypt
    * @return Gets encryptation program for `password`
    */
  def encrypt(password: String): Program[String] =
    Execute(LoadPublicKey(path)) >>= (key => Execute(Encrypt(key, password)))

  def encryptMany(passwords: List[String]): Program[List[String]] =
    Execute(LoadPublicKey(path)) >>= (key =>
      Execute(EncryptMany(key, passwords))
    )

  /** Gets decryptation program for `password`
    * @param password The encrypted password to decrypt
    * @return Gets decryptation program for `password`
    */
  def decrypt(password: String): Program[String] =
    Execute(LoadPrivateKey(path)) >>= (key => Execute(Decrypt(key, password)))

  def decryptMany(passwords: List[String]): Program[List[String]] =
    Execute(LoadPrivateKey(path)) >>= (key =>
      Execute(DecryptMany(key, passwords))
    )
}
