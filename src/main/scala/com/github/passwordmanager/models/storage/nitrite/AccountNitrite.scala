package com.github.passwordmanager.models.storage.nitrite

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal
import org.dizitart.no2.objects.ObjectRepository
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.filters.Filters
import com.github.passwordmanager.models.Account
import com.github.passwordmanager.models.storage.Storage
import com.github.passwordmanager.models.storage.commands.Find

/** A object represent a account storage methods into a nitrite database (similiar to SQL DML)
  */
private object AccountNitrite {

  /** A register and store account into database (similar to SQL insert)
    * @param account The account to register and store
    * @return Either[Storage.StorageError, Int] when Int represent a rows affected
    */
  def put(account: Account): Either[Storage.StorageError, Int] =
    StorageNitrite.openOrCreate().flatMap { database =>
      try {
        val collection = database.getCollection("accounts")
        val rowsAffected =
          collection.insert(Account.write(account)).getAffectedCount()
        Right(rowsAffected)
      } catch {
        case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
      } finally {
        if (database != null)
          database.close()
      }
    }

  /** A delete account stored into database (similar to SQL delete)
    * @param uuid The account uuid (universally unique identifier)
    * @return Either[Storage.StorageError, Int] when int represent a rows affected
    */
  def delete(uuid: String): Either[Storage.StorageError, Int] =
    StorageNitrite.openOrCreate().flatMap { database =>
      try {
        val collection = database.getCollection("accounts")
        val rowsAffected =
          collection.remove(Filters.eq("uuid", uuid)).getAffectedCount()
        Right(rowsAffected)
      } catch {
        case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
      } finally {
        if (database != null)
          database.close()
      }
    }

  /** A update account stored into database (similar to SQL udpate)
    * @param uuid The account `uuid` (universally unique identifier)
    * @param account The account to replace old value
    * @return Either[Storage.StorageError, Int] when Int represent a rows affected
    */
  def update(
      uuid: String,
      account: Account
  ): Either[Storage.StorageError, Int] =
    StorageNitrite.openOrCreate().flatMap { database =>
      try {
        val collection = database.getCollection("accounts")
        val rowsAffected = collection
          .update(Filters.eq("uuid", uuid), Account.write(account))
          .getAffectedCount()
        Right(rowsAffected)
      } catch {
        case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
      } finally {
        if (database != null)
          database.close()
      }
    }

  /** A find account by id on database (similar to SQL select)
    * @param uuid The account `uuid` (universally unique identifier)
    * @return Either[Storage.StorageError, Account] when Account is a record on database in accounts collection
    */
  def findById(uuid: String): Either[Storage.StorageError, Account] =
    StorageNitrite.openOrCreate().flatMap { database =>
      try {
        val collection = database.getCollection("accounts")
        val headOption = collection
          .find(Filters.eq("uuid", uuid))
          .toList
          .asScala
          .map(Account.read)
          .toList
          .headOption
        lazy val error =
          new Storage.StorageError(s"Account with $uuid not found")
        Either.cond(headOption.nonEmpty, headOption.get, error)
      } catch {
        case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
      } finally {
        if (database != null)
          database.close()
      }
    }

  /** A find account by field on database (similar to SQL select)
    * @param field The account field to search like: search account by category, show accept values in [[com.github.passwordmanager.models.storage.commands.Find]]
    * @param value The account value like: value of category field
    * @return Either[Storage.StorageError, List[Account]] when List[Account] is a records on database in accounts collections
    */
  def findByField(
      field: Find.Field,
      value: String
  ): Either[Storage.StorageError, List[Account]] =
    StorageNitrite.openOrCreate().flatMap { database =>
      try {
        if (isAccountField(field)) {
          val collection = database.getCollection("accounts")
          val accounts = collection
            .find(Filters.regex(toAccountField(field), value))
            .toList
            .asScala
            .map(Account.read)
            .toList
          Right(accounts)
        } else
          Left(
            new Storage.StorageError("The field by find is't account's field")
          )
      } catch {
        case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
      } finally {
        if (database != null)
          database.close()
      }
    }

  /** Verify if field is a account's field
    * @param field The unknow field
    * @return If field is account's field
    */
  private def isAccountField(field: Find.Field): Boolean =
    field match {
      case Find.AccountByRole | Find.AccountByCategory | Find.AccountByProject |
          Find.AccountByPlatform =>
        true
      case _ => false
    }

  /** Return account's field like role field or category field
    * ==Example==
    * {{{
    * toAccountField(Field.AccountByRole) // return "role"
    * }}}
    * @param field The find's field mappeable
    * @return field name on database or file
    */
  private def toAccountField(field: Find.Field): String =
    field match {
      case Find.AccountByRole     => "role"
      case Find.AccountByCategory => "category"
      case Find.AccountByProject  => "project"
      case Find.AccountByPlatform => "platform"
    }
}
