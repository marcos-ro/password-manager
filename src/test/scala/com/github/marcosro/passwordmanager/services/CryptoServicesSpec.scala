package com.github.marcosro.passwordmanager.services

import com.github.marcosro.passwordmanager.UnitSpec

class CryptoServicesSpec extends UnitSpec {
  CryptoServices.setPath("/tmp/test.jkey") // working directory

  "Execute(GenerateKeyPair(path))" should "generate a key pair like: public key and private key into test.jkey" in {
    val program = CryptoServices.generateKeyPair
    CryptoServices.interpreter.run(program) match {
      case Right(value) =>
        assert(new java.io.File("/tmp/test.jkey").exists())
      case Left(e) =>
        fail(e.getMessage)
    }
  }

  "Execute(Encrypt(password)) and Execute(Decrpyt(password))" should "a generate encrypted password and decrypted a encrypted password" in {
    val program = CryptoServices.encrypt("default1024") >>= (value =>
      CryptoServices.decrypt(value)
    )
    CryptoServices.interpreter.run(program) match {
      case Right(value) =>
        assert(value == "default1024")
      case Left(e) =>
        fail(e.getMessage)
    }
  }

  "Execute(RandomPassword(12))" should "generate a random password with 12 size" in {
    val program = CryptoServices.randomPassword(12)
    CryptoServices.interpreter.run(program) match {
      case Right(value) =>
        assert(value.size == 12)
      case Left(e) =>
        fail(e.getMessage)
    }
  }
}
