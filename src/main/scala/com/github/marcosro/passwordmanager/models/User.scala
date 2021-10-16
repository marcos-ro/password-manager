package com.github.marcosro.passwordmanager.models

import scalafx.beans.property.StringProperty
import org.dizitart.no2.Document
import org.dizitart.no2.mapper.{Mappable, NitriteMapper}

/** A companion by class [[com.github.marcosro.passwordmanager.models.User]]
  * define utils methods for user like:
  * {{{
  * val document: Document = ...
  * User.read(document) // return User
  * }}}
  */
object User {

  /** Reads the `document` and populate all fields of this instance
    * @param document the document
    */
  def read(document: Document): User = {
    /* extract values */
    val name = document.get("name").asInstanceOf[String]
    val password = document.get("password").asInstanceOf[String]

    /* set values into User */
    new User(name, password)
  }

  /** Writes the instance data to a `Document` and returns it
    * @param user The user map to document
    * @return the document generated
    */
  def write(user: User): Document =
    Document
      .createDocument("name", user.getName)
      .put("password", user.getPassword)
}

/** A class represent a user account
  *
  * Specify the `name` and `password` (plain or encrypted) when creating a new user
  * then access the fields like this:
  * {{{
  * val user = new User("test-user", "test-password")
  * user.getName
  * user.getPassword
  * }}}
  *
  * @constructor Create a new user account with a `name` and `password` (plain or encrypted)
  * @param name The user's name
  * @param password The user's (encrypted or plain) password
  */
class User(private val name: String, private val password: String) {
  private val nameProperty = new StringProperty(this, "username", name)
  private val passwordProperty = new StringProperty(this, "password", password)

  /** Set a new user's `name`
    * @param name The user's name to update
    */
  def setName(name: String): Unit =
    nameProperty.set(name)

  /** Set a new user's (encryptd or plain) `password`
    * @param password The user's (encrypted or plain) password to update
    */
  def setPassword(password: String): Unit =
    passwordProperty.set(password)

  /** Gets a user's `name` field
    * @return Gets a `name` field
    */
  def getName: String =
    nameProperty.get()

  /** Gets a user's `password` field
    * @return Gets a `password` field
    */
  def getPassword: String =
    passwordProperty.get()

  /** Gets a user's `nameProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `nameProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getNameProperty: StringProperty =
    nameProperty

  /** Gets a user's `passwordProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `passwordProperty` (plain or encrypted) field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getPasswordProperty: StringProperty =
    passwordProperty

  /** Gets a user's copy
    * @return Gets a `user` copy
    */
  def copy: User = {
    val name = getName
    val password = getPassword
    new User(name, password)
  }
}
