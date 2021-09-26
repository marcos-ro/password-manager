package com.github.marcosro.passwordmanager.models.crypto

package commands {
  import java.security.{PrivateKey, PublicKey}

  /** A trait represent a crypto command like: encrypt this "random" or other command
    * take any type to return by crypto interpreter
    */
  trait Command[_]

  /** A class represent a key pair generator
    *
    * Sepcify the `path` when create a new key pair generator
    * then access the fields like:
    * {{{
    * val generator = GenerateKeyPairs("path/to/key/dir")
    * generator.path
    * }}}
    * @constructor Create a new key pair generator with `path`
    * @param path Key pair dir output
    */
  case class GenerateKeyPair(path: String) extends Command[Unit]

  /** A class represent a public key loader
    *
    * Specify the `path` when create a new public key loader
    * then access the fields like:
    * {{{
    *  val loader = LoadPublicKey("path/to/public/key")
    *  loader.path
    * }}}
    * @constructor Create a public key loader with `path`
    * @param path Path to public key
    */
  case class LoadPublicKey(path: String) extends Command[PublicKey]

  /**  A class represent a private key loader
    *
    * Specify the `path` when create a new private key loader
    * then access the fields like:
    * {{{
    * val loader = PrivateKey("path/to/private/key")
    * loader.path
    * }}}
    * @constructor Create a private key loader with `path`
    * @param path Path to private key
    */
  case class LoadPrivateKey(path: String) extends Command[PrivateKey]

  /** A class represent a random password generator
    * Specify the `size` when create a new random password generator
    * then access the fields like:
    * {{{
    * val random = RandomPassword(12)
    * random.size
    * }}}
    * @constructor Create a random password with `size`
    * @param size The password's size
    */
  case class RandomPassword(size: Int) extends Command[String]

  /** A class represent a encrypted password
    * Specify the `key` and `password` when create a new encrypted password
    * then access the fields like:
    * {{{
    * val key = ... // load key
    * val encrypt = Encrypt(key, "random")
    * encrypt.password // plain password
    * }}}
    * @constructor Create a encrypted password with `key` and `password`
    * @param key The secret key (public key in this app) sorry hackers 😔 I'm begginer in this a crypto world
    * @param password The plain password
    */
  case class Encrypt(key: PublicKey, password: String) extends Command[String]

  /** A class represent a password decrypted
    * Specify the `key` and `password` when create a new password decrypted
    * then access the fields like:
    * {{{
    * val key = ... // load key
    * val decrypt = Decrypt(key, "encrypted-password")
    * }}}
    * @constructor Create a new decrypted password wiht `password`
    * @param key The secret key (private key in this app) sorry hackers 😔 I'm begginer in this crypto world
    * @param passwod The encrypted password
    */
  case class Decrypt(key: PrivateKey, password: String) extends Command[String]
}

package programs {
  import com.github.marcosro.passwordmanager.models.crypto.commands.Command

  /** A trait represent a crypto program
    */
  sealed trait Program[T] { self =>
    def map[V](f: T => V): Program[V] =
      RunAndThen(self, (t: T) => Return(f(t)))

    def flatMap[V](f: T => Program[V]): Program[V] =
      RunAndThen(self, f)

    def >>=[V](f: T => Program[V]): Program[V] =
      flatMap(f)
  }

  /** A implement instance on crypto program
    */

  /** A class represent a execution of command cyrpto and return a `T` parameter of crypto command
    *
    *  Specify `command` with `T` parameter for know return type of execution when create a new executable crypto command
    * then access the fields like this.
    * {{{
    * val key = ... // load key
    * val execute = Execute(Encrypt(key, ""))
    * execute.command
    * }}}
    * @constructor Create a executable command crypto with `command`
    * @param command The execute's command crypto
    */
  case class Execute[T](command: Command[T]) extends Program[T]

  /** A class represent a execution sequence like example: load encryptation key and encrypt my password with loaded key
    *
    * Specify a `program` and `map` when create a new execution sequence
    * then access the fields like this:
    * {{{
    * val load = Execute(LoadPublicKey("path/to/public/key"))
    * val execute = RunAndThen(load, key => Execute(Encrypt(key, "random")))
    * execute.program
    * execute.map
    * }}}
    * @constructor Create a execution sequence with `program` and `map`
    * @param program The program to execute and pass to result and apply map function
    * @param map Transform to program execution result and map to other program
    */
  case class RunAndThen[T, V](program: Program[T], map: T => Program[V])
      extends Program[V]

  /** A class represent a return and nothing more
    *
    *  Specify a `t` when create a new return point
    * then access the fields like this:
    * {{{
    * val point = Return(20)
    * point.t
    * }}}
    * @constructor Create a return with t
    * @param t The value to return
    */
  case class Return[T](t: T) extends Program[T]
}

import com.github.marcosro.passwordmanager.models.crypto.programs.Program

/** A companion by class [[com.github.marcosro.passwordmanager.models.crypto.Crypto]]
  * define utils methods for crypto like:
  * {{{
  * Crypto.CryptoError("Not exists public key") // Error
  * }}}
  */
object Crypto {

  /** A class rerpresent a crypto error like: algorithm not exists or public key not exists
    */
  class CryptoError(private val message: String) {

    /** Gets error message
      * @return Error message
      */
    def getMessage: String =
      message
  }
}

/** A trait represent a crypto interpreter
  */
trait Crypto {

  /** A abstract crypto interpreter that run crypto programs
    *
    * @param program The crypto's program
    * @return If execution of crypto program is ok return `T` when other case return [[com.github.marcosro.passwordmanager.models.crypto.Crypto.CryptoError]]
    */
  def run[T](program: Program[T]): Either[Crypto.CryptoError, T]
}
