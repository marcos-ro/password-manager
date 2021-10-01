package com.github.marcosro.passwordmanager.models.crypto.default

import com.github.marcosro.passwordmanager.models.crypto.commands._
import com.github.marcosro.passwordmanager.models.crypto.programs._
import com.github.marcosro.passwordmanager.models.crypto.Crypto

/** A class represent a crypto interpreter for default crypto system
  *
  * ==Example==
  * {{{
  * val load = Execute(LoadPublicKey("path/to/public/key"))
  * val execute = RunAndThen(load, key => Execute(Encrypt(key, "random")))
  * val crypto = new DefaultCrypto()
  * crypto.run(execute) // Generate a encrypted password
  * }}}
  */
class DefaultCrypto extends Crypto {

  /** Take a crypto program and return execution result
    * @param program Crypto program to execute and produce a result
    * @return Either[Crypto.CryptoError, T] when T is a result of program execution
    */
  def run[T](program: Program[T]): Either[Crypto.CryptoError, T] =
    program match {
      case RunAndThen(current, next) =>
        run(current).map(next).flatMap(run)
      case Execute(GenerateKeyPair(path)) =>
        AES.generateKeyPair(path)
      case Execute(LoadPublicKey(path)) =>
        AES.loadKeyPairs(path).map(_.getPublic())
      case Execute(LoadPrivateKey(path)) =>
        AES.loadKeyPairs(path).map(_.getPrivate())
      case Execute(Encrypt(key, password)) =>
        AES.encrypt(key, password)
      case Execute(EncryptMany(key, passwords)) =>
        AES.encryptMany(key, passwords)
      case Execute(Decrypt(key, password)) =>
        AES.decrypt(key, password)
      case Execute(DecryptMany(key, passwords)) =>
        AES.decryptMany(key, passwords)
      case Execute(RandomPassword(size)) =>
        Right(AES.randomPassword(size))
      case Return(value) =>
        Right(value)
      case _ =>
        Left(new Crypto.CryptoError("Unkow crypto program"))
    }
}
