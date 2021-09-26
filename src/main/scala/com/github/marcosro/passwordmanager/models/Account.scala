package com.github.marcosro.passwordmanager.models

import scalafx.beans.property.StringProperty

/** A companion by class [[com.github.marcosro.passwordmanager.models.Account]]
  * define utils methods for account like:
  * {{{
  * val documnet: Document = ...
  * Account.read(document) // return Account
  * }}}
  */
object Account {
  import org.dizitart.no2.Document

  /** Reads the `document` and populate all fields of this instance
    * @param document the document
    */
  def read(document: Document): Account = {
    /* extract values */
    val uuid = document.get("uuid").asInstanceOf[String]
    val role = document.get("role").asInstanceOf[String]
    val platform = document.get("platform").asInstanceOf[String]
    val project = document.get("project").asInstanceOf[String]
    val category = document.get("category").asInstanceOf[String]

    /* set user values */
    val user = User.read(document.get("user").asInstanceOf[Document])

    new Account(uuid, user, role, platform, project, category)
  }

  /** Writes the instance data to a `Document` and returns it
    * @param account  The account map to document
    * @return The document generated
    */
  def write(account: Account): Document =
    Document
      .createDocument("uuid", account.getUUID)
      .put("user", User.write(account.getUser))
      .put("role", account.getRole)
      .put("platform", account.getPlatform)
      .put("project", account.getProject)
      .put("category", account.getCategory)
}

/** A class represent a account, example of account on real life: netflix account, oracle cloud account, aws account
  *
  * Specify the `uuid`, `user`, `role`, `platform`, `project` and `category` when creating a new account
  * then access the fields like this:
  * {{{
  * val user = new User("test-user", "test-password")
  * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
  * account.getUUID
  * account.getUser // if you need user's name or password use User get methods
  * account.getPlatform
  * account.getProject
  * account.getCategory
  * }}}
  * @constructor Create a new account with a `uuid`, `user`, `role`, `platform`, `project` and `category`
  * @param uuid The account's uuid
  * @param user The account's user
  * @param role The account's role
  * @param platform The account's platform
  * @param project The account's project
  * @param category The account's category
  */
class Account(
    private val uuid: String,
    private val user: User,
    private val role: String,
    private val platform: String,
    private val project: String,
    private val category: String
) {
  private val uuidProperty = new StringProperty(this, "uuid", uuid)
  private val roleProperty = new StringProperty(this, "role", role)
  private val platformProperty = new StringProperty(this, "platform", platform)
  private val projectProperty = new StringProperty(this, "project", project)
  private val categoryProperty = new StringProperty(this, "category", category)

  /** Set a new account's `uuid` (universally unique identifier)
    * @param uuid The account's `uuid` (universally unique identifier)
    */
  def setUUID(uuid: String): Unit =
    uuidProperty.set(uuid)

  /** Set a new account's `role`, a privileges and access to account, examples: admin, or (remote) admin
    * @param role The account's `role`
    */
  def setRole(role: String): Unit =
    roleProperty.set(role)

  /** Set a new account's `platform`, account into platform like: oracle cloud, aws..
    * @param platform The account's `platform`
    */
  def setPlatform(platform: String): Unit =
    platformProperty.set(platform)

  /** Set a new account's `project`
    * @param project The account's project
    */
  def setProject(project: String): Unit =
    projectProperty.set(project)

  /** Set a new account's `category`
    * @param category The account's category
    */
  def setCategory(category: String): Unit =
    categoryProperty.set(category)

  /** Gets a account's `uuidProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `uuidProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getUUIDProperty: StringProperty =
    uuidProperty

  /** Gets a account's `roleProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `roleProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getRoleProperty: StringProperty =
    roleProperty

  /** Gets account's `platformProperty` fields, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `platformProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getPlatformProperty: StringProperty =
    platformProperty

  /** Gets a account's `projectProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `projectProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getProjectProperty: StringProperty =
    projectProperty

  /** Gets a account's `categoryProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    * @return Gets a `categoryProperty` field, this is a instance of `scalafx.beans.property.StringProperty`
    */
  def getCategoryProperty: StringProperty =
    categoryProperty

  /** Gets a account's `uuid` (universally unique identifier) field
    * @return Gets a `uuid` (universally unique identifier) field
    */
  def getUUID: String =
    uuidProperty.get()

  /** Gets a account's `user`
    * @return Gets a `user` field
    */
  def getUser: User =
    user

  /** Gets a account's `role`
    * @return Gets a `role` field
    */
  def getRole: String =
    roleProperty.get()

  /** Gets a account's `platform`
    * @return Gets a `platform` field
    */
  def getPlatform: String =
    platformProperty.get()

  /** Gets a account's `project`
    * @return Gets a `project` field
    */
  def getProject: String =
    projectProperty.get()

  /** Gets a account's `category`
    * @return Gets a `category` field
    */
  def getCategory: String =
    categoryProperty.get()
}
