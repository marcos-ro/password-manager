package com.github.passwordmanager

/** Manages the data, logic and rules of the application
  */

package object models {}

package models.storage {

  /** Provide storage commands to manipulate a data on database or file
    */
  package object commands {}

  /** Provides storage program to execute commands on database or file
    */
  package object programs {}

  /** Provides storage intrepeter to execute a program storage on database or file
    */
  package object nitrite {}
}

package models.crypto {

  /** Provide crypto commands to manipulate passwords
    */
  package object commands {}

  /** Provides crypto program to execute commands crypto
    */
  package object programs {}

  /** Provides crypto intrepeter to execute a program crypto
    */
  package object filesystem {}
}
